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

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public Result<List<Supplier>> findAll() {
        return Result.success(supplierService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Supplier> findById(@PathVariable Long id) {
        return Result.success(supplierService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Supplier> create(@RequestBody Supplier supplier) {
        return Result.success(supplierService.create(supplier));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Supplier> update(@PathVariable Long id, @RequestBody Supplier supplier) {
        return Result.success(supplierService.update(id, supplier));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        supplierService.deactivate(id);
        return Result.success();
    }
}
