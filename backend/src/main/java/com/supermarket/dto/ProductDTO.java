/**
 * 商品响应 DTO —— 商品列表/详情的 API 响应。
 *
 * 相比 Product 实体，DTO 额外包含：
 *   - categoryName（分类名称，避免前端再查一次分类接口）
 *   - supplierName（供应商名称）
 *   - stockStatus（库存状态：正常/偏低/缺货，由 Service 层计算）
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
public class ProductDTO {

    private Long id;
    private String barcode;
    private String name;
    private String unit;
    private BigDecimal salePrice;
    private BigDecimal purchasePrice;
    private Integer stock;
    private Integer minStock;
    private Integer maxStock;
    private Integer status;

    /** 所属分类名称（冗余，方便前端显示） */
    private String categoryName;
    private Long categoryId;

    /** 供应商名称 */
    private String supplierName;
    private Long supplierId;

    /** 库存状态：NORMAL=正常, LOW=偏低, OUT=缺货 */
    private String stockStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
