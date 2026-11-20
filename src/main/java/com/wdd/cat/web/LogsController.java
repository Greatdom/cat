package com.wdd.cat.web;

import com.wdd.cat.common.entity.Result;
import com.wdd.cat.log.LinkerLogReader;
import com.wdd.cat.log.LogBuffer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * &#064program: cat
 * <p>
 * &#064description: 日志页Controller
 * <p>
 * 两源合并：
 * <pre>
 *   ① java    Java 自己的日志（内存缓冲 200 条）
 *   ② linker  JS 跳板心跳上报的日志（最近 150 条）
 * </pre>
 * 为什么合并：一次消息流转会跨两个进程，分开看是「两个孤立的窗口」；
 * 合并后按时间排、按 itemId 归组，才看得出完整因果链。
 * <p>
 * 宿主（DSH）的日志不归这里管 —— 那是宿主自己的事。
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 19:53
 **/
@RestController
@RequestMapping("/api/logs")
public class LogsController {

    private final LogBuffer javaLogs;
    private final LinkerLogReader linkerLogs;

    public LogsController(LogBuffer javaLogs, LinkerLogReader linkerLogs) {
        this.javaLogs = javaLogs;
        this.linkerLogs = linkerLogs;
    }

    /**
     * 合并查询。
     *
     * @param limit  每源最多取多少条（默认 200）
     * @param itemId 只保留该消息的日志；空则全部
     * @param source 只保留某个来源（java / linker），空则全部
     */
    @GetMapping
    public Result<MergedLogs> logs(
            @RequestParam(defaultValue = "200") int limit,
            @RequestParam(required = false) String itemId,
            @RequestParam(required = false) String source) {

        int capped = Math.max(1, Math.min(limit, 500));
        String needle = itemId == null || itemId.isBlank() ? null : itemId.trim();
        String only = source == null || source.isBlank() ? null : source.trim().toLowerCase();

        List<LogBuffer.Entry> javaList = List.of();
        List<LogBuffer.Entry> linkerList = List.of();
        if (only == null || "java".equals(only)) {
            javaList = javaLogs.byItemId(needle, capped);
        }
        if (only == null || "linker".equals(only)) {
            linkerList = linkerLogs.byItemId(needle, capped);
        }

        List<Map<String, Object>> all = new ArrayList<>();
        for (LogBuffer.Entry e : javaList) {
            all.add(toJson("java", e));
        }
        for (LogBuffer.Entry e : linkerList) {
            all.add(toJson("linker", e));
        }
        // 两源时钟精度不同，按时间排只能保证「大致有序」，同一 itemId 的归组更可靠
        all.sort(Comparator.comparingLong(m -> ((Number) m.get("time")).longValue()));

        Map<String, Integer> sources = new LinkedHashMap<>();
        sources.put("java", javaList.size());
        sources.put("linker", linkerList.size());

        return Result.ok(new MergedLogs(all, all.size(), sources));
    }

    private static Map<String, Object> toJson(String source, LogBuffer.Entry e) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("time", e.time());
        item.put("source", source);
        item.put("level", e.level());
        item.put("chatKey", e.chatKey());
        item.put("itemId", e.itemId());
        item.put("logger", e.logger());
        item.put("text", e.text());
        return item;
    }

    /**
     * 合并结果。
     *
     * @param entries 按时间升序的合并条目
     * @param count   总条数
     * @param sources 各来源各自的条数（便于判断是不是某一路没数据）
     */
    public record MergedLogs(List<Map<String, Object>> entries, int count,
                             Map<String, Integer> sources) {
    }
}
