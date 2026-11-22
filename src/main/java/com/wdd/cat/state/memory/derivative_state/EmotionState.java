package com.wdd.cat.state.memory.derivative_state;


/**
 * &#064program: cat
 * <p>
 * &#064description:基本情绪混合->复合情绪名;mix 和 complex 分别是给人看/给模型读的
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:32
 **/


import com.wdd.cat.state.memory.MemoryData;

import java.util.List;
import java.util.Map;

public record EmotionState(

        // 复合情绪
        Map<MemoryData, Double> mix,   // 八种基本情绪各占多少(0~1)
        //TODO创建该对象时请校验MemoryData.family
        List<String> complex,            // ["心疼"] / ["无奈"]
        // TODO 用一个类似单词表的文件记下Pad+基本情绪到complex的映射

        long updatedAt,      // 什么时候变成这样的
        long fadeAt          // 什么时候开始衰减
) {
    /** 什么都没发生时的默认情绪。 */
    public static EmotionState calm() {
        // TODO valence=0, arousal=0.2, dominance=0.5, mix 空, complex 空
        return null;
    }


    /** 渲染进提示词："你现在的感觉：有点生气，但又心疼。" */
    public String promptFragment() {
        // TODO 从 mix 里取分数最高的几个形容词
        return "";
    }


}