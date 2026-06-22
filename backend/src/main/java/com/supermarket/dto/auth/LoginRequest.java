/**
 * 登录请求 DTO —— POST /api/auth/login 的请求体。
 *
 * DTO = Data Transfer Object，数据传输对象。
 * 作用：定义前端传给后端的 JSON 格式，与数据库实体（Entity）分离。
 *
 * 类似 C++ 中的 struct 用于接口参数传递。
 *
 * @author 徐磊
 */
package com.supermarket.dto.auth;

import jakarta.validation.constraints.NotBlank; // 校验注解：字段不能为空
import lombok.Data;                            // 自动生成 getter/setter

@Data
public class LoginRequest {

    /**
     * 用户名。
     * @NotBlank：Jakarta Validation 校验注解。
     *   不仅不能为 null，也不能是空字符串或纯空格。
     *   校验失败时抛出 MethodArgumentNotValidException，被 GlobalExceptionHandler 捕获。
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;
}
