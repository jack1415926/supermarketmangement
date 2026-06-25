<!--
  销售分析页面
  功能：商品销售排行榜、日/月报表切换、收银员业绩统计
-->
<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card card-blue">
        <div class="stat-num">¥{{ monthTotal }}</div>
        <div class="stat-label">本月销售额</div>
      </div>
      <div class="stat-card card-green">
        <div class="stat-num">{{ monthOrders }}</div>
        <div class="stat-label">本月订单数</div>
      </div>
      <div class="stat-card card-purple">
        <div class="stat-num">¥{{ avgOrder }}</div>
        <div class="stat-label">平均客单价</div>
      </div>
    </div>

    <!-- 切换标签：排行榜 / 业绩 -->
    <div class="tab-bar">
      <button :class="{ active: tab === 'ranking' }" @click="tab = 'ranking'">销售排行榜</button>
      <button :class="{ active: tab === 'performance' }" @click="tab = 'performance'; loadPerformance()">收银员业绩</button>
    </div>

    <!-- 排行榜视图 -->
    <div v-if="tab === 'ranking'">
      <div class="ranking-header">
        <span>排名</span>
        <span>商品名称</span>
        <span>销量</span>
        <span>销售额</span>
      </div>
      <div v-for="(item, index) in rankingList" :key="item.id" class="ranking-item">
        <span class="rank-badge" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
        <span>{{ item.name }}</span>
        <span>{{ item.quantity }}</span>
        <span>¥{{ item.amount }}</span>
      </div>
      <div v-if="rankingList.length === 0" class="empty-tip">暂无销售数据</div>
    </div>

    <!-- 业绩视图 -->
    <div v-if="tab === 'performance'">
      <table class="data-table">
        <thead>
          <tr>
            <th>收银员</th>
            <th>交易笔数</th>
            <th>交易总额</th>
            <th>占比</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in performanceList" :key="item.name">
            <td>{{ item.name }}</td>
            <td>{{ item.count }}</td>
            <td>¥{{ item.total }}</td>
            <td>
              <div class="percent-bar">
                <div class="percent-fill" :style="{ width: item.percent + '%' }"></div>
                <span>{{ item.percent }}%</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="performanceList.length === 0" class="empty-tip">暂无业绩数据</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { reportAPI } from '@/api'

const tab = ref('ranking')
const monthTotal = ref('0.00')
const monthOrders = ref(0)
const avgOrder = ref('0.00')

// 排行榜数据
const rankingList = ref([])

// 获取当月起止日期
function getMonthRange() {
  const now = new Date()
  const start = new Date(now.getFullYear(), now.getMonth(), 1)
  const end = new Date(now.getFullYear(), now.getMonth() + 1, 0)
  // 格式化为 yyyy-MM-dd
  const fmt = d => d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2,'0') + '-' + String(d.getDate()).padStart(2,'0')
  return { startDate: fmt(start), endDate: fmt(end) }
}

async function loadData() {
  try {
    const range = getMonthRange()
    // 加载销售排行榜
    const ranking = await reportAPI.salesRanking(range)
    const rankingData = ranking.data || []
    rankingList.value = rankingData.map(function(item, index) {
      return {
        id: item.productId || index,
        name: '商品' + item.productId,
        quantity: item.totalQuantity || 0,
        amount: Number(item.totalAmount || 0).toFixed(2)
      }
    })
    // 加载日销售汇总计算月度统计
    const daily = await reportAPI.salesDaily(range)
    const days = daily.data || []
    let totalAmount = 0
    let totalCount = 0
    days.forEach(function(d) {
      totalAmount += Number(d.totalAmount || 0)
      totalCount += Number(d.transactionCount || 0)
    })
    monthTotal.value = totalAmount.toFixed(2)
    monthOrders.value = totalCount
    avgOrder.value = totalCount > 0 ? (totalAmount / totalCount).toFixed(2) : '0.00'
  } catch (err) {
    console.error('加载分析数据失败', err)
  }
}
onMounted(() => loadData())

// 业绩数据
const performanceList = ref([])
async function loadPerformance() {
  try {
    const range = getMonthRange()
    const res = await reportAPI.cashierPerformance(range)
    const items = res.data || []
    const grandTotal = items.reduce(function(s, i) { return s + (Number(i.totalAmount || i.total) || 0) }, 0)
    performanceList.value = items.map(function(i) {
      return {
        name: '收银员' + (i.cashierId || ''),
        count: i.transactionCount || 0,
        total: Number(i.totalAmount || i.total || 0).toFixed(2),
        percent: grandTotal > 0 ? Math.round((Number(i.totalAmount || i.total) || 0) / grandTotal * 100) : 0
      }
    })
  } catch (err) {
    console.error('加载业绩失败', err)
  }
}
</script>

<style scoped>
.page-container { background: #fff; border-radius: 8px; padding: 20px; }

/* 统计卡片 */
.stat-cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { border-radius: 10px; padding: 24px; }
.stat-num { font-size: 28px; font-weight: bold; margin-bottom: 8px; }
.stat-label { font-size: 13px; color: #666; }
.card-blue { background: #eaf2fd; }
.card-blue .stat-num { color: #3498db; }
.card-green { background: #eafaf1; }
.card-green .stat-num { color: #27ae60; }
.card-purple { background: #f4ecf7; }
.card-purple .stat-num { color: #8e44ad; }

/* 切换标签 */
.tab-bar { display: flex; gap: 10px; margin-bottom: 20px; }
.tab-bar button { padding: 8px 24px; border: 1px solid #ddd; border-radius: 6px; background: #fff; cursor: pointer; font-size: 14px; }
.tab-bar button.active { background: #667eea; color: #fff; border-color: #667eea; }

/* 排行榜 */
.ranking-header { display: flex; padding: 10px 0; border-bottom: 2px solid #e8e8e8; font-weight: bold; color: #666; font-size: 13px; }
.ranking-header span { flex: 1; text-align: center; }
.ranking-item { display: flex; align-items: center; padding: 14px 0; border-bottom: 1px solid #f0f0f0; }
.ranking-item span { flex: 1; text-align: center; font-size: 14px; }
.rank-badge { width: 28px; height: 28px; line-height: 28px; border-radius: 50%; text-align: center; font-size: 13px; margin: 0 auto; }
.rank-1 { background: #f39c12; color: #fff; }
.rank-2 { background: #bdc3c7; color: #fff; }
.rank-3 { background: #cd7f32; color: #fff; }

.empty-tip { text-align: center; padding: 60px; color: #999; font-size: 15px; }

/* 业绩表格 */
.data-table { width: 100%; border-collapse: collapse; margin-top: 10px; }
.data-table th { background: #f5f6fa; padding: 12px; text-align: left; font-size: 13px; color: #666; border-bottom: 2px solid #e8e8e8; }
.data-table td { padding: 12px; border-bottom: 1px solid #f0f0f0; font-size: 14px; }
.percent-bar { display: flex; align-items: center; gap: 8px; }
.percent-fill { height: 8px; background: #667eea; border-radius: 4px; min-width: 2px; }
.percent-bar span { font-size: 12px; color: #666; min-width: 40px; }
</style>
