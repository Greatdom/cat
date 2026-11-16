package com.wdd.cat.state.need;


import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.common.enums.NeedKind;

/**
 * &#064program: cat
 * <p>
 * &#064description: 一个“能满足某需求的手段”,
 * 例：社交需求 → QQ 渠道「发消息」效果 0.4。
 * 用来回答“我现在能怎么缓解这个需求”
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:38
 **/


public record Capability(
        NeedKind need,              // 满足哪个需求
        String action,              // "发消息" / "走动" / "归纳记忆"
        double effectiveness,       // 0~1，这个手段能缓解多少
        ChannelKind channel         // 通过哪个渠道做
) {}