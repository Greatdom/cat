package com.wdd.cat.common.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * &#064program: cat
 * &#064description: 设置页能调哪些项、中文叫什么、哪些需要重启
 * <p>
 * 只收 {@link EditAbleDefaultConfig} 里的字段 —— 那才是页面该碰的东西。
 * 集中一处：白名单、页面渲染、「需重启」标注要三边对齐，散开一定漏。
 * <p>
 * 键名保留字段名（配置里的真实名字），页面展示用 {@link Item#label()}。
 * &#064author: black-cat
 * &#064create: 2026-11-20 22:45
 **/
public final class EditableSettings {

    private EditableSettings() {
    }

    /** 允许页面写入的键。 */
    public static final Set<String> ALL = Set.of(
            "archiveMaxCount",
            "mergeWindowMs",
            "mergeMaxCount",
            "qqPollAndSendIntervalMs");

    /**
     * 改了必须重启才生效的键。
     * <p>这四个都是「每次用的时候现读字段」，所以现在一个都不需要重启。
     * 留着这个集合，将来有了启动期才读的值往里加。
     */
    public static final Set<String> RESTART_REQUIRED = Set.of();

    public static boolean needsRestart(String key) {
        return RESTART_REQUIRED.contains(key);
    }

    /**
     * 一项设置。
     *
     * @param key   字段名
     * @param label 中文名（页面主标题）
     * @param hint  一句话说明
     * @param unit  单位，空表示无
     * @param min   最小值（含）
     * @param max   最大值（含）
     */
    public record Item(String key, String label, String hint, String unit, long min, long max) {
    }

    /** 全部项，按页面展示顺序。 */
    public static final List<Item> ITEMS = List.of(
            new Item("qqPollAndSendIntervalMs", "QQ 收发间隔",
                    "同一个 QQ 号的拉取和发送都按这个节奏走。太小容易被风控", "毫秒", 500, 60_000),
            new Item("mergeWindowMs", "连发合并窗口",
                    "对方连发几条时，间隔小于这个值就合成一条", "毫秒", 0, 30_000),
            new Item("mergeMaxCount", "一批最多合并",
                    "攒够这么多条就直接放行，不再等窗口", "条", 1, 50),
            new Item("archiveMaxCount", "存档数量上限",
                    "最多能建几个模拟人", "个", 1, 32));

    private static final Map<String, Item> BY_KEY = new LinkedHashMap<>();

    static {
        for (Item item : ITEMS) {
            BY_KEY.put(item.key(), item);
        }
    }

    public static Item item(String key) {
        return BY_KEY.get(key);
    }

    /**
     * 页面分组。
     *
     * @param title       分组标题
     * @param description 分组用途一句话
     * @param keys        组内键，顺序即显示顺序
     */
    public static final List<Group> GROUPS = List.of(
            new Group("收发节奏", "消息进出的快慢", List.of(
                    "qqPollAndSendIntervalMs")),
            new Group("消息合并", "对方连发多条时怎么合", List.of(
                    "mergeWindowMs",
                    "mergeMaxCount")),
            new Group("存档", "模拟人的数量", List.of(
                    "archiveMaxCount")));

    public record Group(String title, String description, List<String> keys) {
    }
}
