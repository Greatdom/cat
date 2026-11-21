package com.wdd.cat.common.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * &#064program: cat
 * &#064description: 可配置项：字段值 + 设置页的元数据 + 反射写入，三样都在这里
 * <p>
 * 为什么合成一个类：以前分成两处 —— 字段值在 {@code EditAbleDefaultConfig}，
 * 中文名/区间/分组在 {@code EditableSettings}，默认值又在校验服务里写死一遍。
 * 三处各写一份，加一个配置项要改三个文件，漏一处就会「页面上有、代码里没有」。
 * <p>
 * 现在只有一处约定：<b>{@link #ITEMS} 里列出的每个 key，本类必须有一个同名的
 * {@code static volatile int/long} 字段</b>。{@code ConfigFieldBindingTest} 会逐项检查这条约定，
 * 所以反射写字段丢掉的编译期检查由测试补回来。
 * <p>
 * 字段是 static 而非 final：设置页保存时直接写回，运行时改了就生效
 * （读取方每次都取字段值，不缓存到局部变量）。
 * &#064author: black-cat
 * &#064create: 2026-11-15 19:44
 **/
public class EditAbleDefaultConfig {

    // ── 字段 ────────────────────────────────────────────────
    // 字段的声明值就是默认值：类初始化时由 DEFAULTS 读一次并固化，
    // ITEMS 里只放中文名/区间/分组，不重复写默认值。

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

    // ── 元数据 ──────────────────────────────────────────────

    /**
     * 一项设置。
     *
     * @param key   字段名（必须与本类字段同名）
     * @param label 中文名（页面主标题）
     * @param hint  一句话说明
     * @param unit  单位，空表示无
     * @param min   最小值（含）
     * @param max   最大值（含）
     */
    public record Item(String key, String label, String hint, String unit, long min, long max) {
    }

    /**
     * 页面分组。
     *
     * @param title       分组标题
     * @param description 分组用途一句话
     * @param keys        组内键，顺序即显示顺序
     */
    public record Group(String title, String description, List<String> keys) {
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

    /**
     * 出厂默认值（字段声明值的副本）。
     *
     * <p>为什么要单独存一份：{@code applyToRuntime()} 需要「先把字段恢复成默认再盖覆盖」，
     * 而字段会被运行时改掉，不能拿字段的当前值当默认值。这份表在类初始化时从字段读一次，
     * 之后不再变化；改了字段声明值，这里自动跟着变。
     */
    private static final Map<String, Long> DEFAULTS;

    /** 页面分组。 */
    public static final List<Group> GROUPS = List.of(
            new Group("收发节奏", "消息进出的快慢", List.of(
                    "qqPollAndSendIntervalMs")),
            new Group("消息合并", "对方连发多条时怎么合", List.of(
                    "mergeWindowMs",
                    "mergeMaxCount")),
            new Group("存档", "模拟人的数量", List.of(
                    "archiveMaxCount")));

    /** 允许页面写入的键（= ITEMS 的 key 集合）。 */
    public static final Set<String> ALL;

    /** key → Item。 */
    private static final Map<String, Item> BY_KEY;

    /** key → 可写的 static 字段。反射结果缓存起来，每次保存不必重新查找。 */
    private static final Map<String, Field> FIELDS;

    static {
        Map<String, Item> byKey = new LinkedHashMap<>();
        Map<String, Field> fields = new LinkedHashMap<>();
        Map<String, Long> defaults = new LinkedHashMap<>();
        for (Item item : ITEMS) {
            byKey.put(item.key(), item);
            Field field = findField(item.key());
            if (field != null) {
                fields.put(item.key(), field);
                Long value = readField(field);
                if (value != null) {
                    defaults.put(item.key(), value);
                }
            }
        }
        BY_KEY = Collections.unmodifiableMap(byKey);
        FIELDS = Collections.unmodifiableMap(fields);
        DEFAULTS = Collections.unmodifiableMap(defaults);
        ALL = Collections.unmodifiableSet(byKey.keySet());
    }

    public static Item item(String key) {
        return key == null ? null : BY_KEY.get(key);
    }

    /**
     * 改了必须重启才生效的键。
     * <p>这四个都是「每次用的时候现读字段」，所以现在一个都不需要重启。
     * 留着这个方法，将来有了启动期才读的值往里加。
     */
    public static boolean needsRestart(String key) {
        return false;
    }

    // ── 读写 ────────────────────────────────────────────────

    /** 各键的出厂默认值（不受运行时改动影响）。 */
    public static Map<String, Object> defaults() {
        return new LinkedHashMap<>(DEFAULTS);
    }

    /**
     * 把一个值写回对应字段。
     *
     * @param key   字段名
     * @param value 新值，会按字段类型（int / long）转换
     * @return 是否真的写入了（key 不认识、或没有对应字段时返回 false）
     */
    public static boolean write(String key, long value) {
        Field field = key == null ? null : FIELDS.get(key);
        if (field == null) {
            return false;
        }
        try {
            if (field.getType() == int.class) {
                field.setInt(null, (int) value);
            } else {
                field.setLong(null, value);
            }
            return true;
        } catch (IllegalAccessException | IllegalArgumentException e) {
            // 字段已 setAccessible，理论上不会走到这；真走到了也不该让保存失败
            return false;
        }
    }

    /** 读一个键的当前值；没有对应字段返回 null。 */
    public static Object read(String key) {
        Field field = key == null ? null : FIELDS.get(key);
        return field == null ? null : readField(field);
    }

    /** 按字段类型读值。 */
    private static Long readField(Field field) {
        try {
            return field.getType() == int.class ? (long) field.getInt(null) : field.getLong(null);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * 找本类里的可写 static 字段。
     *
     * <p>允许 int / long 两种类型：改类型时不用改元数据，测试会检查类型是否合法。
     */
    private static Field findField(String name) {
        try {
            Field field = EditAbleDefaultConfig.class.getDeclaredField(name);
            int mod = field.getModifiers();
            if (!Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                return null;
            }
            if (field.getType() != int.class && field.getType() != long.class) {
                return null;
            }
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
