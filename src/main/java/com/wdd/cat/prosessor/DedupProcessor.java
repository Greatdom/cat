package com.wdd.cat.prosessor;


/**
 * &#064program: cat
 * <p>
 * &#064description: 消息去重前处理器：比消息水位旧的、已经处理过的，全丢掉
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 23:14
 **/


import com.wdd.cat.message.InboundMessage;
import com.wdd.cat.messagecache.DeliveryState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DedupProcessor implements PreProcessor {

    @Autowired
    private DeliveryState deliveryState;

    @Override
    public String kind() {
        return "dedup";
    }

    @Override
    public int order() {
        return 100;
    }

    @Override
    public List<InboundMessage> process(List<InboundMessage> in) {
        // TODO in 为空直接返回 List.of()
        // TODO 逐条过滤：
        //      消息已经被处理过 : deliveryState.isProcessed(msg.archiveId(), msg.channelMessageId())  → 标记完最高水位后丢
        //      消息在水位下 : msg.timestamp() < deliveryState.watermarkOrInit(msg.archiveId(), msg.chatKey()) → 直接丢
        // TODO 剩下的原样返回
        return in;
    }
}