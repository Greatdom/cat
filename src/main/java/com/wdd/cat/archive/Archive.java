package com.wdd.cat.archive;

import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.message.ChatKey;
import com.wdd.cat.state.memory.Entity;

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
        List<ChatKey> contacts,           // 这个存档认识谁（临时，记忆成熟后由记忆替代）
        PipelineConfig pipeline,          // 装配单
        String createdAt,                 // 创建时间
        String updatedAt                 // 更新时间
        //Map<Entity,List<ChatKey>> entities //TODO 记忆系统成熟后用来取代contacts字段
        //TODO 这个字段的使用方法是存储实体和渠道之间的关系,实体就是存档认识的人抽象,ChatKey是认识的人的面具,List<ChatKey>间的元素互斥
        //TODO 新认识人的算法是:在服务器内要加长期记忆的时候如果发现这个记忆所属信息的ChatKey是陌生人就会将其升级为认识的人
        //TODO 目前没用

) {

    /** enabled 为 null 视为 false。 */
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }

    /** 启用中的会话，按渠道过滤。 */
    public List<ChatKey> contactsOf(ChannelKind channel) {
        if (contacts == null) {
            return List.of();
        }
        return contacts.stream()
                .filter(ChatKey::isEnabled)
                .filter(c -> c.channelKind() == channel)
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
    ) {
        /** 已绑定且会话 ID 非空。 */
        public boolean isBound() {
            return SessionStatus.BOUND == status
                    && sessionId != null
                    && !sessionId.isBlank();
        }
    }

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
}
