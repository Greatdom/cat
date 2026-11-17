package com.wdd.cat.processor.post_processor;


import com.wdd.cat.message.child.NormalMessage;
import com.wdd.cat.message.child.OutboundMessage;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 消息回顾
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 19:29
 **/
@Component
public class ObserveProcessor implements PostProcessor{
    @Override
    public String kind() {
        return "ObserveProcessor";
    }

    @Override
    public int order() {
        return 200;
    }

    @Override
    public List<OutboundMessage> process(List<OutboundMessage> in) {
        return List.of();//消息回顾
    }
}
