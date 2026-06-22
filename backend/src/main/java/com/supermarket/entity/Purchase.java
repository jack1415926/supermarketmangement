/**
 * 进货单实体 —— 对应数据库 purchases 表。
 *
 * 进货单记录一次采购行为。一个进货单由多个进货明细（PurchaseItem）组成。
 *
 * 业务流程：
 *   1. 管理员创建进货单，选择供应商
 *   2. 逐一添加进货明细（哪个商品、多少数量、什么进价）
 *   3. 系统自动计算总金额（sum of 明细.小计）
 *   4. 确认入库后，系统自动更新对应商品的库存
 *
 * Purchase（头）与 PurchaseItem（明细）是一对多关系：
 *   一个进货单包含多个商品明细。
 *
 * 类似 C++ 中的：
 *   struct PurchaseOrder { int id; vector<PurchaseItem> items; double total; };
 *
 * @author 徐磊
 */
package com.supermarket.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchases")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 进货单号，唯一标识一次进货。
     * 格式建议：P + 年月日 + 序号，如 P20260622001。
     */
    @Column(name = "purchase_no", nullable = false, unique = true, length = 30)
    private String purchaseNo;

    /**
     * 供应商。
     *
     * @ManyToOne：多个进货单可以来自同一个供应商。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    @ToString.Exclude
    private Supplier supplier;

    /**
     * 经手人（员工）。
     *
     * @ManyToOne：记录是谁办理的这次进货。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    @ToString.Exclude
    private Employee employee;

    /**
     * 进货总金额。
     * 等于所有 purchase_items 中 subtotal 的总和。
     * 冗余存储（可以在明细变更时重算），方便查询时不需要 JOIN 明细表。
     */
    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /**
     * 进货状态：
     *   PENDING    — 待入库（已创建进货单但未确认）
     *   COMPLETED  — 已入库（已确认并更新了库存）
     *   CANCELLED  — 已取消
     */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    /** 备注（可选） */
    @Column(length = 255)
    private String remark;

    // ==================== 关联明细（一对多） ====================

    /**
     * 进货明细列表。
     *
     * @OneToMany：一个进货单包含多个进货明细。
     * mappedBy = "purchase"：关系由 PurchaseItem.purchase 字段维护。
     *   不会创建中间表，只通过 PurchaseItem 表的外键 purchase_id 关联。
     *
     * cascade = CascadeType.ALL：级联操作。
     *   当你保存/删除 Purchase 时，自动保存/删除所有关联的 PurchaseItem。
     *   类似 C++ 中的 "析构时自动清理子对象"。
     *
     * orphanRemoval = true：孤儿删除。
     *   当你从 items 列表中移除一个 PurchaseItem 时，JPA 自动从数据库删除它。
     *
     * @JsonIgnoreProperties("purchase")：
     *   序列化为 JSON 时，items 中的每个 PurchaseItem 不要再次包含 purchase 引用。
     *   否则会形成无限递归：Purchase → items → PurchaseItem → purchase → items → ...
     */
    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("purchase") // 防止 JSON 序列化无限递归
    @ToString.Exclude
    private List<PurchaseItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ==================== 便捷方法 ====================

    /**
     * 添加进货明细。
     * 维护双向关系：设置 items → purchase 和 purchase ← items。
     * 类似 C++ 中向 vector 添加元素同时设置父指针。
     */
    public void addItem(PurchaseItem item) {
        items.add(item);
        item.setPurchase(this);
    }

    /**
     * 移除进货明细。
     * 同时清除双向关系。
     */
    public void removeItem(PurchaseItem item) {
        items.remove(item);
        item.setPurchase(null);
    }
}
