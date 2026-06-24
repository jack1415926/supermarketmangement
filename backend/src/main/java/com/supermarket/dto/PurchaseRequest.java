/**
 * 进货请求 DTO —— POST /api/purchases 的请求体。
 *
 * @author 徐磊
 */
package com.supermarket.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PurchaseRequest {

    /** 供应商 ID */
    @NotNull(message = "供应商不能为空")
    private Long supplierId;

    /** 进货商品列表 */
    @NotEmpty(message = "进货清单不能为空")
    @Valid
    private List<PurchaseItemRequest> items;

    /** 备注（可选） */
    private String remark;

    @Data
    public static class PurchaseItemRequest {

        @NotNull(message = "商品ID不能为空")
        private Long productId;

        @NotNull(message = "数量不能为空")
        @Positive(message = "数量必须大于 0")
        private Integer quantity;

        /** 进货单价（如果为 null，则使用商品的 purchasePrice） */
        @Positive(message = "进价必须大于 0")
        private BigDecimal purchasePrice;
    }
}
