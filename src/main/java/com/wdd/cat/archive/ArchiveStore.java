package com.wdd.cat.archive;

import com.wdd.cat.channel.ConvertContext;
import com.wdd.cat.common.config.EditAbleDefaultConfig;
import com.wdd.cat.common.config.UnEditAbleDefaultConfig;
import com.wdd.cat.common.utils.JsonCodec;
import com.wdd.cat.common.utils.SnowflakeId;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * &#064program: cat
 * &#064description: 存档仓库：增删改查 + 落盘 + 五条校验
 * &#064author: black-cat
 * &#064create: 2026-11-15 20:54
 **/
@Component
public class ArchiveStore {

    private static final Logger log = LoggerFactory.getLogger(ArchiveStore.class);

    /** 存档文件名。 */
    private static final String FILE_NAME = "archive.json";

    private final Map<Long, Archive> archives = new ConcurrentHashMap<>();

    private final SnowflakeId ids;
    private final JsonCodec json;

    public ArchiveStore(SnowflakeId ids, JsonCodec json) {
        this.ids = ids;
        this.json = json;
    }

    // ── 落盘 ──

    @PostConstruct
    void loadAll() {
        Path root = dataRoot();
        if (!Files.isDirectory(root)) {
            return;
        }
        try (var dirs = Files.list(root)) {
            dirs.filter(Files::isDirectory).forEach(dir -> {
                Path file = dir.resolve(FILE_NAME);
                if (!Files.isReadable(file)) {
                    return;
                }
                try {
                    Archive a = json.read(Files.readString(file, StandardCharsets.UTF_8), Archive.class);
                    if (a != null) {
                        archives.put(a.id(), a);
                    }
                } catch (Exception e) {
                    log.warn("存档读不出来，跳过 {}：{}", dir.getFileName(), e.getMessage());
                }
            });
        } catch (IOException e) {
            log.warn("存档目录读不出来：{}", e.getMessage());
        }
        log.info("已加载 {} 个存档", archives.size());
    }

    private void save(Archive archive) {
        Path dir = archiveDir(archive.id());
        try {
            Files.createDirectories(dir);
            Files.createDirectories(dir.resolve("assets"));
            Path target = dir.resolve(FILE_NAME);
            Path tmp = target.resolveSibling(FILE_NAME + ".tmp");
            Files.writeString(tmp, json.writePretty(archive), StandardCharsets.UTF_8);
            moveAtomically(tmp, target);
        } catch (IOException e) {
            throw new UncheckedIOException("存档落盘失败：" + archive.id(), e);
        }
    }

