package com.wdd.cat.web.dto;

import java.util.List;

/**
 * &#064program: cat
 * &#064description: GET /api/inbound_message/get 的响应
 * &#064author: black-cat
 * &#064create: 2026-11-19 12:05
 **/
public record InboundGetResponse(
        List<InboundMessageDto> messages
) {}
