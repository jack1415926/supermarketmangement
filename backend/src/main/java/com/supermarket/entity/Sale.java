/**
 * 销售单实体 —— 对应数据库 sales 表。
 *
 * 销售单记录一次完整的 POS 交易。一个销售单由多个销售明细（SaleItem）组成。
 *
 * 业务流程（POS 收银）：
 *   1. 收银员扫描/录入商品，形成购物清单
 *   2. （可选）扫描会员卡，应用 95 折优惠
 *   3. 确认收款方式（现金/微信/支付宝/银行卡）
 *   4. 输入实收金额，系统计算找零
 *   5. 提交结算，系统生成交易流水、扣减库存、更新会员消费
 *   6. 打印小票
 *
 * Sale（头）与 SaleItem（明细）是一对多关系。
 *
 * 类似 C++ 中的：
 *   struct SaleRecord { int id; vector<SaleItem> items; double total; double paid; double change; };
 *
 * @author 徐磊
 */
package com.supermarket.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 交易流水号，唯一标识一次交易。
     * 格式建议：S + 年月日 + 序号，如 S20260622001。
     * 打印在小票上，方便顾客查询和退换货。
     */
    @Column(name = "flow_no", nullable = false, unique = true, length = 30)
    private String flowNo;

    /**
     * 收银员（哪个员工操作的 POS 收银）。
     *
     * @ManyToOne：多个销售单可以属于同一个收银员。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id")
    @ToString.Exclude
    private Employee cashier;

    /**
     * 会员（可为 null，表示非会员顾客）。
     *
     * @ManyToOne：多个销售单可以属于同一个会员。
     * 为 null 时表示顾客不是会员，不享受折扣。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @ToString.Exclude
    private Member member;

    /**
     * 销售总金额（原价，折扣前）。
     * 等于所有 sale_items 中 subtotal 的总和。
     */
    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /**
     * 折扣金额。
     * 会员 95 折时，discountAmount = totalAmount × 0.05。
     * 最终应收 = totalAmount - discountAmount。
     */
    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /**
     * 实收金额（顾客实际付了多少钱）。
     * 用于计算找零：changeAmount = receivedAmount - 实收应收。
     */
    @Column(name = "received_amount", precision = 12, scale = 2)
    private BigDecimal receivedAmount = BigDecimal.ZERO;

    /**
     * 找零金额。
     * changeAmount = receivedAmount - (totalAmount - discountAmount)。
     * 非现金支付时（微信/支付宝），changeAmount 为 0。
     */
    @Column(name = "change_amount", precision = 12, scale = 2)
    private BigDecimal changeAmount = BigDecimal.ZERO;

    /**
     * 收款方式。
     *   CASH   — 现金
     *   WECHAT — 微信支付
     *   ALIPAY — 支付宝
     *   CARD   — 银行卡
     *
     * 收款方式影响找零逻辑：只有现金支付才需要找零。
     */
    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    /**
     * 交易状态。
     *   COMPLETED — 已完成（正常交易）
     *   REFUNDED  — 已退款
     */
    @Column(length = 20)
    private String status = "COMPLETED";

    /** 备注（可选） */
    @Column(length = 255)
    private String remark;

    // ==================== 关联明细（一对多） ====================

    /**
     * 销售明细列表。
     *
     * cascade = CascadeType.ALL：保存 Sale 时自动保存所有 SaleItem。
     * orphanRemoval = true：从列表移除的 SaleItem 自动从数据库删除。
     */
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("sale") // 防止 JSON 无限递归
    @ToString.Exclude
    private java.util.List<SaleItem> items = new java.util.ArrayList<>();

    /** 交易时间（创建时间） */
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ==================== 便捷方法 ====================

    /** 添加销售明细，维护双向关系 */
    public void addItem(SaleItem item) {
        items.add(item);
        item.setSale(this);
    }

    /** 移除销售明细 */
    public void removeItem(SaleItem item) {
        items.remove(item);
        item.setSale(null);
    }

    /**
     * 计算实际应收金额（扣除折扣后）。
     * 类似 C++ 中的计算函数。
     *
     * @return 应收 = 总金额 - 折扣金额
     */
    public BigDecimal getFinalAmount() {
        if (totalAmount == null) return BigDecimal.ZERO;
        if (discountAmount == null) return totalAmount;
        return totalAmount.subtract(discountAmount);
    }

    /**
     * 计算找零。
     *
     * @return 找零 = 实收 - 应收。如果实收 < 应收，返回负数（表示还欠钱）。
     */
    public BigDecimal getChangeAmount() {
        if (receivedAmount == null) return BigDecimal.ZERO;
        return receivedAmount.subtract(getFinalAmount());
    }
}
