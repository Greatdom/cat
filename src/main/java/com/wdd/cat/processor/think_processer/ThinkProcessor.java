package com.wdd.cat.processor.think_processer;


import com.wdd.cat.common.enums.ProcessorKind;
import com.wdd.cat.message.child.InboundMessage;
import com.wdd.cat.processor.Processor;

/**
 * &#064program: cat
 * <p>
 * &#064description: 上下文处理器接口,用来在信息进入思考链路后到进入跳板前处理信息;根据消息渠道的不同,执行不同的上下文处理策略
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 12:16
 **/

public interface ThinkProcessor extends Processor<InboundMessage>  {
    @Override
    default void validateKind() {
        ProcessorKind.ProcessorFamily expected = ProcessorKind.ProcessorFamily.THINK_PROCESSOR;
        if (kind().getFamily() != expected) {
            throw new IllegalStateException(
                    getClass().getSimpleName() + " 声明为 " + kind()
                            + "，但它是 " + expected + " 的实现");
        }
    }
}
