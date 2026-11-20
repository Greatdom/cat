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
 * 为什么读内存而不是读文件：文件是给人 grep 的，格式冗长；页面要的是结构化条目
 * （时间 / 级别 / itemId / 正文）。这里只收 {@code com.wdd.cat} 自己的日志。
 * <p>
 * 容量固定，满了丢最老的 —— 内存里不该无限增长。
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 20:03
 **/
@Component
public class LogBuffer {

    /** 保留条数。够看近期行为，又不吃内存。 */
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
     *
     * @param time    毫秒时间戳
     * @param level   INFO / WARN / ERROR / DEBUG
     * @param chatKey 聊天标识，可为空
     * @param itemId  消息标识（跨进程串联用），可为空
     * @param logger  打日志的类名（短名）
     * @param text    日志正文
     */
    public record Entry(long time, String level, String chatKey, String itemId,
                        String logger, String text) {

        /** 方便页面显示。 */
        public Instant instant() {
            return Instant.ofEpochMilli(time);
        }
    }
}
