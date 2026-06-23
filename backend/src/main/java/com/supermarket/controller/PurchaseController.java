/**
 * 进货控制器 —— /api/purchases
 *
 * @author 徐磊
 */
package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.dto.PageDTO;
import com.supermarket.dto.PurchaseRequest;
import com.supermarket.entity.Purchase;
import com.supermarket.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    /** 创建进货单 */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Purchase> create(
        @Valid @RequestBody PurchaseRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        return Result.success("进货入库成功", purchaseService.create(request, userDetails.getUsername()));
    }

    /** 进货记录查询 */
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<PageDTO<Purchase>> findAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return Result.success(purchaseService.findAll(page, size));
    }

    /** 进货单详情 */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Purchase> findById(@PathVariable Long id) {
        return Result.success(purchaseService.findById(id));
    }

    /** 进货计划：列出库存低于下限的商品，给出建议进货量 */
    @GetMapping("/plan")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<List<Map<String, Object>>> getPurchasePlan() {
        return Result.success(purchaseService.getPurchasePlan());
    }
}
