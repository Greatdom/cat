package com.wdd.cat.state.memory;
import com.wdd.cat.common.enums.TraitKind;

import java.util.Map;


/**
 * &#064program: cat
 * <p>
 * &#064description: 性格视图：从记忆里算出来的“这个人是什么样”
 * 性格不是独立存的一条数据，而是对记忆的一种读法
 * 所以它有两个面：我自己以为的我，和我实际做的样子
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:32
 **/



public record PersonalityState(

        //  自我认知（从"我关于自己的话"算）
        Map<TraitKind, Double> selfImage,   // 大五人格五维得分
        String selfNarrative,               // "我只是说话直"

        //  行为画像（从"我做过的事"算）
        Map<TraitKind, Double> behavior,    // 同样五维，但来自实际行为
        String behaviorNarrative,           // "但她难过我总会偷偷关心"

        //  偏差：两个面差多少
        double bias,                        // 0 = 一致，1 = 完全不符

        String source,                      // 哪个记忆实现算出来的
        long computedAt                     // 算的时间
) {
    /** 自我认知和行为差得太多，说明有盲点。 */
    public boolean hasBlindSpot() {
        return bias > 0.4;
    }

    /**
     * 渲染进提示词
     * 不写成"你是个傲娇的角色"，
     * 写成"你自认为只是说话直。但每次她难过，你都会找借口关心她。"
     * 不给标签，给行为证据。
     */
    public String promptFragment() {
        // TODO 用 selfNarrative + behaviorNarrative 拼
        // TODO 有盲点时把差异点出来
        return "";
    }

    /** 还没算出性格时用，什么都不偏。 */
    public static PersonalityState neutral() {
        // TODO 五维全 0.5，narrative 空，bias 0
        return null;
    }
}