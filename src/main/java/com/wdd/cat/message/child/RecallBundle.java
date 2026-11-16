package com.wdd.cat.message.child;
import com.wdd.cat.state.environment.EnvironmentState;
import com.wdd.cat.state.memory.EmotionState;
import com.wdd.cat.state.memory.PersonalityView;
import com.wdd.cat.state.memory.ShortTermMemory;
import com.wdd.cat.state.need.Capability;
import com.wdd.cat.state.physiology.PhysiologyState;

import java.util.List;


/**
 * &#064program: cat
 * <p>
 * &#064description: 回忆阶段的产物：拼上下文要用的全部素材
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 18:49
 **/

public record RecallBundle(
        String memoryNarrative,          // 相关的长时记忆，已成文
        PersonalityView personality,     // 性格
        EmotionState emotion,            // 情绪
        ShortTermMemory shortTerm,       // 最近发生了什么
        EnvironmentState environment,    // 在哪、周围什么样
        PhysiologyState physiology,      // 身体状态
        List<Capability> capabilities    // 能做什么
) {}