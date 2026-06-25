/**
 * 登录响应 DTO —— POST /api/auth/login 的返回值（包装在 Result.data 中）。
 *
 * @author 徐磊
 */
package com.supermarket.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** JWT Token 字符串，格式：Bearer <token> */
    private String token;

    /** Token 类型，固定为 "Bearer" */
    private String tokenType = "Bearer";

    /** 用户 ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 显示名称 */
    private String displayName;

    /** 角色（如 ROLE_CASHIER, ROLE_MANAGER, ROLE_ADMIN） */
    private String role;
}
