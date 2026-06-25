-- =============================================================================
-- 小型超市管理系统 - 测试数据
-- 作者：C 角（数据库 + 测试 + 文档）
--
-- 数据量：3 用户 + 6 分类 + 3 供应商 + 12 商品 + 5 员工 + 5 会员
--        + 2 进货单 + 4 进货明细 + 5 销售单 + 8 销售明细 + 2 换班
--
-- 默认密码：所有用户都是 "123456"
-- 对应 BCrypt hash：$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2
-- 如登录失败，把 backend/tools/PasswordResetRunner.java 加进去跑一次即可
-- （说明见文件头注释）
--
-- 销售单金额一致性原则：
--   total_amount      = sum(sale_items.subtotal)
--   discount_amount   = total_amount × 0.05 (有会员) 或 0 (无会员)
--   received_amount   = 顾客实付
--   change_amount     = received_amount - (total_amount - discount_amount)
-- =============================================================================

-- 关闭外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 清空已有数据（按依赖反序）
TRUNCATE TABLE shifts;
TRUNCATE TABLE sale_items;
TRUNCATE TABLE sales;
TRUNCATE TABLE purchase_items;
TRUNCATE TABLE purchases;
TRUNCATE TABLE members;
TRUNCATE TABLE products;
TRUNCATE TABLE categories;
TRUNCATE TABLE suppliers;
TRUNCATE TABLE employees;
TRUNCATE TABLE users;

-- =============================================================================
-- 1. 系统用户（3 条）
-- password 字段 = BCrypt("123456")
-- 说明：role 字段值带 "ROLE_" 前缀，与 Spring Security 的 @PreAuthorize("hasRole('ADMIN')")
--       配合（Spring 会自动给 "ADMIN" 加 "ROLE_" 前缀，但 DB 里存的就是完整字符串）
-- 如登录失败（hash 不匹配），启用 backend/tools/PasswordResetRunner.java 自动重置
-- =============================================================================
INSERT INTO users (id, username, password, display_name, role, status, created_at, updated_at) VALUES
(1, 'admin',   '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', 'ROLE_ADMIN',   1, NOW(), NOW()),
(2, 'manager', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '李店长',     'ROLE_MANAGER', 1, NOW(), NOW()),
(3, 'cashier', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '收银员小张', 'ROLE_CASHIER', 1, NOW(), NOW());

-- =============================================================================
-- 2. 商品分类（6 条，2 级）
-- =============================================================================
INSERT INTO categories (id, name, parent_id, sort_order, status, created_at, updated_at) VALUES
(1, '食品',     NULL, 1, 1, NOW(), NOW()),
(2, '饮品',     NULL, 2, 1, NOW(), NOW()),
(3, '日用品',   NULL, 3, 1, NOW(), NOW()),
(4, '休闲食品', 1,    1, 1, NOW(), NOW()),
(5, '粮油调味', 1,    2, 1, NOW(), NOW()),
(6, '碳酸饮料', 2,    1, 1, NOW(), NOW());

-- =============================================================================
-- 3. 供应商（3 条）
-- =============================================================================
INSERT INTO suppliers (id, name, contact, phone, address, status, created_at, updated_at) VALUES
(1, '可口可乐华南分公司', '王经理', '13800138001', '广东省深圳市南山区科技园路 1 号',  1, NOW(), NOW()),
(2, '蒙牛集团供应链',     '刘经理', '13900139002', '内蒙古呼和浩特市和林格尔县盛乐经济园区', 1, NOW(), NOW()),
(3, '立白日化集团',       '陈经理', '13700137003', '广东省广州市天河区珠江新城 88 号',      1, NOW(), NOW());

-- =============================================================================
-- 4. 商品（12 条）
-- 涵盖 4 个类别，库存涵盖：充足 / 临界预警 / 缺货 3 种状态
-- =============================================================================
INSERT INTO products (id, barcode, name, unit, sale_price, purchase_price, stock, min_stock, max_stock, production_date, shelf_life_days, status, category_id, supplier_id, version, created_at, updated_at) VALUES
-- 碳酸饮料（供应商 1，分类 6）
(1,  '6901234567890', '可口可乐 330ml',     '瓶',  3.50,  2.20, 200,  50, 500, '2026-05-01 00:00:00', 365, 1, 6, 1, 0, NOW(), NOW()),
(2,  '6901234567891', '雪碧 330ml',         '瓶',  3.50,  2.20, 180,  50, 500, '2026-05-15 00:00:00', 365, 1, 6, 1, 0, NOW(), NOW()),
(3,  '6901234567892', '芬达橙味 330ml',     '瓶',  3.00,  1.90, 150,  50, 500, '2026-04-20 00:00:00', 365, 1, 6, 1, 0, NOW(), NOW()),

