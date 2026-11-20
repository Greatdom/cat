package com.wdd.cat.web.dto;


import java.util.List;
import java.util.Map;

/**
 * &#064program: cat
 * <p>
 * &#064description: POST /api/heartbeat 的请求体
 * <p>
 * sessions：sessionId → 会话名，整体覆盖服务端记录。
 * logs：跳板自己的最近若干条日志，整体覆盖日志页里那一路。
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
     * 一条跳板日志。字段与 com.wdd.cat.log.LogBuffer.Entry 对齐。
     *
     * @param time    毫秒时间戳
     * @param level   INFO / WARN / ERROR / DEBUG
     * @param chatKey 聊天标识，可为空
     * @param itemId  消息标识，可为空
     * @param logger  打日志的模块名
     * @param text    正文
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
