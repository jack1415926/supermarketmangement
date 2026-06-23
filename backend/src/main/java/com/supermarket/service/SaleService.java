/**
 * 销售服务 —— POS 结账（最复杂的业务逻辑）、销售记录查询、交班汇总。
 *
 * @Transactional 保证结账操作的原子性：
 *   一个结账操作涉及多步（创建 Sale、创建 SaleItem、扣库存、更新会员积分）。
 *   如果任何一步失败，整个操作回滚，数据保持一致。
 *   类似 C++ 中的数据库事务 BEGIN/COMMIT/ROLLBACK。
 *
 * @author 徐磊
 */
package com.supermarket.service;

import com.supermarket.dto.PageDTO;
import com.supermarket.dto.SaleRequest;
import com.supermarket.dto.SaleResponse;
import com.supermarket.entity.*;
import com.supermarket.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * POS 结账 —— 最核心的业务方法。
     *
     * 流程：
     *   1. 校验购物清单不为空
     *   2. 校验商品存在且库存充足
     *   3. 处理会员折扣（95 折）
     *   4. 计算总金额、折扣、应收、找零
     *   5. 生成交易流水号
     *   6. 创建销售单 + 明细
     *   7. 扣减库存
     *   8. 更新会员累计消费和积分
     *
     * @Transactional 保证整个流程在一个数据库事务中执行。
     *   任何一步失败，所有已执行的数据库操作全部回滚。
     *
     * @param request 结账请求（购物清单 + 会员卡号 + 收款方式 + 实收金额）
     * @param cashierUsername 收银员用户名（从 JWT 中获取）
     * @return 结账响应（流水号、找零等信息）
     */
    @Transactional
    public SaleResponse checkout(SaleRequest request, String cashierUsername) {
        // ========== Step 1: 查询收银员和会员 ==========
        // 注意：这里用 employeeRepository 的关联 user 字段来查
        // 简化做法：直接通过 username 匹配（user 的 username == cashierUsername）
        Employee cashier = employeeRepository.findAll().stream()
            .filter(e -> e.getUser() != null && e.getUser().getUsername().equals(cashierUsername))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("找不到收银员信息: " + cashierUsername));

        Member member = null;
        if (request.getMemberCardNo() != null && !request.getMemberCardNo().isBlank()) {
            member = memberRepository.findByCardNo(request.getMemberCardNo())
                .orElseThrow(() -> new IllegalArgumentException("会员卡不存在: " + request.getMemberCardNo()));
            // 检查会员卡是否有效
            if (member.getStatus() != 1) {
                throw new IllegalArgumentException("会员卡已注销");
            }
            if (member.getValidUntil().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("会员卡已过期，请续卡");
            }
        }

        // ========== Step 2: 校验商品和库存 ==========
        List<SaleItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (SaleRequest.SaleItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                .orElseThrow(() -> new NoSuchElementException("商品不存在: id=" + itemReq.getProductId()));

            if (product.getStatus() != 1) {
                throw new IllegalArgumentException("商品已下架: " + product.getName());
            }
            if (product.getStock() < itemReq.getQuantity()) {
                throw new IllegalArgumentException(
                    "库存不足: " + product.getName() + " 当前库存 " + product.getStock() + "，需要 " + itemReq.getQuantity());
            }

            // 计算小计
            BigDecimal unitPrice = product.getSalePrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            // 创建销售明细
            SaleItem item = new SaleItem();
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setSalePrice(unitPrice);
            item.setSubtotal(subtotal);
            items.add(item);
        }

        // ========== Step 3: 计算折扣和找零 ==========
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (member != null) {
            // 会员享受 95 折
            discountAmount = totalAmount.multiply(new BigDecimal("0.05"))
                .setScale(2, RoundingMode.HALF_UP); // 四舍五入到分
        }
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);
        BigDecimal changeAmount = request.getReceivedAmount().subtract(finalAmount);

        if (changeAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "实收金额不足：应收 " + finalAmount + "，实收 " + request.getReceivedAmount());
        }

        // ========== Step 4: 生成流水号 ==========
        String flowNo = generateFlowNo();

        // ========== Step 5: 创建销售单 ==========
        Sale sale = new Sale();
        sale.setFlowNo(flowNo);
        sale.setCashier(cashier);
        sale.setMember(member);
        sale.setTotalAmount(totalAmount);
        sale.setDiscountAmount(discountAmount);
        sale.setReceivedAmount(request.getReceivedAmount());
        sale.setChangeAmount(changeAmount);
        sale.setPaymentMethod(request.getPaymentMethod());
        sale.setStatus("COMPLETED");
        sale.setCreatedAt(LocalDateTime.now());
        sale = saleRepository.save(sale);

        // ========== Step 6: 保存明细 ==========
        for (SaleItem item : items) {
            item.setSale(sale);
            saleItemRepository.save(item);
        }
        sale.setItems(items);

        // ========== Step 7: 扣减库存 ==========
        for (SaleItem item : items) {
            Product product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }

        // ========== Step 8: 更新会员信息 ==========
        if (member != null) {
            member.setTotalSpent(member.getTotalSpent().add(finalAmount));
            // 积分规则：每消费 10 元积 1 分
            int earnedPoints = finalAmount.divide(BigDecimal.TEN, 0, RoundingMode.DOWN).intValue();
            member.setPoints(member.getPoints() + earnedPoints);
            memberRepository.save(member);
        }

        // ========== 构建响应 ==========
        return buildResponse(sale, items);
    }

    /** 分页查询销售记录 */
    public PageDTO<Sale> findAll(int page, int size, LocalDate date) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Sale> result;
        if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            result = saleRepository.findByDateRange(start, end, pageable);
        } else {
            result = saleRepository.findAll(pageable);
        }
        return PageDTO.from(result);
    }

    /** 按 ID 查询销售单详情 */
    public Sale findById(Long id) {
        return saleRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("销售单不存在: id=" + id));
    }

    /** 当日交班汇总 */
    public java.util.Map<String, Object> getDailySummary(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        BigDecimal totalAmount = saleRepository.getTotalAmountByDateRange(start, end);
        Long count = saleRepository.getTransactionCountByDateRange(start, end);
        java.util.Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("date", date.toString());
        summary.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
        summary.put("transactionCount", count != null ? count : 0);
        summary.put("averageAmount", count != null && count > 0
            ? totalAmount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        return summary;
    }

    /** 生成流水号：S + 年月日 + 4 位序号 */
    private String generateFlowNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 用当前秒数 + 3 位随机数作为序号
        String seq = String.format("%04d", System.currentTimeMillis() % 10000);
        return "S" + datePart + seq;
    }

    /** 构建结账响应 */
    private SaleResponse buildResponse(Sale sale, List<SaleItem> items) {
        SaleResponse resp = new SaleResponse();
        resp.setFlowNo(sale.getFlowNo());
        resp.setSaleId(sale.getId());
        resp.setTotalAmount(sale.getTotalAmount());
        resp.setDiscountAmount(sale.getDiscountAmount());
        resp.setFinalAmount(sale.getTotalAmount().subtract(sale.getDiscountAmount()));
        resp.setReceivedAmount(sale.getReceivedAmount());
        resp.setChangeAmount(sale.getChangeAmount());
        resp.setPaymentMethod(sale.getPaymentMethod());
        resp.setMemberCardNo(sale.getMember() != null ? sale.getMember().getCardNo() : null);
        resp.setCashierName(sale.getCashier() != null ? sale.getCashier().getName() : "");
        resp.setCreatedAt(sale.getCreatedAt());

        List<SaleResponse.SaleItemDetail> details = new ArrayList<>();
        for (SaleItem item : items) {
            SaleResponse.SaleItemDetail d = new SaleResponse.SaleItemDetail();
            d.setProductName(item.getProduct().getName());
            d.setBarcode(item.getProduct().getBarcode());
            d.setUnitPrice(item.getSalePrice());
            d.setQuantity(item.getQuantity());
            d.setSubtotal(item.getSubtotal());
            details.add(d);
        }
        resp.setItems(details);
        return resp;
    }
}
