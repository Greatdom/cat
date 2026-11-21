package com.wdd.cat.web;

import com.wdd.cat.archive.Archive;
import com.wdd.cat.archive.ArchiveStore;
import com.wdd.cat.common.entity.Result;
import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.message.Content;
import com.wdd.cat.message.MessageSource;
import com.wdd.cat.message.child.InboundMessage;
import com.wdd.cat.message.child.NormalMessage;
import com.wdd.cat.message.child.OutboundMessage;
import com.wdd.cat.service.PostService;
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
 * &#064create: 2026-11-16 09:49
 **/
@RestController
@RequestMapping("/api/outbound_message")
public class OutBoundMessageController {

    private static final Logger log = LoggerFactory.getLogger(OutBoundMessageController.class);

    private final ArchiveStore archiveStore;
    private final PostService postService;

    public OutBoundMessageController(ArchiveStore archiveStore, PostService postService) {
        this.archiveStore = archiveStore;
        this.postService = postService;
    }

    /**
     * 跳板把一轮回复推回来，转成 OutboundMessage 后走后处理链。
     * <p>一轮只回一条 ack（带 sessionId）：跳板收到之后清掉该会话整轮的出站缓存。
     * 没入队（消息全空）时<b>不回 ack</b>，跳板会把这一轮留在缓存里按失败处理。
     */
    @PostMapping("/push")
    public Result<OutboundMessageResponse> push(@RequestBody OutboundMessageRequest req) {
        if (req == null || req.messages() == null || req.messages().isEmpty()) {
            return Result.ok(new OutboundMessageResponse(null));
        }
        long archiveId = parseArchiveId(req.archiveId());
        Archive archive = archiveId < 0 ? null : archiveStore.find(archiveId).orElse(null);
        if (archive == null) {
            log.warn("推送的存档不存在：{}", req.archiveId());
            return Result.fail("存档不存在：" + req.archiveId());
        }

        // 源头没了就不知道该发到哪个渠道、回的是哪条消息，这一轮只能丢
        ChannelKind channel = ChannelKind.fromWire(req.inbound() == null
                ? null : req.inbound().channel());
        if (channel == null) {
            log.warn("推送缺少原始的入站消息或渠道无法识别，无法确定目标：{}", req.sessionId());
            return Result.fail("缺少 inboundMessage");
        }

        OutboundMessage out = toOutbound(archive, req, channel);
        if (out.getMessages().isEmpty()) {
            // 全是空白消息：什么都没收下，因此不回 ack
            log.warn("推送的 {} 条回复内容全为空，不入队（会话 {}）",
                    req.messages().size(), req.sessionId());
            return Result.ok(new OutboundMessageResponse(null));
        }

        // 丢进后处理缓存池，之后交给 PostService 的定时器轮询
        postService.enqueue(out);

        log.info("存档 {} 推回 {} 条回复（会话 {}，回的是 {}:{}）",
                archive.id(), out.getMessages().size(), req.sessionId(),
                req.inbound().chatType(), req.inbound().chatId());

        // 收下了：一轮一个 ack
        return Result.ok(OutboundMessageResponse.accepted(req.sessionId()));
    }

    /**
     * 把跳板带回来的入站身份还原成领域对象。
     * <p>只填定位用的字段；上下文（assessment / recallBundle / egoDecision）不回灌。
     */
    private static InboundMessage toInbound(OutboundMessageRequest req, ChannelKind channel) {
        OutboundMessageRequest.InboundRef ref = req.inbound();

        InboundMessage m = new InboundMessage();
        m.setArchiveId(parseArchiveId(ref.archiveId() == null ? req.archiveId() : ref.archiveId()));
        m.setChannel(channel);
        m.setTimestamp(ref.timestamp());
        m.setChannelMessageId(ref.channelMessageId());
        m.setSessionId(ref.sessionId() == null ? req.sessionId() : ref.sessionId());
        // 原话：回顾时要知道"我在回应什么"
        m.setContent(ref.text() == null ? null : Content.text(ref.text()));
        m.setSource(new MessageSource(
                new MessageSource.Origin(channel, ref.chatType(), ref.chatId(), null),
                new MessageSource.Sender(
                        ref.senderId(),
                        ref.senderName(),
                        ref.card(),
                        senderKind(ref.senderKind()))));
        return m;
    }

    private static MessageSource.Sender.SenderKind senderKind(String raw) {
        if (raw == null || raw.isBlank()) {
            return MessageSource.Sender.SenderKind.HUMAN;
        }
        try {
            return MessageSource.Sender.SenderKind.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return MessageSource.Sender.SenderKind.HUMAN;
        }
    }

    /** 请求体 → 领域对象；渠道与目标全部沿用入站消息。 */
    private static OutboundMessage toOutbound(Archive archive, OutboundMessageRequest req,
                                              ChannelKind channel) {
        InboundMessage inbound = toInbound(req, channel);
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
            m.setChannel(channel);
            m.setTimestamp(System.currentTimeMillis());
            m.setSessionId(req.sessionId());
            m.setContent(Content.text(text));
            m.setSource(replySource(archive, channel, item, inbound));
            messages.add(m);
        }

        OutboundMessage out = new OutboundMessage();
        out.setArchiveId(archive.id());
        out.setChannel(channel);
        out.setTimestamp(System.currentTimeMillis());
        out.setSessionId(req.sessionId());
        // 回的是哪条：后处理要拿它做消息回顾
        out.setInboundMessage(inbound);
        // 出站还没有自己的消息号，先沿用被回复的那条，等 QQ 返回真实号再改
        out.setChannelMessageId(inbound.getChannelMessageId());
        out.setSource(replySource(archive, channel, null, inbound));
        out.setMessages(messages);
        return out;
    }

    /**
     * 出站消息的来源：说话的是我这个存档，目标是入站消息来的那个会话。
     *
     * @param item 单条回复（可为 null，表示整条 OutboundMessage 的来源）
     */
    private static MessageSource replySource(Archive archive, ChannelKind channel,
                                             OutboundMessageRequest.Item item,
                                             InboundMessage inbound) {
        MessageSource.Origin origin = inbound.getSource() == null
                ? null : inbound.getSource().origin();

        String chatType = item != null && item.chatType() != null && !item.chatType().isBlank()
                ? item.chatType().trim()
                : (origin == null ? null : origin.chatType());
        String chatId = item != null && item.chatId() != null && !item.chatId().isBlank()
                ? item.chatId().trim()
                : (origin == null ? null : origin.chatId());
        String chatName = origin == null ? null : origin.chatName();

        return new MessageSource(
                new MessageSource.Origin(channel, chatType, chatId, chatName),
                new MessageSource.Sender(
                        accountOf(archive, channel),
                        archive.name(),
                        null,
                        MessageSource.Sender.SenderKind.BOT));
    }

    /** 这个存档在这个渠道上的账号。 */
    private static String accountOf(Archive archive, ChannelKind channel) {
        if (archive.links() == null) {
            return null;
        }
        return archive.links().stream()
                .filter(l -> l.kind() == channel)
                .map(Archive.Link::account)
                .findFirst()
                .orElse(null);
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
