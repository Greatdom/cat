package com.wdd.cat.processor.think_processer;


import com.wdd.cat.common.enums.ProcessorKind;
import com.wdd.cat.message.child.InboundMessage;
import com.wdd.cat.message.child.NormalMessage;
import org.springframework.stereotype.Component;

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
@Component
public class RecallProcessor implements ThinkProcessor {
    @Override
    public ProcessorKind kind() {
        return ProcessorKind.RECALL_PROCESSOR;
    }

    @Override
    public int order() {
        return ProcessorKind.RECALL_PROCESSOR.getOrder();
    }

    @Override
    public List<InboundMessage> process(List<InboundMessage> in) {
        //TODO 回忆：塞进 recallBundle
        return in;
    }
}
