/**
 * 库存控制器 —— /api/inventory
 *
 * @author 徐磊
 */
package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.dto.InventoryDTO;
import com.supermarket.dto.PageDTO;
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

    /** 分页查询库存信息，避免一次性加载所有商品 */
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<PageDTO<InventoryDTO>> findAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return Result.success(inventoryService.findAll(page, size));
    }

    /**
     * 更新商品库存预警阈值。
     * 只更新请求中指定的字段，未传入的字段保持不变。
     */
    @PutMapping("/{productId}/threshold")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Void> updateThreshold(
        @PathVariable Long productId,
        @RequestBody Map<String, Integer> body
    ) {
        // 只取请求中明确传入的值，null 表示保持原值
        Integer minStock = body.containsKey("minStock") ? body.get("minStock") : null;
        Integer maxStock = body.containsKey("maxStock") ? body.get("maxStock") : null;
        // 至少需要传入一个值
        if (minStock == null && maxStock == null) {
            throw new IllegalArgumentException("请至少指定 minStock 或 maxStock");
        }
        inventoryService.updateThreshold(productId, minStock, maxStock);
        return Result.success();
    }

    /** 库存盘点：检查缺货和积压商品（只读查询，使用 GET） */
    @GetMapping("/check")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<List<InventoryDTO>> checkInventory() {
        return Result.success(inventoryService.checkInventory());
    }
}
