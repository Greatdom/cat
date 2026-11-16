package com.wdd.cat.state.physiology;
import com.wdd.cat.state.environment.EnvironmentState;


/**
 * &#064program: cat
 * <p>
 * &#064description: 生理系统：管"身体"的状态——精力、健康、舒服程度、生理周期
 * 它把生理状态喂给需求池，是"本我"的原料之一
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:35
 **/


public interface PhysiologySystem {

    /** 当前生理状态。 */
    PhysiologyState current();

    /** 按流逝时间推进（精力慢慢消耗、健康慢慢变化）。 */
    void tick(long elapsedMs);

    /** 按当前环境调整舒服度（太热/太冷/太吵都会降）。 */
    void applyComfort(EnvironmentState env);

    /** 实现名，用于装配单："none" / "female-cycle" / "energy-only"。 */
    String kind();
}