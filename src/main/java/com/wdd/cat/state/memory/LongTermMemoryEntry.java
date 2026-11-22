package com.wdd.cat.state.memory;


/**
 * &#064program: cat
 * <p>
 * &#064description: 长时记忆里的一条;它记的是"关于某人的某个事实",不是聊天记录本身;聊天记录在短时记忆里，长时记忆是提炼过的东西
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:31
 **/

import com.wdd.cat.common.enums.LongTermMemoryKind;
import com.wdd.cat.message.Content;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record LongTermMemoryEntry(
        String id,                 // 唯一 id
        List<LongTermMemoryKind> kind,           // 什么类型的长期记忆,记忆种类可以是复合的
        Content content,            // 记忆正文,内容一般而言简短
        double importance,         // 重要程度 0~1,表示这个记忆对"我"的主要程度,模拟记忆淡忘机制
        long createdAt,            // 什么时候记下的
        long lastUsedAt,           // 上次被召回是什么时候（越近越优先）
        Set<String> tags,          // 标签，辅助检索
        HashMap<MemoryData,Double> data,    //自定义的可供数值计算哈希表,值(0~1)
        HashMap<Entity,Double> entities,  //记忆和谁有关,关系多大(0~1)
        HashMap<String,Double> links
        //这条短期记忆关联的消息记录曾经召回并在提示词参考了多少长期记忆(id)
        // ,关联多大(-1~1,其中绝对值越大关联性越强,正数代表关联的记忆和本记忆支持本记忆的真实性,反之,负数代表矛盾)
) {}