package com.wdd.cat.common.config;

/**
 * &#064program: cat
 * &#064description: 可配置项的默认值；设置页取不到持久化配置时用这里的值
 * &#064author: black-cat
 * &#064create: 2026-11-15 19:44
 **/
public class EditAbleDefaultConfig {

    /** 存档总数上限。 */
    public static int archiveMaxCount = 4;

    /** 合并窗口（毫秒）。 */
    public static long mergeWindowMs = 1200;

    /** 一次最多合并几条。 */
    public static int mergeMaxCount = 8;

    /** QQ 拉取间隔（毫秒）。 */
    public static long qqPollIntervalMs = 2000;

    /** 出站两条消息的最小间隔（毫秒）。 */
    public static long outboundMinIntervalMs = 500;

    private EditAbleDefaultConfig() {
    }
}
