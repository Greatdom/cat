package com.wdd.cat.web;

import com.wdd.cat.common.config.EditableSettings;
import com.wdd.cat.common.entity.Result;
import com.wdd.cat.service.SettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * &#064program: cat
 * <p>
 * &#064description: 设置页Controller
 * <p>
 * GET  /api/settings            读当前值 + 分组元信息
 * POST /api/settings            保存（只认白名单里的键）
 * POST /api/settings/reset/{k}  某一项恢复默认
 * DELETE /api/settings          清空全部覆盖
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 20:07
 **/
@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private static final Logger log = LoggerFactory.getLogger(SettingsController.class);

    private final SettingsService settings;

    public SettingsController(SettingsService settings) {
        this.settings = settings;
    }

    /** 当前配置 + 分组元信息。页面据此决定渲染哪些项。 */
    @GetMapping
    public Result<SettingsView> get() {
        List<GroupView> groups = new ArrayList<>();
        for (EditableSettings.Group group : EditableSettings.GROUPS) {
            List<ItemView> items = new ArrayList<>();
            for (String key : group.keys()) {
                EditableSettings.Item item = EditableSettings.item(key);
                if (item == null) {
                    continue;
                }
                items.add(new ItemView(item.key(), item.label(), item.hint(), item.unit(),
                        item.min(), item.max(), EditableSettings.needsRestart(key)));
            }
            groups.add(new GroupView(group.title(), group.description(), items));
        }
        return Result.ok(new SettingsView(settings.current(), settings.overrides(), groups));
    }

    /** 保存。body 形如 {"mergeWindowMs": 1500}。 */
    @PostMapping
    public Result<Map<String, Object>> save(@RequestBody Map<String, Object> body) {
        int changed = settings.save(body);
        log.info("设置已保存：{} 项", changed);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("changed", changed);
        out.put("values", settings.current());
        return Result.ok(out);
    }

    /** 某一项恢复默认值。 */
    @PostMapping("/reset/{key}")
    public Result<SettingsView> reset(@PathVariable String key) {
        settings.reset(key);
        return get();
    }

    /** 清空全部覆盖，全回到默认值。 */
    @DeleteMapping
    public Result<SettingsView> clearAll() {
        settings.clearAll();
        log.info("设置已全部恢复默认");
        return get();
    }

    // ── 响应体 ──

    /** 整页数据。 */
    public record SettingsView(
            Map<String, Object> values,     // 当前生效值
            Map<String, Object> overrides,  // 只含被改过的
            List<GroupView> groups          // 页面渲染顺序与文案
    ) {}

    /** 一组。 */
    public record GroupView(String title, String description, List<ItemView> items) {}

    /** 一项。 */
    public record ItemView(
            String key, String label, String hint, String unit,
            long min, long max, boolean needsRestart) {}
}
