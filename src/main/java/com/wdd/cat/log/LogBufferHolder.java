package com.wdd.cat.log;


import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * &#064program: cat
 * <p>
 * &#064description: 把 Spring 容器里的 {@link LogBuffer} 接到 logback 附加器上
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 20:05
 **/

@Component
public class LogBufferHolder {

    private final LogBuffer buffer;

    public LogBufferHolder(LogBuffer buffer) {
        this.buffer = buffer;
    }

    @PostConstruct
    void bind() {
        LogBufferAppender.bind(buffer);
    }
}
