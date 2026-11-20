package com.wdd.cat.web;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

/**
 * &#064program: cat
 * <p>
 * &#064description: 页面入口：把 /settings 这种短路径转到 static 下的 html
 * <p>
 * 三个页面本身是静态资源（static/settings.html 等），这里只做免打 .html 的映射，
 * 顺便提供根路径入口。
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 15:58
 **/
@Controller
public class PageController {

    /** 根路径 → 设置页。 */
    @GetMapping("/")
    public RedirectView index() {
        return new RedirectView("/settings.html");
    }

    /** 设置页。 */
    @GetMapping("/settings")
    public RedirectView settings() {
        return new RedirectView("/settings.html");
    }

    /** 存档页。 */
    @GetMapping("/archives")
    public RedirectView archives() {
        return new RedirectView("/archives.html");
    }

    /** 日志页。 */
    @GetMapping("/logs")
    public RedirectView logs() {
        return new RedirectView("/logs.html");
    }
}
