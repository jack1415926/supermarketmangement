/**
 * 库存控制器 —— /api/inventory
 *
 * @author 徐磊
 */
package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.dto.InventoryDTO;
import com.supermarket.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /** 查询所有商品的库存信息 */
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<List<InventoryDTO>> findAll() {
        return Result.success(inventoryService.findAll());
    }

    /** 更新商品库存预警阈值 */
    @PutMapping("/{productId}/threshold")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Void> updateThreshold(
        @PathVariable Long productId,
        @RequestBody Map<String, Integer> body
    ) {
        Integer minStock = body.getOrDefault("minStock", 0);
        Integer maxStock = body.getOrDefault("maxStock", 999);
        inventoryService.updateThreshold(productId, minStock, maxStock);
        return Result.success();
    }

    /** 库存盘点：检查缺货和积压商品 */
    @PostMapping("/check")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<List<InventoryDTO>> checkInventory() {
        return Result.success(inventoryService.checkInventory());
    }
}
