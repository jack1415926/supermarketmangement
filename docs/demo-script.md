# 演示讲稿 — 超市管理系统

> 每页 PPT 配 100-200 字讲稿，按页念即可
> 演示时长：约 12-15 分钟

---

## 第 1 页 — 封面

"大家好，我们是 [团队名]，今天给大家演示我们做的'小型超市管理系统'。这个项目是 [课程名] 的期末作业，目标是解决中小型超市日常运营的痛点。"

## 第 2 页 — 目录

"今天的汇报分 11 个部分：项目背景、团队分工、技术栈、系统架构、数据库设计、核心功能、API 亮点、测试覆盖、项目亮点、难点解决、未来展望。"

## 第 3 页 — 项目背景

"传统的中小型超市有几个痛点：商品多，库存难管；会员维护成本高；销售对账靠人工，月底对账累死；报表靠 Excel，每次都要重新做。我们的系统就是解决这些事——一个平台搞定商品、库存、销售、会员、报表 7 大模块。系统服务三类用户：收银员、店长、系统管理员。"

## 第 4 页 — 团队分工

"我们组 5 个人，分 3 个角。A 角徐磊负责前端，用 Vue 3 + Vite 做了 13 个页面。B 角殷智元负责后端，Spring Boot + JPA + Spring Security，写了 12 个 Controller 加 JWT 鉴权。我作为 C 角，负责数据库设计、接口测试、功能测试、所有文档和这次答辩。"

## 第 5 页 — 技术栈

"后端用 Java 21 + Spring Boot 3.3.5，配合 Spring Security 做鉴权，JWT 签发 token。数据库是 MySQL 8.0，字符集 utf8mb4 支持中文。前端 Vue 3.5 + Vite 6，状态管理用 Pinia，HTTP 用 Axios。整个项目用 Maven 和 npm 做构建，Git 做版本管理。"

## 第 6 页 — 系统架构

"这是我们的系统架构图。整体是经典三层架构：前端 Vue 单页应用，通过 Axios 调后端 RESTful API；后端 Spring Boot 跑在 8080 端口，12 个 Controller 分模块处理业务逻辑；数据访问用 Spring Data JPA；底层 MySQL 存储。鉴权层用 Spring Security + JWT，过滤器拦截每个请求，权限注解 @PreAuthorize 做角色控制。"

## 第 7 页 — 数据库设计

"数据库有 11 张表（念出来）：users、employees、categories、products、suppliers、purchases、purchase_items、members、sales、sale_items、shifts。表间关系：商品属于分类、关联供应商；销售单有多个销售明细；会员和员工是销售单的关联实体。这张 ER 图我专门用 PlantUML 画的，能直接放到文档和答辩 PPT 里。"

## 第 8 页 — 数据库设计要点

"数据库设计几个要点：字段类型严格对齐 JPA 实体的 @Column 注解；唯一索引加在 username、barcode、card_no、phone 这种业务唯一字段上；外键列加普通索引方便 JOIN；products 表加了 version 字段做乐观锁，防止并发超卖。我交付的 schema.sql 有 15.7KB，data.sql 有 13.8KB——schema 是 11 张表的 DDL，data 是 3 个用户 + 12 个商品 + 5 个会员 + 5 笔销售 + 2 笔进货的测试数据。"

## 第 9 页 — 核心功能演示

"系统最核心的是 POS 收银台——操作流程：扫码枪扫商品条形码（或者手动输入），商品加入购物车，可改数量；可选扫会员卡，系统自动算 95 折；选支付方式（现金、微信、支付宝、银行卡）；输实收金额，系统算找零；点结算，弹出小票预览，扣库存、更新会员消费。后台还有商品/会员/进货/库存/报表 5 大模块的 CRUD 和分析功能。"

## 第 10 页 — API 设计亮点

"几个 API 设计上的亮点：统一响应格式 Result T 包含 code、message、data 三个字段，前端只需解析这一种格式；鉴权用 JWT 加 Bearer Token，24 小时有效；权限用 @PreAuthorize 注解，3 种角色（管理员、店长、收银员）精细控制；商品加了 @Version 乐观锁字段，并发场景下不会超卖；全局异常处理用 @RestControllerAdvice，错误码统一、堆栈不外泄。最后总共 55 个 API 端点，Postman 集合全都有。"

## 第 11 页 — 测试覆盖

"测试方面，C 角交付了 2 套：Postman 接口测试 55 个端点，覆盖 12 个 Controller；功能测试用例表 80 多条，按 8 大模块组织，含 P0 到 P3 四级优先级。还有一个 PasswordResetRunner 工具，开发时一键重置密码。"

## 第 12 页 — 项目亮点

"项目整体几个亮点：三层架构 + 模块化，前端后端完全解耦；统一响应格式让前端开发效率很高；乐观锁防超卖，并发场景下数据一致性强；C 角完整覆盖从数据库到文档到 PPT 的所有文档类工作；PlantUML 画的 11 张表 PNG 答辩可以直接展示；Postman 集合一份 API 契约前后端共用。"

## 第 13 页 — 难点 + 解决方案

"开发中遇到的几个难点：角色格式一开始 data.sql 写 ADMIN，Spring Security 期望 ROLE_ADMIN，权限全 403——统一改成带前缀的 ROLE_ADMIN 解决；DataInitializer 跟 data.sql 会有冲突，用 count() == 0 守卫，data.sql 加载了就跳过；前端 API 调用分散，统一用 Axios 拦截器 + 401 自动跳转登录。"

## 第 14 页 — 未来展望

"项目未来可以扩展的方向：移动端 App（Android / iOS），复用现在的后端 API；线上商城模块，把现在的 11 张表扩 2-3 张订单表就能支持；数据看板用 ECharts 接 ReportController 的 4 个端点做可视化；多门店支持，给 users 加 store_id 字段做数据隔离；小票打印接蓝牙打印机 SDK。"

## 第 15 页 — Q&A

"以上就是我们的项目汇报，谢谢大家！有问题的请举手。"

---

## 演示 Tips

- 演示前先启动后端和前端，**确保能跑通登录和 POS 结账**——这是最可能被问到的实操
- 如果老师问"为什么用 Java 21 / Spring Boot 3.3.5 / MySQL 8.0"，答：版本稳定、文档多、社区活跃、阿里云/腾讯云都直接支持
- 如果问"Postman 集合怎么用"，当场演示：导入 collection → Login 拿 token → 其他接口自动带 token → 看返回 JSON
- 如果问"如果两个收银员同时结账同一商品怎么办"，答：@Version 乐观锁自动 retry，库存不会变负
- 如果问"为什么 data.sql 的密码用了 BCrypt"，答：项目用了 Spring Security 的 BCryptPasswordEncoder，存明文密码会被 GlobalExceptionHandler 拒绝
- 如果问"为什么角色是 ROLE_ADMIN 不是 ADMIN"，答：Spring Security 期望 hasRole('ADMIN') 自动加 ROLE_ 前缀，DB 里存完整字符串
