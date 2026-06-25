# ============================================================
# 图5a：系统整体布局结构
# ============================================================
flowchart LR
    subgraph Layout["系统整体布局"]
        direction TB
        Sidebar["左侧边栏 220px<br/>深色背景 #1a1a2e<br/>导航菜单 12项"] 
        MainArea["主内容区 flex:1"]
    end
    
    Sidebar --- MainArea
    
    subgraph MainArea_detail["主内容区详情"]
        direction TB
        Topbar["顶栏 56px 白色<br/>用户信息 + 退出按钮"]
        Content["内容区<br/>浅灰背景 #f5f6fa<br/>padding: 24px<br/>overflow-y: auto"]
    end
    
    Topbar --- Content

    style Sidebar fill:#1a1a2e,color:#eee
    style Topbar fill:#fff,color:#333
    style Content fill:#f5f6fa,color:#333

# ============================================================
# 图5b：POS收银台界面结构
# ============================================================
flowchart LR
    subgraph POS["POS收银台"]
        subgraph PL["左侧面板 50%"]
            direction TB
            MB["会员卡号输入 + 验证按钮"]
            SB["商品搜索框 + 搜索按钮"]
            SR["搜索结果列表<br/>点击添加到购物车"]
        end
        subgraph PR["右侧面板 50%"]
            direction TB
            CH["购物车表头<br/>名称/数量/单价/小计/操作"]
            CI["购物车商品行<br/>可增减数量、删除"]
            SA["汇总区域<br/>共 N 件 | 合计: ¥XX"]
            CB["收款按钮 F8"]
        end
    end
    
    PL --> PR
    MB --> SB --> SR
    CH --> CI --> SA --> CB

    style MB fill:#eaf2fd
    style CB fill:#27ae60,color:#fff
    style SA fill:#fef9e7

# ============================================================
# 图5c：通用管理页面结构
# ============================================================
flowchart LR
    subgraph MP["通用管理页面 如商品管理"]
        subgraph T1["顶部工具栏"]
            direction LR
            SI["搜索输入框"]
            SB1["搜索按钮"]
            AB["新增按钮 绿色"]
        end
        subgraph T2["数据表格"]
            TH["表头行 灰色背景"]
            TR["数据行 白色/交替色"]
            EP["空状态提示"]
        end
        subgraph T3["弹窗"]
            MD["蒙层遮罩 半透明黑"]
            MC["居中卡片 白色圆角<br/>表单字段<br/>取消/保存按钮"]
        end
    end
    
    SI --> SB1
    SB1 --> AB
    TH --> TR
    TR --> EP

    style AB fill:#27ae60,color:#fff
    style TH fill:#f5f6fa
    style MD fill:#00000033
    style MC fill:#fff
