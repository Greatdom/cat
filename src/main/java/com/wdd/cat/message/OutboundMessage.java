package com.wdd.cat.message;


/**
 * &#064program: cat
 * <p>
 * &#064description: Java 要跳板发出去的东西,比 InboundMessage 简单得多，只带发去哪、发什么
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 21:28
 **/

import com.wdd.cat.common.enums.ChannelKind;

import java.util.List;

/**
 * 一条出站消息：。
 * 。
 */
public record OutboundMessage(
        long archiveId,        // 哪个存档发的
        ChannelKind channel,   // 走哪个渠道
        String chatType,       // "private" / "group"
        String chatId,         // 发到哪个会话
        Content content        // 发什么（文本/文件/多段）
) {
    /** 全部文本。 */
    public String text() { return null; /* TODO = Content.plainText(content) */ }

//    /** 里面所有图片，跳板要单独上传。 */
//    public List<Content.FileRef> images() { /* TODO content 是 Multi 时筛出 FileRef，否则空表 */ }
}