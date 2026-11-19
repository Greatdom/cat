package com.wdd.cat.web.dto;


import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: description
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-19 19:12
 **/

public record HeartBeatRequest(
    List<String> sessionIds
){}
