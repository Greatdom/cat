package com.wdd.cat.common.entity;

import java.io.Serializable;

/**
 * &#064;program: cat
 * <p>
 * &#064;description: 统一结果
 * <p>
 * &#064;author: black-cat
 * <p>
 * &#064;create: 2026-11-16 15:52
 **/


public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 成功码。 */
    public static final int CODE_SUCCESS = 200;
    /** 通用失败码。 */
    public static final int CODE_ERROR = 400;
    //TODO 缺乏认证失败,资源不匹配等码,且应该将码集中到enum新文件


    /** 状态码。0 表示成功，非 0 表示失败。 */
    private int code;

    /** 提示信息。 */
    private String message;

    /** 业务数据。 */
    private T data;

    /** 无参构造，供 Jackson 等反序列化使用。 */
    public Result() {
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ── 成功 ──

    /** 成功，无数据。 */
    public static <T> Result<T> ok() {
        return new Result<>(CODE_SUCCESS, "ok", null);
    }

    /** 成功，带数据。 */
    public static <T> Result<T> ok(T data) {
        return new Result<>(CODE_SUCCESS, "ok", data);
    }

    /** 成功，带数据和自定义提示。 */
    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(CODE_SUCCESS, message, data);
    }

    // ── 失败 ──

    /** 通用失败，默认码。 */
    public static <T> Result<T> fail() {
        return new Result<>(CODE_ERROR, "error", null);
    }

    /** 通用失败，自定义提示。 */
    public static <T> Result<T> fail(String message) {
        return new Result<>(CODE_ERROR, message, null);
    }

    /** 自定义码 + 提示。 */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    /** 自定义码 + 提示 + 数据（如校验错误明细）。 */
    public static <T> Result<T> fail(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

    // ── 便捷判断 ──

    /** 是否成功。 */
    public boolean isSuccess() {
        return this.code == CODE_SUCCESS;
    }

    // ── getter / setter（Jackson 序列化需要 getter，反序列化需要 setter 或无参构造+字段可见） ──

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}