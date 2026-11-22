package com.wdd.cat.state.memory;


import java.util.Map;

/**
 * &#064program: cat
 * <p>
 * &#064description: 三我裁决的初步判断
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-22 19:36
 **/

public record MoralVerdict(
        double idPressure,         // 本我压强 0~1：需求 + 情绪 + 急迫
        double superegoPressure,   // 超我压强 0~1：来自人格的自我约束
        double selfControl,        // 自我控制力 0~1：实际压得住多少
        double leakage,            // 溢出 0~1：压不住会漏多少

        Posture posture,
        String reconciliation,     // 一句人话
        Map<String, Object> trace
) {

    public enum Posture {
        RESTRAINED,   // 克制住了
        COMPROMISED,  // 压了一半，话里有话
        INDULGED,     // 没压住
        MUTED         // 直接走反射通道
    }
}