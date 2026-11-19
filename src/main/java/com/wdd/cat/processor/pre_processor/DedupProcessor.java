package com.wdd.cat.processor.pre_processor;

import com.wdd.cat.common.cache.AvailableSessions;
import com.wdd.cat.common.cache.DeliveryState;
import com.wdd.cat.common.config.UnEditAbleDefaultConfig;
import com.wdd.cat.common.enums.ProcessorKind;
import com.wdd.cat.message.ChatKey;
import com.wdd.cat.message.child.InboundMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * &#064program: cat
 * &#064description: 消息去重前处理器：会话不可用、已投递、正在处理、比水位旧的，全丢掉
 * &#064author: black-cat
 * &#064create: 2026-11-15 23:14
 **/
@Component
public class DedupProcessor implements PreProcessor {

    private final DeliveryState deliveryState;
    private final AvailableSessions availableSessions;

    public DedupProcessor(DeliveryState deliveryState, AvailableSessions availableSessions) {
        this.deliveryState = deliveryState;
        this.availableSessions = availableSessions;
    }

    @Override
    public ProcessorKind kind() {
        return ProcessorKind.DEDUP_PROCESSOR;
    }

    @Override
    public int order() {
        return ProcessorKind.DEDUP_PROCESSOR.getOrder();
    }

    @Override
    public List<InboundMessage> process(List<InboundMessage> in) {
        if (in == null || in.isEmpty()) {
            return List.of();
        }

        long nowSeconds = System.currentTimeMillis() / 1000;
        List<InboundMessage> out = new ArrayList<>(in.size());

        for (InboundMessage msg : in) {
            ChatKey chat = msg.chatKey();
            if (chat == null) {
                continue;
            }

            // ⓪ 没有归属会话、或会话不在可用清单里 → 送不出去，丢
            String sessionId = msg.getSessionId();
            if (sessionId == null || sessionId.isBlank()) {
                continue;
            }
            if (!availableSessions.contains(sessionId)) {
                continue;
            }

            // ① 比水位旧的 = 历史消息，丢
            long watermark = deliveryState.watermarkOrInit(msg.getArchiveId(), chat, nowSeconds);
            if (msg.getTimestamp() / 1000 < watermark) {
                continue;
            }

            // ② 已投递 / 正在处理且未超时 → 丢；否则标记为处理中
            if (!deliveryState.tryAccept(msg)) {
                continue;
            }
            out.add(msg);
        }

        if (!out.isEmpty()) {
            deliveryState.save();
        }
        return out;
    }
}
