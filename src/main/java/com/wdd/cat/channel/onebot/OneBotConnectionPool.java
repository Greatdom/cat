package com.wdd.cat.channel.onebot;


/**
 * &#064program: cat
 * <p>
 * &#064description: OneBot 连接池：每个存档的每个 QQ 号一条连接，复用不重建
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 22:03
 **/

import com.wdd.cat.archive.Archive;
import org.springframework.stereotype.Component;

@Component
public class OneBotConnectionPool {

    /** 取或建。link 里改了 baseUrl/token 就重建。 */
    public OneBotClient forAccount(long archiveId, Archive.Link link) {
        // TODO key = archiveId + ":" + link.account(),要判断这个link是不是kind为qq的
        // TODO 有且 client.matches(baseUrl, token) 就复用，否则新建并替换
        return null;
    }

    /** 存档删了/停用，清掉它的连接。 */
    public void evict(long archiveId) {
        // TODO 移除所有以 archiveId 开头的 key
    }
}