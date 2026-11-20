package com.wdd.cat.log;


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
 * 跳板每轮心跳顺带带上自己最近若干条日志 —— 不额外发请求，代价是刷新粒度
 * 受心跳间隔限制（页面轮询 2 秒，但跳板数据 5 秒才更新一次）。
 * <p>
 * 只存本进程启动后收到的，重启就清空。真正的持久化在跳板本地，这里只是展示用。
 * <p>
 * 意图:只实现跳板的日志管理而不实现宿主的日志管理
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 19:52
 **/

@Component
public class LinkerLogReader {

    /** 保留条数：跳板每次最多报 50 条，留 3 倍冗余以防漏。 */
    private static final int CAPACITY = 150;

    private final Deque<LogBuffer.Entry> entries = new ConcurrentLinkedDeque<>();

    /**
     * 用跳板上报的一批日志<b>整体替换</b>。
     * <p>
     * 为什么是替换而不是追加：跳板每次报的都是「我最近 N 条」，是快照不是增量。
     * 追加会导致同一行在页面里重复出现。
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

    /** 最近 N 条（按上报顺序，已升序）。 */
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

    /** 当前缓存条数，排障用。 */
    public int size() {
        return entries.size();
    }
}
