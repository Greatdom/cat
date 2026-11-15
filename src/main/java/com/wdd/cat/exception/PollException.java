package com.wdd.cat.exception;


/**
 * &#064program: cat
 * <p>
 * &#064description: 拉取失败的异常;用 checked 异常是为了逼调用方处理：拉失败≠没消息，不能静默吞掉
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 21:45
 **/

public class PollException extends Exception {

    public PollException(String message) {
        super(message);
    }

    public PollException(String message, Throwable cause) {
        super(message, cause);
    }
}