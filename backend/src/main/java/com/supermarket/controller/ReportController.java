/**
 * 报表控制器 —— /api/reports
 *
 * @author 徐磊
 */
package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.service.ReportService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Validated // 启用方法参数校验（@Min @Max 等）
public class ReportController {

    private final ReportService reportService;

    /** 销售排行榜（按商品销量） */
    @Operation(summary = "商品销售排行榜")
    @GetMapping("/sales-ranking")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<List<Map<String, Object>>> salesRanking(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return Result.success(reportService.getSalesRanking(startDate, endDate));
    }

    /** 日销售报表 */
    @Operation(summary = "日销售报表")
    @GetMapping("/sales-daily")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<List<Map<String, Object>>> salesDaily(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return Result.success(reportService.getDailySalesReport(startDate, endDate));
    }

    /** 月销售报表 */
    @Operation(summary = "月销售报表")
    @GetMapping("/sales-monthly")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<List<Map<String, Object>>> salesMonthly(
        @RequestParam int year,
        @RequestParam @Min(1) @Max(12) int month
    ) {
        return Result.success(reportService.getMonthlySalesReport(year, month));
    }

    /** 收银员业绩统计 */
    @Operation(summary = "收银员业绩统计")
    @GetMapping("/cashier-performance")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<List<Map<String, Object>>> cashierPerformance(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return Result.success(reportService.getCashierPerformance(startDate, endDate));
    }
}
