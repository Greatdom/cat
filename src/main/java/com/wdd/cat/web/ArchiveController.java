package com.wdd.cat.web;

import com.wdd.cat.archive.Archive;
import com.wdd.cat.archive.ArchiveStore;
import com.wdd.cat.common.cache.AvailableSessions;
import com.wdd.cat.common.entity.Result;
import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.message.ChatKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * &#064program: cat
 * <p>
 * &#064description: 存档Controller：一个存档 = 一个模拟人
 * <p>
 * GET    /api/archives       列表
 * GET    /api/archives/{id}  单个
 * POST   /api/archives       id 留空 = 新建，有值 = 更新
 * DELETE /api/archives/{id}  删除（移进回收站，不做物理删除）
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 19:37
 **/
@RestController
@RequestMapping("/api/archives")
public class ArchiveController {

    private static final Logger log = LoggerFactory.getLogger(ArchiveController.class);

    private final ArchiveStore archiveStore;
    private final AvailableSessions availableSessions;

    public ArchiveController(ArchiveStore archiveStore, AvailableSessions availableSessions) {
        this.archiveStore = archiveStore;
        this.availableSessions = availableSessions;
    }

    /** 全部存档，按 id 升序。 */
    @GetMapping
    public Result<List<ArchiveView>> list() {
        return Result.ok(archiveStore.all().stream().map(ArchiveView::from).toList());
    }

    /**
     * 新建/编辑页要用的候选清单。
     * <p>
     * 会话来自跳板心跳上报（只有 DSH 自己知道有哪些会话）；
     * 聊天来自现有存档里已经配过的 ChatKey，外加当前在用的账号拼出的群/私聊。
     * 页面用下拉框选，不用手抄 id。
     */
    @GetMapping("/candidates")
    public Result<CandidatesView> candidates() {
        // 会话：跳板报上来的，带名字
        List<String> sessionIds = availableSessions.getSessionIds();
        List<SessionOption> sessions = new ArrayList<>(sessionIds.size());
        for (String id : sessionIds) {
            sessions.add(new SessionOption(id, availableSessions.title(id)));
        }

        // 聊天：现有存档里出现过的都算候选，去重后按 wire 排序
        Map<String, ChatOption> chats = new LinkedHashMap<>();
        for (Archive a : archiveStore.all()) {
            if (a.contacts() == null) {
                continue;
            }
            for (ChatKey c : a.contacts()) {
                chats.putIfAbsent(c.wire(), new ChatOption(c.wire(), c.wire()));
            }
        }

        // 账号：现有存档的连接
        List<AccountOption> accounts = new ArrayList<>();
        for (Archive a : archiveStore.all()) {
            if (a.links() == null) {
                continue;
            }
            for (Archive.Link l : a.links()) {
                if (l.account() == null || l.account().isBlank()) {
                    continue;
                }
                accounts.add(new AccountOption(
                        l.kind() == null ? null : l.kind().wire(),
                        l.account(),
                        l.baseUrl(),
                        a.name()));
            }
        }

        return Result.ok(new CandidatesView(sessions, new ArrayList<>(chats.values()), accounts));
    }

    /** 读一个存档。 */
    @GetMapping("/{id}")
    public Result<ArchiveView> get(@PathVariable String id) {
        long archiveId = parseId(id);
        if (archiveId < 0) {
            return Result.fail("存档 id 不合法：" + id);
        }
        return archiveStore.find(archiveId)
                .map(a -> Result.ok(ArchiveView.from(a)))
                .orElseGet(() -> Result.fail("存档不存在：" + id));
    }

