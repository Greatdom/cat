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
    private volatile Map<Long, Archive> reported;

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
    public boolean replaceAll(Map<Long, Archive> incoming) {
        if (incoming == null) {
            return false;
        }
        reported = Map.copyOf(incoming);
        return true;
    }

    /** 丢弃上报数据，回退到从 ArchiveStore 推导。 */
    public synchronized void rebuildFromStore() {
        reported = null;
    }

    /** 当前注册表。 */
    public Map<Long, Archive> served() {
        Map<Long, Archive> snapshot = reported;
        return snapshot != null ? snapshot : derive();
    }

    public Optional<Archive> of(long archiveId) {
        return Optional.ofNullable(served().get(archiveId));
    }

    /** 所有存档涉及的全部会话。 */
    public Set<ChatKey> allChats() {
        Set<ChatKey> out = new HashSet<>();
        for (Archive a : served().values()) {
            out.addAll(chatsOf(a));
        }
        return out;
    }

    /** 一个存档负责的会话。 */
    public static Set<ChatKey> chatsOf(Archive a) {
        if (a == null || a.contacts() == null) {
            return Set.of();
        }
        return a.contacts().stream()
                .filter(ChatKey::isEnabled)
                .collect(Collectors.toSet());
    }

    private Map<Long, Archive> derive() {
        return archiveStore.enabled().stream()
                .collect(Collectors.toMap(Archive::id, a -> a));
    }
}
