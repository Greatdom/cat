package com.wdd.cat.log;


import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * &#064program: cat
 * <p>
 * &#064description: 实现对java控制台/内存日志的查阅
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 20:03
 **/
@Component
public class LogBuffer {

    /** 日志保留条数上限 */
    private static final int CAPACITY = 200;

    private final Deque<Entry> entries = new ConcurrentLinkedDeque<>();

    /** 追加一条。由 {@link LogBufferAppender} 在日志事件里调用。 */
    public void add(Entry entry) {
        if (entry == null) {
            return;
        }
        entries.addLast(entry);
        while (entries.size() > CAPACITY) {
            entries.pollFirst();
        }
    }

    /** 取最近 N 条，按时间升序。 */
    public List<Entry> recent(int limit) {
        int capped = Math.max(1, Math.min(limit, CAPACITY));
        List<Entry> all = new ArrayList<>(entries);
        int from = Math.max(0, all.size() - capped);
        return new ArrayList<>(all.subList(from, all.size()));
    }

    /** 按 itemId 精确筛选；空则等同 recent。 */
    public List<Entry> byItemId(String itemId, int limit) {
        if (itemId == null || itemId.isBlank()) {
            return recent(limit);
        }
        List<Entry> matched = new ArrayList<>();
        for (Entry entry : entries) {
            if (itemId.equals(entry.itemId())) {
                matched.add(entry);
            }
        }
        int capped = Math.max(1, Math.min(limit, CAPACITY));
        int from = Math.max(0, matched.size() - capped);
        return new ArrayList<>(matched.subList(from, matched.size()));
    }

    /**
     * 一条日志。
     */
    public record Entry(long time, String level, String chatKey, String itemId,
                        String logger, String text) {

        public Instant instant() {
            return Instant.ofEpochMilli(time);
        }
    }
}
