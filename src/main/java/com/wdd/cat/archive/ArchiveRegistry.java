package com.wdd.cat.archive;

import com.wdd.cat.message.ChatKey;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * &#064program: cat
 * &#064description: 跳板心跳上报的"我将要处理哪些存档"快照，内存态，不落盘
 * &#064author: black-cat
 * &#064create: 2026-11-15 21:02
 **/
@Component
public class ArchiveRegistry {

    /**
     * 存档注册表
     */
    private volatile Map<Long, ServedArchive> served = Map.of();

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
        served = Map.copyOf(incoming);
        return true;
    }

    /** 当前注册表的只读视图。 */
    public Map<Long, ServedArchive> served() {
        return served;
    }

    public Optional<ServedArchive> of(long archiveId) {
        return Optional.ofNullable(served.get(archiveId));
    }

    /** 所有存档涉及的全部会话。 */
    public Set<ChatKey> allChats() {
        Set<ChatKey> out = new HashSet<>();
        for (ServedArchive s : served.values()) {
            if (s.chats() != null) {
                out.addAll(s.chats());
            }
        }
        return out;
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
