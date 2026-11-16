package com.wdd.cat.log;


import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * &#064program: cat
 * <p>
 * &#064description: logback 附加器：把日志事件写进 {@link LogBuffer}，供 {@code /logs} 页面读取
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 20:02
 **/

public class LogBufferAppender extends AppenderBase<ILoggingEvent> {
    public static void bind(LogBuffer buffer) {
    }

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {

    }
}
