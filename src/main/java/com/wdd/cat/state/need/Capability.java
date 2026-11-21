package com.wdd.cat.state.need;


import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.common.enums.ContentKind;
import com.wdd.cat.common.enums.NeedKind;
import com.wdd.cat.message.Content;

import java.util.HashMap;

/**
 * &#064program: cat
 * <p>
 * &#064description: 渠道能力表,每个渠道能执行什么行为,进而满足哪种需求
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:38
 **/


public record Capability(
        //TODO 要重写NeedKind的equal和HashCode方法
        HashMap<NeedKind, Double> capabilities,//这个行为能满足什么需求,具体满足了多少;Double要归一化,取值0~1
        String action,              // 行为的名称,例如"发消息" / "走动" / "归纳记忆"
        ChannelKind channel,         // 通过哪个渠道做
        ContentKind contentKind,             //使用这种能力时,应该给跳板的输出json的具体item注入的具体消息Content的具体类型
        Content contentTemplate       //Content的模板
) {}