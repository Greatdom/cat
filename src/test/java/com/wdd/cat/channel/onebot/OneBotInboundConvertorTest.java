package com.wdd.cat.channel.onebot;

import com.wdd.cat.channel.ConvertContext;
import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.common.utils.JsonCodec;
import com.wdd.cat.message.Content;
import com.wdd.cat.message.child.NormalMessage;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** OneBot 事件 → NormalMessage 的转换。 */
class OneBotInboundConvertorTest {

    private final JsonCodec json = new JsonCodec();
    private final OneBotInboundConvertor convertor = new OneBotInboundConvertor();

    private ConvertContext ctx(String self) {
        return new ConvertContext(1L, ChannelKind.EXTERNAL_QQ, self,
                Set.of("external_qq:10000", "external_qq:10004"));
    }

    private JsonNode event(String raw) {
        return json.read(raw);
    }

    @Test
    void convertsGroupTextMessage() {
        JsonNode e = event("""
                {"message_id":"7","time":1794932985,"user_id":10002,
                 "sender":{"user_id":10002,"nickname":"用户10002","card":"小明明"},
                 "message":[{"type":"text","data":{"text":"今天好累啊"}}],
                 "group_id":20001}
                """);

        List<NormalMessage> out = convertor.convert(List.of(e), ctx("10000"));

        assertThat(out).hasSize(1);
        NormalMessage m = out.get(0);
        assertThat(m.text()).isEqualTo("今天好累啊");
        assertThat(m.chatType()).isEqualTo("group");
        assertThat(m.chatId()).isEqualTo("20001");
        assertThat(m.senderId()).isEqualTo("10002");
        assertThat(m.getChannelMessageId()).isEqualTo("7");
        assertThat(m.getTimestamp()).isEqualTo(1794932985_000L);
        assertThat(m.getChannel()).isEqualTo(ChannelKind.EXTERNAL_QQ);
    }

    @Test
    void dropsOwnMessages() {
        JsonNode e = event("""
                {"message_id":"1","time":1,"user_id":10000,
                 "sender":{"user_id":10000,"nickname":"机器人10000","card":""},
                 "message":[{"type":"text","data":{"text":"你好"}}],
                 "group_id":20001}
                """);
        assertThat(convertor.convert(List.of(e), ctx("10000"))).isEmpty();
    }

    @Test
    void dropsOtherArchivesMessages() {
        JsonNode e = event("""
                {"message_id":"2","time":1,"user_id":10004,
                 "sender":{"user_id":10004,"nickname":"机器人10004","card":""},
                 "message":[{"type":"text","data":{"text":"呀"}}],
                 "group_id":20001}
                """);
        assertThat(convertor.convert(List.of(e), ctx("10000"))).isEmpty();
    }

    @Test
    void convertsPrivateMessage() {
        JsonNode e = event("""
                {"message_id":"3","time":1,"user_id":10002,
                 "sender":{"user_id":10002,"nickname":"用户10002","card":""},
                 "message":[{"type":"text","data":{"text":"在吗"}}]}
                """);
        List<NormalMessage> out = convertor.convert(List.of(e), ctx("10000"));
        assertThat(out).hasSize(1);
        assertThat(out.get(0).chatType()).isEqualTo("private");
        assertThat(out.get(0).chatId()).isEqualTo("10002");
    }

    @Test
    void textAndImageBecomeMulti() {
        JsonNode e = event("""
                {"message_id":"4","time":1,"user_id":10002,
                 "sender":{"user_id":10002,"nickname":"u","card":""},
                 "message":[{"type":"text","data":{"text":"看图"}},
                            {"type":"image","data":{"url":"http://x/a.jpg","file":"a.jpg"}}],
                 "group_id":20001}
                """);
        List<NormalMessage> out = convertor.convert(List.of(e), ctx("10000"));
        assertThat(out).hasSize(1);
        Content c = out.get(0).getContent();
        assertThat(c).isInstanceOf(Content.Multi.class);
        assertThat(Content.plainText(c)).isEqualTo("看图 [图片]");
    }

    @Test
    void emptyMessageArrayIsDropped() {
        JsonNode e = event("""
                {"message_id":"5","time":1,"user_id":10002,
                 "sender":{"user_id":10002,"nickname":"u","card":""},
                 "message":[],"group_id":20001}
                """);
        assertThat(convertor.convert(List.of(e), ctx("10000"))).isEmpty();
    }
}
