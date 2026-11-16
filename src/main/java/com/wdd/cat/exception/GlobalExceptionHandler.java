package com.wdd.cat.exception;

import com.wdd.cat.common.entity.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * &#064program: cat
 * <p>
 * &#064description: 全局异常处理器
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 19:32
 **/
@RestControllerAdvice
public class GlobalExceptionHandler  {

        @ExceptionHandler(Exception.class)
        public Result<?> handle(Exception e) {
            return Result.fail(e.getMessage());
        }
    }