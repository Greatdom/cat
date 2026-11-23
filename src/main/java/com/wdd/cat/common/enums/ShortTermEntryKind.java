package com.wdd.cat.common.enums;


/**
 * &#064program: cat
 * <p>
 * &#064description: 短时记忆里一条记录属于哪一类,
 * 作用是为了给模型推理提供上下文,计算情绪和作为归纳长期记忆的材料
 * 除了 {@link #I_ASKED}，其余都按时间窗口过期删除
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 20:20
 **/

public enum ShortTermEntryKind {

    /** 一轮用户发起,我回复的短期记忆 */
    A_TERN,

    /** 一轮用户发起,我不予理会的短期记忆 */
    YOU_ASK,

    /**
     * 我问了什么
     */
    I_ASKED,


    /** 感知到什么（硬件上报的状态、环境变化）。 */
    I_SENSED
}