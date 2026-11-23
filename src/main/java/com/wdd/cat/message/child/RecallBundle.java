package com.wdd.cat.message.child;
import com.wdd.cat.state.environment.EnvironmentState;
import com.wdd.cat.state.memory.*;
import com.wdd.cat.state.memory.derivative_state.EmotionState;
import com.wdd.cat.state.memory.derivative_state.PersonalityState;
import com.wdd.cat.state.need.Capability;
import com.wdd.cat.state.physiology.PhysiologyState;

import java.util.HashMap;
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

        //具体的记忆Entry表现在跳板的提示词中时,提示词应该关心Entity是谁而不是ChatKey是什么,
        // 因为ChatKey是Entity的外部渠道的表现,是角色的面具,而Entity才是"我"互动的角色本身


        HashMap<LongTermMemoryEntry,Double> longTermMemories,      // 相关的长时记忆以及三我裁决给每个记忆的权重(0~1)
        PersonalityState personality,     // 性格
        EmotionState emotionState,            // 情绪
        List<ShortTermMemoryEntry> shortTermMemories,       // 最近发生了什么
        EnvironmentState environment,    // 在哪、周围什么样
        PhysiologyState physiologyState,      // 身体状态
        List<Capability> capabilities    // 能做什么
) {

    /** 什么长期记忆都没召回到。 */
    public static List<LongTermMemoryEntry> empty() {
        // TODO 返回空
        return null;
    }

    /** 我提过但对方还没回答的问题，需要一直惦记着。 */
    public List<ShortTermMemoryEntry> pendingAsks() {
        // TODO 筛 kind == I_ASKED && !resolved
        return List.of();
    }

    /** 渲染成提示词片段，塞进 DSH 的上下文。 */
    public String toPromptFragment() {
        // TODO 按时间顺序把 entries 拼成自然语言
        // TODO pendingAsks 单独提一段，提醒模型"你问过这个，还没回音"
        return "";
    }

}