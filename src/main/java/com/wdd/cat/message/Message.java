package com.wdd.cat.message;

import com.wdd.cat.common.enums.ChannelKind;

/**
 * &#064program: cat
 * &#064description: Message 父类
 * &#064author: black-cat
 * &#064create: 2026-11-17 11:48
 **/
public abstract class Message {

    private long archiveId;             // 归哪个存档
    private ChannelKind channel;        // 渠道类型
    private MessageSource source;       // 从哪来 + 谁发的
    private long timestamp;             // 消息时间戳（毫秒）
    private String channelMessageId;    // 渠道侧的消息 ID，去重用
    private String sessionId;           // 归属的 DSH 会话 ID，决定进哪个投递桶

    /** 会话类型 "private" / "group"。 */
    public String chatType() {
        return source == null || source.origin() == null ? "" : source.origin().chatType();
    }

    /** 会话 ID。 */
    public String chatId() {
        return source == null || source.origin() == null ? "" : source.origin().chatId();
    }

    /** 发送者 ID。 */
    public String senderId() {
        return source == null || source.sender() == null ? "" : source.sender().id();
    }

    /** 会话标识；缺字段或渠道不是外部渠道时返回 null。 */
    public ChatKey chatKey() {
        ChatKey.ChatType type = ChatKey.ChatType.fromWire(chatType());
        String id = chatId();
        if (type == null || id == null || id.isBlank() || channel == null
                || channel.getFamily() != ChannelKind.ChannelFamily.EXTERNAL) {
            return null;
        }
        return new ChatKey(type, id, channel, Boolean.TRUE);
    }

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

    public String getChannelMessageId() {
        return channelMessageId;
    }

    public void setChannelMessageId(String channelMessageId) {
        this.channelMessageId = channelMessageId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
