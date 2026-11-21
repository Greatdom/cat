package com.wdd.cat.web.dto;


import java.util.List;
import java.util.Map;

/**
 * &#064program: cat
 * <p>
 * &#064description: POST /api/heartbeat 的请求体
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-19 19:12
 **/

public record HeartBeatRequest(
    Map<String, String> sessions,
    List<LogEntry> logs
){

    /**
     * 一条跳板日志,字段与 com.wdd.cat.log.LogBuffer.Entry 对齐
     */
    public record LogEntry(
            long time,
            String level,
            String chatKey,
            String itemId,
            String logger,
            String text
    ) {}
}
