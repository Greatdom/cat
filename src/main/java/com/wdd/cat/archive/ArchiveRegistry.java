package com.wdd.cat.archive;

import com.wdd.cat.message.ChatKey;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * &#064program: cat
 * &#064description: "我在服务哪些存档和会话"的快照
 * <p>
 * 第一版直接从 ArchiveStore 推导；跳板心跳接入后改用上报数据覆盖。
 * &#064author: black-cat
 * &#064create: 2026-11-15 21:02
 **/
@Component
public class ArchiveRegistry {

    private final ArchiveStore archiveStore;

    /** 跳板上报的快照；null 表示还没收到过，回退到 ArchiveStore 推导。 */
    private volatile Map<Long, ServedArchive> reported;

    public ArchiveRegistry(ArchiveStore archiveStore) {
        this.archiveStore = archiveStore;
    }

    @PostConstruct
    void init() {
        rebuildFromStore();
    }

    /**
     * 整体替换注册表。
     * <p>三态：null = 别动，空 map = 清空，非空 = 替换。
     *
     * @return 是否真的更新了
     */
    public boolean replaceAll(Map<Long, ServedArchive> incoming) {
        if (incoming == null) {
            return false;
        }
        reported = Map.copyOf(incoming);
        return true;
    }

    /** 从存档配置重新推导（跳板没上报时的兜底）。 */
    public synchronized void rebuildFromStore() {
        reported = null;
    }

    /** 当前注册表。 */
    public Map<Long, ServedArchive> served() {
        Map<Long, ServedArchive> snapshot = reported;
        return snapshot != null ? snapshot : derive();
    }

    public Optional<ServedArchive> of(long archiveId) {
        return Optional.ofNullable(served().get(archiveId));
    }

    /** 所有存档涉及的全部会话。 */
    public Set<ChatKey> allChats() {
        Set<ChatKey> out = new HashSet<>();
        for (ServedArchive s : served().values()) {
            if (s.chats() != null) {
                out.addAll(s.chats());
            }
        }
        return out;
    }

    private Map<Long, ServedArchive> derive() {
        return archiveStore.enabled().stream()
                .collect(Collectors.toMap(
                        Archive::id,
                        a -> new ServedArchive(
                                a.session() == null ? null : a.session().sessionId(),
                                a.session() == null || a.session().status() == null
                                        ? "LOST" : a.session().status().name(),
                                a.contacts() == null ? Set.of()
                                        : a.contacts().stream()
                                                .filter(Archive.Contact::isEnabled)
                                                .map(Archive.Contact::chatKey)
                                                .collect(Collectors.toSet()))));
    }

    /** 已在服务的存档。 */
    public record ServedArchive(
            String sessionId,
            String sessionStatus,
            Set<ChatKey> chats
    ) {
        /** 已绑定且会话 ID 非空。 */
        public boolean isBound() {
            return "BOUND".equalsIgnoreCase(sessionStatus)
                    && sessionId != null
                    && !sessionId.isBlank();
        }
    }
}
