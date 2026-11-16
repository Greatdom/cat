package com.wdd.cat.state.need;


import com.wdd.cat.common.enums.ChannelKind;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 每个渠道声明自己能做什么
 * 加一个新渠道（微信/ESP32），只要实现这个接口，
 * 能力表就自动多出对应的几条 Capability
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 17:45
 **/

public interface CapabilityProvider {
    ChannelKind kind();
    List<Capability> capabilities();
}