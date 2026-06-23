/**
 * 换班控制器 —— /api/shifts
 *
 * @author 徐磊
 */
package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Shift;
import com.supermarket.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    /** 开始班次（收银员登录后调用） */
    @PostMapping("/start")
    @PreAuthorize("hasAnyRole('CASHIER','MANAGER','ADMIN')")
    public Result<Shift> startShift(@AuthenticationPrincipal UserDetails userDetails) {
        return Result.success("班次已开始", shiftService.startShift(userDetails.getUsername()));
    }

    /** 结束班次（交班） */
    @PostMapping("/end")
    @PreAuthorize("hasAnyRole('CASHIER','MANAGER','ADMIN')")
    public Result<Shift> endShift(@AuthenticationPrincipal UserDetails userDetails) {
        return Result.success("交班成功", shiftService.endShift(userDetails.getUsername()));
    }

    /** 查询当前活跃班次 */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('CASHIER','MANAGER','ADMIN')")
    public Result<Shift> getActiveShift(@AuthenticationPrincipal UserDetails userDetails) {
        Shift shift = shiftService.getActiveShift(userDetails.getUsername());
        return Result.success(shift);
    }

    /** 查询某收银员的所有班次记录 */
    @GetMapping("/cashier/{cashierId}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<List<Shift>> findByCashier(@PathVariable Long cashierId) {
        return Result.success(shiftService.findByCashier(cashierId));
    }

    /** 查询所有活跃班次（管理员查看当前在岗收银员） */
    @GetMapping("/active-all")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<List<Shift>> findActiveShifts() {
        return Result.success(shiftService.findActiveShifts());
    }
}
