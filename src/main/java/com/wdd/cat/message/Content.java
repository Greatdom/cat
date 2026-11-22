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
//TODO将来要实现不能让Content递归依赖的算法
public sealed interface Content {

    ContentKind kind();

//    record RelationShip(ChatKey chatKey) implements Content {
//        @Override
//        public ContentKind kind() {return ContentKind.RELATIONSHIP;}
//    }
//
//    record LongTermMemoryEntryTemplate() implements Content {
//        @Override
//        public ContentKind kind() {return ContentKind.LONG_TERM_MEMORY_ENTRY_TEMPLATE;}
//    }
//
//    record ShortTermMemoryEntryTemplate() implements Content {
//        @Override
//        public ContentKind kind(){return ContentKind.SHORT_TERM_MEMORY_ENTRY_TEMPLATE;}
//    }

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

    /** 抽出全部文本，给拼提示词用。 */
    static String plainText(Content c) {
        if (c == null) {
            return "";
        }
        if (c instanceof Text t) {
            return t.value() == null ? "" : t.value();
        }
        if (c instanceof FileRef f) {
            if (f.fileKind() == null) {
                return "[文件]";
            }
            return switch (f.fileKind()) {
                case IMAGE -> "[图片]";
                case AUDIO -> "[语音]";
                case VIDEO -> "[视频]";
                case FILE -> "[文件]";
            };
        }
        if (c instanceof Coordinate co) {
            return "[坐标: " + co.x() + ", " + co.y() + ", " + co.z() + "]";
        }
        if (c instanceof Multi m) {
            if (m.parts() == null || m.parts().isEmpty()) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (Content part : m.parts()) {
                String s = plainText(part);
                if (s.isEmpty()) {
                    continue;
                }
                if (!sb.isEmpty()) {
                    sb.append(' ');
                }
                sb.append(s);
            }
            return sb.toString();
        }
        return "";
    }
}
