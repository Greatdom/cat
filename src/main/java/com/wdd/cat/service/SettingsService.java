package com.wdd.cat.service;

import com.wdd.cat.common.config.EditAbleDefaultConfig;
import com.wdd.cat.common.config.EditableSettings;
import com.wdd.cat.common.config.UnEditAbleDefaultConfig;
import com.wdd.cat.common.utils.JsonCodec;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * &#064program: cat
 * <p>
 * &#064description: 设置相关业务：读写 {@link EditAbleDefaultConfig} 里那几项
 * <p>
 * 默认值写死在 {@link EditAbleDefaultConfig}，用户覆盖值落盘到 data/settings.json。
 * 读的时候默认值打底、覆盖值盖上。
 * <p>
 * 保存时同时把值写回字段，所以改完立刻生效 —— 前提是读取方每次都取字段值，
 * 不把它缓存进局部变量。
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 20:08
 **/
@Component
public class SettingsService {

    private static final Logger log = LoggerFactory.getLogger(SettingsService.class);

    private static final String FILE_NAME = "settings.json";

    private final JsonCodec json;

    /** 用户覆盖值：只有被改过的键。 */
    private volatile Map<String, Object> overrides = new LinkedHashMap<>();

    public SettingsService(JsonCodec json) {
        this.json = json;
    }

    @PostConstruct
    void load() {
        Path file = file();
        if (!Files.isReadable(file)) {
            log.info("没有 settings.json，全部走默认值");
            return;
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> loaded = json.read(Files.readString(file, StandardCharsets.UTF_8),
                    Map.class);
            if (loaded != null) {
                Map<String, Object> next = new LinkedHashMap<>();
                // 只留还认识的键：老文件里可能有已经删掉的项
                for (Map.Entry<String, Object> e : loaded.entrySet()) {
                    if (EditableSettings.ALL.contains(e.getKey())) {
                        next.put(e.getKey(), e.getValue());
                    }
                }
                overrides = next;
            }
            log.info("已载入 {} 项用户设置", overrides.size());
        } catch (Exception e) {
            log.warn("settings.json 读不出来，全部走默认值：{}", e.getMessage());
            return;
        }
        applyToRuntime();
    }

    /** 当前生效的值（默认值 + 覆盖）。 */
    public Map<String, Object> current() {
        Map<String, Object> out = defaults();
        out.putAll(overrides);
        return out;
    }

    /** 只返回被用户改过的键。 */
    public Map<String, Object> overrides() {
        return new LinkedHashMap<>(overrides);
    }

    /**
     * 保存用户改动。只接受白名单里的键，值会夹到合法区间内。
     *
     * @return 实际保存的键数
     */
    public int save(Map<String, Object> incoming) {
        if (incoming == null || incoming.isEmpty()) {
            return 0;
        }
        Map<String, Object> next = new LinkedHashMap<>(overrides);
        int changed = 0;
        for (Map.Entry<String, Object> entry : incoming.entrySet()) {
            String key = entry.getKey();
            EditableSettings.Item item = EditableSettings.item(key);
            if (item == null) {
                log.debug("忽略不可编辑的键：{}", key);
                continue;
            }
            Long value = toNumber(entry.getValue(), item.min(), item.max());
            if (value == null) {
                log.warn("设置项 {} 的值不合法，跳过：{}", key, entry.getValue());
                continue;
            }
            next.put(key, value);
            changed++;
        }
        overrides = next;
        applyToRuntime();
        persist();
        return changed;
    }

    /** 删除一项覆盖，回到默认值。 */
    public boolean reset(String key) {
        if (key == null || !overrides.containsKey(key)) {
            return false;
        }
        Map<String, Object> next = new LinkedHashMap<>(overrides);
        next.remove(key);
        overrides = next;
        applyToRuntime();
        persist();
        return true;
    }

    /** 清空全部覆盖，全回到默认值。 */
    public void clearAll() {
        overrides = new LinkedHashMap<>();
        applyToRuntime();
        persist();
    }

    // ── 内部 ──

    /** 各键的默认值：从字段读，所以「恢复默认」拿到的是代码里写死的值。 */
    private static Map<String, Object> defaults() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("archiveMaxCount", 4);
        out.put("mergeWindowMs", 1200L);
        out.put("mergeMaxCount", 8);
        out.put("qqPollAndSendIntervalMs", 2000L);
        return out;
    }

    /** 先恢复默认，再盖上覆盖 —— 否则删掉一项不会退回默认。 */
    private void applyToRuntime() {
        Map<String, Object> all = defaults();
        all.putAll(overrides);
        for (Map.Entry<String, Object> e : all.entrySet()) {
            if (e.getValue() instanceof Number n) {
                writeField(e.getKey(), n.longValue());
            }
        }
    }

    /** 按字段名写回 {@link EditAbleDefaultConfig}。 */
    private static void writeField(String key, long value) {
        switch (key) {
            case "archiveMaxCount" -> EditAbleDefaultConfig.archiveMaxCount = (int) value;
            case "mergeWindowMs" -> EditAbleDefaultConfig.mergeWindowMs = value;
            case "mergeMaxCount" -> EditAbleDefaultConfig.mergeMaxCount = (int) value;
            case "qqPollAndSendIntervalMs" -> EditAbleDefaultConfig.qqPollAndSendIntervalMs = value;
            default -> {
                // 白名单里没有的键不写
            }
        }
    }

    /** 转成数字并夹到区间内；解析不了返回 null。 */
    private static Long toNumber(Object raw, long min, long max) {
        long v;
        if (raw instanceof Number n) {
            v = n.longValue();
        } else if (raw instanceof String s) {
            try {
                v = Long.parseLong(s.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            return null;
        }
        return Math.max(min, Math.min(max, v));
    }

    /** 落盘。失败只记日志，不阻断。 */
    private synchronized void persist() {
        Path file = file();
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            Path tmp = file.resolveSibling(FILE_NAME + ".tmp");
            Files.writeString(tmp, json.writePretty(overrides), StandardCharsets.UTF_8);
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException | RuntimeException e) {
            log.warn("settings.json 落盘失败：{}", e.getMessage());
        }
    }

    private static Path file() {
        return Path.of(UnEditAbleDefaultConfig.DATA_DIR).resolve(FILE_NAME);
    }
}
