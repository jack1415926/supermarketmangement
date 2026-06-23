/**
 * 会员服务 —— 会员注册、续期、刷卡验证、消费记录查询。
 *
 * @author 徐磊
 */
package com.supermarket.service;

import com.supermarket.dto.MemberRequest;
import com.supermarket.dto.MemberVerifyResponse;
import com.supermarket.dto.PageDTO;
import com.supermarket.entity.Member;
import com.supermarket.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /** 分页查询，支持姓名搜索 */
    public PageDTO<Member> findAll(int page, int size, String keyword) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Member> result;
        if (keyword != null && !keyword.isBlank()) {
            result = memberRepository.findByNameContaining(keyword, pageable);
        } else {
            result = memberRepository.findAll(pageable);
        }
        return PageDTO.from(result);
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("会员不存在: id=" + id));
    }

    /** 注册会员（生成卡号，有效期一年） */
    @Transactional
    public Member create(MemberRequest request) {
        if (memberRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("手机号已被注册: " + request.getPhone());
        }
        Member member = new Member();
        member.setName(request.getName());
        member.setPhone(request.getPhone());
        member.setIdCard(request.getIdCard());
        member.setGender(request.getGender());
        // 卡号：用户指定或自动生成 "M" + 当前时间毫秒数
        member.setCardNo(request.getCardNo() != null ? request.getCardNo()
            : "M" + System.currentTimeMillis());
        member.setPoints(0);
        member.setTotalSpent(BigDecimal.ZERO);
        member.setValidUntil(LocalDateTime.now().plusYears(1));
        member.setStatus(1);
        return memberRepository.save(member);
    }

    /** 会员卡续期（延长一年） */
    @Transactional
    public Member renew(Long id) {
        Member member = findById(id);
        member.setValidUntil(member.getValidUntil().plusYears(1));
        member.setStatus(1); // 如果已过期注销，续期后重新激活
        return memberRepository.save(member);
    }

    /**
     * 会员卡验证（POS 刷卡时调用）。
     * 返回会员信息和折扣率。
     */
    public MemberVerifyResponse verifyByCardNo(String cardNo) {
        Member member = memberRepository.findByCardNo(cardNo)
            .orElseThrow(() -> new NoSuchElementException("会员卡不存在: " + cardNo));

        MemberVerifyResponse response = new MemberVerifyResponse();
        response.setId(member.getId());
        response.setCardNo(member.getCardNo());
        response.setName(member.getName());
        response.setPhone(member.getPhone());
        response.setTotalSpent(member.getTotalSpent());
        response.setPoints(member.getPoints());
        response.setValidUntil(member.getValidUntil());

        // 判断是否在有效期内
        boolean isExpired = member.getValidUntil().isBefore(LocalDateTime.now());
        boolean isActive = member.getStatus() == 1;
        response.setValid(isActive && !isExpired);

        // 有效会员享受 95 折
        response.setDiscountRate(response.isValid() ? new BigDecimal("0.95") : BigDecimal.ONE);

        if (!isActive) {
            response.setMessage("会员卡已注销");
        } else if (isExpired) {
            response.setMessage("会员卡已过期，请续卡");
        } else {
            response.setMessage("会员卡有效，享受95折优惠");
        }

        return response;
    }
}
