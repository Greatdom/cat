package com.wdd.cat.channel.onebot;

import com.wdd.cat.channel.ConvertContext;
import com.wdd.cat.channel.InboundConvertor;
import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.common.enums.FileKind;
import com.wdd.cat.message.Content;
import com.wdd.cat.message.child.NormalMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * &#064program: cat
 * &#064description: OneBot 原始消息 → NormalMessage
 * &#064author: black-cat
 * &#064create: 2026-11-15 22:29
 **/
@Component
public class OneBotInboundConvertor implements InboundConvertor<JsonNode> {

    private static final Logger log = LoggerFactory.getLogger(OneBotInboundConvertor.class);

    @Override
    public ChannelKind kind() {
        return ChannelKind.EXTERNAL_QQ;
    }

    @Override
    public List<NormalMessage> convert(List<JsonNode> raw, ConvertContext ctx) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        List<NormalMessage> out = new ArrayList<>(raw.size());
        for (JsonNode event : raw) {
            NormalMessage msg = convertOne(event, ctx);
            if (msg != null) {
                out.add(msg);
            }
        }
        return out;
    }

    /** 一条事件 → 一条 NormalMessage；被过滤时返回 null。 */
    NormalMessage convertOne(JsonNode event, ConvertContext ctx) {
        if (event == null || !event.isObject()) {
            return null;
        }

        String senderId = text(event.path("user_id"));
        if (senderId.isEmpty()) {
            senderId = text(event.path("sender").path("user_id"));
        }
        if (ctx.shouldDrop(senderId)) {
            return null;
        }

        Content content = translate(event.path("message"));
        if (content == null) {
            return null;
        }

        NormalMessage msg = new NormalMessage();
        msg.setArchiveId(ctx.archiveId());
        msg.setChannel(ChannelKind.EXTERNAL_QQ);
        msg.setTimestamp(event.path("time").asLong(0) * 1000);
        msg.setContent(content);
        msg.setRaw(event);

        String chatType;
        String chatId;
        if (event.has("group_id")) {
            chatType = "group";
            chatId = text(event.path("group_id"));
        } else {
            chatType = "private";
            chatId = senderId;
        }
        msg.setSource(new com.wdd.cat.message.MessageSource(
                new com.wdd.cat.message.MessageSource.Origin(
                        ChannelKind.EXTERNAL_QQ,
                        chatType,
                        chatId,
                        text(event.path("group_name"))),
                new com.wdd.cat.message.MessageSource.Sender(
                        senderId,
                        text(event.path("sender").path("nickname")),
                        text(event.path("sender").path("card")),
                        com.wdd.cat.message.MessageSource.Sender.SenderKind.HUMAN)));

        // message_id 也要带上，供去重用
        msg.setChannelMessageId(text(event.path("message_id")));
        return msg;
    }

    /** 段数组 → Content。 */
    Content translate(JsonNode segments) {
        if (segments == null || !segments.isArray() || segments.isEmpty()) {
            return null;
        }
        List<Content> parts = new ArrayList<>();
        for (JsonNode seg : segments) {
            String type = text(seg.path("type"));
            JsonNode data = seg.path("data");
            switch (type) {
                case "text" -> {
                    String t = text(data.path("text"));
                    if (!t.isEmpty()) {
                        parts.add(Content.text(t));
                    }
                }
                case "image" -> {
                    String url = text(data.path("url"));
                    String file = text(data.path("file"));
                    if (!url.isEmpty() || !file.isEmpty()) {
                        parts.add(new Content.FileRef(url, file, "", FileKind.IMAGE));
                    }
                }
                default -> log.trace("跳过不支持的段类型：{}", type);
            }
        }
        if (parts.isEmpty()) {
            return null;
        }
        return parts.size() == 1 ? parts.get(0) : new Content.Multi(parts);
    }

    private static String text(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "";
        }
        String s = node.asString("");
        return s == null ? "" : s.trim();
    }
}
