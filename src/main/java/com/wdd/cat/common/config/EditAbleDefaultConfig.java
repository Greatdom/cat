package com.wdd.cat.common.config;

/**
 * &#064program: cat
 * &#064description: 可配置项的默认值；设置页取不到持久化配置时用这里的值
 * <p>
 * 这些字段是 static 而非 final：设置页保存时会把值直接写回来，
 * 运行时改了就生效（读取方每次都取字段值，不缓存到局部变量）。
 * &#064author: black-cat
 * &#064create: 2026-11-15 19:44
 **/
public class EditAbleDefaultConfig {

    /** 存档总数上限。 */
    public static volatile int archiveMaxCount = 4;

    /** 合并窗口：消息在队列里躺多久才取出合并（毫秒）。 */
    public static volatile long mergeWindowMs = 1200;

    /** 一次最多合并几条。 */
    public static volatile int mergeMaxCount = 8;

    /** QQ 拉取和推送的间隔（毫秒）。收发共用：同一个号，风控是一体的。 */
    public static volatile long qqPollAndSendIntervalMs = 2000;


    private EditAbleDefaultConfig() {
    }
}
