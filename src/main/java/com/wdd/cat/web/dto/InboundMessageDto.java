package com.wdd.cat.web.dto;

import com.wdd.cat.message.Content;
import com.wdd.cat.message.MessageSource;
import com.wdd.cat.message.child.Assessment;
import com.wdd.cat.message.child.InboundMessage;
import com.wdd.cat.message.child.RecallBundle;

import java.util.Map;

/**
 * &#064program: cat
 * &#064description: 给跳板的入站消息视图；雪花 id 一律用字符串
 * <p>
 * 只读，不做反序列化——跳板推回来的东西走别的 DTO。
 * &#064author: black-cat
 * &#064create: 2026-11-19 12:00
 **/
public record InboundMessageDto(
        String archiveId,
        String sessionId,
        String channel,
        String chatType,
        String chatId,
        String senderId,
        String senderName,
        String card,
        String senderKind,
        String channelMessageId,
        long timestamp,
        Content content,
        Assessment assessment,
        RecallBundle recallBundle
) {

    /** 从领域对象转换。 */
    public static InboundMessageDto from(InboundMessage msg) {
        if (msg == null) {
            return null;
        }
        MessageSource src = msg.getSource();
        MessageSource.Origin origin = src == null ? null : src.origin();
        MessageSource.Sender sender = src == null ? null : src.sender();

        return new InboundMessageDto(
                Long.toString(msg.getArchiveId()),
                msg.getSessionId(),
                msg.getChannel() == null ? null : msg.getChannel().wire(),
                origin == null ? null : origin.chatType(),
                origin == null ? null : origin.chatId(),
                sender == null ? null : sender.id(),
                sender == null ? null : sender.name(),
                sender == null ? null : sender.card(),
                sender == null || sender.kind() == null ? null : sender.kind().name(),
                msg.getChannelMessageId(),
                msg.getTimestamp(),
                msg.getContent(),
                msg.getAssessment(),
                msg.getRecallBundle()
        );
    }


}
