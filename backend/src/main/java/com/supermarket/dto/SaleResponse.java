/**
 * 结账响应 DTO —— POST /api/sales 的返回值。
 *
 * POS 结账完成后返回给前端的交易结果。
 * 前端用小票或弹窗展示这些信息。
 *
 * @author 徐磊
 */
package com.supermarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponse {

    /** 交易流水号 */
    private String flowNo;

    /** 销售单 ID */
    private Long saleId;

    /** 商品明细 */
    private List<SaleItemDetail> items;

    /** 原价总金额（折扣前） */
    private BigDecimal totalAmount;

    /** 折扣金额（会员 95 折优惠金额） */
    private BigDecimal discountAmount;

    /** 实际应收金额（折扣后） */
    private BigDecimal finalAmount;

    /** 实收金额 */
    private BigDecimal receivedAmount;

    /** 找零金额 */
    private BigDecimal changeAmount;

    /** 收款方式 */
    private String paymentMethod;

    /** 会员卡号（如果是会员交易） */
    private String memberCardNo;

    /** 收银员姓名 */
    private String cashierName;

    /** 交易时间 */
    private LocalDateTime createdAt;

    /** 单个商品明细 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaleItemDetail {
        private String productName;
        private String barcode;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal subtotal;
    }
}
