<!--
  换班汇总页面
  功能：收银员交班时查看当班交易统计、交班确认
-->
<template>
  <div class="page-container">
    <h2>换班汇总</h2>
    <p class="shift-time">当班时间：{{ shiftData.startTime }} ~ {{ shiftData.endTime }}</p>

    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-num">{{ shiftData.transactionCount }}</div>
        <div class="stat-label">交易笔数</div>
      </div>
      <div class="stat-card">
        <div class="stat-num">¥{{ shiftData.totalAmount }}</div>
        <div class="stat-label">交易总额</div>
      </div>
      <div class="stat-card">
        <div class="stat-num">¥{{ shiftData.cashAmount }}</div>
        <div class="stat-label">现金收款</div>
      </div>
    </div>

    <table class="data-table" style="margin-top:20px;">
      <thead>
        <tr>
          <th>流水号</th>
          <th>金额</th>
          <th>时间</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="t in shiftData.transactions" :key="t.id">
          <td>{{ t.flowNo }}</td>
          <td>¥{{ t.amount }}</td>
          <td>{{ t.time }}</td>
        </tr>
      </tbody>
    </table>

    <button class="shift-btn" @click="confirmShift">确认交班</button>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const shiftData = ref({
  startTime: '2026-06-22 08:00',
  endTime: new Date().toLocaleTimeString('zh-CN', { hour12: false }),
  transactionCount: 25,
  totalAmount: '3,560.00',
  cashAmount: '2,100.00',
  transactions: [
    { id: 1, flowNo: 'LS20240622001', amount: '156.50', time: '08:30' },
    { id: 2, flowNo: 'LS20240622002', amount: '89.00', time: '09:12' },
    { id: 3, flowNo: 'LS20240622003', amount: '320.00', time: '10:05' },
    { id: 4, flowNo: 'LS20240622004', amount: '45.50', time: '10:45' },
    { id: 5, flowNo: 'LS20240622005', amount: '210.00', time: '11:20' },
  ]
})

function confirmShift() {
  alert('交班确认成功！')
}
</script>

<style scoped>
.page-container { background: #fff; border-radius: 8px; padding: 24px; }
.page-container h2 { margin-bottom: 8px; }
.shift-time { color: #999; font-size: 13px; margin-bottom: 24px; }
.stat-cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.stat-card { background: #f5f6fa; border-radius: 10px; padding: 24px; text-align: center; }
.stat-num { font-size: 28px; font-weight: bold; color: #333; margin-bottom: 8px; }
.stat-label { font-size: 13px; color: #999; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { background: #f5f6fa; padding: 12px; text-align: left; font-size: 13px; color: #666; border-bottom: 2px solid #e8e8e8; }
.data-table td { padding: 12px; border-bottom: 1px solid #f0f0f0; }
.shift-btn { width: 100%; margin-top: 24px; padding: 14px; background: #27ae60; color: #fff; border: none; border-radius: 8px; font-size: 18px; cursor: pointer; }
.shift-btn:hover { background: #219a52; }
</style>
