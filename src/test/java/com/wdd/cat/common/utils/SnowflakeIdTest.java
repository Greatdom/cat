package com.wdd.cat.common.utils;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SnowflakeIdTest {

    @Test
    void idsAreUnique() {
        SnowflakeId gen = new SnowflakeId();
        Set<Long> seen = new HashSet<>();
        for (int i = 0; i < 10_000; i++) {
            assertThat(seen.add(gen.next())).as("第 %d 个 id 重复", i).isTrue();
        }
    }

    @Test
    void idsAreMonotonic() {
        SnowflakeId gen = new SnowflakeId();
        long prev = gen.next();
        for (int i = 0; i < 1_000; i++) {
            long cur = gen.next();
            assertThat(cur).isGreaterThan(prev);
            prev = cur;
        }
    }

    @Test
    void nextStringParsesBack() {
        SnowflakeId gen = new SnowflakeId();
        String s = gen.nextString();
        assertThat(Long.parseLong(s)).isPositive();
        assertThat(s.length()).isLessThan(20);
    }

    @Test
    void rejectsOutOfRangeIds() {
        assertThatThrownBy(() -> new SnowflakeId(-1, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SnowflakeId(0, 32)).isInstanceOf(IllegalArgumentException.class);
    }
}
