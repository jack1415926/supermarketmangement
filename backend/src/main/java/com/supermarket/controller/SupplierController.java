/**
 * 供应商控制器 —— /api/suppliers
 *
 * @author 徐磊
 */
package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Supplier;
import com.supermarket.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "供应商列表")
    @GetMapping
    public Result<List<Supplier>> findAll() {
        return Result.success(supplierService.findAll());
    }

    @Operation(summary = "查询单个供应商")
    @GetMapping("/{id}")
    public Result<Supplier> findById(@PathVariable Long id) {
        return Result.success(supplierService.findById(id));
    }

    @Operation(summary = "新增供应商")
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Supplier> create(@RequestBody Supplier supplier) {
        return Result.success(supplierService.create(supplier));
    }

    @Operation(summary = "更新供应商")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Supplier> update(@PathVariable Long id, @RequestBody Supplier supplier) {
        return Result.success(supplierService.update(id, supplier));
    }

    @Operation(summary = "删除供应商")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        supplierService.deactivate(id);
        return Result.success();
    }
}
