package com.wdd.cat.processor.post_processor;


import com.wdd.cat.common.enums.ProcessorKind;
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
    public ProcessorKind kind() {
        return ProcessorKind.OBSERVE_PROCESSOR;
    }

    @Override
    public int order() {
        return ProcessorKind.OBSERVE_PROCESSOR.getOrder();
    }

    @Override
    public List<OutboundMessage> process(List<OutboundMessage> in) {
        return List.of();//消息回顾
    }
}
