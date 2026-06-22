-- =============================================================================
-- 小型超市管理系统 - 数据库 Schema
-- 作者：C 角（数据库 + 测试 + 文档）
-- 数据库：MySQL 8.0
-- 字符集：utf8mb4（支持中文 + emoji）
-- 排序规则：utf8mb4_unicode_ci
--
-- 表结构严格对齐后端 JPA 实体类的 @Table / @Column 注解
-- 运行前请先创建数据库：CREATE DATABASE supermarket DEFAULT CHARSET utf8mb4;
-- =============================================================================

-- 关闭外键检查（避免 DROP 顺序问题）
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- 1. users（系统用户表）
-- =============================================================================
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username     VARCHAR(50)  NOT NULL                COMMENT '登录用户名',
    password     VARCHAR(100) NOT NULL                COMMENT 'BCrypt 加密后的密码',
    display_name VARCHAR(50)                          COMMENT '显示名称（真实姓名）',
    role         VARCHAR(20)  NOT NULL                COMMENT '角色：CASHIER/MANAGER/ADMIN',
    status       INT          NOT NULL DEFAULT 1      COMMENT '状态：1启用 0禁用',
    created_at   DATETIME                              COMMENT '创建时间',
    updated_at   DATETIME                              COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    KEY idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- =============================================================================
