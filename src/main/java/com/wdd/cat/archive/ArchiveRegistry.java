package com.wdd.cat.archive;


import com.wdd.cat.message.ChatKey;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * &#064program: cat
 * <p>
 * &#064description: 存档注册表：跳板每次心跳时上报的「我将要处理哪些存档」的快照,实现成内存态，跳板一发就整体替换，不落盘
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 21:02
 **/


@Component
public class ArchiveRegistry {

    /**
     * 整体替换注册表。
     * @param incoming null 表示「这次别动」，保留现状并返回 false
     * @return 是否真的更新了
     */
    public boolean replaceAll(Map<Long, ServedArchive> incoming) {
        // TODO incoming == null 直接 return false
        // TODO 否则整体替换，return true
        //三态语义（反复踩过坑的bug）：null = 别动 / [] = 清空 / [...] = 替换
        return false;
    }

    /** 当前注册表的只读视图。 */
    public Map<Long, ServedArchive> served() {
        // TODO 返回不可变快照
        return Map.of();
    }

    /** 按存档 ID 取。 */
    public Optional<ServedArchive> of(long archiveId) {
        // TODO 从 map 里 get
        return Optional.empty();
    }

    /** 所有存档涉及的全部会话，用来快速判断「这条消息归谁」。 */
    public Set<ChatKey> allChats() {
        // TODO 把所有 ServedArchive.chats 合并
        return Set.of();
    }

    /** 已在服务的存档。 */
    public record ServedArchive(
            String sessionId,          // DSH 会话 ID
            String sessionStatus,      // "BOUND" / "LOST"
            Set<ChatKey> chats          // 这个存档负责的会话
    ) {
        /** 已绑定且会话 ID 非空。 */
        public boolean isBound() {
            // TODO "BOUND".equals(sessionStatus) && sessionId != null
            return false;
        }
    }

}