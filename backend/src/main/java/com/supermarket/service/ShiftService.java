/**
 * 换班服务 —— 开始班次、结束班次（交班）、换班记录查询。
 *
 * @author 徐磊
 */
package com.supermarket.service;

import com.supermarket.entity.Employee;
import com.supermarket.entity.Shift;
import com.supermarket.repository.EmployeeRepository;
import com.supermarket.repository.SaleRepository;
import com.supermarket.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;
    private final SaleRepository saleRepository; // 用于查询当班交易数据

    /**
     * 开始班次（收银员登录后自动创建）。
     * 一个收银员同时只能有一个活跃班次。
     */
    @Transactional
    public Shift startShift(String username) {
        Employee cashier = employeeRepository.findByUserUsername(username)
            .orElseThrow(() -> new IllegalStateException("找不到收银员信息"));
        // 检查是否已有活跃班次
        shiftRepository.findByCashierIdAndStatus(cashier.getId(), "ACTIVE")
            .ifPresent(s -> { throw new IllegalStateException("已有活跃班次，请先交班"); });
        Shift shift = new Shift();
        shift.setCashier(cashier);
        shift.setStartTime(LocalDateTime.now());
        shift.setTransactionCount(0);
        shift.setTotalAmount(BigDecimal.ZERO);
        shift.setStatus("ACTIVE");
        return shiftRepository.save(shift);
    }

    /**
     * 结束班次（交班）。
     *
     * 从 sales 表查询该收银员在班次时间范围内的交易数据，
     * 自动填充 transactionCount 和 totalAmount，确保报表准确。
     */
    @Transactional
    public Shift endShift(String username) {
        Employee cashier = employeeRepository.findByUserUsername(username)
            .orElseThrow(() -> new IllegalStateException("找不到收银员信息"));
        Shift shift = shiftRepository.findByCashierIdAndStatus(cashier.getId(), "ACTIVE")
            .orElseThrow(() -> new NoSuchElementException("没有活跃的班次"));

        // 查询班次期间该收银员的交易统计（从 sales 表按 cashier_id + 日期聚合计算）
        LocalDateTime start = shift.getStartTime();
        LocalDateTime end = LocalDateTime.now();
        Long cashierId = cashier.getId();
        BigDecimal totalAmount = saleRepository.getTotalAmountByCashierAndDateRange(cashierId, start, end);
        Long transactionCount = saleRepository.getTransactionCountByCashierAndDateRange(cashierId, start, end);

        shift.setEndTime(end);
        shift.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);
        shift.setTransactionCount(transactionCount != null ? transactionCount.intValue() : 0);
        shift.setStatus("CLOSED");
        return shiftRepository.save(shift);
    }

    /** 查询当前活跃班次 */
    @Transactional(readOnly = true)
    public Shift getActiveShift(String username) {
        Employee cashier = employeeRepository.findByUserUsername(username)
            .orElseThrow(() -> new IllegalStateException("找不到收银员信息"));
        return shiftRepository.findByCashierIdAndStatus(cashier.getId(), "ACTIVE")
            .orElse(null); // 返回 null 表示没有活跃班次
    }

    /** 查询某收银员的所有班次记录 */
    @Transactional(readOnly = true)
    public List<Shift> findByCashier(Long cashierId) {
        return shiftRepository.findByCashierIdOrderByStartTimeDesc(cashierId);
    }

    /** 查询所有活跃班次 */
    @Transactional(readOnly = true)
    public List<Shift> findActiveShifts() {
        return shiftRepository.findByStatus("ACTIVE");
    }
}
