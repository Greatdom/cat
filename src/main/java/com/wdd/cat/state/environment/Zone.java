package com.wdd.cat.state.environment;
import java.util.Map;
import java.util.Set;


/**
 * &#064program: cat
 * <p>
 * &#064description: 一个区域：空间里的一片格子集合（卧室、客厅、书房）
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:34
 **/


public record Zone(
        String name,                    // "卧室"
        Set<Cell> cells,                // 这块区域占哪些格子
        Map<String, Double> ambience    // 这个区域默认的环境量，覆盖 SpaceGrid
) {}