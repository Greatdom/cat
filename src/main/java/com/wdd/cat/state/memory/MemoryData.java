package com.wdd.cat.state.memory;


import com.wdd.cat.common.enums.ChannelKind;

/**
 * &#064program: cat
 * <p>
 * &#064description: 对某条长期/短期记忆的数据描述及其数值判定,一般而且取值范围(-1~1),也有的场景需要(0~1)
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-22 17:46
 **/

public enum MemoryData {

    OPENNESS(MemoryDataFamily.Personality),            // 开放性
    CONSCIENTIOUSNESS(MemoryDataFamily.Personality),   // 尽责性
    EXTRAVERSION(MemoryDataFamily.Personality),        // 外向性
    AGREEABLENESS(MemoryDataFamily.Personality),       // 宜人性
    NEUROTICISM(MemoryDataFamily.Personality),         // 神经质

    //ATTITUDE的好感度,强调于对某个经验人事的情感倾向
    //ATTITUDE的安全感/信任度,强调于对某个经验人事的信任程度
    //ATTITUDE的关注度,强调于对某个经验人事感不感兴趣
    //ATTITUDE的尊重度,强调于对某个经验人事是否足以令自己敬畏

    //FEEDBACK的积极度,强调于这个短期记忆产生时的语境/得到的反馈是积极的还是消极的

    //CLOCK_TAG的范围,强调这件事的时间范围
    //CLOCK_TAG的时效性,强调这件事可能的时效性

    JOY(MemoryDataFamily.BasicEmotion),           // 高兴
    SADNESS(MemoryDataFamily.BasicEmotion),       // 难过
    ANGER(MemoryDataFamily.BasicEmotion),         // 生气
    FEAR(MemoryDataFamily.BasicEmotion),          // 害怕
    DISGUST(MemoryDataFamily.BasicEmotion),       // 厌恶
    SURPRISE(MemoryDataFamily.BasicEmotion),      // 惊讶
    TRUST(MemoryDataFamily.BasicEmotion),         // 信任
    ANTICIPATION(MemoryDataFamily.BasicEmotion);   // 期待

    //TODO 要拓展具体数值



    private final MemoryData.MemoryDataFamily family;

    /** 传输出小写，如 "joy"。 */
    public String wire() {
        return name().toLowerCase();
    }

    MemoryData(MemoryData.MemoryDataFamily family) {
        this.family = family;
    }

    public MemoryDataFamily getFamily() {
        return family;
    }

    /**
     * 数值的性质
     */
    public enum MemoryDataFamily {
        //用于短期记忆
        BasicEmotion,//情绪数值
        FEEDBACK, //反馈数值
        //用于长期记忆
        Personality,//性格数值
        ATTITUDE,//态度数值
        CLOCK_TAG//时间标识
    }

}
