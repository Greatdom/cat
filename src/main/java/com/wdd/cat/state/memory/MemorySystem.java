package com.wdd.cat.state.memory;


import com.wdd.cat.common.enums.MemoryKind;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 记忆系统接口
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:14
 **/

public interface MemorySystem {
    /** 长时检索。各实现自由发挥。 */
    List<LongTermMemoryEntry> recall(RecallQuery query);

    /** 性格视图（从长时记忆算）。 */
    PersonalityState personality(List<LongTermMemoryEntry> longTermMemories);

    /** 情绪状态（从短时记忆算）。 */
    EmotionState emotion(List<ShortTermMemoryEntry> shortTermMemories);

    //TODO 可能根据具体情况开发"兴趣视图",从长时记忆得出,描述了这个人对什么感兴趣,这样就能模拟"我对某个东西不感兴趣"的情况

    //TODO 可能根据具体情况开发"人际关系视图",从长时记忆得出,描述了这个人和谁认识,关系如何

    //也就是说每种长期记忆种类理论上都可以用来开发一种视图,但这有必要吗?如果有必要,该怎么建模呢?


    /** 短时记忆（无状态化的关键）。 */
    List<ShortTermMemoryEntry> shortTerm(long archiveId);

    /** 写长时记忆。 */
    void write(LongTermMemoryEntry entry);

    /** 写短时记忆。 */
    void appendShortTerm(ShortTermMemoryEntry entry);

    /** 标记某条 I_ASKED 已被回应。 */
    void resolveAsk(long archiveId, String entryId);

    /** 实现名，用于装配单。 */
    MemoryKind kind();   // "soul-md" / "fish-memory" / "long-short-term" / "none"
}
