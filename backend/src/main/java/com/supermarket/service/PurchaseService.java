/**
 * 进货服务 —— 创建进货单、进货记录查询、进货计划。
 *
 * @author 徐磊
 */
package com.supermarket.service;

import com.supermarket.dto.PageDTO;
import com.supermarket.dto.PurchaseRequest;
import com.supermarket.entity.*;
import com.supermarket.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * 创建进货单。
     * 流程：
     *   1. 校验供应商和商品
     *   2. 计算总金额
     *   3. 创建进货单和明细
     *   4. 更新商品库存（入库）
     */
    @Transactional
    public Purchase create(PurchaseRequest request, String cashierUsername) {
        // 查找经办人
        Employee employee = employeeRepository.findAll().stream()
            .filter(e -> e.getUser() != null && e.getUser().getUsername().equals(cashierUsername))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("找不到员工信息"));

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
            .orElseThrow(() -> new NoSuchElementException("供应商不存在"));

        // 创建进货单
        Purchase purchase = new Purchase();
        purchase.setPurchaseNo(generatePurchaseNo());
        purchase.setSupplier(supplier);
        purchase.setEmployee(employee);
        purchase.setStatus("COMPLETED");
        purchase.setRemark(request.getRemark());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<PurchaseItem> items = new ArrayList<>();

        for (PurchaseRequest.PurchaseItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                .orElseThrow(() -> new NoSuchElementException("商品不存在: id=" + itemReq.getProductId()));

            BigDecimal price = itemReq.getPurchasePrice() != null
                ? itemReq.getPurchasePrice() : product.getPurchasePrice();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            PurchaseItem item = new PurchaseItem();
            item.setPurchase(purchase);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setPurchasePrice(price);
            item.setSubtotal(subtotal);
            items.add(item);

            // 更新商品库存和进价
            product.setStock(product.getStock() + itemReq.getQuantity());
            product.setPurchasePrice(price);
            productRepository.save(product);
        }

        purchase.setTotalAmount(totalAmount);
        purchase = purchaseRepository.save(purchase);

        for (PurchaseItem item : items) {
            purchaseItemRepository.save(item);
        }
        purchase.setItems(items);
        return purchase;
    }

    /** 分页查询进货记录 */
    public PageDTO<Purchase> findAll(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Purchase> result = purchaseRepository.findAll(pageable);
        return PageDTO.from(result);
    }

    /** 按 ID 查询 */
    public Purchase findById(Long id) {
        return purchaseRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("进货单不存在: id=" + id));
    }

    /**
     * 进货计划：查询所有库存低于下限的商品，建议补货。
     * 每个商品建议进货数量 = (minStock * 2) - stock（即补到下限的两倍）。
     */
    public List<Map<String, Object>> getPurchasePlan() {
        List<Product> lowStockProducts = productRepository.findByStockLessThanEqualMinStock();
        List<Map<String, Object>> plan = new ArrayList<>();
        for (Product p : lowStockProducts) {
            Map<String, Object> item = new HashMap<>();
            item.put("productId", p.getId());
            item.put("productName", p.getName());
            item.put("barcode", p.getBarcode());
            item.put("currentStock", p.getStock());
            item.put("minStock", p.getMinStock());
            int suggestQty = (p.getMinStock() * 2) - p.getStock();
            item.put("suggestQuantity", Math.max(suggestQty, 1));
            if (p.getSupplier() != null) {
                item.put("supplierName", p.getSupplier().getName());
            }
            plan.add(item);
        }
        return plan;
    }

    private String generatePurchaseNo() {
        return "P" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            + String.format("%04d", System.currentTimeMillis() % 10000);
    }
}
