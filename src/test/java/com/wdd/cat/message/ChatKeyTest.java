package com.wdd.cat.message;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatKeyTest {

    @Test
    void parseAndToStringRoundTrip() {
        assertThat(ChatKey.parse("group:20001").toString()).isEqualTo("group:20001");
        assertThat(ChatKey.parse("private:10002").toString()).isEqualTo("private:10002");
    }

    @Test
    void parseIsCaseInsensitive() {
        assertThat(ChatKey.parse("GROUP:20001").type()).isEqualTo(ChatKey.ChatType.GROUP);
        assertThat(ChatKey.parse("Private:1").type()).isEqualTo(ChatKey.ChatType.PRIVATE);
    }

    @Test
    void groupFlag() {
        assertThat(ChatKey.parse("group:1").isGroup()).isTrue();
        assertThat(ChatKey.parse("private:1").isGroup()).isFalse();
    }

    @Test
    void rejectsMalformed() {
        assertThatThrownBy(() -> ChatKey.parse(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ChatKey.parse("group20001")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ChatKey.parse("group:")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ChatKey.parse(":20001")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ChatKey.parse("unknown:20001")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ChatKey.parse("group:abc")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankId() {
        assertThatThrownBy(() -> new ChatKey(ChatKey.ChatType.GROUP, " "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