-- 休闲食品（分类 4）
(4,  '6901234567893', '乐事薯片原味 70g',  '袋',  6.50,  4.00,   0,  20, 200, '2026-03-10 00:00:00', 180, 1, 4, 1, 0, NOW(), NOW()),
(5,  '6901234567894', '好丽友派 6 个装',    '盒', 18.80, 12.00,   5,  10, 100, '2026-06-10 00:00:00',  90, 1, 4, 1, 0, NOW(), NOW()),
(6,  '6901234567895', '老干妈辣酱 280g',    '瓶', 12.80,  8.00,  80,  20, 200, '2026-01-15 00:00:00', 540, 1, 4, 1, 0, NOW(), NOW()),

-- 粮油调味（分类 5）
(7,  '6901234567896', '鲁花花生油 5L',      '桶', 138.00, 98.00,  30,  10, 100, '2026-02-20 00:00:00', 540, 1, 5, 1, 0, NOW(), NOW()),
(8,  '6901234567897', '海天生抽 500ml',     '瓶',   8.50,  5.20,  60,  20, 150, '2026-03-25 00:00:00', 540, 1, 5, 1, 0, NOW(), NOW()),

-- 乳制品（食品分类 1，供应商 2）
(9,  '6901234567898', '蒙牛纯牛奶 250ml',   '盒',   3.20,  2.00, 300, 100, 600, '2026-06-18 00:00:00', 180, 1, 1, 2, 0, NOW(), NOW()),
(10, '6901234567899', '蒙牛酸奶 100g×8',    '排',  12.50,  8.00, 120,  30, 300, '2026-06-15 00:00:00',  21, 1, 1, 2, 0, NOW(), NOW()),

-- 日用品（分类 3，供应商 3）
(11, '6920000000001', '立白洗洁精 1.2kg',   '瓶',   9.90,  6.00, 100,  30, 300, NULL, NULL, 1, 3, 3, 0, NOW(), NOW()),
(12, '6920000000002', '舒肤佳沐浴露 720ml', '瓶',  39.90, 25.00,  40,  15, 150, NULL, NULL, 1, 3, 3, 0, NOW(), NOW());

-- =============================================================================
-- 5. 员工（5 条）
-- 关联系统账号：员工 1 → cashier、员工 3 → manager；其他暂未开通账号
-- =============================================================================
INSERT INTO employees (id, name, phone, email, position, salary, hire_date, status, user_id, created_at, updated_at) VALUES
(1, '张磊', '13800138001', 'zhanglei@supermarket.local', '收银员', 3500.00, '2024-03-15', 1, 3,    NOW(), NOW()),
(2, '王芳', '13800138002', 'wangfang@supermarket.local', '收银员', 3800.00, '2024-05-20', 1, NULL, NOW(), NOW()),
(3, '刘强', '13800138003', 'liuqiang@supermarket.local', '店长',   6500.00, '2022-08-10', 1, 2,    NOW(), NOW()),
(4, '李娜', '13800138004', 'lina@supermarket.local',     '采购员', 5000.00, '2023-11-05', 1, NULL, NOW(), NOW()),
(5, '陈伟', '13800138005', 'chenwei@supermarket.local',  '收银员', 3500.00, '2025-09-01', 1, NULL, NOW(), NOW());

