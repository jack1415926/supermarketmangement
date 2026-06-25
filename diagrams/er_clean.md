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
