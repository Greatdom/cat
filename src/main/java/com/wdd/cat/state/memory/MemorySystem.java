package com.wdd.cat.state.memory;


import com.wdd.cat.common.enums.MemoryKind;
import com.wdd.cat.state.memory.derivative_state.EmotionState;
import com.wdd.cat.state.memory.derivative_state.PersonalityState;

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
    /** 拿长时记忆 */
    List<LongTermMemoryEntry> recall(RecallQuery query);

    /** 拿短时记忆 */
    List<ShortTermMemoryEntry> shortTerm(long archiveId);

    /** 写长时记忆。 */
    void write(long archiveId,LongTermMemoryEntry entry);

    /** 写短时记忆。 */
    void appendShortTerm(long archiveId,ShortTermMemoryEntry entry);

    /** 实现名，用于装配单。 */
    MemoryKind kind();   // "soul-md" / "fish-memory" / "long-short-term" / "none"

}
