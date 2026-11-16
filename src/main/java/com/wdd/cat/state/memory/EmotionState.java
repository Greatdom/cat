package com.wdd.cat.state.memory;


/**
 * &#064program: cat
 * <p>
 * &#064description: 情绪状态=PAD 三维 + 基本情绪混合 + 复合情绪名;mix 和 complex 分别是给人看/给模型读的
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:32
 **/

import com.wdd.cat.common.enums.BasicEmotion;

import java.util.List;
import java.util.Map;

public record EmotionState(

        // PAD 三维
        double valence,      // -1 难受 ~ +1 舒服
        double arousal,      // 0 平静 ~ 1 激动
        double dominance,    // 0 被控制 ~ 1 掌控

        // 复合情绪
        Map<BasicEmotion, Double> mix,   // 八种基本情绪各占多少
        List<String> complex,            // ["心疼"] / ["无奈"]

        long updatedAt,      // 什么时候变成这样的
        long fadeAt          // 什么时候开始衰减
) {
    /** 什么都没发生时的默认情绪。 */
    public static EmotionState calm() {
        // TODO valence=0, arousal=0.2, dominance=0.5, mix 空, complex 空
        return null;
    }

    /** 情绪对三我权重的调节系数。 */
    public IdSuperegoBias idSuperegoBias() {
        // TODO 亢奋时本我放大、超我压低
        return null;
    }

    /** 渲染进提示词："你现在的感觉：有点生气，但又心疼。" */
    public String promptFragment() {
        // TODO 从 mix 里取分数最高的几个形容词
        return "";
    }

    /**
     * 情绪对三我权重的影响:情绪高涨时本我更容易冲动，超我的约束力下降。
     */
    public record IdSuperegoBias(
            double idMultiplier,          // 亢奋时 > 1
            double superegoMultiplier     // 亢奋时 < 1
    ) {}

}