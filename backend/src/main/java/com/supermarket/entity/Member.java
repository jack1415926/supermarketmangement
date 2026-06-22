/**
 * 会员实体 —— 对应数据库 members 表。
 *
 * 会员是超市的忠实顾客，持有会员卡，享受以下权益：
 *   1. 购物折扣（全场 95 折）
 *   2. 消费积分累计（每消费一定金额获得积分）
 *   3. 积分可用于兑换商品或抵扣金额
 *
 * 会员卡有效期一年，到期后可续期。
 * 未续期的会员将自动注销（由定时任务或管理员手动操作）。
 *
 * @author 徐磊
 */
package com.supermarket.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 会员卡号，唯一标识一张会员卡。
     * 格式建议：M + 8 位数字，如 M00000001。
     * POS 收银时通过扫码或手动输入卡号来识别会员。
     */
    @Column(name = "card_no", nullable = false, unique = true, length = 20)
    private String cardNo;

    /** 会员姓名 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 手机号，必须唯一 */
    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    /**
     * 身份证号。
     * 用于实名认证和防止重复注册。
     */
    @Column(name = "id_card", length = 18)
    private String idCard;

    /** 性别：男/女 */
    @Column(length = 2)
    private String gender;

    /** 出生日期 */
    @Column(name = "birth_date")
    private LocalDateTime birthDate;

    /**
     * 积分余额。
     * 积分规则：每消费 10 元获得 1 积分（可在 Service 层配置）。
     * 积分可用于兑换商品或抵扣金额。
     */
    @Column(nullable = false)
    private Integer points = 0;

    /**
     * 累计消费金额。
     * 每次购物结账时累加，用于统计会员贡献度。
     * 注意：这是累计值，只增不减。
     *
     * BigDecimal 确保金额精确。
     */
    @Column(name = "total_spent", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO; // BigDecimal.ZERO = "0.00"

    /**
     * 会员卡有效期截止日期。
     * 注册时自动设为创建日期 + 一年。
     * 超过此日期后会员卡失效，需续期。
     */
    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    /**
     * 状态：1=正常 0=已注销。
     * 注销的会员卡无法享受折扣。
     */
    @Column(nullable = false)
    private Integer status = 1;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
