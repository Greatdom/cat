package com.wdd.cat.service.tool;


import com.wdd.cat.common.enums.NeedKind;

import java.util.Map;

/**
 * &#064program: cat
 * <p>
 * &#064description: 判断走哪条链路路需要的现场信息
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 12:24
 **/


//public record DecisionContext(
//        //四套状态系统（情绪/环境/生理/反射规则）还没做，第一版用简单的
//        Object emotion,               // TODO 换成 EmotionState
//        Map<NeedKind, Double> needs,
//        Object environment,           // TODO 换成 EnvironmentState
//        Object physiology,            // TODO 换成 PhysiologyState
//        Object reflexRules            // TODO 换成 List<ReflexRule>
//) {}
public record DecisionContext(
        double arousal,            // 情绪激动度（PAD 的 A），放大本我冲动
        double reflexThreshold,    // 反射阈值
        boolean hasReflexRule      // 是否有规则命中（阶段 3 由 ReflexEngine 算）
) {
    public static DecisionContext defaults() {
        return new DecisionContext(0.0, 0.8, false);
    }
}