package com.wdd.cat.log;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.MDC;

/**
 * &#064program: cat
 * <p>
 * &#064description: logback 附加器：把日志事件写进 {@link LogBuffer}，供 {@code /logs} 页面读取
 * <p>
 * 为什么挂在附加器上而不是各处手动 add：手动要求每个日志点都记得加一行，
 * 漏了就是「页面看不到」的盲区。挂在 logback 上则自动收录，用一个过滤器把范围
 * 收窄到本服务就够了。
 * <p>
 * ⚠ 这个类由 logback 通过 logback-spring.xml 的 &lt;appender class="..."&gt; 实例化，
 * 不是 Spring Bean，所以不能构造注入。靠静态 setter 拿缓冲实例。
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 20:02
 **/

public class LogBufferAppender extends AppenderBase<ILoggingEvent> {

    /** 只收本服务包的日志。 */
    private static final String ROOT_PACKAGE = "com.wdd.cat";

    /** Spring 启动后注入的缓冲实例。 */
    private static volatile LogBuffer buffer;

    /** 由 {@link LogBufferHolder} 在容器就绪后调用。 */
    public static void bind(LogBuffer target) {
        buffer = target;
    }

    @Override
    protected void append(ILoggingEvent event) {
        LogBuffer target = buffer;
        if (target == null || event == null) {
            return;   // 容器还没起来，先丢掉
        }
        String loggerName = event.getLoggerName();
        if (loggerName == null || !loggerName.startsWith(ROOT_PACKAGE)) {
            return;
        }

        String chatKey = null;
        String itemId = null;
        try {
            chatKey = MDC.get("chatKey");
            itemId = MDC.get("itemId");
        } catch (RuntimeException e) {
            // MDC 不可用不影响收录
        }

        String text = event.getFormattedMessage();
        IThrowableProxy thrown = event.getThrowableProxy();
        if (thrown != null) {
            text = text + " | " + thrown.getClassName() + ": " + thrown.getMessage();
        }

        target.add(new LogBuffer.Entry(
                event.getTimeStamp(),
                levelOf(event.getLevel()),
                chatKey,
                itemId,
                shortName(loggerName),
                text));
    }

    private static String levelOf(Level level) {
        if (level == null) {
            return "INFO";
        }
        return switch (level.toInt()) {
            case Level.ERROR_INT -> "ERROR";
            case Level.WARN_INT -> "WARN";
            case Level.INFO_INT -> "INFO";
            case Level.DEBUG_INT -> "DEBUG";
            default -> "TRACE";
        };
    }

    private static String shortName(String loggerName) {
        int dot = loggerName.lastIndexOf('.');
        return dot < 0 ? loggerName : loggerName.substring(dot + 1);
    }
}
