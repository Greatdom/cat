package com.wdd.cat.web;

import com.wdd.cat.common.cache.DeliveryState;
import com.wdd.cat.common.cache.InboundMessageControllerQueues;
import com.wdd.cat.common.entity.Result;
import com.wdd.cat.message.ChatKey;
import com.wdd.cat.web.dto.AckRequest;
import com.wdd.cat.web.dto.InboundGetResponse;
import com.wdd.cat.web.dto.InboundMessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * &#064program: cat
 * &#064description: 跳板来取待投递消息，以及确认收到
 * &#064author: black-cat
 * &#064create: 2026-11-16 09:44
 **/
@RestController
@RequestMapping("/api/inbound_message")
public class InboundMessageController {

    private static final Logger log = LoggerFactory.getLogger(InboundMessageController.class);

    private final InboundMessageControllerQueues queues;
    private final DeliveryState deliveryState;

    public InboundMessageController(InboundMessageControllerQueues queues,
                                    DeliveryState deliveryState) {
        this.queues = queues;
        this.deliveryState = deliveryState;
    }

    /** 每个会话桶只吐一条；只 peek 不删，等 ack。 */
    @GetMapping("/get")
    public Result<InboundGetResponse> get(@RequestParam(defaultValue = "10") int limit) {
        List<InboundMessageDto> out = queues.peek(limit).stream()
                .map(InboundMessageDto::from)
                .toList();
        return Result.ok(new InboundGetResponse(out));
    }

    /** 跳板确认收到：从队列删除并标记 DELIVERED。 */
    @PostMapping("/ack")
    public Result<?> ack(@RequestBody AckRequest req) {
        if (req == null || req.items() == null) {
            return Result.ok();
        }
        for (AckRequest.Item item : req.items()) {
            if (item == null) {
                continue;
            }
            queues.ack(item.sessionId(), item.channelMessageId());

            long archiveId = parseArchiveId(item.archiveId());
            if (archiveId < 0) {
                continue;
            }
            deliveryState.markDelivered(archiveId, item.channelMessageId(),
                    item.timestamp() / 1000, parseChatKey(item.chatKey()));
        }
        log.info("ack 完成，剩余待投递 {} 条", queues.size());
        return Result.ok();
    }

    private static long parseArchiveId(String raw) {
        if (raw == null || raw.isBlank()) {
            return -1L;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    private static ChatKey parseChatKey(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return ChatKey.parse(raw);
        } catch (IllegalArgumentException e) {
            return null;   // chatKey 坏了就只标状态、不推水位
        }
    }
}
