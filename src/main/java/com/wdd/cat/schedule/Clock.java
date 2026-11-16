package com.wdd.cat.schedule;


/**
 * &#064program: cat
 * <p>
 * &#064description: 现实/存档时钟的抽象表示;所有业务逻辑只读它;
 * 不直接读 System.currentTimeMillis()
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 15:47
 **/

public interface Clock {
    /** 存档时间（秒）。业务唯一时钟。 */
    long nowEpochSecond();
}
