package com.wdd.cat.state.environment;


import java.util.Map;

/**
 * &#064program: cat
 * <p>
 * &#064description: 一张三维格子地图
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:34
 **/

public record SpaceGrid(
        String id,                      // 唯一 id
        String name,                    // "黑猫的家"
        double cellSize,                // 一格多少米
        int sizeX, int sizeY, int sizeZ,// 空间尺寸（格子数）
        Map<Cell, Terrain> terrain      // 每格的地形，只记特殊格
) {}