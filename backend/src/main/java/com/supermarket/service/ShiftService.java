/**
 * 换班服务 —— 开始班次、结束班次（交班）、换班记录查询。
 *
 * @author 徐磊
 */
package com.supermarket.service;

import com.supermarket.entity.Employee;
import com.supermarket.entity.Shift;
import com.supermarket.repository.EmployeeRepository;
import com.supermarket.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;

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
        shift.setTotalAmount(java.math.BigDecimal.ZERO);
        shift.setStatus("ACTIVE");
        return shiftRepository.save(shift);
    }

    /**
     * 结束班次（交班）。
     * 更新当班交易统计并关闭班次。
     */
    @Transactional
    public Shift endShift(String username) {
        Employee cashier = employeeRepository.findByUserUsername(username)
            .orElseThrow(() -> new IllegalStateException("找不到收银员信息"));
        Shift shift = shiftRepository.findByCashierIdAndStatus(cashier.getId(), "ACTIVE")
            .orElseThrow(() -> new NoSuchElementException("没有活跃的班次"));
        shift.setEndTime(LocalDateTime.now());
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
