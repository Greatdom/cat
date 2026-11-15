package com.wdd.cat.common.utils;

import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * &#064program: cat
 * <p>
 * &#064description: JSON 编解码工具
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 22:00
 **/

@Component
public class JsonCodec {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 序列化成紧凑一行。
     *
     * <p>只用于「机器读、人不看」的场合：HTTP 请求体、单行日志。
     */
    public String write(Object value) {
        return mapper.writeValueAsString(value);
    }

    /**
     * 序列化成带缩进的可读格式。
     *
     * <p><b>所有落盘文件都必须用这个</b>：存档、水位、记忆库、设置。
     * 这些文件人会打开看、会手动改、出问题时要靠它排查。
     */
    public String writePretty(Object value) {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
    }

    /** 解析成树；调用方自行判空。 */
    public JsonNode read(String text) {
        return mapper.readTree(text);
    }

    /** 解析成指定类型（状态文件用）。 */
    public <T> T read(String text, Class<T> type) {
        return mapper.readValue(text, type);
    }
}
