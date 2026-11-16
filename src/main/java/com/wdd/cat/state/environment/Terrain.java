package com.wdd.cat.state.environment;
import java.util.Map;


/**
 * &#064program: cat
 * <p>
 * &#064description: 一格的地形。kind 如 "floor" / "wall" / "water" / "carpet"，properties 放滑不滑、软不软这类
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 17:26
 **/


public record Terrain(String kind, Map<String, Double> properties) {}