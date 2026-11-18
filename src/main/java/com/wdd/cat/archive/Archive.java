package com.wdd.cat.archive;

import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.message.ChatKey;

import java.util.List;

/**
 * &#064program: cat
 * &#064description: 一个存档 = 一个模拟人
 * &#064author: black-cat
 * &#064create: 2026-11-15 20:35
 **/
public record Archive(
        String version,                   // 存档版本
        long id,                          // 雪花 ID，传给跳板时转 String
        String name,                      // 存档名
        Boolean enabled,                  // 是否启用
        Memory memory,                    // 记忆库引用
        Session session,                  // 绑定的会话
        List<Link> links,                 // 外部连接
        List<Contact> contacts,           // 这个存档认识谁（临时，见 Contact）
        PipelineConfig pipeline,          // 装配单
        String createdAt,                 // 创建时间
        String updatedAt                  // 更新时间
) {

    /** enabled 为 null 视为 false。 */
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }

    /** 启用中的联系人，按渠道过滤。 */
    public List<Contact> contactsOf(ChannelKind channel) {
        if (contacts == null) {
            return List.of();
        }
        return contacts.stream()
                .filter(Contact::isEnabled)
                .filter(c -> c.channel() == channel)
                .toList();
    }

    /** 会话状态。 */
    public enum SessionStatus { BOUND, LOST }

    /** 记忆库引用。 */
    public record Memory(String storeId) {}

    /** 绑定的会话。 */
    public record Session(
            String sessionId,
            SessionStatus status,
            String title
    ) {}

    /** 外部连接。 */
    public record Link(
            Long id,
            ChannelKind kind,
            Boolean enabled,
            String account,
            String baseUrl,
            String token,
            String deviceId
    ) {
        /** enabled 为 null 视为 false。 */
        public boolean isEnabled() {
            return Boolean.TRUE.equals(enabled);
        }

        public Link {
            if (kind == null) throw new IllegalArgumentException("kind 不能为空");
            if (kind.getFamily() != ChannelKind.ChannelFamily.EXTERNAL) {
                throw new IllegalArgumentException("Link 只能用外部渠道：" + kind);
            }
        }
    }

    /**
     * 这个存档认识谁 —— 决定从哪些会话拉消息。
     *
     * <p><b>临时方案</b>：记忆系统成熟后由记忆替代（关系变成记忆的一种），
     * 那时这个字段只剩"拉哪些会话"的路由用途。
     */
    public record Contact(
            ChannelKind channel,   // 走哪个渠道
            String chatType,       // "private" / "group"
            String chatId,         // 群号 / 对方号
            Boolean enabled
    ) {
        public Contact {
            if (channel == null) throw new IllegalArgumentException("channel 不能为空");
            if (channel.getFamily() != ChannelKind.ChannelFamily.EXTERNAL) {
                throw new IllegalArgumentException("Contact 只能用外部渠道：" + channel);
            }
            if (ChatKey.ChatType.fromWire(chatType) == null) {
                throw new IllegalArgumentException("chatType 只能是 private / group：" + chatType);
            }
            if (chatId == null || chatId.isBlank()) {
                throw new IllegalArgumentException("chatId 不能为空");
            }
        }

        /** enabled 为 null 视为 false。 */
        public boolean isEnabled() {
            return Boolean.TRUE.equals(enabled);
        }

        /** 转成会话标识。 */
        public ChatKey chatKey() {
            return new ChatKey(ChatKey.ChatType.fromWire(chatType), chatId);
        }
    }
}
