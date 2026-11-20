package com.wdd.cat.processor.post_processor;


import com.wdd.cat.common.enums.ProcessorKind;
import com.wdd.cat.message.child.NormalMessage;
import com.wdd.cat.message.child.OutboundMessage;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 消息润色,拆分
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 19:28
 **/
@Component
public class ExpressProcessor implements PostProcessor{
    //废弃E:\程序代码\dsh\references\cat-legacy\cat\src\main\java\com\wdd\cat\link\PromptComposer.java的业务
    @Override
    public ProcessorKind kind() {
        return ProcessorKind.EXPRESS_PROCESSOR;
    }

    @Override
    public int order() {
        return ProcessorKind.EXPRESS_PROCESSOR.getOrder();
    }

    @Override
    public List<OutboundMessage> process(List<OutboundMessage> in) {
        //TODO 润色与拆条：本来要给每条 NormalMessage 再拆开，本版原样返回
        return in;
    }
}
