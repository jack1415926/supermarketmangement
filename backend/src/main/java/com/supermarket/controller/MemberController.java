/**
 * 会员控制器 —— /api/members
 *
 * @author 徐磊
 */
package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.dto.MemberRequest;
import com.supermarket.dto.MemberVerifyResponse;
import com.supermarket.dto.PageDTO;
import com.supermarket.entity.Member;
import com.supermarket.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /** 分页查询会员列表 */
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<PageDTO<Member>> findAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String keyword
    ) {
        return Result.success(memberService.findAll(page, size, keyword));
    }

    /** 注册会员 */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Member> create(@Valid @RequestBody MemberRequest request) {
        return Result.success(memberService.create(request));
    }

    /** 会员卡续期 */
    @PutMapping("/{id}/renew")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Member> renew(@PathVariable Long id) {
        return Result.success(memberService.renew(id));
    }

    /**
     * 会员卡验证（POS 收银时刷卡验证）。
     * 此端点需要收银员、管理员或系统管理员角色。
     */
    @GetMapping("/verify/{cardNo}")
    @PreAuthorize("hasAnyRole('CASHIER','MANAGER','ADMIN')")
    public Result<MemberVerifyResponse> verify(@PathVariable String cardNo) {
        return Result.success(memberService.verifyByCardNo(cardNo));
    }
}