-- =============================================================================
-- 6. 会员（5 条）
-- 状态：1 正常 / 0 注销；valid_until 涵盖 3 种情况（有效 / 即将过期 / 已过期）
-- =============================================================================
INSERT INTO members (id, card_no, name, phone, id_card, gender, birth_date, points, total_spent, valid_until, status, created_at, updated_at) VALUES
(1, 'M00000001', '赵丽华', '13900139001', '440101199003151234', '女', '1990-03-15 00:00:00',  120,   856.50, '2027-06-22 00:00:00', 1, '2024-06-22 10:00:00', NOW()),
(2, 'M00000002', '孙建国', '13900139002', '440101198512203456', '男', '1985-12-20 00:00:00',   45,   320.00, '2026-12-10 00:00:00', 1, '2024-06-22 10:30:00', NOW()),
(3, 'M00000003', '周晓敏', '13900139003', '440101199507082345', '女', '1995-07-08 00:00:00',  680,  4580.30, '2026-08-15 00:00:00', 1, '2023-08-15 14:20:00', NOW()),
(4, 'M00000004', '吴海涛', '13900139004', '440101198802144567', '男', '1988-02-14 00:00:00',   20,   150.00, '2025-06-01 00:00:00', 0, '2023-06-01 09:15:00', NOW()),
(5, 'M00000005', '郑文静', '13900139005', '440101199211295678', '女', '1992-11-29 00:00:00', 1850, 12500.80, '2027-01-20 00:00:00', 1, '2024-01-20 11:45:00', NOW());

-- =============================================================================
-- 7. 进货单（2 条）+ 进货明细（4 条）
-- =============================================================================
INSERT INTO purchases (id, purchase_no, supplier_id, employee_id, total_amount, status, remark, created_at, updated_at) VALUES
(1, 'P20260620001', 1, 4,  880.00, 'COMPLETED', '可口可乐 6 箱到货', '2026-06-20 09:30:00', '2026-06-20 09:45:00'),
(2, 'P20260621001', 2, 4, 1080.00, 'PENDING',   '蒙牛酸奶本周订单',   '2026-06-21 14:20:00', '2026-06-21 14:20:00');

INSERT INTO purchase_items (id, purchase_id, product_id, quantity, purchase_price, subtotal) VALUES
(1, 1, 1, 200, 2.20, 440.00),
(2, 1, 2, 200, 2.20, 440.00),
(3, 2, 9, 300, 2.00, 600.00),
(4, 2, 10, 60, 8.00, 480.00);

-- 校验：purchase 1 明细合计 = 440 + 440 = 880.00 ✓
-- 校验：purchase 2 明细合计 = 600 + 480 = 1080.00 ✓

-- =============================================================================
-- 8. 销售单（5 条）+ 销售明细（8 条）
-- 全部金额经过校验：
--   total = sum(sale_items.subtotal)
--   discount = total × 0.05 (有会员) | 0
--   change = received - (total - discount)
-- =============================================================================
INSERT INTO sales (id, flow_no, cashier_id, member_id, total_amount, discount_amount, received_amount, change_amount, payment_method, status, remark, created_at) VALUES
-- 销售 1：收银员张磊卖给赵丽华，2 瓶可口可乐，会员 95 折
-- items: 2 × 3.50 = 7.00; discount = 7.00 × 0.05 = 0.35; final = 6.65; change = 10.00 - 6.65 = 3.35
(1, 'S20260622001', 1, 1,    7.00, 0.35, 10.00, 3.35, 'CASH',   'COMPLETED', '', '2026-06-22 09:15:32'),

-- 销售 2：收银员张磊卖给周晓敏，好丽友派 + 芬达 + 海天生抽，会员 95 折
-- items: 18.80 + 3.00 + 8.50 = 30.30
-- discount = 30.30 × 0.05 = 1.515，四舍五入到分 = 1.52
-- final = 30.30 - 1.52 = 28.78；实付 30.00，change = 1.22
(2, 'S20260622002', 1, 3,   30.30, 1.52, 30.00, 1.22, 'WECHAT', 'COMPLETED', '顾客买了点零食', '2026-06-22 10:42:18'),

-- 销售 3：收银员王芳卖给非会员，蒙牛纯牛奶 + 海天生抽，无折扣
-- items: 3.20 + 8.50 = 11.70；final = 11.70；change = 0
(3, 'S20260622003', 2, NULL, 11.70, 0.00, 11.70, 0.00, 'ALIPAY', 'COMPLETED', '', '2026-06-22 11:08:55'),

