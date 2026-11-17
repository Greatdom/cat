package com.wdd.cat.processor.think_processer;


import com.wdd.cat.message.child.InboundMessage;
import com.wdd.cat.message.child.NormalMessage;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 消息调取记忆及情境思考处理器
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 19:15
 **/

public class RecallProcessor implements ThinkProcessor {
    @Override
    public String kind() {
        return "RecallProcessor";
    }

    @Override
    public int order() {
        return 200;
    }

    @Override
    public List<InboundMessage> process(List<InboundMessage> in) {
        return List.of(); //往Message塞进RecallBundle
    }
}
