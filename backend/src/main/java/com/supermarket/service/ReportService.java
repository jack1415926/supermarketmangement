/**
 * 报表服务 —— 销售排行榜、日报/月报、收银员业绩统计。
 *
 * 所有报表方法都是只读的聚合查询（GROUP BY + SUM + COUNT），
 * 不修改数据，因此标记 @Transactional(readOnly = true)。
 *
 * JPQL 的聚合查询返回 Object[] 列表，需要手动转换为 Map/DTO。
 *
 * @author 徐磊
 */
package com.supermarket.service;

import com.supermarket.repository.SaleItemRepository;
import com.supermarket.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SaleItemRepository saleItemRepository;
    private final SaleRepository saleRepository;

    /**
     * 销售排行榜：按商品销量降序排列。
     *
     * SaleItemRepository.getSalesRanking() 的 JPQL：
     *   SELECT si.product.id, SUM(si.quantity), COUNT(DISTINCT si.sale.id)
     *   FROM SaleItem si WHERE ... GROUP BY si.product.id ORDER BY SUM(si.quantity) DESC
     *
     * 返回 Object[] 数组：[productId, totalQuantity, saleCount]
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSalesRanking(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        List<Object[]> rows = saleItemRepository.getSalesRanking(start, end);

        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;
        for (Object[] row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("rank", rank++);
            item.put("productId", row[0]);
            item.put("totalQuantity", row[1]);
            item.put("saleCount", row[2]);
            result.add(item);
        }
        return result;
    }

    /**
     * 日销售报表：按日期分组的销售额和订单数。
     *
     * SaleRepository.getDailySalesReport() 返回原生 SQL 聚合结果：
     *   [sale_date, transaction_count, total_amount]
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDailySalesReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        List<Object[]> rows = saleRepository.getDailySalesReport(start, end);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", row[0].toString());
            item.put("transactionCount", row[1]);
            item.put("totalAmount", row[2]);
            // 客单价 = 总金额 / 交易笔数
            BigDecimal totalAmount = (BigDecimal) row[2];
            Long count = (Long) row[1];
            BigDecimal average = count > 0
                ? totalAmount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            item.put("averageAmount", average);
            result.add(item);
        }
        return result;
    }

    /**
     * 月销售报表：按月份分组的销售额和订单数。
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMonthlySalesReport(int year, int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusSeconds(1);
        List<Object[]> rows = saleRepository.getMonthlySalesReport(start, end);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("year", row[0]);
            item.put("month", row[1]);
            item.put("transactionCount", row[2]);
            item.put("totalAmount", row[3]);
            result.add(item);
        }
        return result;
    }

    /**
     * 收银员业绩统计：按收银员分组的交易笔数和金额。
     *
     * SaleRepository.getCashierDailySummary() 返回：
     *   [cashierId, transactionCount, totalAmount]
     *
     * 这里按日期范围统计，从 sales 表按 cashier_id 分组聚合。
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCashierPerformance(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        List<Object[]> rows = saleRepository.getCashierDailySummary(start, end);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("cashierId", row[0]);
            item.put("transactionCount", row[1]);
            item.put("totalAmount", row[2]);
            result.add(item);
        }
        return result;
    }
}
