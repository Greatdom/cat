package com.wdd.cat.exception;

/**
 * &#064program: cat
 * &#064description: OneBot 调用失败
 * &#064author: black-cat
 * &#064create: 2026-11-18 00:20
 **/
public class OneBotException extends RuntimeException {

    private final String action;

    public OneBotException(String action, String message) {
        super("[" + action + "] " + message);
        this.action = action;
    }

    public OneBotException(String action, String message, Throwable cause) {
        super("[" + action + "] " + message, cause);
        this.action = action;
    }

    public String action() {
        return action;
    }
}
