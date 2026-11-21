package com.wdd.cat.processor.pre_processor;

import com.wdd.cat.common.config.EditAbleDefaultConfig;
import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.message.ChatKey;
import com.wdd.cat.message.Content;
import com.wdd.cat.message.MessageSource;
import com.wdd.cat.message.child.InboundMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MergeProcessorTest {

    private static final long WINDOW = EditAbleDefaultConfig.mergeWindowMs;

    /** 一个「已经躺够窗口」的时间戳。 */
    private static long aged(long offset) {
        return System.currentTimeMillis() - WINDOW - 1000 + offset;
    }

    private InboundMessage msg(String text, long ts, String chatId) {
        InboundMessage m = new InboundMessage();
        m.setArchiveId(1L);
        m.setChannel(ChannelKind.EXTERNAL_QQ);
        m.setChannelMessageId("id-" + ts);
        m.setTimestamp(ts);
        m.setContent(Content.text(text));
        m.setSource(new MessageSource(
                new MessageSource.Origin(ChannelKind.EXTERNAL_QQ, "group", chatId, ""),
                new MessageSource.Sender("10002", "u", "", MessageSource.Sender.SenderKind.HUMAN)));
        return m;
    }

    @Test
    void freshMessageBuffersNothing() {
        MergeProcessor p = new MergeProcessor();
        long now = System.currentTimeMillis();
        assertThat(p.process(List.of(msg("a", now, "20001")))).isEmpty();
    }

    @Test
    void oldMessageComesOutImmediately() {
        MergeProcessor p = new MergeProcessor();
        long t = aged(0);
        List<InboundMessage> out = p.process(List.of(msg("在吗", t, "20001")));
        assertThat(out).hasSize(1);
        assertThat(out.get(0).text()).isEqualTo("在吗");
    }

    @Test
    void mergesConsecutiveIntoMulti() {
        MergeProcessor p = new MergeProcessor();
        long t = aged(0);
        List<InboundMessage> out = p.process(List.of(
                msg("在吗", t, "20001"),
                msg("在吗", t + 500, "20001"),
                msg("在不在", t + 1000, "20001")));

        assertThat(out).hasSize(1);
        InboundMessage merged = out.get(0);
        assertThat(merged.getContent()).isInstanceOf(Content.Multi.class);
        assertThat(merged.text()).isEqualTo("在吗 在吗 在不在");
        assertThat(merged.getTimestamp()).isEqualTo(t + 1000);
        assertThat(merged.getChannelMessageId()).isEqualTo("id-" + (t + 1000));
    }

    @Test
    void gapBiggerThanWindowSplits() {
        MergeProcessor p = new MergeProcessor();
        long t = aged(0);
        List<InboundMessage> out = p.process(List.of(
                msg("a", t, "20001"),
                msg("b", t + WINDOW + 5000, "20001")));

        assertThat(out).hasSize(1);
        assertThat(out.get(0).text()).isEqualTo("a");
    }

    @Test
    void differentChatsDoNotMerge() {
        MergeProcessor p = new MergeProcessor();
        long t = aged(0);
        List<InboundMessage> out = p.process(List.of(
                msg("群1", t, "20001"),
                msg("群2", t + 100, "20002")));

        assertThat(out).hasSize(2);
    }

    @Test
    void respectsMaxCount() {
        MergeProcessor p = new MergeProcessor();
        long t = aged(0);
        List<InboundMessage> in = new java.util.ArrayList<>();
        for (int i = 0; i < 12; i++) {
            in.add(msg("m" + i, t + i * 100, "20001"));
        }

        List<InboundMessage> out = p.process(in);
        assertThat(out).isNotEmpty();
        assertThat(out.get(0).getContent()).isInstanceOf(Content.Multi.class);
        assertThat(((Content.Multi) out.get(0).getContent()).parts())
                .hasSize(EditAbleDefaultConfig.mergeMaxCount);
    }

    @Test
    void chatKeyIgnoresEnabled() {
        ChatKey a = new ChatKey(ChatKey.ChatType.GROUP, "20001", ChannelKind.EXTERNAL_QQ, true);
        ChatKey b = new ChatKey(ChatKey.ChatType.GROUP, "20001", ChannelKind.EXTERNAL_QQ, false);
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
