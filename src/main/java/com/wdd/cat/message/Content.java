package com.wdd.cat.message;


/**
 * &#064program: cat
 * <p>
 * &#064description: 消息里的真正内容
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 21:13
 **/

import com.wdd.cat.common.enums.ContentKind;
import com.wdd.cat.common.enums.FileKind;

import java.util.List;
//TODO将来可以拓展语音等Content实现
public sealed interface Content {

    ContentKind kind();

    /** 纯文本 */
    record Text(String value) implements Content {
        public ContentKind kind() { return ContentKind.TEXT; }
    }

    /** 文件引用（图片/语音/视频/文档通用）★ 合并了原 ImageRef + InboundImage */
    record FileRef(
            String url,
            String path,        // 本地路径（下载后）
            String mime,
            FileKind fileKind
    ) implements Content {
        public ContentKind kind() { return ContentKind.FILE; }
    }

    /** 坐标（ESP32 上报位置） */
    record Coordinate(double x, double y, double z) implements Content {
        public ContentKind kind() { return ContentKind.COORDINATE; }
    }

    /** 多段组合 */
    record Multi(List<Content> parts) implements Content {
        public ContentKind kind() { return ContentKind.MULTI; }
    }

    // ── 便利方法 ──
    static Content text(String s) { return new Text(s); }
    static Content image(String url, String path) {
        return new FileRef(url, path, "image/*", FileKind.IMAGE);
    }
    //TODO应该有更多便利方法

    /** 抽出全部文本,把任意 Content 压缩成一个纯字符串（给 PromptComposer 用）。 */
    static String plainText(Content c){
        return null;
        //TODO
    }
}
