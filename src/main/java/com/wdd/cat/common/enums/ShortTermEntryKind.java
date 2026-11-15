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

    /** 用户说了什么。 */
    USER_SAID,

    /** 我说了什么。 */
    I_SAID,

    /**
     * 我问了什么，还在等回应。
     *
     * <p><b>唯一不按窗口过期的类型</b>——用户隔多久回"要"都仍然有效，
     * 所以必须一直留着直到被回应（{@code resolved} 置 true）。
     * 没有它，跨轮追问就会变成"我刚问了什么？没印象了"。
     */
    I_ASKED,

    /** 我做了什么（建了日程 / 发起主动对话）。记下来避免重复问。 */
    I_DID,

    /** 感知到什么（硬件上报的状态、环境变化）。 */
    SENSED
}