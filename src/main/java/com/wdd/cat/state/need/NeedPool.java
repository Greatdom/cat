package com.wdd.cat.state.need;


/**
 * &#064program: cat
 * <p>
 * &#064description: 需求池:数值越接近 1 说明越缺、越想满足。随时间上涨，被满足后下降
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:37
 **/

import com.wdd.cat.common.enums.NeedKind;

import java.util.List;
import java.util.Map;


public interface NeedPool {

    /** 现在的各项强度，只读快照。 */
    Map<NeedKind, Double> snapshot();

    /** 时间流逝导致的需求上涨（S 形曲线 / Sigmoid 曲线描述）。 */
    void accumulate(long elapsedMs);

    /** 事件加速：某件事让这个需求更迫切（比如被冷落 → SOCIAL 涨）。 */
    void boost(NeedKind kind, double amount);

    /** 被满足了，降下来。按结果算，不按动作算。 */
    void consume(NeedKind kind, double amount);

    /** 已经超过阈值的需求，会变成主动行为。 */
    List<Desire> ripeDesires();
}