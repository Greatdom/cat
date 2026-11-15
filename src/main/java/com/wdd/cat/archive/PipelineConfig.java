package com.wdd.cat.archive;


/**
 * &#064program: cat
 * <p>
 * &#064description: 存档的装配单：每个链路钩子用哪个实现
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 20:49
 **/

public record PipelineConfig(
        //TODO 变量和方法参数应该用内部枚举类来代替string
        String preset,           // "advisor" / "blackcat" / null
        String memory,           // "soul-md" / "fish-memory" / "long-short-term" / "none"
        String environment,      // "static-home" / "static-office" / "esp32"
        String physiology,       // "none" / "female-cycle" / "energy-only"
        String understand,       // "basic-rules"
        String intent,           // "never-initiate" / "desire-v1"
        String express,          // "qq-space-split" / "plain"
        String observe,          // "off" / "short-term" / "aggregated"
        String missedSchedule    // "catch-up" / "drop-log" / "boost-need" / "ignore"
) {
    /** 按预设名整包取一套 */
    public static PipelineConfig preset(String name) { return null; }

    /** 新建存档的默认值。 */
    public static PipelineConfig defaultConfig() { return null; }
}