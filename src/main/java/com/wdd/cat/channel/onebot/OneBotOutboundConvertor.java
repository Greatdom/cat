package com.wdd.cat.channel.onebot;

import com.wdd.cat.channel.OutboundConvertor;
import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.common.enums.FileKind;
import com.wdd.cat.message.Content;
import com.wdd.cat.message.child.NormalMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * &#064program: cat
 * &#064description: NormalMessage → OneBot 消息段数组
 * &#064author: black-cat
 * &#064create: 2026-11-15 22:30
 **/
@Component
public class OneBotOutboundConvertor implements OutboundConvertor<Map<String, Object>> {

    @Override
    public ChannelKind kind() {
        return ChannelKind.EXTERNAL_QQ;
    }

    @Override
    public List<Map<String, Object>> convert(NormalMessage normalMessage) {
        return segments(normalMessage == null ? null : normalMessage.getContent());
    }

    private List<Map<String, Object>> segments(Content content) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (content == null) {
            return out;
        }
        if (content instanceof Content.Text t) {
            if (t.value() != null && !t.value().isEmpty()) {
                out.add(text(t.value()));
            }
            return out;
        }
        if (content instanceof Content.FileRef f) {
            if (f.fileKind() == FileKind.IMAGE && f.path() != null && !f.path().isEmpty()) {
                out.add(image(f.path()));
            }
            return out;
        }
        if (content instanceof Content.Multi m) {
            if (m.parts() != null) {
                for (Content part : m.parts()) {
                    out.addAll(segments(part));
                }
            }
            return out;
        }
        // Coordinate 等其余类型暂不发送
        return out;
    }

    private static Map<String, Object> text(String value) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("text", value);
        Map<String, Object> seg = new LinkedHashMap<>();
        seg.put("type", "text");
        seg.put("data", data);
        return seg;
    }

    /** 图片读本地文件转 base64；跳板和 Java 不一定同机，不能传路径。 */
    private static Map<String, Object> image(String path) {
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(java.nio.file.Path.of(path));
            data.put("file", "base64://" + Base64.getEncoder().encodeToString(bytes));
        } catch (Exception e) {
            data.put("file", path);
        }
        Map<String, Object> seg = new LinkedHashMap<>();
        seg.put("type", "image");
        seg.put("data", data);
        return seg;
    }
}
