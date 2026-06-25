# 小型超市管理系统 - 图表代码（最终版）
# 打开 https://mermaid.live，逐段粘贴，导出 PNG

# ============================================================
# 图1：系统用例图
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

    style Admin fill:#e74c3c,color:#fff
    style Manager fill:#e67e22,color:#fff
    style Cashier fill:#27ae60,color:#fff
    style F11 fill:#f39c12,color:#fff

# ============================================================
# 图2a：核心业务 ER 图
# ============================================================
erDiagram
    categories ||--o{ products : "属于"
    suppliers ||--o{ products : "供货"
    suppliers ||--o{ purchases : "供货方"

    products ||--o{ purchase_items : ""
    products ||--o{ sale_items : ""

    purchases ||--|{ purchase_items : "明细"
    sales ||--|{ sale_items : "明细"

    members |o--o{ sales : "会员"

    categories {
        bigint id PK
        varchar name
    }

    suppliers {
        bigint id PK
        varchar name
        varchar contact
        varchar phone
    }

    products {
        bigint id PK
        varchar name
        varchar barcode
        decimal sale_price
        decimal purchase_price
        int stock
        int min_stock
        int max_stock
    }

    purchases {
        bigint id PK
        varchar purchase_no
        decimal total_amount
    }

    purchase_items {
        bigint id PK
        int quantity
        decimal purchase_price
        decimal subtotal
    }

    sales {
        bigint id PK
        varchar flow_no
        decimal total_amount
        decimal discount_amount
        decimal received_amount
        decimal change_amount
    }

    sale_items {
        bigint id PK
        int quantity
        decimal sale_price
        decimal subtotal
    }

    members {
        bigint id PK
        varchar card_no
        varchar name
        varchar phone
        date valid_until
    }

# ============================================================
# 图2b：用户权限 ER 图
# ============================================================
erDiagram
    users ||--o| employees : "对应"

    users {
        bigint id PK
        varchar username
        varchar password
        varchar role
        int status
    }

    employees {
        bigint id PK
        varchar name
        varchar phone
        varchar position
        decimal salary
        date hire_date
        int status
    }

# ============================================================
# 图3：系统架构图
# ============================================================
graph TB
    subgraph 前端["浏览器"]
        V["Vue 3 + Vite"]
        P["Pinia 状态管理"]
        R["Vue Router 路由"]
        X["Axios HTTP"]
    end

    subgraph 后端["Spring Boot :8080"]
        C["Controller REST API"]
        S["Spring Security JWT"]
        SV["Service 业务层"]
        RP["Repository JPA"]
    end

    DB[("MySQL :3306<br/>supermarket")]

    V --> P
    V --> R
    V --> X
    X -- "JSON + Bearer Token" --> S
    S --> C
    C --> SV
    SV --> RP
    RP --> DB

    style V fill:#42b883,color:#fff
    style C fill:#6db33f,color:#fff
    style DB fill:#4479a1,color:#fff

# ============================================================
# 图4：POS 收银结算流程图
# ============================================================
flowchart TD
    A["1.开始"] --> B["2.输入会员卡号(可选)"]
    B --> C{"有会员卡?"}
    C -- "是" --> D["3.调用API验证<br/>应用95折"]
    C -- "否" --> E["4.搜索商品"]
    D --> E
    E --> F["5.添加到购物车"]
    F --> G{"继续添加?"}
    G -- "是" --> E
    G -- "否" --> H["6.自动计算总额"]
    H --> I["7.点击收款"]
    I --> J["8.输入实收金额"]
    J --> K["9.系统计算找零"]
    K --> L["10.POST /api/sales"]
    L --> M["11.扣库存+生成小票"]
    M --> N["12.结束"]

    style A fill:#27ae60,color:#fff
    style N fill:#3498db,color:#fff
