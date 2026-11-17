package com.wdd.cat.archive;

import com.wdd.cat.common.enums.*;

/**
 * &#064program: cat
 * &#064description: 存档的装配单：每个链路钩子用哪个实现
 * &#064author: black-cat
 * &#064create: 2026-11-15 20:49
 **/
public record PipelineConfig(
        PresetKind preset,           // "advisor" / "blackcat" / null
        MemoryKind memory,           // "soul-md" / "fish-memory" / "long-short-term" / "none"
        EnvironmentKind environment,      // "static-home" / "static-office" / "esp32"
        PhysiologyKind physiology,       // "none" / "female-cycle" / "energy-only"
        UnderstandKind understand,       // "basic-rules"
        IntentKind intent,           // "never-initiate" / "desire-v1"
        ObserveKind observe,          // "off" / "short-term" / "aggregated"
        MissedPolicyKind missedSchedule    // "catch-up" / "drop-log" / "boost-need" / "ignore"
) {


    /** 按预设名整包取一套；不认识的名字返回默认。 */
    public static PipelineConfig preset(PresetKind name) {
        if (name == null || name == PresetKind.NULL) {
            return defaultConfig();
        }
        return switch (name) {
            case ADVISOR -> new PipelineConfig(
                    PresetKind.ADVISOR,
                    MemoryKind.SOUL_MD,
                    EnvironmentKind.STATIC_OFFICE,
                    PhysiologyKind.NONE,
                    UnderstandKind.BASIC_RULES,
                    IntentKind.NEVER_INITIATE,
                    ObserveKind.SHORT_TERM,
                    MissedPolicyKind.IGNORE);
            case BLACK_CAT -> new PipelineConfig(
                    PresetKind.BLACK_CAT,
                    MemoryKind.LONG_SHORT_TERM,
                    EnvironmentKind.STATIC_HOME,
                    PhysiologyKind.ENERGY_ONLY,
                    UnderstandKind.BASIC_RULES,
                    IntentKind.DESIRE_V1,
                    ObserveKind.SHORT_TERM,
                    MissedPolicyKind.CATCH_UP);
            default -> defaultConfig();
        };
    }

    /** 新建存档的默认值。 */
    public static PipelineConfig defaultConfig() {
        return new PipelineConfig(
                PresetKind.NULL,
                MemoryKind.LONG_SHORT_TERM,
                EnvironmentKind.STATIC_HOME,
                PhysiologyKind.ENERGY_ONLY,
                UnderstandKind.BASIC_RULES,
                IntentKind.NEVER_INITIATE,
                ObserveKind.SHORT_TERM,
                MissedPolicyKind.IGNORE);
    }





}
