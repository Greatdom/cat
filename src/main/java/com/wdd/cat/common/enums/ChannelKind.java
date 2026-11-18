package com.wdd.cat.common.enums;


/**
 * &#064program: cat
 * &#064description: 外部连接来源的枚举,用来点明消息的来源
 * 约定:
 * 1.INTERNAL开头是服务器自己发出的消息类型
 * 2.ADD开头是大模型在回复消息时产生的消息类型
 * 3.EXTERNAL开头是外部链接会产生的消息类型
 * &#064author: black-cat
 * &#064create: 2026-11-15 19:51
 **/

public enum ChannelKind {
    INTERNAL_DATE(ChannelFamily.INTERNAL),      // 站内日程发出的消息
    INTERNAL_DESIRE(ChannelFamily.INTERNAL),    // 站内欲望
    ADD_DATE(ChannelFamily.ADD),                // 模型要求附加一条日程(拦截内置日程插件后由中转站生成)
    EXTERNAL_QQ(ChannelFamily.EXTERNAL);        // QQ
    //TODO 还有没实现的,例如INTERNAL_MEMORY ESP32 WECHAT EXTERNAL_AGENT

    private final ChannelFamily family;

    ChannelKind(ChannelFamily family) {
        this.family = family;
    }

    public ChannelFamily getFamily() {
        return family;
    }

    /** 传输出小写，如 "external_qq"。 */
    public String wire() {
        return name().toLowerCase();
    }

    /** 大小写不敏感，未知返回 null。 */
    public static ChannelKind fromWire(String s) {
        if (s == null) {
            return null;
        }
        String v = s.trim().toUpperCase();
        for (ChannelKind k : values()) {
            if (k.name().equals(v)) {
                return k;
            }
        }
        return null;
    }

    public enum ChannelFamily {
        INTERNAL,   // 服务器自己产生的，不进 poller
        ADD,        // 大模型回复时产生的，从跳板进来
        EXTERNAL    // 外部连接产生的，进出 poller
    }
}