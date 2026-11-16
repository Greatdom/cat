package com.wdd.cat.state.environment;


import java.util.List;
import java.util.Map;

/**
 * &#064program: cat
 * <p>
 * &#064description: 环境状态：这个存档现在身处何处、周围什么样
 * 三层结构：SpaceGrid（整个空间）→ Zone（当前区域）→ Cell（具体格子）
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:34
 **/


public record EnvironmentState(
        SpaceGrid grid,                 // 体素空间（可为 null —— 没有空间数据时只是"知道在哪")
        Zone currentZone,               // 当前在哪个区域（卧室/客厅/公司）
        Cell position,                  // 当前坐标
        Map<String, Double> ambience,   // 环境量：temperature / brightness / noise
        List<Prop> nearby,              // 附近能互动的东西（开关灯、喝水）
        long updatedAt,                 // 上次更新的时间
        String source                   // 数据哪来的："default" / "esp32" / "user-defined"
) {
    /** 默认的"舒服的家"，静态环境用。 */
    public static EnvironmentState cozyHome() {
        // TODO 一个固定的卧室场景
        return null;
    }
}