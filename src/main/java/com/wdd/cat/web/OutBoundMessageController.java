package com.wdd.cat.web;

import com.wdd.cat.archive.Archive;
import com.wdd.cat.archive.ArchiveStore;
import com.wdd.cat.common.entity.Result;
import com.wdd.cat.message.Content;
import com.wdd.cat.message.MessageSource;
import com.wdd.cat.message.child.NormalMessage;
import com.wdd.cat.message.child.OutboundMessage;
import com.wdd.cat.web.dto.OutboundMessageRequest;
import com.wdd.cat.web.dto.OutboundMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 专门用于从跳板接受已回复消息的Controller
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 09:49
 **/
@RestController
@RequestMapping("/api/outbound_message")
public class OutBoundMessageController {

    private static final Logger log = LoggerFactory.getLogger(OutBoundMessageController.class);

    private final ArchiveStore archiveStore;

    public OutBoundMessageController(ArchiveStore archiveStore) {
        this.archiveStore = archiveStore;
    }

    /**
     * 跳板把一轮回复推回来，转成 OutboundMessage 后走后处理链。
     * <p>返回 ack：跳板只有在收到 ack 之后才清掉自己对应的出站缓存。
     */
    @PostMapping("/push")
    public Result<OutboundMessageResponse> push(@RequestBody OutboundMessageRequest req) {
        if (req == null || req.messages() == null || req.messages().isEmpty()) {
            return Result.ok(new OutboundMessageResponse(List.of()));
        }
        long archiveId = parseArchiveId(req.archiveId());
        Archive archive = archiveId < 0 ? null : archiveStore.find(archiveId).orElse(null);
        if (archive == null) {
            log.warn("推送的存档不存在：{}", req.archiveId());
            return Result.fail("存档不存在：" + req.archiveId());
        }

        OutboundMessage out = toOutbound(archive, req);
        if (out.getMessages().isEmpty()) {
            return Result.ok(new OutboundMessageResponse(List.of()));
        }

        //TODO 交给 PostService 跑后处理链，再由 OutBoundService 分发出去
        log.info("存档 {} 推回 {} 条回复（会话 {}）",
                archive.id(), out.getMessages().size(), req.sessionId());

        return Result.ok(new OutboundMessageResponse(ackItems(archive, req)));
    }

    /** 一条回复都没被接受时不 ack，跳板留着下一轮重推。 */
    private static List<OutboundMessageResponse.Item> ackItems(Archive archive,
                                                               OutboundMessageRequest req) {
        List<OutboundMessageResponse.Item> items = new ArrayList<>();
        List<OutboundMessageRequest.Item> in = req.messages();
        for (int i = 0; i < in.size(); i++) {
            OutboundMessageRequest.Item item = in.get(i);
            if (item == null || item.text() == null || item.text().isBlank()) {
                continue;
            }
            items.add(new OutboundMessageResponse.Item(
                    Long.toString(archive.id()), req.sessionId(), i, item.text()));
        }
        return items;
    }

    /** 请求体 → 领域对象；chatType/chatId 留空时退回存档自己的会话。 */
    private static OutboundMessage toOutbound(Archive archive, OutboundMessageRequest req) {
        List<NormalMessage> messages = new ArrayList<>();
        for (OutboundMessageRequest.Item item : req.messages()) {
            if (item == null) {
                continue;
            }
            String text = item.text();
            if (text == null || text.isBlank()) {
                continue;
            }
            NormalMessage m = new NormalMessage();
            m.setArchiveId(archive.id());
            m.setChannel(archive.links().isEmpty() ? null : archive.links().get(0).kind());
            m.setTimestamp(System.currentTimeMillis());
            m.setSessionId(req.sessionId());
            m.setContent(Content.text(text));
            m.setSource(source(archive, item));
            messages.add(m);
        }

        OutboundMessage out = new OutboundMessage();
        out.setArchiveId(archive.id());
        out.setSessionId(req.sessionId());
        out.setTimestamp(System.currentTimeMillis());
        out.setMessages(messages);
        return out;
    }

    /** 出站消息的来源：存档自己 + 目标会话。 */
    private static MessageSource source(Archive archive, OutboundMessageRequest.Item item) {
        String chatType = item.chatType() == null || item.chatType().isBlank()
                ? null : item.chatType().trim();
        String chatId = item.chatId() == null || item.chatId().isBlank()
                ? null : item.chatId().trim();
        return new MessageSource(
                new MessageSource.Origin(
                        archive.links().isEmpty() ? null : archive.links().get(0).kind(),
                        chatType, chatId, null),
                new MessageSource.Sender(
                        archive.links().isEmpty() ? null : archive.links().get(0).account(),
                        archive.name(), null, MessageSource.Sender.SenderKind.BOT));
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
}
