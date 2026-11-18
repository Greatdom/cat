package com.wdd.cat.message;

import com.wdd.cat.common.enums.ChannelKind;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatKeyTest {

    private static ChatKey group(String id) {
        return new ChatKey(ChatKey.ChatType.GROUP, id, ChannelKind.EXTERNAL_QQ, true);
    }

    @Test
    void parseAndWireRoundTrip() {
        assertThat(ChatKey.parse("external_qq:group:20001").wire())
                .isEqualTo("external_qq:group:20001");
        assertThat(ChatKey.parse("external_qq:private:10002").wire())
                .isEqualTo("external_qq:private:10002");
    }

    @Test
    void parseIsCaseInsensitive() {
        assertThat(ChatKey.parse("EXTERNAL_QQ:GROUP:20001").type())
                .isEqualTo(ChatKey.ChatType.GROUP);
        assertThat(ChatKey.parse("External_QQ:Private:1").type())
                .isEqualTo(ChatKey.ChatType.PRIVATE);
    }

    @Test
    void parseKeepsChannel() {
        assertThat(ChatKey.parse("external_qq:group:20001").channelKind())
                .isEqualTo(ChannelKind.EXTERNAL_QQ);
    }

    @Test
    void groupFlag() {
        assertThat(group("1").isGroup()).isTrue();
        assertThat(new ChatKey(ChatKey.ChatType.PRIVATE, "1", ChannelKind.EXTERNAL_QQ, true)
                .isGroup()).isFalse();
    }

    @Test
    void enabledTreatsNullAsFalse() {
        assertThat(new ChatKey(ChatKey.ChatType.GROUP, "1", ChannelKind.EXTERNAL_QQ, null)
                .isEnabled()).isFalse();
        assertThat(group("1").isEnabled()).isTrue();
    }

    @Test
    void rejectsNonExternalChannel() {
        assertThatThrownBy(() ->
                new ChatKey(ChatKey.ChatType.GROUP, "1", ChannelKind.INTERNAL_DATE, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("外部渠道");
    }

    @Test
    void rejectsMalformed() {
        assertThatThrownBy(() -> ChatKey.parse(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ChatKey.parse("group:20001")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ChatKey.parse("external_qq:group")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ChatKey.parse("external_qq:group:")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ChatKey.parse("unknown:group:1")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ChatKey.parse("external_qq:unknown:1")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ChatKey.parse("external_qq:group:abc")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ChatKey.parse("internal_date:group:1")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankId() {
        assertThatThrownBy(() -> new ChatKey(ChatKey.ChatType.GROUP, " ", ChannelKind.EXTERNAL_QQ, true))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
