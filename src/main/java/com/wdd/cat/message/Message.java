package com.wdd.cat.message;


import com.wdd.cat.common.enums.ChannelKind;

/**
 * &#064program: cat
 * <p>
 * &#064description: Message父类
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-17 11:48
 **/

public abstract class Message {
    private long archiveId;             // 归哪个存档
    private ChannelKind channel;        // 渠道类型
    private MessageSource source;       // 从哪来 + 谁发的

    public long getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(long archiveId) {
        this.archiveId = archiveId;
    }

    public ChannelKind getChannel() {
        return channel;
    }

    public void setChannel(ChannelKind channel) {
        this.channel = channel;
    }

    public MessageSource getSource() {
        return source;
    }

    public void setSource(MessageSource source) {
        this.source = source;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    private long timestamp;             // 消息时间戳（毫秒）

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