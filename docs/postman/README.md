# Postman 测试用例 — 超市管理系统

> 作者：C 角（数据库 + 测试 + 文档）
> 最后更新：2026-06-23

## 文件清单

- `supermarket.postman_collection.json` —— Postman v2.1 collection，**55 个端点** 覆盖 12 个 Controller

## 怎么用

### 1. 启动后端
```bash
cd backend
mvn spring-boot:run
```
后端监听 `http://localhost:8080`，确保 MySQL 已启动且 `supermarket` 库已建好。

### 2. 导入到 Postman

1. 打开 Postman → 顶栏 **Import** → 选 `File` 标签 → 拖这个 `supermarket.postman_collection.json` 进来
2. 导入后左侧出现 **"超市管理系统 API"** 文件夹，下面 12 个子文件夹

### 3. 调 base_url

打开 collection → 右侧 **Variables** 标签：
- `base_url` = `http://localhost:8080`（默认就是这个）
- 如果你后端跑了别的端口，改这里

### 4. 测试流程（**严格按顺序**）

1. **先跑 1. 认证 Auth → Login**  
   输入 `admin` / `123456`，点 Send  
   → **Tests 标签**会自动跑，把返回的 token 存到 `{{token}}` 变量

2. **其他 11 个文件夹的请求**  
   → 自动用 `{{token}}` 调需要鉴权的接口，不用手动加 header

3. **CRUD 测试自动串联**  
   一些请求（Create 分类/商品/会员）会把新 ID 存到 `{{newCategoryId}}` 等变量，  
   后面的 Update / Delete 直接用这个 ID 接着测

## 测试覆盖范围

| # | 模块 | 端点数 | 关键场景 |
|---|---|---|---|
| 1 | 认证 | 4 | 登录成功 / 错误密码 / 当前用户 / 登出 |
| 2 | 商品分类 | 4 | 列表 / 新增 / 修改 / 删除 |
| 3 | 商品 | 7 | 列表 / 关键字搜索 / 扫码 / 按条形码 / CRUD |
| 4 | 供应商 | 5 | 列表 / 按 ID / CRUD |
| 5 | 会员 | 7 | 列表 / 注册 / 刷卡验证（正常/过期/不存在）/ 续期 |
| 6 | 员工 | 5 | 列表 / 按 ID / CRUD |
| 7 | 进货 | 4 | 列表 / 按 ID / 进货入库 / 进货建议 |
| 8 | 销售 | 6 | POS 结账（无会员/会员95折）/ 列表 / 按日期 / 详情 / 日汇总 |
| 9 | 库存 | 3 | 列表 / 改阈值 / 盘点 |
| 10 | 换班 | 5 | 开始 / 当前 / 按收银员 / 在岗列表 / 交班 |
| 11 | 报表 | 4 | 销售排行 / 日报 / 月报 / 收银员业绩 |
| 12 | 健康检查 | 1 | ping |
| **合计** | | **55** | |

## 几个关键测试用例的意图

### 认证 - 错误密码
确保 Spring Security 正确返回 401 而不是 500。

### 会员 - 验证 3 种状态
- `M00000001` (正常)
- `M00000004` (已过期，data 应该带 valid 标志)
- `M99999999` (不存在，应该 404)

### POS 结账 - 校验找零
**Checkout (POS 结账 - 无会员)** 的 Tests 标签里有一行：
```javascript
pm.test('change = received - total', () => pm.expect(sale.changeAmount).to.eql(10 - 7));
```
收到 10 元，卖 7 元，找零必须是 3 元。**这是 data.sql 里的销售 1 场景**。

### 销售 - 按日期筛选
`/api/sales?date=2026-06-22` 配合 data.sql 的 5 笔销售做端到端验证。

## 与 data.sql 的配合

测试数据基于我之前提交的 `data.sql`：
- 用户：`admin` / `manager` / `cashier`，密码 `123456`
- 商品条形码 6901234567890 是「可口可乐 330ml」
- 会员 M00000001 是「赵丽华」
- 库存预警测试用商品 6901234567894 (好丽友派，库存 5 < min_stock 10)

**如果登录失败**（data.sql 的 BCrypt hash 跟项目 PasswordEncoder 不一致），  
把后端 `tools/PasswordResetRunner.java` 留着，启动一次会自动重置所有密码为 123456。

## 已知限制

1. **后端必须先跑起来**——collection 是动态请求，需要真实的服务响应
2. **测试数据必须存在**——依赖 data.sql 已加载，否则像"按 ID 查商品 1"会 404
3. **token 过期**——24 小时后 token 失效，需要重新跑 Login
4. **并发问题**——如果多人共用一个 MySQL 同时跑，可能 ID 冲突（建议本地独立数据库）

## 报告 Bug

测试失败的请求：
1. 截图保存（URL + Request + Response 三件套）
2. 在 issue 里开 ticket，标题格式：`[API Bug] 端点名 - 错误描述`
3. 抄送殷智元（B 角后端）和我（C 角测试）
