package com.wdd.cat.state.physiology;


/**
 * &#064program: cat
 * <p>
 * &#064description: 生理周期（经期，或睡眠周期）
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:36
 **/

public record CycleState(
        String kind,        // "menstrual" / "sleep"
        int day,            // 第几天（从 1 开始）
        int length,         // 整个周期多少天
        double intensity    // 强度 0~1
) {}