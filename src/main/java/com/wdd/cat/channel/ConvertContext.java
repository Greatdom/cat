package com.wdd.cat.channel;

import com.wdd.cat.common.enums.ChannelKind;

import java.util.Set;

/**
 * &#064program: cat
 * &#064description: 转换上下文：Convertor 判断「这条消息接不接收」的依据
 * &#064author: black-cat
 * &#064create: 2026-11-15 21:42
 **/
public record ConvertContext(
        long archiveId,         // 哪个存档
        ChannelKind channel,    // 哪个渠道
        String selfAccount,     // 本存档的账号（"10000"）
        Set<String> accountKeys // 所有存档已占用的账号，"external_qq:10000" 格式
) {

    /** 是不是自己发的。 */
    public boolean isSelf(String senderId) {
        return senderId != null && senderId.equals(selfAccount);
    }

    /** 是不是另一个存档的号。 */
    public boolean isAnotherArchive(String senderId) {
        if (senderId == null || senderId.equals(selfAccount)) {
            return false;
        }
        return accountKeys != null && accountKeys.contains(key(channel, senderId));
    }

    /** 该不该丢 = 自己发的 或 别的存档的号。 */
    public boolean shouldDrop(String senderId) {
        return isSelf(senderId) || isAnotherArchive(senderId);
    }

    /** 拼账号键："external_qq:10000"；必须和 ArchiveStore.allAccounts() 同格式。 */
    public static String key(ChannelKind kind, String account) {
        return (kind.name() + ":" + account).toLowerCase();
    }
}
