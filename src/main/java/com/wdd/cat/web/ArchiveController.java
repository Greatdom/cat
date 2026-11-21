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
 * &#064description: 存档Controller
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

    @GetMapping
    public Result<List<ArchiveView>> list() {
        return Result.ok(archiveStore.all().stream().map(ArchiveView::from).toList());
    }

    /**
     * 新建/编辑存档页要用的候选清单。
     *
     * <p>只给两样东西，页面缺什么就自己算：
     * <ol>
     *   <li><b>可用会话</b> —— sessionId → 会话名。这是跳板心跳上报的，
     *       只有 DSH 自己知道，Java 这边造不出来。</li>
     *   <li><b>可用渠道</b> —— 只列<b>外部</b>渠道。存档的 {@code Link} 和 {@code contacts}
     *       都只接受外部渠道，把 INTERNAL_* / ADD_* 列出来会让人选到一个必然报错的项。</li>
     * </ol>
     *
     * <p>以前还返回「已有聊天」和「已有账号」两个候选表，但那是从别的存档里抄来的 ——
     * 新建一个存档时，页面真正需要的是「这个存档要连什么」，而不是「别人连过什么」。
     * 页面现在直接手填 + 下拉选，见 {@code archives.html}。
     */
    @GetMapping("/candidates")
    public Result<CandidatesView> candidates() {
        // 会话：跳板报上来的，带名字。用 LinkedHashMap 保留上报顺序
        Map<String, String> sessions = new LinkedHashMap<>();
        for (String id : availableSessions.getSessionIds()) {
            if (id == null || id.isBlank()) {
                continue;
            }
            sessions.put(id, availableSessions.title(id));
        }

        // 渠道：只要外部渠道，按枚举声明顺序
        List<ChannelKind> channels = new ArrayList<>();
        for (ChannelKind kind : ChannelKind.values()) {
            if (kind.getFamily() == ChannelKind.ChannelFamily.EXTERNAL) {
                channels.add(kind);
            }
        }

        return Result.ok(new CandidatesView(sessions, channels));
    }

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

    /** 新建或更新:id 留空 = 新建,有值 = 更新;字段为 null 表示这次不改 */
    @PostMapping
    public Result<ArchiveView> save(@RequestBody ArchiveRequest body) {
        if (body == null) {
            return Result.fail("请求体不能为空");
        }
        try {
            boolean creating = body.id() == null || body.id().isBlank();
            // 编辑时先把旧存档查出来：token 页面拿不到，留空表示沿用旧值
            Archive old = null;
            if (!creating) {
                long id = parseId(body.id());
                if (id < 0) {
                    return Result.fail("存档 id 不合法：" + body.id());
                }
                old = archiveStore.find(id).orElse(null);
                if (old == null) {
                    return Result.fail("存档不存在：" + body.id());
                }
            }

            Archive draft = body.toDomain(old);
            Archive saved = creating ? archiveStore.create(draft) : archiveStore.update(old.id(), draft);
            return Result.ok(ArchiveView.from(saved));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("保存存档失败：{}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

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

        /**
         * 转成领域对象。
         *
         * @param old 编辑前的存档；新建时传 null。用来补回页面拿不到的 token。
         */
        Archive toDomain(Archive old) {
            return new Archive(
                    null,
                    0L,
                    name,
                    enabled,
                    null,
                    session == null ? null : session.toDomain(),
                    links == null ? null
                            : links.stream().map(l -> l.toDomain(old)).toList(),
                    chats == null ? null : chats.stream().map(ChatKey::parse).toList(),
                    null,
                    null,
                    null);
        }
    }

    /** 绑定的会话 */
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

    /** 外部连接;token 只进不出。 */
    public record LinkView(
            Long id,
            String kind,
            Boolean enabled,
            String account,
            String baseUrl,
            String token,
            String deviceId,
            /** 服务端是否已经存着 token。token 本身不下发，页面靠它决定提示语。 */
            Boolean hasStoredToken
    ) {

        /**
         * 转成领域对象。
         *
         * @param old 编辑前的存档（新建时传 null）。
         *            token 是「只进不出」的字段：页面拿不到原值，所以留空必须理解成
         *            「这次不改」，按 kind + account 找回旧值；否则编辑一次就把 token 抹了。
         */
        Archive.Link toDomain(Archive old) {
            ChannelKind channel = ChannelKind.fromWire(kind);
            String resolvedToken = token;
            if (resolvedToken == null && old != null && old.links() != null) {
                resolvedToken = old.links().stream()
                        .filter(l -> l.kind() == channel
                                && java.util.Objects.equals(l.account(), account))
                        .map(Archive.Link::token)
                        .findFirst()
                        .orElse(null);
            }
            return new Archive.Link(id, channel,
                    enabled == null ? Boolean.TRUE : enabled,
                    account, baseUrl, resolvedToken, deviceId);
        }

        /** 出站视图：token 一律不回显，只说「存过没有」。 */
        static LinkView from(Archive.Link l) {
            String token = l.token();
            return new LinkView(l.id(),
                    l.kind() == null ? null : l.kind().wire(),
                    l.isEnabled(), l.account(), l.baseUrl(), null, l.deviceId(),
                    token != null && !token.isBlank());
        }
    }

    // ── 响应体 ──

    /**
     * 候选清单：只有两样，页面缺什么自己算。
     *
     * @param sessions sessionId → 会话名（跳板心跳上报；名字可能为 null）
     * @param channels 可用的外部渠道
     */
    public record CandidatesView(
            Map<String, String> sessions,
            List<ChannelKind> channels
    ) {}


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
