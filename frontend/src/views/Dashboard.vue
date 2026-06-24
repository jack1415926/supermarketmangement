<!--
  工作台页面
  功能：管理员首页概览，展示今日销售、库存预警、快捷入口
-->
<template>
  <div class="dashboard">
    <!-- 统计卡片行 -->
    <div class="stat-cards">
      <div class="stat-card card-blue">
        <div class="stat-num">&yen;{{ stats.todaySales }}</div>
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
        <tr v-if="warnList.length === 0">
          <td colspan="4" style="text-align:center;color:#999;padding:20px;">暂无预警商品</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { reportAPI, inventoryAPI } from '@/api'

const stats = ref({
  todaySales: '0',
  todayOrders: 0,
  warnCount: 0,
  memberCount: 0
})

const warnList = ref([])

/** 获取今天的日期字符串 yyyy-MM-dd */
function getToday() {
  const d = new Date()
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
}

/** 加载统计数据 */
async function loadStats() {
  try {
    const today = getToday()
    // 日销售汇总（需要传 startDate/endDate）
    const daily = await reportAPI.salesDaily({ startDate: today, endDate: today })
    const dailyData = daily.data?.data || daily.data || []
    if (Array.isArray(dailyData) && dailyData.length > 0) {
      var sum = dailyData.reduce(function(s, d) { return s + Number(d.totalAmount || 0) }, 0)
      var cnt = dailyData.reduce(function(s, d) { return s + Number(d.transactionCount || 0) }, 0)
      stats.value.todaySales = sum.toFixed(2)
      stats.value.todayOrders = cnt
    }

    // 库存预警（从库存列表过滤低库存商品）
    const invRes = await inventoryAPI.list({})
    const rawList = invRes.data?.content || invRes.data?.data || invRes.data || []
    const lowStock = Array.isArray(rawList)
      ? rawList.filter(function(i) { return i.status === 'LOW' || i.status === 'OUT' || (i.stock != null && i.minStock != null && i.stock <= i.minStock) })
      : []
    stats.value.warnCount = lowStock.length
    warnList.value = lowStock.map(function(i) {
      return {
        id: i.productId,
        name: i.productName,
        stock: i.stock,
        min: i.minStock
      }
    })
  } catch (err) {
    console.error('加载统计数据失败', err)
    stats.value = { todaySales: '0.00', todayOrders: 0, warnCount: 0, memberCount: 0 }
  }
}

onMounted(function() { loadStats() })
</script>

<style scoped>
.dashboard { display: flex; flex-direction: column; gap: 20px; }

.stat-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }

.stat-card { background: #fff; border-radius: 10px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.stat-num { font-size: 28px; font-weight: bold; margin-bottom: 8px; }
.stat-label { font-size: 13px; color: #999; }

.card-blue .stat-num { color: #3498db; }
.card-green .stat-num { color: #27ae60; }
.card-orange .stat-num { color: #e67e22; }
.card-purple .stat-num { color: #8e44ad; }

.section-title { font-size: 16px; font-weight: bold; color: #333; margin-top: 8px; }
.quick-actions { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; }
.quick-btn { display: flex; align-items: center; justify-content: center; padding: 16px; background: #fff; border-radius: 8px; text-decoration: none; color: #333; font-size: 14px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); transition: all 0.2s; }
.quick-btn:hover { background: #667eea; color: #fff; }

.data-table { width: 100%; border-collapse: collapse; background: #fff; border-radius: 8px; overflow: hidden; }
.data-table th { background: #f5f6fa; padding: 12px; text-align: left; font-size: 13px; color: #666; border-bottom: 2px solid #e8e8e8; }
.data-table td { padding: 12px; border-bottom: 1px solid #f0f0f0; font-size: 14px; }
.tag-warn { padding: 2px 8px; background: #e74c3c; color: #fff; border-radius: 4px; font-size: 12px; }
</style>
