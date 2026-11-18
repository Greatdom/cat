package com.wdd.cat.channel.onebot;

import com.wdd.cat.common.utils.JsonCodec;
import com.wdd.cat.exception.OneBotException;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * &#064program: cat
 * &#064description: OneBot v11 HTTP 客户端；一个机器人账号一个实例
 * &#064author: black-cat
 * &#064create: 2026-11-15 21:57
 **/
public class OneBotClient {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final JsonCodec json;
    private final String baseUrl;
    private final String accessToken;
    private final int timeoutMs;

    public OneBotClient(JsonCodec json, String baseUrl, String accessToken, int timeoutMs) {
        this.json = json;
        this.baseUrl = trimSlash(baseUrl);
        this.accessToken = accessToken == null ? "" : accessToken.trim();
        this.timeoutMs = timeoutMs > 0 ? timeoutMs : 10_000;
    }

    /** 发一个 action，返回响应里的 data 节点。 */
    public JsonNode call(String action, Map<String, Object> payload) {
        String url = baseUrl + "/" + action;
        String body = json.write(payload == null ? Map.of() : payload);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMs))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        if (!accessToken.isEmpty()) {
            builder.header("Authorization", "Bearer " + accessToken);
        }

        HttpResponse<String> response;
        try {
            response = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OneBotException(action, "调用被中断");
        } catch (Exception e) {
            throw new OneBotException(action, "连不上 QQ 后端（" + baseUrl + "）：" + e.getMessage(), e);
        }

        if (response.statusCode() / 100 != 2) {
            throw new OneBotException(action, "HTTP " + response.statusCode() + "：" + brief(response.body()));
        }

        JsonNode root;
        try {
            root = json.read(response.body());
        } catch (Exception e) {
            throw new OneBotException(action, "响应不是合法 JSON：" + brief(response.body()), e);
        }

        int retcode = root.path("retcode").asInt(-1);
        String status = root.path("status").asText("");
        if (!"ok".equals(status) || retcode != 0) {
            String detail = root.path("wording").asText(root.path("msg").asText(""));
            throw new OneBotException(action,
                    "被拒绝：retcode " + retcode + (detail.isEmpty() ? "" : " " + detail));
        }
        return root.path("data");
    }

    /** 这个 client 是不是这个 baseUrl + token（连接池复用查重）。 */
    public boolean matches(String baseUrl, String token) {
        String otherToken = token == null ? "" : token.trim();
        return this.baseUrl.equals(trimSlash(baseUrl)) && this.accessToken.equals(otherToken);
    }

    public String baseUrl() {
        return baseUrl;
    }

    private static String trimSlash(String s) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        while (t.endsWith("/")) {
            t = t.substring(0, t.length() - 1);
        }
        return t;
    }

    private static String brief(String body) {
        if (body == null) {
            return "";
        }
        return body.length() <= 200 ? body : body.substring(0, 200) + "...";
    }
}
