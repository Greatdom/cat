package com.wdd.cat.processor.think_processer;


import com.wdd.cat.common.enums.ProcessorKind;
import com.wdd.cat.message.child.InboundMessage;
import com.wdd.cat.message.child.NormalMessage;
import org.springframework.stereotype.Component;

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
@Component
public class SlimProcessor implements ThinkProcessor {
    @Override
    public ProcessorKind kind() {
        return ProcessorKind.SLIM_PROCESSOR;
    }

    @Override
    public int order() {
        return ProcessorKind.SLIM_PROCESSOR.getOrder();
    }

    @Override
    public List<InboundMessage> process(List<InboundMessage> in) {
        //TODO 瘦身：裁掉用不上的上下文字段
        return in;
    }
}
