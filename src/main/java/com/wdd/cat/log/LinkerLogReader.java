package com.wdd.cat.log;


import com.wdd.cat.common.config.UnEditAbleDefaultConfig;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * &#064program: cat
 * <p>
 * &#064description: 跳板日志读取器
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 19:52
 **/

@Component
public class LinkerLogReader {

    /** 日志保留条数上限 */
    private static final int CAPACITY = UnEditAbleDefaultConfig.LINKER_LOG_CAPACITY;

    private final Deque<LogBuffer.Entry> entries = new ConcurrentLinkedDeque<>();

    /**
     * 用跳板上报的一批日志整体替换
     */
    public void replace(List<LogBuffer.Entry> incoming) {
        entries.clear();
        if (incoming == null) {
            return;
        }
        int from = Math.max(0, incoming.size() - CAPACITY);
        for (int i = from; i < incoming.size(); i++) {
            entries.addLast(incoming.get(i));
        }
    }

    /** 最近 N 条 */
    public List<LogBuffer.Entry> recent(int limit) {
        int capped = Math.max(1, Math.min(limit, CAPACITY));
        List<LogBuffer.Entry> all = new ArrayList<>(entries);
        int from = Math.max(0, all.size() - capped);
        return new ArrayList<>(all.subList(from, all.size()));
    }

    /** 按 itemId 过滤。 */
    public List<LogBuffer.Entry> byItemId(String itemId, int limit) {
        if (itemId == null || itemId.isBlank()) {
            return recent(limit);
        }
        List<LogBuffer.Entry> matched = new ArrayList<>();
        for (LogBuffer.Entry entry : entries) {
            if (itemId.equals(entry.itemId())) {
                matched.add(entry);
            }
        }
        int capped = Math.max(1, Math.min(limit, CAPACITY));
        int from = Math.max(0, matched.size() - capped);
        return new ArrayList<>(matched.subList(from, matched.size()));
    }

    /** 当前缓存条数 */
    public int size() {
        return entries.size();
    }
}
