package com.wdd.cat.state.environment;


import com.wdd.cat.common.enums.EnvironmentKind;

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
    EnvironmentKind kind();
}