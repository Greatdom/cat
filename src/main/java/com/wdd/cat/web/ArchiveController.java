package com.wdd.cat.web;


import com.wdd.cat.common.entity.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * &#064program: cat
 * <p>
 * &#064description: 存档Controller
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 19:37
 **/
@RestController
@RequestMapping("/api/archives")
public class ArchiveController {
    //TODO
    @GetMapping
    public Result<?> list() {
        return null;
    }

    /** 读一个存档。 */
    @GetMapping("/{id}")
    public Result<?> get(@PathVariable String id) {
       return null;
    }

    /**
     新建或更新
     */
    @PostMapping
    public Result<?> save(@RequestBody ArchiveRequest body) {
        return null;

    }

    /** 删除。记忆库会被移进回收站，不做物理删除。 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable String id) {
        return null;

    }

    public record ArchiveRequest() {
    }

}
