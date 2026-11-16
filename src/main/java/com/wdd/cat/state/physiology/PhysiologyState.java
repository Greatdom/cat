package com.wdd.cat.state.physiology;


/**
 * &#064program: cat
 * <p>
 * &#064description: 某一刻的生理状态
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:35
 **/

import com.wdd.cat.common.enums.NeedKind;

import java.util.Map;
import java.util.Set;

/**
 * 某一刻的生理状态。
 */
public record PhysiologyState(
        double health,          // 健康 0~1
        double energy,          // 精力 0~1，睡一觉能补
        double comfort,         // 舒服程度 0~1，太热太吵会掉
        CycleState cycle,       // 生理周期，没开这个实现时为 null
        Set<String> symptoms,   // 症状标记："headache" / "sleepy" / "cramp"
        long updatedAt,         // 上次更新时间
        String source           // 哪来的："default" / "manual" / "esp32"
) {
    /** 生理对需求的直接喂养。 */
    public Map<NeedKind, Double> needBoost() {
        // TODO 精力低 → ENERGY 需求高
        // TODO comfort 低 → COMFORT 需求高
        // TODO 健康差 → COMFORT 需求高
        return Map.of();
    }

    /** 对情绪敏感度的调节（生理期会更敏感）。 */
    public double emotionSensitivity() {
        // TODO 正常 1.0；生理期或症状多时 > 1，情绪更容易被放大
        return 1.0;
    }
}