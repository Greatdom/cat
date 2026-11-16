package com.wdd.cat.message.child;


import com.wdd.cat.common.enums.Urgency;

/**
 * &#064program: cat
 * <p>
 * &#064description: 理解阶段的结果：这条消息是什么
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 18:48
 **/


public record Assessment(
        Urgency urgency,          // 急不急
        double arousalGuess,      // 情绪唤醒度粗估，可能不准
        double valenceGuess,      // 情绪效价粗估，可能不准
        boolean addressedToMe,    // 是不是在跟我说话（可靠）
        boolean isLateNight,      // 是不是深夜（可靠）
        String text               // 原文
) {
    /**
     * 内部触发的合成结果——不是"别人说了什么"，跳过分析。
     */
    public static Assessment synthetic(String description) {
        return new Assessment(Urgency.NORMAL, 0, 0, false, false, description);
    }
}