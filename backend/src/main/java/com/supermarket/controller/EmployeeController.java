/**
 * 员工控制器 —— /api/employees
 *
 * @author 徐磊
 */
package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Employee;
import com.supermarket.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "员工列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<List<Employee>> findAll() {
        return Result.success(employeeService.findAll());
    }

    @Operation(summary = "查询单个员工")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Employee> findById(@PathVariable Long id) {
        return Result.success(employeeService.findById(id));
    }

    @Operation(summary = "新增员工")
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Employee> create(@RequestBody Employee employee) {
        return Result.success(employeeService.create(employee));
    }

    @Operation(summary = "更新员工信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Employee> update(@PathVariable Long id, @RequestBody Employee employee) {
        return Result.success(employeeService.update(id, employee));
    }

    @Operation(summary = "员工离职（软删除）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        employeeService.deactivate(id);
        return Result.success();
    }
}
