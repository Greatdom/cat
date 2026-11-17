package com.wdd.cat.processor.pre_processor;

import com.wdd.cat.common.enums.ProcessorKind;
import com.wdd.cat.message.child.InboundMessage;
import com.wdd.cat.message.child.NormalMessage;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 合并消息：T 秒窗口内、最多 N 条，会合并成一条
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 23:17
 **/


@Component
public class MergeProcessor implements PreProcessor {

    //废弃E:\程序代码\dsh\references\cat-legacy\cat\src\main\java\com\wdd\cat\link\RateLimiter.java的业务

    /** 攒批窗口，毫秒。 */
//    private static final long WINDOW_MS = T;


    /** 一次最多合并几条，超过就先合。 */
//    private static final int MAX_MERGE = N;

    //TODO 维护一群按"存档:渠道:具体聊天"分桶的内部queue
    //XXX<Queue> queues;

    @Override
    public ProcessorKind kind() {
        return ProcessorKind.MERGE_PROCESSOR;
    }

    @Override
    public int order() {
        return ProcessorKind.MERGE_PROCESSOR.getOrder();
    }

    @Override
    public List<InboundMessage> process(List<InboundMessage> in) {
        // TODO  ChannelKind.ESP32 时原样返回，不合并
        // TODO  将原始数据按时间戳顺序按"存档:渠道:具体聊天"分桶进入不同的内部queue
        // TODO 每次process时以一个原始信息为单位取出时间戳至今已有Tms的消息进行合并
        // TODO 已合并的原始消息丢弃
        // TODO 用算法将消息链表进行合并成一条并重进链表,规则:
        // TODO 一条合并后的消息包含能合并的按时间戳顺序排序的至多N条原始消息(包括在队列内的未处理的原始消息),相邻消息的时间间隔不大于Tms
        //TODO 将合并后的消息链表返回
        return in;
    }
}