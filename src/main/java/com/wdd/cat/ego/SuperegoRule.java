package com.wdd.cat.ego;

import com.wdd.cat.common.enums.SuperegoAction;
import com.wdd.cat.common.enums.TraitKind;
import com.wdd.cat.state.memory.PersonalityView;

import java.util.Map;

/**
 * &#064program: cat
 * <p>
 * &#064description: 一条超我规则：某个条件下，我"该"或"不该"做什么;
 * 超我不看欲望，只看道德/人设/关系规则
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 17:53
 **/


public record SuperegoRule(
        String id,
        String condition,                   // 触发条件表达式
        SuperegoAction action,              // FORBID 禁止 / REQUIRE 要求 / SOFTER 缓和
        double baseWeight,                  // 基础权重，与性格无关的底数
        Map<TraitKind, Double> traitWeights // 性格怎么调节它：宜人性高→这条规则更重
) {
    /** 按当前性格算出这条规则的实际权重。 */
    public double effectiveWeight(PersonalityView p) {
        // TODO baseWeight × 用 traitWeights 对 p 做加权
        // TODO 性格算不出来时退回 baseWeight
        return baseWeight;
    }
}