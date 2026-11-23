package com.wdd.cat.state.memory.derivative_state;

import com.wdd.cat.state.memory.MemoryData;

import java.math.BigDecimal;
import java.util.Map;


/**
 * &#064program: cat
 * <p>
 * &#064description: 性格视图：从记忆里算出来的“这个人是什么样”
 * 性格不是独立存的一条数据，而是对记忆的一种读法
 * 所以它有两个面：我自己以为的我，和我实际做的样子
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:32
 **/



public record PersonalityState(

        //  自我认知:仅在意识层面表现出来的性格,无潜意识权重
        Map<MemoryData, BigDecimal> selfImage,   // 大五人格五维得分(0~1)
        String selfNarrative,               // "我只是说话直"
        //TODO创建该对象时请校验MemoryData.family

        //  真实性格:在意识和潜意识层面共同表现出来的性格,其中潜意识的权重变大
        Map<MemoryData, BigDecimal> behavior,    // 同样五维，但来自实际行为(0~1)
        String behaviorNarrative,           // "但她难过我总会偷偷关心"
        //TODO创建该对象时请校验MemoryData.family

        //潜意识的概念:指的是被压抑的记忆的组合;如何在记忆系统模拟潜意识?如果召回的记忆在召回分数的临界点或者已经被遗忘的记忆,那么这些记忆组成潜意识
        //意识的概念:指的是想到的召回分数高或者处于核心的记忆的组合;核心记忆是什么?就是重要度为1的记忆,它是不会衰退的;也就是说如果重要度为0.99,那么也不是核心记忆


        //  偏差：两个面差多少
        double bias,                        // 0 = 一致，1 = 完全不符

        String source,                      // 哪个记忆实现算出来的
        long computedAt                     // 算的时间
) {
    /** 自我认知和真实性格差得太多，说明有盲点。 */
    public boolean hasBlindSpot() {
        return bias > 0.4;
    }

    //自我认知和真实性格的差共同塑造了复杂性格,例如毒舌傲娇,腹黑,看起来开朗的抑郁者,看起来很喜欢享受生活的哲学家

    /**
     * 渲染进提示词
     * 不写成"你是个傲娇的角色"，
     * 写成"你自认为只是说话直。但每次她难过，你都会找借口关心她。"
     * 不给标签，给行为证据。
     */
    public String promptFragment() {
        // TODO 用 selfNarrative + behaviorNarrative 拼,用一个持久化的文件写下所有的"性格数值->性格描述的映射"并和当前数值进行比较
        // TODO 有盲点时把差异点出来
        return "";
    }

    /** 还没算出性格时用，什么都不偏。 */
    public static PersonalityState defaultPersonalityState() {
        // TODO 五维全 0.5，narrative 空，bias 0
        return null;
    }
}