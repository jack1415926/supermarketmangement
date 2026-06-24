/**
 * 进货明细实体 —— 对应数据库 purchase_items 表。
 *
 * 每条记录代表进货单中的一个商品项。
 * 记录进货了哪个商品、多少件、什么单价。
 *
 * PurchaseItem（明细）与 Purchase（头）是多对一关系：
 *   多个明细属于同一个进货单。
 *
 * PurchaseItem 与 Product 也是多对一关系：
 *   多个进货明细可以涉及同一个商品（不同批次的进货）。
 *
 * 类似 C++ 中订单的 line item 结构：
 *   struct LineItem { Product* product; int quantity; double price; };
 *
 * @author 徐磊
 */
package com.supermarket.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属进货单。
     *
     * @ManyToOne：多个明细属于同一个进货单。
     * FetchType.LAZY：默认不加载进货单信息，只在需要时查询。
     * @JoinColumn(name = "purchase_id")：外键列。
     *
     * @JsonIgnoreProperties("items")：
     *   序列化为 JSON 时不要加载 purchase 下的 items 列表。
     *   否则会形成无限循环嵌套。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    @JsonIgnoreProperties("items")
    @ToString.Exclude
    private Purchase purchase;

    /**
     * 进货的商品。
     *
     * @ManyToOne：多个明细可以涉及同一个商品。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    private Product product;

    /**
     * 进货数量。
     * 入库后，Product.stock 会增加这个数量。
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * 进货单价（从供应商采购的价格）。
     * 用于更新 Product.purchasePrice 和计算成本。
     */
    @Column(name = "purchase_price", precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    /**
     * 小计 = quantity × purchasePrice。
     * 理论上可以计算得到，但冗余存储方便查询和校验。
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal subtotal;
}
