package com.wdd.cat.processor.think_processer;


import com.wdd.cat.message.child.InboundMessage;
import com.wdd.cat.message.child.NormalMessage;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 自我判定思考处理器
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 19:16
 **/

public class EgoProcessor implements ThinkProcessor {
    @Override
    public String kind() {
        return "EgoProcessor";
    }

    @Override
    public int order() {
        return 300;
    }

    @Override
    public List<InboundMessage> process(List<InboundMessage> in) {
        return List.of(); //往Message插进EgoDecision
    }
}
