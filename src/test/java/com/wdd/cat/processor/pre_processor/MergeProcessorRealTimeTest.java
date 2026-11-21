package com.wdd.cat.processor.pre_processor;

import com.wdd.cat.common.config.EditAbleDefaultConfig;
import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.message.Content;
import com.wdd.cat.message.MessageSource;
import com.wdd.cat.message.child.InboundMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 用真实时间戳验证合并行为。 */
class MergeProcessorRealTimeTest {

    private InboundMessage msg(String text, long ts) {
        InboundMessage m = new InboundMessage();
        m.setArchiveId(1L);
        m.setChannel(ChannelKind.EXTERNAL_QQ);
        m.setChannelMessageId("id-" + ts);
        m.setTimestamp(ts);
        m.setContent(Content.text(text));
        m.setSource(new MessageSource(
                new MessageSource.Origin(ChannelKind.EXTERNAL_QQ, "group", "20001", ""),
                new MessageSource.Sender("10002", "u", "", MessageSource.Sender.SenderKind.HUMAN)));
        return m;
    }

    @Test
    void freshMessagesDoNotEmerge() {
        MergeProcessor p = new MergeProcessor();
        long now = System.currentTimeMillis();
        assertThat(p.process(List.of(msg("a", now)))).isEmpty();
        assertThat(p.process(List.of())).isEmpty();
    }

    @Test
    void messagesOlderThanWindowEmerge() {
        MergeProcessor p = new MergeProcessor();
        long old = System.currentTimeMillis() - EditAbleDefaultConfig.mergeWindowMs - 2000;
        assertThat(p.process(List.of(msg("很久以前", old)))).hasSize(1);
    }

    @Test
    void mergedContentIsMulti() {
        MergeProcessor p = new MergeProcessor();
        long old = System.currentTimeMillis() - EditAbleDefaultConfig.mergeWindowMs - 2000;
        List<InboundMessage> out = p.process(List.of(
                msg("在吗", old),
                msg("在吗", old + 300),
                msg("在不在", old + 600)));
        assertThat(out).hasSize(1);
        assertThat(out.get(0).text()).isEqualTo("在吗 在吗 在不在");
    }
}