    /** 新建或更新：id 留空 = 新建，有值 = 更新。字段为 null 表示这次不改。 */
    @PostMapping
    public Result<ArchiveView> save(@RequestBody ArchiveRequest body) {
        if (body == null) {
            return Result.fail("请求体不能为空");
        }
        try {
            Archive draft = body.toDomain();
            boolean creating = body.id() == null || body.id().isBlank();
            Archive saved;
            if (creating) {
                saved = archiveStore.create(draft);
            } else {
                long id = parseId(body.id());
                if (id < 0) {
                    return Result.fail("存档 id 不合法：" + body.id());
                }
                saved = archiveStore.update(id, draft);
            }
            return Result.ok(ArchiveView.from(saved));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("保存存档失败：{}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /** 删除。记忆库会被移进回收站，不做物理删除。 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable String id) {
        long archiveId = parseId(id);
        if (archiveId < 0) {
            return Result.fail("存档 id 不合法：" + id);
        }
        if (archiveStore.find(archiveId).isEmpty()) {
            return Result.fail("存档不存在：" + id);
        }
        archiveStore.delete(archiveId);
        return Result.ok();
    }

    private static long parseId(String raw) {
        if (raw == null || raw.isBlank()) {
            return -1L;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    // ── 请求体 ──

    /**
     * 新建/更新。
     * <p>字段为 null 表示「这次不改」，交由 ArchiveStore.update 沿用旧值。
     */
    public record ArchiveRequest(
            String id,
            String name,
            Boolean enabled,
            SessionView session,
            List<LinkView> links,
            List<String> chats
    ) {

        Archive toDomain() {
            return new Archive(
                    null,
                    0L,
                    name,
                    enabled,
                    null,
                    session == null ? null : session.toDomain(),
                    links == null ? null : links.stream().map(LinkView::toDomain).toList(),
                    chats == null ? null : chats.stream().map(ChatKey::parse).toList(),
                    null,
                    null,
                    null);
        }
    }

    /** 绑定的 DSH 会话。 */
    public record SessionView(String sessionId, String status, String title) {

        Archive.Session toDomain() {
            Archive.SessionStatus s = "LOST".equalsIgnoreCase(status)
                    ? Archive.SessionStatus.LOST
                    : Archive.SessionStatus.BOUND;
            return new Archive.Session(sessionId, s, title);
        }

        static SessionView from(Archive.Session s) {
            return s == null ? null
                    : new SessionView(s.sessionId(),
                    s.status() == null ? null : s.status().name(), s.title());
        }
    }

    /** 外部连接。token 只进不出。 */
    public record LinkView(
            Long id,
            String kind,
            Boolean enabled,
            String account,
            String baseUrl,
            String token,
            String deviceId
    ) {

        Archive.Link toDomain() {
            return new Archive.Link(id, ChannelKind.fromWire(kind),
                    enabled == null ? Boolean.TRUE : enabled,
                    account, baseUrl, token, deviceId);
        }

        static LinkView from(Archive.Link l) {
            return new LinkView(l.id(),
                    l.kind() == null ? null : l.kind().wire(),
                    l.isEnabled(), l.account(), l.baseUrl(), null, l.deviceId());
        }
    }

    // ── 响应体 ──

    /** 候选清单：新建/编辑页的下拉选项来源。 */
    public record CandidatesView(
            List<SessionOption> sessions,
            List<ChatOption> chats,
            List<AccountOption> accounts
    ) {}

    /** 一个可选的 DSH 会话。 */
    public record SessionOption(String sessionId, String title) {}

    /** 一个可选的聊天。 */
    public record ChatOption(String chatKey, String label) {}

    /** 一个可选的账号（来自现有存档的连接）。 */
    public record AccountOption(String kind, String account, String baseUrl, String archiveName) {}


    /** 存档视图。雪花 id 一律字符串，token 不下发。 */
    public record ArchiveView(
            String id,
            String name,
            boolean enabled,
            SessionView session,
            List<LinkView> links,
            List<String> chats,
            String createdAt,
            String updatedAt
    ) {

        static ArchiveView from(Archive a) {
            return new ArchiveView(
                    Long.toString(a.id()),
                    a.name(),
                    a.isEnabled(),
                    SessionView.from(a.session()),
                    a.links() == null ? List.of() : a.links().stream().map(LinkView::from).toList(),
                    a.contacts() == null ? List.of() : a.contacts().stream().map(ChatKey::wire).toList(),
                    a.createdAt(),
                    a.updatedAt());
        }
    }
}
