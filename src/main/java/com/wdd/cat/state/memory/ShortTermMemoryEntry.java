package com.wdd.cat.state.memory;


import com.wdd.cat.common.enums.ShortTermEntryKind;
import com.wdd.cat.message.Content;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * &#064program: cat
 * <p>
 * &#064description: 短时记忆里的一条
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:32
 **/

public record ShortTermMemoryEntry(
        String id,                  // 唯一 id
        long createdAt,            // 什么时候记下的
        ShortTermEntryKind kind,    // 短期记忆种类
        String text,             // 一轮交互中提问者的原话和对应的"我"的心理活动及其回答
        HashMap<MemoryData, BigDecimal> data,    //自定义的可供数值计算哈希表
        double importance,         // 重要程度 0~1,表示这个记忆对"我"的主要程度,模拟注意力机制
        Entity entity,  //我在对谁做出反应,用于判断这个记忆和谁的相关性比较大,可以是Entity.family的全部枚举种类


        HashMap<String,BigDecimal> links
        //这条短期记忆关联的消息记录曾经召回并在提示词参考了多少长期记忆(id)
        // ,关联多大(-1~1,其中绝对值越大关联性越强,正数代表关联的记忆和本记忆支持本记忆的真实性,反之,负数代表矛盾)

) {}