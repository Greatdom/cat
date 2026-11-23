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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record LongTermMemoryEntry(
        String id,                 // 唯一 id
        LongTermMemoryKind kind,           // 什么类型的长期记忆,记忆种类可以是复合的
        String text,             // 记忆正文,内容一般而言简短
        double importance,         // 重要程度 0~1,表示这个记忆对"我"的主要程度,模拟记忆淡忘机制
        long createdAt,            // 什么时候记下的
        long lastUsedAt,           // 上次被召回是什么时候（越近越优先）
        Set<String> tags,          // 标签，辅助检索
        HashMap<MemoryData, BigDecimal> data,    //自定义的可供数值计算哈希表,值(0~1)
        Map<String,Entity> entities,
        // entities是记忆正文中人称代词和所指代实体的键值对,其中Entity可以是Entity.family中除了STRANGER之外的全部枚举种类
        //在跳板交卷填充这个字段时:
        // 例如记忆文本写"A认为B是个大方的人",其中A可以指代自己,别人,物体等,B也可以指代自己,别人,物体等
        // 又例如"A向他们进行了演讲",其中A可以指代自己,别人,物体等;对于"他们",如果这个词是特指,应该被替换为具体的"A,B,C,D",
        // 如果是部分泛指,那么应该写"A,B以及其他人"
        // 如果是全泛指,直接写"他们"
        // 如果某个人称代词(A,B,C,D等)在跳板的交卷环节被判定为之前不认识的人事,那么这个值会是null,
        // 到了ObserveProcessor将会各种地被新注册一个Entity指代新认识的人事
        HashMap<String,BigDecimal> links
        //这条短期记忆关联的消息记录曾经召回并在提示词参考了多少长期记忆(id)
        // ,关联多大(-1~1,其中绝对值越大关联性越强,正数代表关联的记忆和本记忆支持本记忆的真实性,反之,负数代表矛盾)
) {}