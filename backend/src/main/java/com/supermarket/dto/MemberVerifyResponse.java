/**
 * 会员验证响应 DTO —— GET /api/members/verify/{cardNo} 的返回值。
 *
 * POS 收银时扫描会员卡后，返回会员简要信息和折扣率。
 *
 * @author 徐磊
 */
package com.supermarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberVerifyResponse {

    /** 会员 ID */
    private Long id;

    /** 会员卡号 */
    private String cardNo;

    /** 姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 累计消费金额 */
    private BigDecimal totalSpent;

    /** 当前积分 */
    private Integer points;

    /** 会员卡有效期截止日期 */
    private LocalDateTime validUntil;

    /** 是否有效（在有效期内且未被注销） */
    private boolean valid;

    /** 折扣率（0.95 表示 95 折） */
    private BigDecimal discountRate;

    /** 提示信息（如 "会员卡已过期，请续卡"） */
    private String message;
}
