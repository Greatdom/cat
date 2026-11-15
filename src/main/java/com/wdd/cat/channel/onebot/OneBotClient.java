package com.wdd.cat.channel.onebot;
import com.wdd.cat.common.utils.JsonCodec;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

/**
 * &#064program: cat
 * <p>
 * &#064description: OneBot v11 HTTP 连接,负责提供收发请求的客户端
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 21:57
 **/


public class OneBotClient {
    JsonCodec json;
    String baseUrl;
    String accessToken;
    int timeoutMs;

    public OneBotClient(JsonCodec json, String baseUrl, String accessToken, int timeoutMs) {
        // TODO 存字段
    }

    /** 发一个 action，返回响应里的 data 节点。 */
    public JsonNode call(String action, Map<String, Object> payload) {
        // TODO POST {baseUrl}/{action}，带 Authorization: Bearer {accessToken}
        // TODO 超时 timeoutMs，失败抛异常或返回 retcode != 0
        return null;
    }

    /** 这个 client 是不是这个 baseUrl + token（连接池复用查重）。 */
    public boolean matches(String baseUrl, String token) {
        // TODO 两者都相等
        return false;
    }

    public String baseUrl() {
        return baseUrl;   // TODO 返回构造时存的 baseUrl
    }
}