-- 2. employees（员工表）
-- =============================================================================
DROP TABLE IF EXISTS employees;
CREATE TABLE employees (
    id         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    name       VARCHAR(50)   NOT NULL                COMMENT '员工姓名',
    phone      VARCHAR(20)                            COMMENT '手机号',
    email      VARCHAR(100)                           COMMENT '邮箱',
    position   VARCHAR(50)                            COMMENT '职位：收银员/店长/仓库管理员等',
    salary     DECIMAL(10,2)                          COMMENT '月薪',
    hire_date  DATE                                   COMMENT '入职日期',
    status     INT           NOT NULL DEFAULT 1       COMMENT '状态：1在职 0离职',
    user_id    BIGINT                                 COMMENT '关联的登录账号 ID',
    created_at DATETIME                               COMMENT '创建时间',
    updated_at DATETIME                               COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_employees_user_id (user_id),
    KEY idx_employees_status (status),
    KEY idx_employees_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工表';

-- =============================================================================
-- 3. categories（商品分类表，自引用）
-- =============================================================================
DROP TABLE IF EXISTS categories;
CREATE TABLE categories (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    name       VARCHAR(50) NOT NULL                COMMENT '分类名称',
    parent_id  BIGINT                              COMMENT '父分类 ID，NULL=一级分类',
    sort_order INT         NOT NULL DEFAULT 0      COMMENT '同级排序号，越小越靠前',
    status     INT         NOT NULL DEFAULT 1      COMMENT '状态：1启用 0禁用',
    created_at DATETIME                             COMMENT '创建时间',
    updated_at DATETIME                             COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_name (name),
    KEY idx_categories_parent_id (parent_id),
    KEY idx_categories_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- =============================================================================
-- 4. suppliers（供应商表）
-- =============================================================================
DROP TABLE IF EXISTS suppliers;
CREATE TABLE suppliers (
    id         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    name       VARCHAR(100)  NOT NULL                COMMENT '供应商名称（公司名）',
    contact    VARCHAR(50)                           COMMENT '联系人',
    phone      VARCHAR(20)                           COMMENT '联系电话',
    address    VARCHAR(200)                          COMMENT '公司地址',
    status     INT           NOT NULL DEFAULT 1      COMMENT '状态：1合作中 0已停用',
    created_at DATETIME                              COMMENT '创建时间',
    updated_at DATETIME                              COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_suppliers_name (name),
    KEY idx_suppliers_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商表';

-- =============================================================================
-- 5. products（商品表）
-- =============================================================================
DROP TABLE IF EXISTS products;
CREATE TABLE products (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    barcode         VARCHAR(50)   NOT NULL                COMMENT '条形码（EAN-13）',
    name            VARCHAR(100)  NOT NULL                COMMENT '商品名称',
    unit            VARCHAR(20)                           COMMENT '规格/单位：瓶/袋/箱/kg',
    sale_price      DECIMAL(10,2) NOT NULL                COMMENT '零售价',
    purchase_price  DECIMAL(10,2) NOT NULL                COMMENT '进货价',
    stock           INT           NOT NULL DEFAULT 0      COMMENT '当前库存',
    min_stock       INT           NOT NULL DEFAULT 0      COMMENT '库存下限（预警阈值）',
    max_stock       INT           NOT NULL DEFAULT 999    COMMENT '库存上限（积压阈值）',
    production_date DATETIME                              COMMENT '生产日期',
    shelf_life_days INT                                   COMMENT '保质期（天）',
    status          INT           NOT NULL DEFAULT 1      COMMENT '状态：1上架 0下架',
    category_id     BIGINT                                 COMMENT '所属分类 ID',
    supplier_id     BIGINT                                 COMMENT '供应商 ID',
    created_at      DATETIME                              COMMENT '创建时间',
    updated_at      DATETIME                              COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_products_barcode (barcode),
    KEY idx_products_name (name),
    KEY idx_products_category_id (category_id),
    KEY idx_products_supplier_id (supplier_id),
    KEY idx_products_status (status),
    KEY idx_products_stock (stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- =============================================================================
-- 6. purchases（进货单头表）
-- =============================================================================
DROP TABLE IF EXISTS purchases;
CREATE TABLE purchases (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    purchase_no   VARCHAR(30)   NOT NULL                COMMENT '进货单号：P+年月日+序号',
    supplier_id   BIGINT                                 COMMENT '供应商 ID',
    employee_id   BIGINT                                 COMMENT '经手人（员工）ID',
    total_amount  DECIMAL(12,2) NOT NULL DEFAULT 0      COMMENT '进货总金额',
    status        VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/COMPLETED/CANCELLED',
    remark        VARCHAR(255)                           COMMENT '备注',
    created_at    DATETIME                               COMMENT '创建时间',
    updated_at    DATETIME                               COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_purchases_purchase_no (purchase_no),
    KEY idx_purchases_supplier_id (supplier_id),
    KEY idx_purchases_employee_id (employee_id),
    KEY idx_purchases_status (status),
    KEY idx_purchases_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='进货单头表';

-- =============================================================================
-- 7. purchase_items（进货单明细表）
-- =============================================================================
DROP TABLE IF EXISTS purchase_items;
CREATE TABLE purchase_items (
    id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    purchase_id    BIGINT        NOT NULL                COMMENT '所属进货单 ID',
    product_id     BIGINT        NOT NULL                COMMENT '商品 ID',
    quantity       INT           NOT NULL                COMMENT '进货数量',
    purchase_price DECIMAL(10,2)                         COMMENT '进货单价',
    subtotal       DECIMAL(12,2)                         COMMENT '小计 = quantity × purchase_price',
    PRIMARY KEY (id),
    KEY idx_purchase_items_purchase_id (purchase_id),
    KEY idx_purchase_items_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='进货单明细表';

-- =============================================================================
-- 8. members（会员表）
-- =============================================================================
DROP TABLE IF EXISTS members;
CREATE TABLE members (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    card_no     VARCHAR(20)   NOT NULL                COMMENT '会员卡号：M+8位数字',
    name        VARCHAR(50)   NOT NULL                COMMENT '会员姓名',
    phone       VARCHAR(20)   NOT NULL                COMMENT '手机号',
    id_card     VARCHAR(18)                           COMMENT '身份证号',
    gender      VARCHAR(2)                            COMMENT '性别：男/女',
    birth_date  DATETIME                               COMMENT '出生日期',
    points      INT           NOT NULL DEFAULT 0      COMMENT '积分余额',
    total_spent DECIMAL(12,2) NOT NULL DEFAULT 0      COMMENT '累计消费金额',
    valid_until DATETIME                               COMMENT '会员卡有效期截止日',
    status      INT           NOT NULL DEFAULT 1      COMMENT '状态：1正常 0已注销',
    created_at  DATETIME                               COMMENT '创建时间',
    updated_at  DATETIME                               COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_members_card_no (card_no),
    UNIQUE KEY uk_members_phone (phone),
    KEY idx_members_id_card (id_card),
    KEY idx_members_status (status),
    KEY idx_members_valid_until (valid_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员表';

-- =============================================================================
-- 9. sales（销售单头表，POS 交易）
-- =============================================================================
DROP TABLE IF EXISTS sales;
CREATE TABLE sales (
    id               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    flow_no          VARCHAR(30)   NOT NULL                COMMENT '交易流水号：S+年月日+序号',
    cashier_id       BIGINT                                 COMMENT '收银员（员工）ID',
    member_id        BIGINT                                 COMMENT '会员 ID，NULL=非会员',
    total_amount     DECIMAL(12,2) NOT NULL DEFAULT 0      COMMENT '原价总金额（折扣前）',
    discount_amount  DECIMAL(12,2) NOT NULL DEFAULT 0      COMMENT '折扣金额',
    received_amount  DECIMAL(12,2) NOT NULL DEFAULT 0      COMMENT '实收金额',
    change_amount    DECIMAL(12,2) NOT NULL DEFAULT 0      COMMENT '找零金额',
    payment_method   VARCHAR(20)                           COMMENT '支付方式：CASH/WECHAT/ALIPAY/CARD',
    status           VARCHAR(20)   NOT NULL DEFAULT 'COMPLETED' COMMENT '交易状态：COMPLETED/REFUNDED',
    remark           VARCHAR(255)                          COMMENT '备注',
    created_at       DATETIME                              COMMENT '交易时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sales_flow_no (flow_no),
    KEY idx_sales_cashier_id (cashier_id),
    KEY idx_sales_member_id (member_id),
    KEY idx_sales_created_at (created_at),
    KEY idx_sales_payment_method (payment_method),
    KEY idx_sales_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售单头表';

-- =============================================================================
-- 10. sale_items（销售单明细表）
-- =============================================================================
DROP TABLE IF EXISTS sale_items;
CREATE TABLE sale_items (
    id         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    sale_id    BIGINT        NOT NULL                COMMENT '所属销售单 ID',
    product_id BIGINT        NOT NULL                COMMENT '商品 ID',
    quantity   INT           NOT NULL                COMMENT '销售数量',
    sale_price DECIMAL(10,2)                         COMMENT '销售单价',
    subtotal   DECIMAL(12,2)                         COMMENT '小计 = quantity × sale_price',
    PRIMARY KEY (id),
    KEY idx_sale_items_sale_id (sale_id),
    KEY idx_sale_items_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售单明细表';

-- =============================================================================
-- 11. shifts（换班记录表）
-- =============================================================================
DROP TABLE IF EXISTS shifts;
CREATE TABLE shifts (
    id                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    cashier_id        BIGINT                                 COMMENT '收银员（员工）ID',
    start_time        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '班次开始时间',
    end_time          DATETIME                               COMMENT '班次结束时间',
    transaction_count INT           NOT NULL DEFAULT 0      COMMENT '当班交易笔数',
    total_amount      DECIMAL(12,2) NOT NULL DEFAULT 0      COMMENT '当班交易总额',
    status            VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE' COMMENT '班次状态：ACTIVE/CLOSED',
    PRIMARY KEY (id),
    KEY idx_shifts_cashier_id (cashier_id),
    KEY idx_shifts_status (status),
    KEY idx_shifts_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='换班记录表';

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 提示：以上脚本不包含外键约束（FOREIGN KEY），
-- 因为 Spring Data JPA + Hibernate 在启动时会自动建外键，
-- 而且本地开发期禁用外键更灵活（删数据方便）。
-- 部署到生产环境时建议：
--   1. 启用 MySQL 的 innodb_foreign_key_enforce 严格模式
--   2. 在 DDL 里加 FOREIGN KEY 约束，确保数据完整性
-- =============================================================================
