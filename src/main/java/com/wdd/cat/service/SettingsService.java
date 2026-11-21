package com.wdd.cat.service;

import com.wdd.cat.common.config.EditAbleDefaultConfig;
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
 * &#064description: 设置相关业务:读写 {@link EditAbleDefaultConfig} 里那几项
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

    /** 用户覆盖值:只有被改过的键 */
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
                    if (EditAbleDefaultConfig.ALL.contains(e.getKey())) {
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

    /** 当前生效的值 */
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
     */
    public int save(Map<String, Object> incoming) {
        if (incoming == null || incoming.isEmpty()) {
            return 0;
        }
        Map<String, Object> next = new LinkedHashMap<>(overrides);
        int changed = 0;
        for (Map.Entry<String, Object> entry : incoming.entrySet()) {
            String key = entry.getKey();
            EditAbleDefaultConfig.Item item = EditAbleDefaultConfig.item(key);
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

    /**
     * 各键的默认值。
     *
     * <p>从 {@link EditAbleDefaultConfig#ITEMS} 取，不再在本类重写一遍 ——
     * 以前这里手抄了 4 个数字，加配置项时很容易和字段声明对不上。
     */
    private static Map<String, Object> defaults() {
        return EditAbleDefaultConfig.defaults();
    }

    /**
     * 把「默认值 + 用户覆盖」一起写回字段。
     *
     * <p>注意<b>必须先写默认值再写覆盖</b>：用户删掉某一项覆盖后，
     * 只有先写回默认值，那一项才会真的退回默认。
     */
    private void applyToRuntime() {
        for (Map.Entry<String, Object> e : defaults().entrySet()) {
            write(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, Object> e : overrides.entrySet()) {
            write(e.getKey(), e.getValue());
        }
    }

    /** 写一个值到字段；键不认识或值不是数字就跳过。 */
    private static void write(String key, Object raw) {
        if (raw instanceof Number n) {
            EditAbleDefaultConfig.write(key, n.longValue());
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
