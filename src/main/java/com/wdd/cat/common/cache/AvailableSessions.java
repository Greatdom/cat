package com.wdd.cat.common.cache;

import com.wdd.cat.archive.Archive;
import com.wdd.cat.archive.ArchiveStore;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * &#064program: cat
 * &#064description: 当前可用的跳板会话清单
 * 心跳只是把它返回给跳板。
 * &#064author: black-cat
 * &#064create: 2026-11-19 12:45
 **/
@Component
public class AvailableSessions {

    //TODO维护一个可用的SessionId的链表List<String>
    private List<String> sessionIds;

    /** 还在不在（现存存档里有个会话就叫这个名字）。 */
    public boolean contains(String sessionId) {
        return true;//TODO 未实现
    }

    public List<String> getSessionIds() {
        return sessionIds;
    }

    public void setSessionIds(List<String> sessionIds) {
        this.sessionIds = sessionIds;
    }
}
