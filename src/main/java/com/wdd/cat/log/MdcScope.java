package com.wdd.cat.log;

import org.slf4j.MDC;

/**
 * &#064program: cat
 * <p>
 * &#064description: MDC 作用域 —— 给日志打上「这条属于哪个会话 / 哪条消息」的标签
 * <p>
 * 为什么需要：一条消息从「拉取 → 去重合并 → 入队 → 投递 → 发送」要经过好几个类，
 * 日志散落在各处。没有共同标识，只能靠时间戳猜，猜不准就查不出因果链。
 * 打上 {@code chatKey} / {@code itemId} 之后，{@code /logs} 页就能按这两个字段筛出同一条消息的全过程。
 * <p>
 * <b>必须配合 try-with-resources 使用</b>，否则标签会残留到下一个任务：
 * <pre>
 *   try (MdcScope ignored = MdcScope.of(chatKey, channelMessageId)) {
 *       log.info("...");
 *   }
 * </pre>
 * 原因是三个 Service 都跑在 {@link com.wdd.cat.common.utils.CatScheduler} 的线程池上，
 * 同一个线程会被反复复用；不恢复原值，上一条消息的标签就会挂到下一条消息的日志上。
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-21 12:25
 **/
public final class MdcScope implements AutoCloseable {

    /** 会话键，形如 "external_qq:group:20001"。 */
    public static final String KEY_CHAT = "chatKey";

    /** 消息号，用渠道侧的消息 id。 */
    public static final String KEY_ITEM = "itemId";

    /** 进作用域之前的值，close 时恢复（可能是 null，表示原本没有）。 */
    private final String previousChat;
    private final String previousItem;

    private MdcScope(String chatKey, String itemId) {
        this.previousChat = read(KEY_CHAT);
        this.previousItem = read(KEY_ITEM);
        write(KEY_CHAT, chatKey);
        write(KEY_ITEM, itemId);
    }

    /**
     * 同时打上会话和消息两个标签。
     *
     * @param chatKey 会话键，可空（空则该字段不设置）
     * @param itemId  消息号，可空
     */
    public static MdcScope of(String chatKey, String itemId) {
        return new MdcScope(chatKey, itemId);
    }

    /** 只打会话标签（合并、入队这类「围绕一个会话」的阶段用）。 */
    public static MdcScope chat(String chatKey) {
        return new MdcScope(chatKey, null);
    }

    /** 只打消息标签（出站发送这类「围绕一条消息」的阶段用）。 */
    public static MdcScope item(String itemId) {
        return new MdcScope(null, itemId);
    }

    /**
     * 不设置任何标签，只在 close 时把当前值清掉。
     * <p>用于「提前退出」的分支：配合 try-with-resources 保证出口只有一处。
     */
    public static MdcScope clearing() {
        return new MdcScope(null, null);
    }

    /** 读当前值；MDC 不可用时返回 null，不影响业务。 */
    private static String read(String key) {
        try {
            return MDC.get(key);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** 写值；空串等同于「没有」，用 remove 而不是塞空串。 */
    private static void write(String key, String value) {
        try {
            if (value == null || value.isBlank()) {
                MDC.remove(key);
            } else {
                MDC.put(key, value);
            }
        } catch (RuntimeException e) {
            // MDC 底层是 ThreadLocal，个别容器下可能不可用；日志打不打标签不该影响业务
        }
    }

    @Override
    public void close() {
        write(KEY_CHAT, previousChat);
        write(KEY_ITEM, previousItem);
    }
}
