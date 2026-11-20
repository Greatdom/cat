package com.wdd.cat.common.cache;

import com.wdd.cat.common.config.UnEditAbleDefaultConfig;
import com.wdd.cat.common.utils.JsonCodec;
import com.wdd.cat.message.ChatKey;
import com.wdd.cat.message.child.InboundMessage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * &#064program: cat
 * &#064description: 消息投递状态 + 各会话水位，决定「哪些消息算新的」
 * <p>
 * 两道防线：按 archiveId:msgId 记投递状态，按 archiveId:chatKey 记时间水位。
 * only 跳板 ack 之后消息才转 DELIVERED。
 * &#064author: black-cat
 * &#064create: 2026-11-15 23:10
 **/
@Component
public class DeliveryState {

    private static final Logger log = LoggerFactory.getLogger(DeliveryState.class);

    /** 投递状态。 */
    public enum Status {
        /** 刚拉回来，没进过链路。 */
        NEW,
        /** 正在链路里；超时未推进会回到 NEW。 */
        PROCESSING,
        /** 已经送到跳板。 */
        DELIVERED
    }

    /** 一条记录：状态 + 更新时间。 */
    public record Entry(Status status, long updatedAt) {}

    private static final String FILE_NAME = "watermarks.json";

    /** archiveId:channelMessageId → Entry。 */
    private final Map<String, Entry> deliveries = new ConcurrentHashMap<>();

    /** archiveId:chatKey → 最大时间戳（秒）。 */
    private final Map<String, Long> watermarks = new ConcurrentHashMap<>();

    private final JsonCodec json;

    public DeliveryState(JsonCodec json) {
        this.json = json;
    }

    // ── 键 ──

    /** 投递状态的键。 */
    public static String deliveryKey(long archiveId, String channelMessageId) {
        return archiveId + ":" + channelMessageId;
    }

    /** 水位的键。 */
    public static String watermarkKey(long archiveId, ChatKey chat) {
        return archiveId + ":" + chat.wire();
    }

    // ── 查询 ──

    /** 当前状态；没见过返回 null。 */
    public Entry get(long archiveId, String channelMessageId) {
        if (channelMessageId == null || channelMessageId.isBlank()) {
            return null;
        }
        return deliveries.get(deliveryKey(archiveId, channelMessageId));
    }

    /** 已投递。 */
    public boolean isDelivered(long archiveId, String channelMessageId) {
        Entry e = get(archiveId, channelMessageId);
        return e != null && e.status() == Status.DELIVERED;
    }

    /** 正在处理且未超时。 */
    public boolean isProcessingAlive(long archiveId, String channelMessageId, long now) {
        Entry e = get(archiveId, channelMessageId);
        return e != null && e.status() == Status.PROCESSING
                && now - e.updatedAt() < UnEditAbleDefaultConfig.processingTimeoutMs();
    }

    /**
     * 该不该收：已投递不要，正在处理且未超时不要，其余都收。
     * <p>收的会同时被标记成 PROCESSING。
     */
    public boolean tryAccept(InboundMessage msg) {
        String id = msg.getChannelMessageId();
        if (id == null || id.isBlank()) {
            // 没有消息号就无法去重，只能放行
            return true;
        }
        long archiveId = msg.getArchiveId();
        long now = System.currentTimeMillis();

        if (isDelivered(archiveId, id)) {
            return false;
        }
        if (isProcessingAlive(archiveId, id, now)) {
            return false;
        }
        deliveries.put(deliveryKey(archiveId, id), new Entry(Status.PROCESSING, now));
        return true;
    }

    // ── 水位 ──

    /** 当前水位；没见过的会话返回 0。 */
    public long watermark(long archiveId, ChatKey chat) {
        if (chat == null) {
            return 0L;
        }
        return watermarks.getOrDefault(watermarkKey(archiveId, chat), 0L);
    }

    /**
     * 取水位；第一次见到某个会话时初始化为「此刻」，避免补投历史。
     *
     * @param nowSeconds 当前存档时间（秒）
     */
    public long watermarkOrInit(long archiveId, ChatKey chat, long nowSeconds) {
        if (chat == null) {
            return 0L;
        }
        String key = watermarkKey(archiveId, chat);
        Long existing = watermarks.putIfAbsent(key, nowSeconds);
        return existing != null ? existing : nowSeconds;
    }

    /** 推进水位，只增不减。 */
    public void advanceWatermark(long archiveId, ChatKey chat, long timeSeconds) {
        if (chat == null || timeSeconds <= 0) {
            return;
        }
        watermarks.merge(watermarkKey(archiveId, chat), timeSeconds, Math::max);
    }

    // ── 状态推进 ──

    /** 标记为已投递,也就是说如果上一条消息没处理完却丢失了,恰好这一条消息处理完了,上一条消息就不再允许被处理了*/
    public void markDelivered(long archiveId, String channelMessageId, long timeSeconds, ChatKey chat) {
        if (channelMessageId == null || channelMessageId.isBlank()) {
            return;
        }
        deliveries.put(deliveryKey(archiveId, channelMessageId),
                new Entry(Status.DELIVERED, System.currentTimeMillis()));
        advanceWatermark(archiveId, chat, timeSeconds);
        save();
    }

    /** 存档被删时清掉它的记录。 */
    public void forget(long archiveId) {
        String prefix = archiveId + ":";
        deliveries.keySet().removeIf(k -> k.startsWith(prefix));
        watermarks.keySet().removeIf(k -> k.startsWith(prefix));
        save();
    }

    // ── 落盘 ──

    @PostConstruct
    void load() {
        Path file = file();
        if (!Files.isReadable(file)) {
            return;
        }
        try {
            Snapshot s = json.read(Files.readString(file, StandardCharsets.UTF_8), Snapshot.class);
            if (s != null && s.deliveries() != null) {
                deliveries.putAll(s.deliveries());
            }
            if (s != null && s.watermarks() != null) {
                watermarks.putAll(s.watermarks());
            }
            log.info("已恢复水位状态：{} 条投递记录，{} 个会话水位", deliveries.size(), watermarks.size());
        } catch (Exception e) {
            log.warn("水位文件读不出来，从头开始：{}", e.getMessage());
        }
    }

    /** 落盘。失败只记日志，不阻断流转。 */
    public synchronized void save() {
        Path file = file();
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            Snapshot s = new Snapshot(1,
                    new LinkedHashMap<>(deliveries),
                    new HashMap<>(watermarks));
            Path tmp = file.resolveSibling(FILE_NAME + ".tmp");
            Files.writeString(tmp, json.writePretty(s), StandardCharsets.UTF_8);
            moveAtomically(tmp, file);
        } catch (IOException | RuntimeException e) {
            log.warn("水位落盘失败：{}", e.getMessage());
        }
    }

    private static void moveAtomically(Path from, Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Path file() {
        return Path.of(UnEditAbleDefaultConfig.DATA_DIR).resolve(FILE_NAME);
    }

    /** 落盘形状。 */
    private record Snapshot(int version, Map<String, Entry> deliveries, Map<String, Long> watermarks) {}
}
