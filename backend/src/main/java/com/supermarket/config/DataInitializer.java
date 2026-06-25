/**
 * 数据初始化器 —— 首次启动时自动创建演示数据。
 *
 * 包括：用户、员工、分类、供应商、商品、会员。
 *
 * @author 殷智元
 */
package com.supermarket.config;

import com.supermarket.entity.*;
import com.supermarket.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeRepository employeeRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("数据库已有 {} 个用户，跳过初始化。", userRepository.count());
            return;
        }

        log.info("========== 开始创建演示数据 ==========");

        // ==================== 1. 用户 ====================
        log.info("创建默认账号...");
        User admin = saveUser("admin", "123456", "系统管理员", "ROLE_ADMIN");
        User manager = saveUser("manager", "123456", "店长", "ROLE_MANAGER");
        User cashier = saveUser("cashier", "123456", "收银员", "ROLE_CASHIER");

        // ==================== 2. 员工 ====================
        log.info("创建员工档案...");
        Employee empAdmin = saveEmployee("殷智元", "13800000001", "admin@shop.com", "系统管理员", 8000, admin);
        Employee empManager = saveEmployee("徐磊", "13800000002", "manager@shop.com", "店长", 6000, manager);
        Employee empCashier = saveEmployee("章瀚", "13800000003", "cashier@shop.com", "收银员", 4000, cashier);

        // ==================== 3. 分类 ====================
        log.info("创建商品分类...");
        Category cBeverage = saveCategory("饮料", null, 1);
        Category cSoda = saveCategory("碳酸饮料", cBeverage.getId(), 1);
        Category cJuice = saveCategory("果汁", cBeverage.getId(), 2);
        Category cTea = saveCategory("茶饮", cBeverage.getId(), 3);

        Category cSnack = saveCategory("零食", null, 2);
        Category cChips = saveCategory("膨化食品", cSnack.getId(), 1);
        Category cCandy = saveCategory("糖果巧克力", cSnack.getId(), 2);
        Category cBiscuit = saveCategory("饼干糕点", cSnack.getId(), 3);

        Category cDaily = saveCategory("日用品", null, 3);
        Category cClean = saveCategory("清洁用品", cDaily.getId(), 1);
        Category cPaper = saveCategory("纸制品", cDaily.getId(), 2);
        Category cCare = saveCategory("个人护理", cDaily.getId(), 3);

        Category cAlcohol = saveCategory("酒类", null, 4);

        // ==================== 4. 供应商 ====================
        log.info("创建供应商...");
        Supplier sFood = saveSupplier("华润五丰食品有限公司", "王经理", "010-88886666", "北京市朝阳区光华路100号");
        Supplier sDaily = saveSupplier("宝洁日化（中国）有限公司", "李经理", "020-88887777", "广州市天河区珠江新城");
        Supplier sBeer = saveSupplier("青岛啤酒股份有限公司", "赵经理", "0532-88889999", "青岛市市北区登州路56号");

        // ==================== 5. 商品 ====================
        log.info("创建商品...");

        // 饮料类 — 碳酸饮料 (supplier: 华润五丰)
        saveProduct("6901234567890", "可口可乐 330ml", "罐", "3.00", "2.00", 100, 20, 500, cSoda, sFood);
        saveProduct("6901234567891", "百事可乐 330ml", "罐", "3.00", "2.00", 80, 20, 500, cSoda, sFood);
        saveProduct("6901234567892", "雪碧 330ml", "罐", "3.00", "2.00", 75, 20, 500, cSoda, sFood);

        // 饮料类 — 果汁
        saveProduct("6901234567893", "美汁源果粒橙 450ml", "瓶", "3.50", "2.50", 60, 15, 300, cJuice, sFood);
        saveProduct("6901234567894", "汇源橙汁 1L", "盒", "12.00", "8.50", 30, 10, 200, cJuice, sFood);

        // 饮料类 — 茶饮
        saveProduct("6901234567895", "农夫山泉 550ml", "瓶", "2.00", "1.20", 200, 50, 800, cTea, sFood);
        saveProduct("6901234567896", "东方树叶 500ml", "瓶", "4.00", "3.00", 45, 15, 200, cTea, sFood);

        // 零食类 — 膨化食品
        saveProduct("6901234567897", "乐事薯片原味 75g", "袋", "7.00", "4.50", 50, 10, 200, cChips, sFood);
        saveProduct("6901234567898", "旺旺仙贝 52g", "袋", "5.00", "3.00", 60, 15, 250, cChips, sFood);

        // 零食类 — 糖果巧克力
        saveProduct("6901234567899", "德芙丝滑巧克力 80g", "盒", "15.00", "10.00", 25, 5, 100, cCandy, sFood);
        saveProduct("6901234567900", "大白兔奶糖 200g", "袋", "12.00", "8.00", 40, 10, 150, cCandy, sFood);

        // 零食类 — 饼干糕点
        saveProduct("6901234567901", "奥利奥夹心饼干 97g", "盒", "8.00", "5.50", 45, 10, 180, cBiscuit, sFood);

        // 日用品 — 清洁用品 (supplier: 宝洁)
        saveProduct("6901234567902", "蓝月亮洗衣液 1kg", "瓶", "25.00", "18.00", 20, 5, 100, cClean, sDaily);
        saveProduct("6901234567903", "威猛先生洁厕液 500ml", "瓶", "9.00", "6.00", 35, 10, 150, cClean, sDaily);

        // 日用品 — 纸制品
        saveProduct("6901234567904", "维达抽纸 3包装", "提", "12.00", "8.00", 35, 10, 150, cPaper, sDaily);
        saveProduct("6901234567905", "心相印卷纸 10卷装", "提", "22.00", "16.00", 25, 8, 120, cPaper, sDaily);

        // 日用品 — 个人护理（设置海飞丝低库存用于展示预警）
        Product shampoo = saveProduct("6901234567906", "海飞丝去屑洗发水 200ml", "瓶", "35.00", "25.00", 3, 5, 50, cCare, sDaily);

        // 酒类 (supplier: 青岛啤酒)
        saveProduct("6901234567907", "青岛啤酒经典 500ml", "罐", "5.00", "3.50", 120, 20, 500, cAlcohol, sBeer);
        saveProduct("6901234567908", "张裕解百纳干红 750ml", "瓶", "88.00", "60.00", 10, 3, 50, cAlcohol, sBeer);

        // ==================== 6. 会员 ====================
        log.info("创建会员...");
        saveMember("M00000001", "张三", "13900001111", "110101199001011234", "男", 1,
            LocalDateTime.now().plusYears(1));
        saveMember("M00000002", "李四", "13900002222", "110101199508082345", "女", 1,
            LocalDateTime.now().minusDays(1)); // 已过期，用于演示

        log.info("========== 演示数据创建完成！ ==========");
        log.info("  账号：admin / manager / cashier，密码都是 123456");
        log.info("  商品：20 个，覆盖饮料/零食/日用品/酒类");
        log.info("  会员：M00000001(有效) / M00000002(已过期)");
        log.info("  海飞丝库存仅 3 件(下限5)，可用于展示缺货预警");
    }

    // ==================== 辅助方法 ====================

    private User saveUser(String username, String password, String displayName, String role) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(password));
        u.setDisplayName(displayName);
        u.setRole(role);
        u.setStatus(1);
        return userRepository.save(u);
    }

    private Employee saveEmployee(String name, String phone, String email, String position, int salary, User user) {
        Employee e = new Employee();
        e.setName(name);
        e.setPhone(phone);
        e.setEmail(email);
        e.setPosition(position);
        e.setSalary(BigDecimal.valueOf(salary));
        e.setHireDate(LocalDate.of(2025, 9, 1));
        e.setStatus(1);
        e.setUser(user);
        return employeeRepository.save(e);
    }

    private Category saveCategory(String name, Long parentId, int sortOrder) {
        Category c = new Category();
        c.setName(name);
        c.setParentId(parentId);
        c.setSortOrder(sortOrder);
        c.setStatus(1);
        return categoryRepository.save(c);
    }

    private Supplier saveSupplier(String name, String contact, String phone, String address) {
        Supplier s = new Supplier();
        s.setName(name);
        s.setContact(contact);
        s.setPhone(phone);
        s.setAddress(address);
        s.setStatus(1);
        return supplierRepository.save(s);
    }

    private Product saveProduct(String barcode, String name, String unit,
                                 String salePrice, String purchasePrice,
                                 int stock, int minStock, int maxStock,
                                 Category category, Supplier supplier) {
        Product p = new Product();
        p.setBarcode(barcode);
        p.setName(name);
        p.setUnit(unit);
        p.setSalePrice(new BigDecimal(salePrice));
        p.setPurchasePrice(new BigDecimal(purchasePrice));
        p.setStock(stock);
        p.setMinStock(minStock);
        p.setMaxStock(maxStock);
        p.setStatus(1);
        p.setCategory(category);
        p.setSupplier(supplier);
        return productRepository.save(p);
    }

    private Member saveMember(String cardNo, String name, String phone, String idCard,
                               String gender, int status, LocalDateTime validUntil) {
        Member m = new Member();
        m.setCardNo(cardNo);
        m.setName(name);
        m.setPhone(phone);
        m.setIdCard(idCard);
        m.setGender(gender);
        m.setStatus(status);
        m.setPoints(0);
        m.setTotalSpent(BigDecimal.ZERO);
        m.setValidUntil(validUntil);
        return memberRepository.save(m);
    }
}