    private static void moveAtomically(Path from, Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Path dataRoot() {
        return Path.of(UnEditAbleDefaultConfig.DATA_DIR);
    }

    private static Path archiveDir(long id) {
        return dataRoot().resolve(Long.toString(id));
    }
    private static Path rubbishDir(long id) {
        return dataRoot().resolve("rubbish")
                .resolve(id + "-" + System.currentTimeMillis());
    }

    // ── 查询 ──

    /** 全部存档，按 id 升序。 */
    public List<Archive> all() {
        return archives.values().stream()
                .sorted((a, b) -> Long.compare(a.id(), b.id()))
                .toList();
    }

    public Optional<Archive> find(long id) {
        return Optional.ofNullable(archives.get(id));
    }

    /** 只返回启用的存档。 */
    public List<Archive> enabled() {
        return all().stream().filter(Archive::isEnabled).toList();
    }

    /** 所有已占用的账号，"external_qq:10000" 格式。 */
    public Set<String> allAccounts() {
        Set<String> out = new HashSet<>();
        for (Archive a : archives.values()) {
            if (a.links() == null) {
                continue;
            }
            for (Archive.Link l : a.links()) {
                if (l.account() != null && !l.account().isBlank()) {
                    out.add(ConvertContext.key(l.kind(), l.account()));
                }
            }
        }
        return out;
    }

    public int maxCount() {
        return EditAbleDefaultConfig.archiveMaxCount;
    }

    // ── 增删改 ──

    public Archive create(Archive draft) {
        if (draft == null) {
            throw new IllegalArgumentException("存档不能为空");
        }
        if (archives.size() >= maxCount()) {
            throw new IllegalStateException("存档数量已达上限 " + maxCount());
        }
        validate(draft, -1L);

        String now = Instant.now().toString();
        Archive created = new Archive(
                UnEditAbleDefaultConfig.ARCHIVE_VERSION,
                ids.next(),
                draft.name().trim(),
                draft.enabled() == null ? Boolean.TRUE : draft.enabled(),
                draft.memory() == null ? new Archive.Memory("long-short-term") : draft.memory(),
                draft.session(),
                draft.links() == null ? List.of() : draft.links(),
                draft.contacts() == null ? List.of() : draft.contacts(),
                draft.pipeline() == null ? PipelineConfig.defaultConfig() : draft.pipeline(),
                now,
                now
        );
        archives.put(created.id(), created);
        save(created);
        log.info("创建存档 {}（{}）", created.id(), created.name());
        return created;
    }

    public Archive update(long id, Archive draft) {
        Archive old = archives.get(id);
        if (old == null) {
            throw new IllegalArgumentException("存档不存在：" + id);
        }
        if (draft == null) {
            throw new IllegalArgumentException("存档不能为空");
        }
        validate(draft, id);

        Archive updated = new Archive(
                old.version(),
                id,
                draft.name().trim(),
                draft.enabled() == null ? old.enabled() : draft.enabled(),
                draft.memory() == null ? old.memory() : draft.memory(),
                draft.session() == null ? old.session() : draft.session(),
                draft.links() == null ? old.links() : draft.links(),
                draft.contacts() == null ? old.contacts() : draft.contacts(),
                draft.pipeline() == null ? old.pipeline() : draft.pipeline(),
                old.createdAt(),
                Instant.now().toString()
        );
        archives.put(id, updated);
        save(updated);
        log.info("更新存档 {}（{}）", id, updated.name());
        return updated;
    }

    public void delete(long id) {
        Archive removed = archives.remove(id);
        if (removed == null) return;

        Path from = archiveDir(id);
        Path to = rubbishDir(id);
        try {
            Files.createDirectories(to.getParent());
            Files.move(from, to);
        } catch (IOException e) {
            log.warn("移进回收站失败：{}", e.getMessage());
        }
        log.info("存档 {} 移进回收站 {}", id, to);
    }


    /** 改会话绑定状态。 */
    public void markSession(long id, Archive.SessionStatus status) {
        Archive old = archives.get(id);
        if (old == null) {
            return;
        }
        Archive.Session s = old.session();
        Archive.Session next = s == null
                ? new Archive.Session(null, status, null)
                : new Archive.Session(s.sessionId(), status, s.title());
        Archive updated = new Archive(
                old.version(), old.id(), old.name(), old.enabled(), old.memory(),
                next, old.links(), old.contacts(), old.pipeline(), old.createdAt(),
                Instant.now().toString()
        );
        archives.put(id, updated);
        save(updated);
    }

    /** 改会话名（由跳板回填）。 */
    public void updateSessionTitle(long id, String title) {
        Archive old = archives.get(id);
        if (old == null || old.session() == null) {
            return;
        }
        Archive.Session s = old.session();
        Archive.Session next = new Archive.Session(s.sessionId(), s.status(), title);
        Archive updated = new Archive(
                old.version(), old.id(), old.name(), old.enabled(), old.memory(),
                next, old.links(), old.contacts(), old.pipeline(), old.createdAt(),
                Instant.now().toString()
        );
        archives.put(id, updated);
        save(updated);
    }

    // ── 存档合法性校验 ──

    private void validate(Archive draft, long selfId) {
        String name = draft.name() == null ? "" : draft.name().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("存档名不能为空");
        }

        Set<String> names = new HashSet<>();
        Set<String> sessionIds = new HashSet<>();
        Set<String> accounts = new HashSet<>();
        for (Archive a : archives.values()) {
            if (a.id() == selfId) {
                continue;
            }
            names.add(a.name());
            if (a.session() != null && a.session().sessionId() != null) {
                sessionIds.add(a.session().sessionId());
            }
            if (a.links() != null) {
                a.links().stream()
                        .filter(l -> l.account() != null)
                        .map(l -> (l.kind() + ":" + l.account()).toLowerCase())
                        .forEach(accounts::add);
            }
        }

        if (names.contains(name)) {
            throw new IllegalArgumentException("存档名重复：" + name);
        }

        if (draft.session() != null && draft.session().sessionId() != null
                && sessionIds.contains(draft.session().sessionId())) {
            throw new IllegalArgumentException("会话已被其他存档占用：" + draft.session().sessionId());
        }

        if (draft.links() != null) {
            List<String> keys = draft.links().stream()
                    .filter(l -> l.account() != null && !l.account().isBlank())
                    .map(l -> (l.kind() + ":" + l.account()).toLowerCase())
                    .collect(Collectors.toCollection(ArrayList::new));
            Set<String> dup = new HashSet<>();
            for (String k : keys) {
                if (!accounts.add(k)) {
                    dup.add(k);
                }
            }
            if (!dup.isEmpty()) {
                throw new IllegalArgumentException("账号重复：" + dup);
            }
        }
    }
}
