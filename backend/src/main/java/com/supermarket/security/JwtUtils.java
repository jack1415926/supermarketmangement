/**
 * JWT 工具类 —— 负责 Token 的生成、解析和校验。
 *
 * JWT（JSON Web Token）是一种用于身份验证的无状态令牌。
 * 结构（三部分，用 . 分隔）：Header.Payload.Signature
 *   Header  — 算法类型（HS256）和 Token 类型（JWT）
 *   Payload — 存放用户信息（username, role, 过期时间 等），不加密但签名防篡改
 *   Signature— 对前两部分的签名，用密钥生成。只有持有密钥的服务器才能验证。
 *
 * 工作流程：
 *   登录成功 → 服务器生成 JWT → 返回给客户端
 *   后续请求 → 客户端在 Authorization header 携带 JWT → 服务器验证签名和有效期
 *
 * 优点：
 *   1. 无状态：服务器不需要存 session，适合分布式部署
 *   2. 跨域友好：Token 可以跨域名使用
 *   3. 适合 RESTful API（无 Cookie 依赖）
 *
 * 类似 C++ 中的数字签名验证：对数据签名 → 传输 → 验证签名。
 *
 * @author 徐磊
 */
package com.supermarket.security;

import io.jsonwebtoken.*;                       // JwtException, Jwts, Claims, ExpiredJwtException 等
import io.jsonwebtoken.security.Keys;            // 密钥工具
import org.springframework.beans.factory.annotation.Value; // 读取 application.yml 中的配置
import org.springframework.stereotype.Component;           // 标记为 Spring 管理的 Bean

import javax.crypto.SecretKey; // 加密密钥
import java.nio.charset.StandardCharsets; // 字符编码常量
import java.util.Date; // Java 日期类

/**
 * @Component：告诉 Spring 把这个类作为单例 Bean 管理。
 *   其他类可以通过 @Autowired 或构造函数注入来使用它。
 *   类似 C++ 中的全局单例对象。
 */
@Component
public class JwtUtils {

    /** 签名密钥（SecretKey 对象，由 secret 字符串转换而来） */
    private final SecretKey key;

    /** Token 过期时间（毫秒） */
    private final long expirationMs;

    /**
     * 构造函数注入配置值。
     *
     * @Value("${jwt.secret}") 从 application.yml 中读取 jwt.secret 的值。
     * Spring 在创建 Bean 时自动注入。
     *
     * @param secret 从 application.yml 读取的 jwt.secret
     * @param expirationMs 从 application.yml 读取的 jwt.expiration
     */
    public JwtUtils(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration}") long expirationMs
    ) {
        // 用密钥字符串创建 HMAC-SHA 密钥对象
        // HMAC-SHA256 需要至少 256 位（32 字节）的密钥
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * 生成 JWT Token。
     *
     * JWT Payload（载荷）包含：
     *   sub (subject)    — 用户名，标识 Token 所属用户
     *   role             — 用户角色（自定义 claim）
     *   iat (issued at)  — 签发时间
     *   exp (expiration) — 过期时间
     *
     * @param username 用户名
     * @param role 角色（如 ROLE_CASHIER）
     * @return JWT Token 字符串
     */
    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
            .subject(username)                      // 设置主题（用户名）
            .claim("role", role)                    // 自定义字段：用户角色
            .issuedAt(now)                          // 签发时间
            .expiration(expiryDate)                 // 过期时间
            .signWith(key)                          // 用密钥签名
            .compact();                             // 构建为字符串
    }

    /**
     * 从 Token 中提取用户名。
     *
     * @param token JWT Token
     * @return 用户名（sub 字段的值）
     * @throws JwtException 如果 Token 无效或已过期
     */
    public String getUsernameFromToken(String token) {
        return parseToken(token).getPayload().getSubject();
    }

    /**
     * 从 Token 中提取角色。
     *
     * @param token JWT Token
     * @return 角色字符串（如 "ROLE_CASHIER"）
     */
    public String getRoleFromToken(String token) {
        return parseToken(token).getPayload().get("role", String.class);
    }

    /**
     * 验证 Token 是否有效。
     * 校验内容：签名是否正确、是否已过期。
     *
     * @param token JWT Token
     * @return true 表示 Token 有效
     */
    public boolean validateToken(String token) {
        // 防御性检查：拒绝 null 或空字符串
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 捕获所有 JWT 相关异常(Signature/Expired/Premature/Malformed/Unsupported)
            return false;
        }
    }

    /**
     * 内部方法：解析 Token 并返回 Jws 对象。
     *
     * Jws<Claims> 是解析后的 JWT，包含：
     *   getPayload() → Claims（可读取 sub, role, exp 等字段）
     *   如果签名无效或过期，这里会抛出异常。
     *
     * @param token JWT Token 字符串
     * @return 解析后的 Jws 对象
     */
    private Jws<Claims> parseToken(String token) {
        return Jwts.parser()
            .verifyWith(key)       // 设置验证密钥
            .build()
            .parseSignedClaims(token); // 解析并验证签名
    }
}
