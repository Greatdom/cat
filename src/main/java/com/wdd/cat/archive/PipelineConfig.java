package com.wdd.cat.archive;

/**
 * &#064program: cat
 * &#064description: 存档的装配单：每个链路钩子用哪个实现
 * &#064author: black-cat
 * &#064create: 2026-11-15 20:49
 **/
public record PipelineConfig(
        String preset,           // "advisor" / "blackcat" / null
        String memory,           // "soul-md" / "fish-memory" / "long-short-term" / "none"
        String environment,      // "static-home" / "static-office" / "esp32"
        String physiology,       // "none" / "female-cycle" / "energy-only"
        String understand,       // "basic-rules"
        String intent,           // "never-initiate" / "desire-v1"
        String observe,          // "off" / "short-term" / "aggregated"
        String missedSchedule    // "catch-up" / "drop-log" / "boost-need" / "ignore"
) {

    /** 按预设名整包取一套；不认识的名字返回默认。 */
    public static PipelineConfig preset(String name) {
        if (name == null) {
            return defaultConfig();
        }
        return switch (name.toLowerCase()) {
            case "advisor" -> new PipelineConfig(
                    "advisor", "soul-md", "static-office", "none",
                    "basic-rules", "never-initiate", "short-term", "ignore");
            case "blackcat" -> new PipelineConfig(
                    "blackcat", "long-short-term", "static-home", "energy-only",
                    "basic-rules", "desire-v1", "short-term", "catch-up");
            default -> defaultConfig();
        };
    }

    /** 新建存档的默认值。 */
    public static PipelineConfig defaultConfig() {
        return new PipelineConfig(
                null, "long-short-term", "static-home", "energy-only",
                "basic-rules", "never-initiate", "short-term", "ignore");
    }
}
