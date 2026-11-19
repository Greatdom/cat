package com.wdd.cat.common.cache;

import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * &#064program: cat
 * &#064description: 当前可用的跳板会话清单；心跳上报什么，它就是什么
 * <p>
 * 空清单 = 跳板还没报过，此时任何消息都送不出去。
 * &#064author: black-cat
 * &#064create: 2026-11-19 12:45
 **/
@Component
public class AvailableSessions {

    /** 整体替换，读多写少。 */
    private volatile Set<String> sessionIds = Set.of();

    /** 还在不在清单里。 */
    public boolean contains(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return false;
        }
        return sessionIds.contains(sessionId);
    }

    /** 当前清单。 */
    public List<String> getSessionIds() {
        return List.copyOf(sessionIds);
    }

    /** 心跳到达时整体覆盖；空表等于清空。 */
    public void replace(List<String> incoming) {
        Set<String> next = new LinkedHashSet<>();
        if (incoming != null) {
            for (String id : incoming) {
                if (id != null && !id.isBlank()) {
                    next.add(id.trim());
                }
            }
        }
        sessionIds = Set.copyOf(next);
    }

    public int size() {
        return sessionIds.size();
    }

    //长期计划:心跳到达后追加而不是覆盖
    // 还有维护一个时间戳,当具体的sessionId被更新时更新时间戳,然后维护一个定时任务,
    //任务的内容是如果发现具体的sessionId太久没更新就会被剔除掉
}
