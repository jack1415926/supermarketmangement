/**
 * 统一响应结果类 —— 所有 API 接口的返回格式。
 *
 * 响应格式固定为：
 * {
 *   "code": 200,       // 状态码，200=成功，其他=失败
 *   "message": "success", // 提示信息
 *   "data": { ... }    // 返回数据（可为 null）
 * }
 *
 * 类似 C++ 中的模板类或结构体，用于统一函数的返回类型。
 * 前端只需要解析这一个格式，不用每个接口单独处理。
 *
 * @author 殷智元
 */
package com.supermarket.common;

import com.fasterxml.jackson.annotation.JsonInclude; // Jackson 注解：控制 JSON 序列化行为

import lombok.Data; // Lombok 注解：自动生成 getter/setter/toString/equals/hashCode

/**
 * @param <T> 泛型参数，表示 data 字段的类型。
 *            类似 C++ 的 template<typename T>。
 *            例如 Result<User> 表示 data 是一个 User 对象。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // 序列化为 JSON 时，值为 null 的字段不输出
public class Result<T> {

    /** 状态码，对应 HTTP 状态码的语义 */
    private int code;

    /** 提示信息，描述请求处理结果 */
    private String message;

    /** 返回的数据，泛型，可以是任意类型 */
    private T data;

    // ==================== 构造函数 ====================

    /** 私有构造函数，强制使用静态工厂方法创建对象（类似 C++ 的 private 构造函数 + 静态工厂函数） */
    private Result() {}

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ==================== 成功响应的静态工厂方法 ====================

    /**
     * 请求成功，返回数据。
     * 用法：return Result.success(user);
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 请求成功，不返回数据（如删除操作）。
     * 用法：return Result.success();
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /**
     * 请求成功，自定义提示消息。
     * 用法：return Result.success("操作成功", user);
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    // ==================== 失败响应的静态工厂方法 ====================

    /**
     * 请求失败，返回错误码和消息。
     * 用法：return Result.error(400, "参数错误");
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 请求失败，使用预定义错误码并附带详细数据。
     * 用法：return Result.error(400, "校验失败", errorDetails);
     */
    public static <T> Result<T> error(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

    // ==================== 常用错误码的快捷方法 ====================

    /** 400 Bad Request —— 客户端请求参数有误 */
    public static <T> Result<T> badRequest(String message) {
        return error(400, message);
    }

    /** 401 Unauthorized —— 未登录或 Token 无效 */
    public static <T> Result<T> unauthorized(String message) {
        return error(401, message);
    }

    /** 403 Forbidden —— 权限不足 */
    public static <T> Result<T> forbidden(String message) {
        return error(403, message);
    }

    /** 404 Not Found —— 资源不存在 */
    public static <T> Result<T> notFound(String message) {
        return error(404, message);
    }

    /** 500 Internal Server Error —— 服务器内部错误 */
    public static <T> Result<T> serverError(String message) {
        return error(500, message);
    }
}
