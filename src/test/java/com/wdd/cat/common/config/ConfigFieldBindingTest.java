package com.wdd.cat.common.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 配置项与字段的绑定检查。
 *
 * <p>{@code EditAbleDefaultConfig} 靠反射按名字写字段，编译器管不到「ITEMS 里写了个不存在的键」。
 * 这个测试把那道检查补回来：每个配置项都必须有对应的 static volatile int/long 字段。
 */
class ConfigFieldBindingTest {

    /** 测试会改字段值，跑完恢复出厂默认，免得影响别的用例。 */
    @AfterEach
    void restoreDefaults() {
        EditAbleDefaultConfig.defaults()
                .forEach((key, value) ->
                        EditAbleDefaultConfig.write(key, ((Number) value).longValue()));
    }

    @Test
    void everyItemHasAField() {
        for (EditAbleDefaultConfig.Item item : EditAbleDefaultConfig.ITEMS) {
            Field field;
            try {
                field = EditAbleDefaultConfig.class.getDeclaredField(item.key());
            } catch (NoSuchFieldException e) {
                throw new AssertionError("配置项 " + item.key() + " 没有同名字段", e);
            }
            assertThat(Modifier.isStatic(field.getModifiers()))
                    .as("%s 必须是 static 字段", item.key()).isTrue();
            assertThat(Modifier.isFinal(field.getModifiers()))
                    .as("%s 不能是 final，否则运行时改不了", item.key()).isFalse();
            assertThat(field.getType())
                    .as("%s 必须是 int 或 long", item.key())
                    .isIn(int.class, long.class);
        }
    }

    @Test
    void everyFieldHasAnItem() {
        Set<String> keys = new HashSet<>(EditAbleDefaultConfig.ALL);
        for (Field field : EditAbleDefaultConfig.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            if (field.getType() != int.class && field.getType() != long.class) {
                continue;
            }
            assertThat(keys)
                    .as("字段 %s 没在 ITEMS 里登记，设置页永远看不到它", field.getName())
                    .contains(field.getName());
        }
    }

    @Test
    void groupsCoverAllItemsExactlyOnce() {
        Set<String> inGroups = new HashSet<>();
        for (EditAbleDefaultConfig.Group group : EditAbleDefaultConfig.GROUPS) {
            for (String key : group.keys()) {
                assertThat(inGroups.add(key)).as("键 %s 在分组里出现了两次", key).isTrue();
                assertThat(EditAbleDefaultConfig.ALL).as("分组里的 %s 不在 ITEMS 里", key).contains(key);
            }
        }
        assertThat(inGroups).as("有配置项没归进任何分组").containsExactlyInAnyOrderElementsOf(
                EditAbleDefaultConfig.ALL);
    }

    @Test
    void itemRangesAreSane() {
        for (EditAbleDefaultConfig.Item item : EditAbleDefaultConfig.ITEMS) {
            assertThat(item.min()).as("%s 的 min 不能大于 max", item.key()).isLessThanOrEqualTo(item.max());
            Object defaultValue = EditAbleDefaultConfig.defaults().get(item.key());
            assertThat(defaultValue).as("%s 必须有默认值", item.key()).isNotNull();
            long v = ((Number) defaultValue).longValue();
            assertThat(v).as("%s 的默认值 %d 超出区间 [%d, %d]", item.key(), v, item.min(), item.max())
                    .isBetween(item.min(), item.max());
        }
    }

    @Test
    void writeAndReadRoundTrip() {
        for (EditAbleDefaultConfig.Item item : EditAbleDefaultConfig.ITEMS) {
            long target = item.min();
            assertThat(EditAbleDefaultConfig.write(item.key(), target))
                    .as("写入 %s 应该成功", item.key()).isTrue();
            assertThat(EditAbleDefaultConfig.read(item.key()))
                    .as("读回 %s 应该等于写入值", item.key())
                    .isEqualTo(target);
        }
    }

    @Test
    void writeUnknownKeyIsIgnored() {
        assertThat(EditAbleDefaultConfig.write("noSuchKey", 1)).isFalse();
        assertThat(EditAbleDefaultConfig.write(null, 1)).isFalse();
        assertThat(EditAbleDefaultConfig.read("noSuchKey")).isNull();
    }

    @Test
    void defaultsDoNotChangeWhenFieldChanges() {
        Map<String, Object> before = EditAbleDefaultConfig.defaults();
        Object original = before.get("mergeMaxCount");

        EditAbleDefaultConfig.write("mergeMaxCount", 42);
        assertThat(EditAbleDefaultConfig.read("mergeMaxCount")).isEqualTo(42L);
        assertThat(EditAbleDefaultConfig.defaults().get("mergeMaxCount"))
                .as("改动运行时值不能污染出厂默认值").isEqualTo(original);
    }
}
