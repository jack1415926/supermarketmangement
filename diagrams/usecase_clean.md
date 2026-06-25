# ============================================================
# 图1：系统用例图（清晰版）
# ============================================================
graph TD
    subgraph Funcs["系统功能模块"]
        F1[POS收银]
        F2[交班结算]
        F3[商品管理]
        F4[进货管理]
        F5[库存管理]
        F6[会员管理]
        F7[人员管理]
        F8[供应商管理]
        F9[销售记录]
        F10[销售分析]
        F11[系统管理]
    end

    Cashier(("收银员"))
    Manager(("管理员"))
    Admin(("超级管理员"))

    Cashier --> F1
    Cashier --> F2

    Manager -.-> Cashier
    Manager --> F3
    Manager --> F4
    Manager --> F5
    Manager --> F6
    Manager --> F7
    Manager --> F8
    Manager --> F9
    Manager --> F10

    Admin -.-> Manager
    Admin --> F11

    style Admin fill:#e74c3c,color:#fff,stroke:#c0392b
    style Manager fill:#e67e22,color:#fff,stroke:#d35400
    style Cashier fill:#27ae60,color:#fff,stroke:#1e8449
    style F11 fill:#f39c12,color:#fff
    style F1 fill:#3498db,color:#fff
    style F2 fill:#3498db,color:#fff

# ============================================================
# 图注：
# 实线箭头 = 直接拥有的权限
# 虚线箭头 = 继承上级权限
# 收银员：POS收银 + 交班结算
# 管理员：收银员全部权限 + 商品/进货/库存/会员/人员/供应商/销售/分析
# 超级管理员：管理员全部权限 + 系统管理
# ============================================================
