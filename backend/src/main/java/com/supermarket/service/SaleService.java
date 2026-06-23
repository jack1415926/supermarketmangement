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
import jakarta.persistence.OptimisticLockException; // JPA 乐观锁异常，并发更新冲突时抛出
import org.springframework.orm.ObjectOptimisticLockingFailureException; // Spring 包装的乐观锁异常

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * POS 结账 —— 对外开放的入口，带乐观锁重试。
     *
     * 高并发下多个收银台可能同时扣减同一商品的库存，
     * Product 实体的 @Version 乐观锁会在并发冲突时抛出 OptimisticLockException。
     * 此方法最多重试 3 次，每次自动重新读取库存，保证数据正确。
     */
    public SaleResponse checkout(SaleRequest request, String cashierUsername) {
        int maxRetries = 3;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                return doCheckout(request, cashierUsername);
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                if (attempt == maxRetries - 1) {
                    throw new IllegalStateException(
                        "结账失败：商品库存已被更新，请重新扫描商品后再次结账", e);
                }
                // 重试前短暂等待，给其他事务完成的时间
                try { Thread.sleep(50); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
            }
        }
        throw new IllegalStateException("结账失败，已达最大重试次数");
    }

    /**
     * POS 结账 —— 核心业务逻辑（内部方法，由 checkout() 带重试调用）。
     *
     * 流程：
     *   1. 校验购物清单不为空
     *   2. 校验商品存在且库存充足
     *   3. 处理会员折扣（95 折）
     *   4. 计算总金额、折扣、应收、找零
     *   5. 生成交易流水号
     *   6. 创建销售单（级联自动保存明细）
     *   7. 批量扣减库存（@Version 乐观锁防超卖）
     *   8. 更新会员累计消费和积分
     *
     * @Transactional 保证整个流程在一个数据库事务中执行。
     *   任何一步失败，所有已执行的数据库操作全部回滚。
     *   并发冲突时抛出 OptimisticLockException，由外层 checkout() 重试。
     *
     * @param request 结账请求
     * @param cashierUsername 收银员用户名
     * @return 结账响应
     */
    @Transactional
    public SaleResponse doCheckout(SaleRequest request, String cashierUsername) {
        // ========== Step 1: 查询收银员（走数据库索引，不用全表扫描） ==========
        Employee cashier = employeeRepository.findByUserUsername(cashierUsername)
            .orElseThrow(() -> new IllegalStateException("找不到收银员信息: " + cashierUsername));

        Member member = null;
        if (request.getMemberCardNo() != null && !request.getMemberCardNo().isBlank()) {
            member = memberRepository.findByCardNo(request.getMemberCardNo())
                .orElseThrow(() -> new IllegalArgumentException("会员卡不存在: " + request.getMemberCardNo()));
            if (member.getStatus() != 1) {
                throw new IllegalArgumentException("会员卡已注销");
            }
            if (member.getValidUntil().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("会员卡已过期，请续卡");
            }
        }

        // ========== Step 2: 校验商品和库存 ==========
        List<SaleItem> items = new ArrayList<>();
        List<Product> productsToUpdate = new ArrayList<>(); // 收集需要更新库存的商品
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

            BigDecimal unitPrice = product.getSalePrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            SaleItem item = new SaleItem();
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setSalePrice(unitPrice);
            item.setSubtotal(subtotal);
            items.add(item);

            // 预扣库存，收集到列表最后批量 save
            product.setStock(product.getStock() - itemReq.getQuantity());
            productsToUpdate.add(product);
        }

        // ========== Step 3: 计算折扣和找零 ==========
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (member != null) {
            discountAmount = totalAmount.multiply(new BigDecimal("0.05"))
                .setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);
        BigDecimal changeAmount = request.getReceivedAmount().subtract(finalAmount);

        if (changeAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "实收金额不足：应收 " + finalAmount + "，实收 " + request.getReceivedAmount());
        }

        // ========== Step 4: 生成流水号（UUID 防重复） ==========
        String flowNo = generateFlowNo();

        // ========== Step 5: 创建销售单（cascade 自动保存明细） ==========
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

        // 设置双向关系，JPA cascade 会自动保存明细，无需手动 save items
        for (SaleItem item : items) {
            sale.addItem(item);
        }
        sale = saleRepository.save(sale); // cascade = ALL，明细自动保存

        // ========== Step 6: 批量扣减库存（一次性 saveAll） ==========
        productRepository.saveAll(productsToUpdate);

        // ========== Step 7: 更新会员信息 ==========
        if (member != null) {
            member.setTotalSpent(member.getTotalSpent().add(finalAmount));
            int earnedPoints = finalAmount.divide(BigDecimal.TEN, 0, RoundingMode.DOWN).intValue();
            member.setPoints(member.getPoints() + earnedPoints);
            memberRepository.save(member);
        }

        return buildResponse(sale, items);
    }

    /** 分页查询销售记录 */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public Sale findById(Long id) {
        return saleRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("销售单不存在: id=" + id));
    }

    /** 当日交班汇总 */
    @Transactional(readOnly = true)
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

    /**
     * 生成流水号：S + 年月日 + UUID 前 8 位。
     * 使用 UUID 避免高并发下时间戳取模导致的重复。
     */
    private String generateFlowNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "S" + datePart + uid;
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
