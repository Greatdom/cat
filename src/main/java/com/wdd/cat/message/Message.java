package com.wdd.cat.message;


import com.wdd.cat.common.enums.ChannelKind;

/**
 * &#064program: cat
 * <p>
 * &#064description: 一条入站消息：跳板收上来、转换成 Java 认识的样子;整条处理链的输入就是它
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 21:23
 **/

public record Message(
        long archiveId,             // 归哪个存档
        String channelMessageId,    // 跳板那边的消息 ID，去重和回执用
        ChannelKind channel,        // 渠道类型

        MessageSource source,       // 从哪来 + 谁发的
        Content content,            // 消息内容（文本/文件/坐标/多段）
        long timestamp,             // 消息时间戳（毫秒）

        String raw                  // 原始报文，调试用，平时可以丢

        // ── 无状态化：给 DSH 拼上下文用 ──
        //TODO 根据实际业务拓展字段
//        String memoryContext,       // 记忆召回结果（慢变，按需刷新）
//        String sceneContext         // 本轮情境（快变，每轮重算）
) {
    /** 全部文本，给拼 Prompt 用。 */
    public String text() {
        // TODO return Content.plainText(content)
        return "";
    }

    /** 会话类型 "private" / "group"。 */
    public String chatType() {
        // TODO return source.chatType()
        return "";
    }

    /** 会话 ID。 */
    public String chatId() {
        // TODO return source.chatId()
        return "";
    }

    /** 发送者 ID。 */
    public String senderId() {
        // TODO return source.senderId()
        return "";
    }

    /** 消息名（"group:20001"）。 */
    public ChatKey chatKey() {
        // TODO new ChatKey(ChatType.parse(chatType()), chatId())
        return null;
    }
}