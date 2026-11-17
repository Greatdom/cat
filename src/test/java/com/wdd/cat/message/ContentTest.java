package com.wdd.cat.message;

import com.wdd.cat.common.enums.FileKind;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ContentTest {

    @Test
    void plainTextOfText() {
        assertThat(Content.plainText(new Content.Text("你好"))).isEqualTo("你好");
        assertThat(Content.plainText(new Content.Text(null))).isEmpty();
    }

    @Test
    void plainTextOfFileRef() {
        assertThat(Content.plainText(new Content.FileRef("u", "p", "image/jpeg", FileKind.IMAGE)))
                .isEqualTo("[图片]");
        assertThat(Content.plainText(new Content.FileRef("u", "p", "audio/amr", FileKind.AUDIO)))
                .isEqualTo("[语音]");
        assertThat(Content.plainText(new Content.FileRef("u", "p", "video/mp4", FileKind.VIDEO)))
                .isEqualTo("[视频]");
        assertThat(Content.plainText(new Content.FileRef("u", "p", "application/pdf", FileKind.FILE)))
                .isEqualTo("[文件]");
    }

    @Test
    void plainTextOfCoordinate() {
        assertThat(Content.plainText(new Content.Coordinate(3, 2, 1)))
                .isEqualTo("[坐标: 3.0, 2.0, 1.0]");
    }

    @Test
    void plainTextOfMulti() {
        Content c = new Content.Multi(List.of(
                new Content.Text("看图"),
                new Content.FileRef("u", "p", "image/jpeg", FileKind.IMAGE)
        ));
        assertThat(Content.plainText(c)).isEqualTo("看图 [图片]");
    }

    @Test
    void plainTextOfNestedMulti() {
        Content inner = new Content.Multi(List.of(new Content.Text("a"), new Content.Text("b")));
        Content outer = new Content.Multi(List.of(new Content.Text("开头"), inner));
        assertThat(Content.plainText(outer)).isEqualTo("开头 a b");
    }

    @Test
    void plainTextSkipsEmptyParts() {
        Content c = new Content.Multi(List.of(new Content.Text(""), new Content.Text("x")));
        assertThat(Content.plainText(c)).isEqualTo("x");
    }

    @Test
    void plainTextOfNullAndEmpty() {
        assertThat(Content.plainText(null)).isEmpty();
        assertThat(Content.plainText(new Content.Multi(List.of()))).isEmpty();
    }

    @Test
    void convenienceFactories() {
        assertThat(Content.text("hi")).isEqualTo(new Content.Text("hi"));
        assertThat(Content.image("u", "p"))
                .isEqualTo(new Content.FileRef("u", "p", "image/*", FileKind.IMAGE));
    }
}
