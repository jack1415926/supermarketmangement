<!--
  工作台页面
  功能：管理员首页概览，展示今日销售、库存预警、快捷入口
-->
<template>
  <div class="dashboard">
    <!-- 统计卡片行 -->
    <div class="stat-cards">
      <div class="stat-card card-blue">
        <div class="stat-num">¥{{ stats.todaySales }}</div>
        <div class="stat-label">今日销售额</div>
      </div>
      <div class="stat-card card-green">
        <div class="stat-num">{{ stats.todayOrders }}</div>
        <div class="stat-label">今日订单数</div>
      </div>
      <div class="stat-card card-orange">
        <div class="stat-num">{{ stats.warnCount }}</div>
        <div class="stat-label">库存预警</div>
      </div>
      <div class="stat-card card-purple">
        <div class="stat-num">{{ stats.memberCount }}</div>
        <div class="stat-label">会员总数</div>
      </div>
    </div>
  </div>
  <!-- 快捷入口 -->
<div class="section-title">快捷操作</div>
<div class="quick-actions">
  <router-link to="/products" class="quick-btn">📦 商品管理</router-link>
  <router-link to="/purchases" class="quick-btn">📥 进货入库</router-link>
  <router-link to="/inventory" class="quick-btn">🏪 库存盘点</router-link>
  <router-link to="/members" class="quick-btn">👤 会员注册</router-link>
  <router-link to="/reports" class="quick-btn">📈 销售分析</router-link>
  <router-link to="/employees" class="quick-btn">👥 人员管理</router-link>
</div>

<!-- 库存预警列表 -->
<div class="section-title">库存预警</div>
<table class="data-table">
  <thead>
    <tr>
      <th>商品名称</th>
      <th>当前库存</th>
      <th>库存下限</th>
      <th>状态</th>
    </tr>
  </thead>
  <tbody>
    <tr v-for="item in warnList" :key="item.id">
      <td>{{ item.name }}</td>
      <td>{{ item.stock }}</td>
      <td>{{ item.min }}</td>
      <td><span class="tag-warn">缺货预警</span></td>
    </tr>
  </tbody>
</table>
</template>
<script setup>
import { ref } from 'vue'

// 统计数据
const stats = ref({
  todaySales: '0',
  todayOrders: 0,
  warnCount: 0,
  memberCount: 0
})

// 页面加载时初始化（当前用假数据）
stats.value = {
  todaySales: '3,568.00',
  todayOrders: 47,
  warnCount: 5,
  memberCount: 128
}
// 库存预警列表
const warnList = ref([
  { id: 1, name: '康师傅方便面', stock: 30, min: 50 },
  { id: 2, name: '海飞丝洗发水', stock: 8, min: 20 },
  { id: 3, name: '中华牙膏', stock: 15, min: 30 },
  { id: 4, name: '农夫山泉 550ml', stock: 45, min: 50 },
  { id: 5, name: '奥利奥饼干', stock: 10, min: 25 }
])
</script>
<style scoped>
.dashboard { display: flex; flex-direction: column; gap: 20px; }

/* 统计卡片网格 */
.stat-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }

/* 单个卡片 */
.stat-card { background: #fff; border-radius: 10px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.stat-num { font-size: 28px; font-weight: bold; margin-bottom: 8px; }
.stat-label { font-size: 13px; color: #999; }

/* 卡片颜色 */
.card-blue .stat-num { color: #3498db; }
.card-green .stat-num { color: #27ae60; }
.card-orange .stat-num { color: #e67e22; }
.card-purple .stat-num { color: #8e44ad; }
/* 快捷入口 */
.section-title { font-size: 16px; font-weight: bold; color: #333; margin-top: 8px; }
.quick-actions { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; }
.quick-btn { display: flex; align-items: center; justify-content: center; padding: 16px; background: #fff; border-radius: 8px; text-decoration: none; color: #333; font-size: 14px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); transition: all 0.2s; }
.quick-btn:hover { background: #667eea; color: #fff; }

/* 预警表格 */
.data-table { width: 100%; border-collapse: collapse; background: #fff; border-radius: 8px; overflow: hidden; }
.data-table th { background: #f5f6fa; padding: 12px; text-align: left; font-size: 13px; color: #666; border-bottom: 2px solid #e8e8e8; }
.data-table td { padding: 12px; border-bottom: 1px solid #f0f0f0; font-size: 14px; }
.tag-warn { padding: 2px 8px; background: #e74c3c; color: #fff; border-radius: 4px; font-size: 12px; }
</style>
