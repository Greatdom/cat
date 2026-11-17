package com.wdd.cat.state.memory;


import com.wdd.cat.common.enums.MemoryKind;

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
    RecallResult recall(RecallQuery query);

    /** 性格视图（从长时记忆算）。 */
    PersonalityView personality();

    /** 情绪状态（从短时记忆算）。 */
    EmotionState emotion();

    /** 短时记忆（无状态化的关键）。 */
    ShortTermMemory shortTerm();

    /** 写长时记忆。 */
    void write(LongTermMemoryEntry entry);

    /** 写短时记忆。 */
    void appendShortTerm(ShortTermMemoryEntry entry);

    /** 标记某条 I_ASKED 已被回应。 */
    void resolveAsk(long archiveId, String entryId);

    /** 实现名，用于装配单。 */
    MemoryKind kind();   // "soul-md" / "fish-memory" / "long-short-term" / "none"
}
