/**
 * SpringDoc OpenAPI 配置 —— 自动生成 Swagger UI API 文档。
 *
 * SpringDoc 是 OpenAPI 3.0 规范的 Spring Boot 实现。
 * 启动后访问 http://localhost:8080/swagger-ui.html 即可看到所有 API 接口文档。
 *
 * 自动扫描所有 @RestController 和 @RequestMapping 的方法，
 * 生成接口列表，包括：路径、HTTP 方法、请求参数、响应格式、JWT 认证配置。
 *
 * 前端同学可以直接在 Swagger UI 页面上在线调试所有接口。
 *
 * @author 殷智元
 */
package com.supermarket.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    /**
     * 配置 OpenAPI 文档的基本信息。
     *
     * @return OpenAPI 对象
     */
    @Bean
    public OpenAPI supermarketOpenAPI() {
        return new OpenAPI()
            // ==================== 文档基本信息 ====================
            .info(new Info()
                .title("小型超市管理系统 API")          // 文档标题
                .description("包含 POS 收银、商品管理、会员管理、进货管理、库存管理、销售分析等全部接口")
                .version("v1.0.0")
                .license(new License()
                    .name("内部使用")
                    .url("https://github.com/jack1415926/supermarketmangement")
                )
            )
            // ==================== JWT 认证配置 ====================
            // 添加全局 JWT Bearer Token 认证方式
            .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
            .components(new Components()
                .addSecuritySchemes("BearerAuth", new SecurityScheme()
                    .name("BearerAuth")
                    .type(SecurityScheme.Type.HTTP)          // HTTP 认证
                    .scheme("bearer")                        // Bearer Token
                    .bearerFormat("JWT")                     // Token 格式
                    .description("在下方输入 JWT Token（登录接口返回的 token 值）")
                )
            );
    }
}
