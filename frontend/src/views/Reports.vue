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
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const tab = ref('ranking')
const monthTotal = ref('45,680.00')
const monthOrders = ref(356)
const avgOrder = ref('128.31')

// 排行榜数据
const rankingList = ref([
  { id: 1, name: '可口可乐 330ml', quantity: 520, amount: '1,820.00' },
  { id: 2, name: '康师傅方便面', quantity: 380, amount: '1,520.00' },
  { id: 3, name: '农夫山泉 550ml', quantity: 350, amount: '700.00' },
  { id: 4, name: '奥利奥饼干', quantity: 280, amount: '2,520.00' },
  { id: 5, name: '中华牙膏', quantity: 220, amount: '2,860.00' },
])

// 业绩数据
const performanceList = ref([])
function loadPerformance() {
  performanceList.value = [
    { name: '张三', count: 125, total: '18,500.00', percent: 40 },
    { name: '李四', count: 98, total: '15,200.00', percent: 33 },
    { name: '王五', count: 133, total: '11,980.00', percent: 27 },
  ]
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
.rank-3 { background: #e67e22; color: #fff; }

/* 业绩表格 */
.data-table { width: 100%; border-collapse: collapse; margin-top: 8px; }
.data-table th { background: #f5f6fa; padding: 12px; text-align: left; font-size: 13px; color: #666; border-bottom: 2px solid #e8e8e8; }
.data-table td { padding: 12px; border-bottom: 1px solid #f0f0f0; font-size: 14px; }

/* 百分比条 */
.percent-bar { display: flex; align-items: center; gap: 8px; }
.percent-fill { height: 8px; background: #667eea; border-radius: 4px; min-width: 4px; }
.percent-bar span { font-size: 12px; color: #666; }
</style>