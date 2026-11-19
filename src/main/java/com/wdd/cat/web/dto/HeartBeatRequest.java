package com.wdd.cat.web.dto;


import java.util.Map;

/**
 * &#064program: cat
 * <p>
 * &#064description: POST /api/heartbeat 的请求体：sessionId → 会话名
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-19 19:12
 **/

public record HeartBeatRequest(
    Map<String, String> sessions
){}
