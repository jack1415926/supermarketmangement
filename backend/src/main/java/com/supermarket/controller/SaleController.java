/**
 * 销售控制器 —— /api/sales
 *
 * @author 徐磊
 */
package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.dto.PageDTO;
import com.supermarket.dto.SaleRequest;
import com.supermarket.dto.SaleResponse;
import com.supermarket.entity.Sale;
import com.supermarket.entity.User;
import com.supermarket.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    /**
     * POS 结账（收银员操作）。
     * @AuthenticationPrincipal 注入当前登录用户（从 JWT 中提取）。
     */
    @Operation(summary = "POS结账（核心接口）")
    @PostMapping
    @PreAuthorize("hasAnyRole('CASHIER','MANAGER','ADMIN')")
    public Result<SaleResponse> checkout(
        @Valid @RequestBody SaleRequest request,
        @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        // 从 UserDetails 中获取当前收银员的用户名
        // 注意：JwtAuthFilter 把 User 实体设为了 principal，但 User implements 的是 username
        // 实际上 principal 是 User entity，可以通过 getUsername() 获取
        String username = userDetails.getUsername();
        return Result.success("结账成功", saleService.checkout(request, username));
    }

    /** 销售记录查询 */
    @Operation(summary = "销售记录分页查询")
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<PageDTO<Sale>> findAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return Result.success(saleService.findAll(page, size, date));
    }

    /** 销售单详情 */
    @Operation(summary = "查询单笔销售详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Sale> findById(@PathVariable Long id) {
        return Result.success(saleService.findById(id));
    }

    /** 当日交班汇总 */
    @Operation(summary = "当日交班汇总")
    @GetMapping("/daily-summary")
    @PreAuthorize("hasAnyRole('CASHIER','MANAGER','ADMIN')")
    public Result<Map<String, Object>> dailySummary(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (date == null) date = LocalDate.now();
        return Result.success(saleService.getDailySummary(date));
    }
}
