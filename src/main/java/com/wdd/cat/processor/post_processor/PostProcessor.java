package com.wdd.cat.processor.post_processor;


import com.wdd.cat.common.enums.ProcessorKind;
import com.wdd.cat.message.child.OutboundMessage;
import com.wdd.cat.processor.Processor;

/**
 * &#064program: cat
 * <p>
 * &#064description: 后处理器接口,用来在得到跳板的答复后处理信息;根据消息渠道的不同,执行不同的后处理策略
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 12:14
 **/

public interface PostProcessor extends Processor<OutboundMessage> {
    @Override
    default void validateKind() {
        ProcessorKind.ProcessorFamily expected = ProcessorKind.ProcessorFamily.POST_PROCESSOR;
        if (kind().getFamily() != expected) {
            throw new IllegalStateException(
                    getClass().getSimpleName() + " 声明为 " + kind()
                            + "，但它是 " + expected + " 的实现");
        }
    }
}
