package com.wdd.cat.ego;


/**
 * &#064program: cat
 * <p>
 * &#064description: 本我和超我决定自我
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 17:58
 **/

import com.wdd.cat.state.memory.EmotionState;
import com.wdd.cat.state.memory.PersonalityState;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EgoDecider {

    public EgoDecision decide(IdWant want, List<SuperegoRule> rules,
                              PersonalityState p, EmotionState emotion) {
        // TODO 按裁决规则：
        //      本我强 + 超我硬（≥0.95） → 超我胜
        //      本我强 + 超我软（0.3）   → 本我胜，调整表达
        //      本我弱 + 超我中（0.7）   → 自我折中
        // TODO emotion.idSuperegoBias() 先调整两边权重再比
        return null;
    }
}