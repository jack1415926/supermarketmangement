/**
 * 销售明细实体 —— 对应数据库 sale_items 表。
 *
 * 每条记录代表一次交易中的一个商品项。
 * 记录卖了哪个商品、多少数量、什么价格。
 *
 * SaleItem（明细）与 Sale（头）是多对一关系：
 *   多个明细属于同一笔交易。
 *
 * SaleItem 与 Product 也是多对一关系：
 *   多笔交易都可能包含同一个商品。
 *
 * 类似 C++ 中购物车的 line item：
 *   struct CartItem { Product* product; int quantity; double unitPrice; };
 *
 * @author 徐磊
 */
package com.supermarket.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属销售单。
     *
     * @ManyToOne：多个明细属于同一笔交易。
     * @JsonIgnoreProperties("items")：序列化时不加载 sale 的 items 列表，防止无限递归。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    @JsonIgnoreProperties("items")
    @ToString.Exclude
    private Sale sale;

    /**
     * 销售的商品。
     *
     * @ManyToOne：多笔交易的明细都可以涉及同一个商品。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    private Product product;

    /**
     * 销售数量。
     * 结账后要从 Product.stock 中扣除这个数量。
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * 销售单价（顾客支付的单价）。
     * 通常等于 Product.salePrice，但在促销活动时可能不同。
     */
    @Column(name = "sale_price", precision = 10, scale = 2)
    private BigDecimal salePrice;

    /**
     * 小计 = quantity × salePrice。
     * 当前行商品的总金额。
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal subtotal;
}