-- 销售 4：收银员张磊卖给郑文静 (VIP)，2 盒蒙牛纯牛奶，会员 95 折
-- items: 2 × 3.20 = 6.40；discount = 0.32；final = 6.08；实付 10.00，change = 3.92
(4, 'S20260622004', 1, 5,    6.40, 0.32, 10.00, 3.92, 'CASH',   'COMPLETED', 'VIP 顾客',     '2026-06-22 14:30:10'),

-- 销售 5：收银员张磊卖给非会员，1 瓶雪碧
-- items: 3.50；final = 3.50；change = 0
(5, 'S20260622005', 1, NULL, 3.50, 0.00,  3.50, 0.00, 'CARD',   'COMPLETED', '', '2026-06-22 15:55:42');

INSERT INTO sale_items (id, sale_id, product_id, quantity, sale_price, subtotal) VALUES
-- 销售 1 的明细：2 瓶可口可乐
(1, 1, 1, 2, 3.50, 7.00),
-- 销售 2 的明细：好丽友派 + 芬达 + 海天生抽
(2, 2, 5, 1, 18.80, 18.80),
(3, 2, 3, 1, 3.00, 3.00),
(4, 2, 8, 1, 8.50, 8.50),
-- 销售 3 的明细：蒙牛纯牛奶 + 海天生抽
(5, 3, 9, 1, 3.20, 3.20),
(6, 3, 8, 1, 8.50, 8.50),
-- 销售 4 的明细：2 盒蒙牛纯牛奶
(7, 4, 9, 2, 3.20, 6.40),
-- 销售 5 的明细：1 瓶雪碧
(8, 5, 2, 1, 3.50, 3.50);

-- =============================================================================
-- 9. 换班记录（2 条）
-- =============================================================================
INSERT INTO shifts (id, cashier_id, start_time, end_time, transaction_count, total_amount, status) VALUES
-- 上一班：张磊 早班（8:00-16:00），处理 4 笔交易，合计 47.20 元
(1, 1, '2026-06-22 08:00:00', '2026-06-22 16:00:00', 4, 47.20, 'CLOSED'),
-- 当前班：王芳 晚班（16:00 开始），已处理 1 笔，11.70 元
(2, 2, '2026-06-22 16:00:00', NULL,                  1, 11.70, 'ACTIVE');

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 一致性自检 SQL（执行后所有"差异"查询应无返回行）
-- =============================================================================
-- 1. 销售单总额 vs 明细合计
-- SELECT s.id, s.total_amount, IFNULL(SUM(si.subtotal), 0) AS items_sum
--   FROM sales s LEFT JOIN sale_items si ON s.id = si.sale_id
--   GROUP BY s.id, s.total_amount
--   HAVING s.total_amount <> items_sum;

-- 2. 销售单找零 = 实收 - (总额 - 折扣)
-- SELECT id, change_amount, ROUND(received_amount - (total_amount - discount_amount), 2) AS expected
--   FROM sales
--   HAVING ABS(change_amount - expected) > 0.01;

-- 3. 进货单总额 vs 明细合计
-- SELECT p.id, p.total_amount, IFNULL(SUM(pi.subtotal), 0) AS items_sum
--   FROM purchases p LEFT JOIN purchase_items pi ON p.id = pi.purchase_id
--   GROUP BY p.id, p.total_amount
--   HAVING p.total_amount <> items_sum;

-- 4. 数据条数总览
-- SELECT 'users' AS tbl, COUNT(*) AS cnt FROM users
-- UNION ALL SELECT 'employees',         COUNT(*) FROM employees
-- UNION ALL SELECT 'categories',        COUNT(*) FROM categories
-- UNION ALL SELECT 'suppliers',         COUNT(*) FROM suppliers
-- UNION ALL SELECT 'products',          COUNT(*) FROM products
-- UNION ALL SELECT 'members',           COUNT(*) FROM members
-- UNION ALL SELECT 'purchases',         COUNT(*) FROM purchases
-- UNION ALL SELECT 'purchase_items',    COUNT(*) FROM purchase_items
-- UNION ALL SELECT 'sales',             COUNT(*) FROM sales
-- UNION ALL SELECT 'sale_items',        COUNT(*) FROM sale_items
-- UNION ALL SELECT 'shifts',            COUNT(*) FROM shifts;
-- 期望：3 / 5 / 6 / 3 / 12 / 5 / 2 / 4 / 5 / 8 / 2
