package com.wdd.cat.state.environment;


/**
 * &#064program: cat
 * <p>
 * &#064description: 环境系统
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:32
 **/

public interface EnvironmentSystem {
    EnvironmentState current();
    void update(EnvironmentUpdate update);
    String kind();
}