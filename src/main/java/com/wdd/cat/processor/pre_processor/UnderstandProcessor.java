package com.wdd.cat.processor.pre_processor;


import com.wdd.cat.common.enums.ProcessorKind;
import com.wdd.cat.message.child.InboundMessage;
import com.wdd.cat.message.child.NormalMessage;
import com.wdd.cat.processor.pre_processor.PreProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 消息理解思考处理器
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 12:51
 **/
@Component
public class UnderstandProcessor implements PreProcessor {
    @Override
    public ProcessorKind kind() {
        return ProcessorKind.UNDERSTAND_PROCESSOR;
    }

    @Override
    public int order() {
        return ProcessorKind.UNDERSTAND_PROCESSOR.getOrder();
    }

    @Override
    public List<InboundMessage> process(List<InboundMessage> in) {
        //TODO 理解：这条消息是什么（急不急、重不重要、对我好坏），塞进 assessment
        return in;
    }
}
