/**
 * 结账请求 DTO —— POST /api/sales 的请求体。
 *
 * 前端 POS 收银台提交的结账数据。
 *
 * @author 徐磊
 */
package com.supermarket.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SaleRequest {

    /**
     * 购物清单。
     * @NotEmpty：列表不能为空，至少有一件商品。
     * @Valid：级联校验——对列表中每个 SaleItemRequest 也执行字段校验。
     */
    @NotEmpty(message = "购物清单不能为空")
    @Valid
    private List<SaleItemRequest> items;

    /** 会员卡号（可选，非会员为 null） */
    private String memberCardNo;

    /**
     * 收款方式。
     * @NotBlank：不能为空。
     */
    @NotBlank(message = "收款方式不能为空")
    private String paymentMethod;

    /**
     * 实收金额（顾客付了多少钱）。
     * @NotNull：不能为 null。
     * @Positive：必须大于 0。
     */
    @NotNull(message = "实收金额不能为空")
    @Positive(message = "实收金额必须大于 0")
    private BigDecimal receivedAmount;

    /**
     * 购物清单中的单个商品项。
     * 嵌套在 SaleRequest 内部，不需要单独的类文件。
     */
    @Data
    public static class SaleItemRequest {

        /** 商品 ID */
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        /** 购买数量 */
        @NotNull(message = "数量不能为空")
        @Positive(message = "数量必须大于 0")
        private Integer quantity;
    }
}
