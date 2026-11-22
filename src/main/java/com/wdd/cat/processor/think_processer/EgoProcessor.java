package com.wdd.cat.processor.think_processer;


import com.wdd.cat.common.enums.ProcessorKind;
import com.wdd.cat.message.child.InboundMessage;
import com.wdd.cat.message.child.NormalMessage;
import org.springframework.stereotype.Component;

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
@Component
public class EgoProcessor implements ThinkProcessor {
    @Override
    public ProcessorKind kind() {
        return ProcessorKind.EGO_PROCESSOR;
    }

    @Override
    public int order() {
        return ProcessorKind.EGO_PROCESSOR.getOrder();
    }

    @Override
    public List<InboundMessage> process(List<InboundMessage> in) {
        //TODO 三我裁决,得到MoralVerdict后给InboundMessage的长期消息算权重
        return in;
    }
}
