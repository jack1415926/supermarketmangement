/**
 * 库存响应 DTO —— GET /api/inventory 的返回值。
 *
 * @author 徐磊
 */
package com.supermarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {

    private Long productId;
    private String productName;
    private String barcode;
    private String unit;
    private String categoryName;

    /** 当前库存 */
    private Integer stock;

    /** 库存下限 */
    private Integer minStock;

    /** 库存上限 */
    private Integer maxStock;

    /** 零售价 */
    private BigDecimal salePrice;

    /** 进货价 */
    private BigDecimal purchasePrice;

    /** 库存金额 = stock × purchasePrice */
    private BigDecimal stockValue;

    /**
     * 库存状态：NORMAL=正常, LOW=低于下限, OUT=缺货, HIGH=高于上限
     * 由 Service 层根据 stock/minStock/maxStock 计算。
     */
    private String status;
}
