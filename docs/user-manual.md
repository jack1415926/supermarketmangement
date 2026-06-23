# 超市管理系统 — 用户手册

> 适用版本：v1.0
> 编制：C 角（章瀚）
> 最后更新：2026-06-23

## 一、系统简介

小型超市管理系统是面向中小型超市日常运营管理的信息化平台，覆盖商品管理、进货、销售（POS 收银）、库存、会员、人员、报表等核心业务模块。系统采用前后端分离架构，后端 Spring Boot 3.3.5，前端 Vue 3.5，数据库 MySQL 8.0。

系统服务三类用户：

| 角色 | 主要职责 | 典型操作 |
|---|---|---|
| **收银员** | POS 收银、班次管理 | 登录 → 开始班次 → 扫码收银 → 处理会员/支付 → 交班 |
| **店长/管理员** | 商品/会员/库存/进货/报表管理 | 登录 → 后台管理 → 增删改查 → 查看报表 |
| **系统管理员** | 用户/权限/系统配置 | 登录 → 系统管理 → 配置用户和角色 |

## 二、环境要求

### 软件要求

| 软件 | 最低版本 | 推荐版本 | 说明 |
|---|---|---|---|
| MySQL | 8.0+ | 8.0.36+ | 字符集 utf8mb4 |
| JDK | 17+ | 21 | 后端需要 |
| Maven | 3.8+ | 3.9+ | 后端构建（项目自带 mvnw 可不装） |
| Node.js | 18+ | 20+ | 前端构建 |
| Git | 2.30+ | 最新 | 代码拉取 |

### 硬件要求

- **最低**：2 核 CPU、4GB 内存、20GB 磁盘
- **推荐**：4 核 CPU、8GB 内存、50GB SSD

## 三、安装部署

### 1. 克隆代码

```bash
git clone https://github.com/jack1415926/supermarketmangement.git
cd supermarketmangement
```

### 2. 初始化数据库

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库（字符集 utf8mb4）
CREATE DATABASE supermarket DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit;

# 导入 schema 和测试数据
mysql -u root -p supermarket < backend/src/main/resources/schema.sql
mysql -u root -p supermarket < backend/src/main/resources/data.sql
```

### 3. 启动后端

```bash
cd backend
# Windows
mvnw.cmd spring-boot:run

# Linux/macOS
./mvnw spring-boot:run
```

后端启动后监听 `http://localhost:8080`，看到 `Started SupermarketApplication` 表示成功。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动后访问 `http://localhost:5173`。

### 5. 端口检查

启动前确认以下端口未被占用：

```bash
# Windows
netstat -ano | findstr ":8080 :5173 :3306"

# Linux/macOS
lsof -i :8080 -i :5173 -i :3306
```

### 6. 默认账号

| 账号 | 密码 | 角色 | 来源 |
|---|---|---|---|
| admin | admin123 | 系统管理员 | DataInitializer（首次启动自动创建） |
| manager | 123456 | 店长 | data.sql（需 PasswordResetRunner 重置） |
| cashier | 123456 | 收银员 | data.sql（需 PasswordResetRunner 重置） |

**如果登录失败**：在 `backend/src/main/java/com/supermarket/tools/PasswordResetRunner.java` 启用 `@Component` 注解，启动项目会自动重置所有密码为 123456。验证后删除该类。

### 7. 常见安装问题

| 问题 | 排查方向 |
|---|---|
| 后端启动报 Communications link failure | MySQL 没启动 / 端口 3306 被防火墙挡 / 密码错 |
| 后端报 Access denied for user root | application.yml 里 datasource.password 跟你 MySQL 密码不一致 |
| 前端 npm install 报网络错 | 切 npm 镜像：npm config set registry https://registry.npmmirror.com |
| 前端 5173 端口占用 | vite.config.js 改 server.port |
| 启动后登录 401 | JWT 密钥不一致 / Token 过期（24h）/ 密码没匹配上 |
| 数据库表不存在 | 检查 schema.sql 是否执行成功（字符集得是 utf8mb4） |

## 四、功能使用说明

### 收银员

**登录与开始班次**
1. 浏览器打开 http://localhost:5173，输入账号密码登录
2. 左侧菜单点"开始班次" → 系统记录上班时间

**POS 收银**
1. 进入"POS 收银台"
2. 用扫码枪扫描商品条形码（或手动输入）
3. 数量默认 1，可手动改
4. **会员折扣**（可选）：扫会员卡 → 系统自动算 95 折
5. 选支付方式（现金/微信/支付宝/银行卡）
6. 输实收金额 → 系统算找零 → 点"结算"
7. 弹出小票预览 → 完成

**交班**
1. 点"结束班次" → 系统汇总当班交易笔数和金额
2. 核对收银机现金 → 退出登录

### 店长/管理员

**商品管理**
- 商品列表：左侧菜单 → 商品管理 → 列表/搜索/分页
- 新增商品：点"新增" → 填条形码/名称/分类/价格/库存 → 保存
- 下架商品：列表点"下架"（不删除，保留交易历史）

**会员管理**
- 注册：会员管理 → 新增 → 填手机号/身份证 → 系统自动生成卡号
- 续期：会员列表 → 选到期会员 → 点"续期" → 延长 1 年
- 消费记录：会员详情 → 查看历史交易

**进货管理**
- 创建进货单：进货管理 → 新增 → 选供应商 → 加商品 → 确认入库
- 进货建议：系统根据库存下限自动生成建议进货列表
- 查询：按日期/供应商筛选历史进货

**库存管理**
- 列表：库存管理 → 实时库存数 + 库存金额
- 预警：缺货（红色）/ 积压（黄色）高亮
- 盘点：盘点录入 → 系统算盘盈盘亏

**销售报表**
- 排行榜：报表 → 销售排行 → 选日期范围 → 按销量/销售额排序
- 日报/月报：选日期 → 看趋势图
- 收银员业绩：选日期 → 各收银员交易笔数/金额

### 系统管理员

- **用户管理**：系统管理 → 增删改查账号、分配角色
- **权限管理**：基于 RBAC，3 种角色（ROLE_ADMIN/ROLE_MANAGER/ROLE_CASHIER）
- **数据备份**：定期 mysqldump supermarket > backup.sql

## 五、API 文档

### 鉴权

除 `/api/auth/login` 和 `/api/health` 外，所有接口需要 JWT 鉴权。

请求头加：
```
Authorization: Bearer <token>
```

token 24 小时有效，过期需重新登录。

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

错误码：
- 200：成功
- 400：参数错误
- 401：未登录/token 无效
- 403：权限不足
- 404：资源不存在
- 500：服务器错误

### 端点列表（按模块）

详细 55 个端点见 `docs/postman/supermarket.postman_collection.json`，可直接导入 Postman。

## 附录

### 版本信息
- 后端：Spring Boot 3.3.5 + Java 21 + Spring Security + JPA + MySQL
- 前端：Vue 3.5 + Vite 6 + Pinia 2 + Vue Router 4 + Axios
- 数据库：MySQL 8.0（utf8mb4）

### 默认端口
- 后端：8080
- 前端：5173
- MySQL：3306

### 相关文档
- 系统设计文档：docs/design/system-design.md
- 数据库 ER 图：docs/er-diagram.png
- API 测试集合：docs/postman/
- 功能测试用例：docs/test-cases/functional-test-cases.md

### 联系与反馈
- GitHub Issues：https://github.com/jack1415926/supermarketmangement/issues
