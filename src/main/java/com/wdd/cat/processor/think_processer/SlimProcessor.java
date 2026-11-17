package com.wdd.cat.processor.think_processer;


import com.wdd.cat.message.child.InboundMessage;
import com.wdd.cat.message.child.NormalMessage;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 消息瘦身思考处理器
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-17 00:33
 **/

public class SlimProcessor implements ThinkProcessor {
    @Override
    public String kind() {
        return "";
    }

    @Override
    public int order() {
        return 400;
    }

    @Override
    public List<InboundMessage> process(List<InboundMessage> in) {
        return List.of();//TODO 消息瘦身以免消息过于臃肿
    }
}
