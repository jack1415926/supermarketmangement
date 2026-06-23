/**
 * SaleService 单元测试 —— POS 结账（最复杂的业务逻辑）。
 *
 * @author 殷智元
 */
package com.supermarket.service;

import com.supermarket.dto.SaleRequest;
import com.supermarket.dto.SaleResponse;
import com.supermarket.entity.*;
import com.supermarket.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaleService — 销售结账服务测试")
class SaleServiceTest {

    @Mock private SaleRepository saleRepository;
    @Mock private ProductRepository productRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks
    private SaleService saleService;

    private Product cola;
    private Employee cashier;
    private Member member;
    private SaleRequest request;

    @BeforeEach
    void setUp() {
        // --- 商品：可口可乐，库存 100，售价 3 元 ---
        cola = new Product();
        cola.setId(1L);
        cola.setBarcode("6901234567890");
        cola.setName("可口可乐 330ml");
        cola.setSalePrice(new BigDecimal("3.00"));
        cola.setPurchasePrice(new BigDecimal("2.00"));
        cola.setStock(100);
        cola.setMinStock(10);
        cola.setMaxStock(500);
        cola.setStatus(1);

        // --- 收银员 ---
        User user = new User();
        user.setUsername("cashier01");
        cashier = new Employee();
        cashier.setId(1L);
        cashier.setName("张三");
        cashier.setUser(user);

        // --- 会员 ---
        member = new Member();
        member.setId(1L);
        member.setCardNo("M00000001");
        member.setName("李四");
        member.setStatus(1);
        member.setValidUntil(LocalDateTime.now().plusDays(30));
        member.setTotalSpent(BigDecimal.ZERO);
        member.setPoints(0);

        // --- 结账请求：买 5 瓶可乐 ---
        SaleRequest.SaleItemRequest item = new SaleRequest.SaleItemRequest();
        item.setProductId(1L);
        item.setQuantity(5);

        request = new SaleRequest();
        request.setItems(List.of(item));
        request.setPaymentMethod("CASH");
        request.setReceivedAmount(new BigDecimal("20.00"));
    }

    @Test
    @DisplayName("正常结账（无会员）：应正确计算总金额和找零")
    void checkout_noMember() {
        // given
        when(employeeRepository.findByUserUsername("cashier01")).thenReturn(Optional.of(cashier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(cola));
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> {
            Sale s = inv.getArgument(0);
            s.setId(1L); // 模拟数据库分配 ID
            return s;
        });
        when(productRepository.saveAll(anyList())).thenReturn(List.of(cola));

        // when
        SaleResponse resp = saleService.checkout(request, "cashier01");

        // then:
        // 总金额 = 3.00 × 5 = 15.00
        assertEquals(0, new BigDecimal("15.00").compareTo(resp.getTotalAmount()));
        // 无会员，折扣为 0
        assertEquals(0, BigDecimal.ZERO.compareTo(resp.getDiscountAmount()));
        // 实收 20，找零 = 20 - 15 = 5
        assertEquals(0, new BigDecimal("5.00").compareTo(resp.getChangeAmount()));
        // 库存从 100 扣到 95
        assertEquals(95, cola.getStock());
        // 流水号非空
        assertNotNull(resp.getFlowNo());
        // 收银员名称
        assertEquals("张三", resp.getCashierName());
    }

    @Test
    @DisplayName("会员结账：应享受 95 折优惠，并累计积分")
    void checkout_withMember() {
        // given
        request.setMemberCardNo("M00000001");
        when(employeeRepository.findByUserUsername("cashier01")).thenReturn(Optional.of(cashier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(cola));
        when(memberRepository.findByCardNo("M00000001")).thenReturn(Optional.of(member));
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> {
            Sale s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });
        when(productRepository.saveAll(anyList())).thenReturn(List.of(cola));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // when
        SaleResponse resp = saleService.checkout(request, "cashier01");

        // then:
        // 总金额 = 15.00
        assertEquals(0, new BigDecimal("15.00").compareTo(resp.getTotalAmount()));
        // 折扣 = 15.00 × 0.05 = 0.75
        assertEquals(0, new BigDecimal("0.75").compareTo(resp.getDiscountAmount()));
        // 应收 = 15.00 - 0.75 = 14.25
        assertEquals(0, new BigDecimal("14.25").compareTo(resp.getFinalAmount()));
        // 找零 = 20.00 - 14.25 = 5.75
        assertEquals(0, new BigDecimal("5.75").compareTo(resp.getChangeAmount()));
        // 会员积分：14.25 / 10 = 1 积分
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("实收金额不足：应抛出异常")
    void checkout_insufficientPayment() {
        request.setReceivedAmount(new BigDecimal("1.00")); // 应收 15，只付 1 元
        when(employeeRepository.findByUserUsername("cashier01")).thenReturn(Optional.of(cashier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(cola));

        assertThrows(IllegalArgumentException.class,
            () -> saleService.checkout(request, "cashier01"));
    }

    @Test
    @DisplayName("库存不足：应抛出异常")
    void checkout_insufficientStock() {
        cola.setStock(2); // 需要 5 件，库存只有 2 件
        when(employeeRepository.findByUserUsername("cashier01")).thenReturn(Optional.of(cashier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(cola));

        assertThrows(IllegalArgumentException.class,
            () -> saleService.checkout(request, "cashier01"));
    }

    @Test
    @DisplayName("商品已下架：应抛出异常")
    void checkout_productInactive() {
        cola.setStatus(0);
        when(employeeRepository.findByUserUsername("cashier01")).thenReturn(Optional.of(cashier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(cola));

        assertThrows(IllegalArgumentException.class,
            () -> saleService.checkout(request, "cashier01"));
    }

    @Test
    @DisplayName("会员卡过期：应抛出异常（会员验证在商品校验之前，product stub 不会被调用）")
    void checkout_memberExpired() {
        member.setValidUntil(LocalDateTime.now().minusDays(1)); // 昨天过期
        request.setMemberCardNo("M00000001");
        when(employeeRepository.findByUserUsername("cashier01")).thenReturn(Optional.of(cashier));
        // 会员卡过期会在商品校验之前就抛出异常，所以 productRepository 不会被调用
        when(memberRepository.findByCardNo("M00000001")).thenReturn(Optional.of(member));

        assertThrows(IllegalArgumentException.class,
            () -> saleService.checkout(request, "cashier01"));
    }

    @Test
    @DisplayName("流水号唯一：两次结账的流水号不应相同")
    void flowNumber_unique() throws InterruptedException {
        when(employeeRepository.findByUserUsername("cashier01")).thenReturn(Optional.of(cashier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(cola));
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> {
            Sale s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });
        when(productRepository.saveAll(anyList())).thenReturn(List.of(cola));

        SaleResponse resp1 = saleService.checkout(request, "cashier01");
        // 短暂等待确保 UUID 不同，然后重新 mock
        Thread.sleep(10);
        SaleResponse resp2 = saleService.checkout(request, "cashier01");

        assertNotEquals(resp1.getFlowNo(), resp2.getFlowNo());
    }
}
