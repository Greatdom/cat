package com.wdd.cat.message;

/**
 * &#064program: cat
 * <p>
 * &#064description: 一个会话的标识，形如 "group:20001"
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 21:24
 **/
public record ChatKey(ChatType type, String id) {

    public ChatKey {
        if (type == null) {
            throw new IllegalArgumentException("chatType 不能为空");
        }
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("chatId 不能为空");
        }
        id = id.trim();
    }

    /** 解析 "group:20001"；格式不对抛 IllegalArgumentException。 */
    public static ChatKey parse(String s) {
        if (s == null) {
            throw new IllegalArgumentException("chatKey 不能为空");
        }
        int colon = s.indexOf(':');
        if (colon <= 0 || colon == s.length() - 1) {
            throw new IllegalArgumentException("chatKey 格式错误：" + s);
        }
        ChatType type = ChatType.fromWire(s.substring(0, colon));
        String id = s.substring(colon + 1).trim();
        if (type == null || id.isEmpty() || !id.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("chatKey 格式错误：" + s);
        }
        return new ChatKey(type, id);
    }

    /** 线上格式 "group:20001"。 */
    public String wire() {
        return type.wire() + ":" + id;
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
