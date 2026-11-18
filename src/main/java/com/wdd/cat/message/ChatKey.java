package com.wdd.cat.message;

import com.wdd.cat.common.enums.ChannelKind;

/**
 * &#064program: cat
 * <p>
 * &#064description: 一个会话的标识，形如 "external_qq:group:20001"
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 21:24
 **/
public record ChatKey(ChatType type, String id, ChannelKind channelKind, Boolean enabled) {

    public ChatKey {
        if (type == null) {
            throw new IllegalArgumentException("chatType 不能为空");
        }
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("chatId 不能为空");
        }
        if (channelKind == null) {
            throw new IllegalArgumentException("channelKind 不能为空");
        }
        if (channelKind.getFamily() != ChannelKind.ChannelFamily.EXTERNAL) {
            throw new IllegalArgumentException("ChatKey 只能用外部渠道：" + channelKind);
        }
        id = id.trim();
    }

    /** enabled 为 null 视为 false。 */
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }

    /** 解析 "external_qq:group:20001"；格式不对抛 IllegalArgumentException。 */
    public static ChatKey parse(String s) {
        if (s == null) {
            throw new IllegalArgumentException("chatKey 不能为空");
        }
        int first = s.indexOf(':');
        int second = first < 0 ? -1 : s.indexOf(':', first + 1);
        if (first <= 0 || second <= first || second == s.length() - 1) {
            throw new IllegalArgumentException("chatKey 格式错误：" + s);
        }
        ChannelKind channel = ChannelKind.fromWire(s.substring(0, first));
        ChatType type = ChatType.fromWire(s.substring(first + 1, second));
        String id = s.substring(second + 1).trim();
        if (channel == null || type == null || id.isEmpty()
                || !id.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("chatKey 格式错误：" + s);
        }
        return new ChatKey(type, id, channel, Boolean.TRUE);
    }

    /** 线上格式 "external_qq:group:20001"。 */
    public String wire() {
        return channelKind.wire() + ":" + type.wire() + ":" + id;
    }

    public boolean isGroup() {
        return type == ChatType.GROUP;
    }

    @Override
    public String toString() {
        return wire();
    }

    public enum ChatType {
        PRIVATE, GROUP;

        /** 传输出小写。 */
        public String wire() {
            return name().toLowerCase();
        }

        /** 大小写不敏感，未知返回 null。 */
        public static ChatType fromWire(String s) {
            if (s == null) {
                return null;
            }
            return switch (s.trim().toUpperCase()) {
                case "PRIVATE" -> PRIVATE;
                case "GROUP" -> GROUP;
                default -> null;
            };
        }
    }
}
