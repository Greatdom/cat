package com.wdd.cat.message;


import com.wdd.cat.common.enums.ChannelKind;

/**
 * &#064program: cat
 * <p>
 * &#064description: 消息的来源消息,用于追溯和定位
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 21:19
 **/

public record MessageSource(
        Origin origin,
        Sender sender
) {

    /** 从哪来。 */
    public record Origin(
            ChannelKind channel,
            String chatType,      // "private" / "group"
            String chatId,
            String chatName
    ) {}

    /** 谁发的。 */
    public record Sender(
            String id,
            String name,
            String card,          // 群名片
            SenderKind kind
    ) {
        public enum SenderKind { HUMAN, BOT, SYSTEM }
    }
}
