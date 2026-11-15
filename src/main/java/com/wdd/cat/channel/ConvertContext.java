package com.wdd.cat.channel;
import com.wdd.cat.common.enums.ChannelKind;

import java.util.Set;
/**
 * &#064program: cat
 * <p>
 * &#064description: 转换上下文：Convertor 判断「这条消息接不接收」的依据
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 21:42
 **/



public record ConvertContext(
        long archiveId,         // 哪个存档
        ChannelKind channel,    // 哪个渠道
        String selfAccount,     // 本存档的账号（"10000"）
        Set<String> accountKeys // 所有存档已占用的账号，"qq:10000" 格式
) {
    /** 是不是自己发的（自己发的不能当别人的消息）。 */
    public boolean isSelf(String senderId) {
        // TODO selfAccount.equals(senderId)
        return false;
    }

    /** 是不是另一个存档的号（避免存档之间互相串话）。 */
    public boolean isAnotherArchive(String senderId) {
        // TODO accountKeys 里有 key(channel, senderId) 且不等于自己
        return false;
    }

    /** 这条消息该不该丢掉 = isSelf || isAnotherArchive。 */
    public boolean shouldDrop(String senderId) {
        // TODO return isSelf(senderId) || isAnotherArchive(senderId)
        return false;
    }

    /** 拼账号键："qq:10000"。带渠道是为了跨渠道撞号。 */
    public static String key(ChannelKind kind, String account) {
        // TODO return kind.name().toLowerCase() + ":" + account
        return "";
    }
}