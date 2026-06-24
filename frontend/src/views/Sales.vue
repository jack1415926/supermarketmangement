<!--
  销售记录页面
  功能：交易记录列表、查看销售单详情、交班汇总
-->
<template>
  <div class="page-container">
    <div class="page-toolbar">
      <input v-model="keyword" placeholder="搜索流水号..." @keyup.enter="fetchList" />
      <button @click="fetchList">搜索</button>
      <button @click="loadShiftSummary">交班汇总</button>
    </div>
    <table class="data-table">
      <thead>
        <tr>
          <th>流水号</th>
          <th>金额</th>
          <th>折扣</th>
          <th>实收</th>
          <th>收银员</th>
          <th>时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.flowNo }}</td>
          <td>&yen;{{ item.totalAmount }}</td>
          <td>&yen;{{ item.discountAmount }}</td>
          <td>&yen;{{ item.receivedAmount }}</td>
          <td>{{ item.cashier }}</td>
          <td>{{ item.saleTime }}</td>
          <td>
            <button @click="openDetail(item)">详情</button>
          </td>
        </tr>
      </tbody>
    </table>
    <div v-if="list.length === 0" class="empty-tip">暂无销售记录</div>

    <!-- 销售详情弹窗 -->
    <div v-if="showDetail" class="modal-overlay" @click.self="showDetail = false">
      <div class="modal-card">
        <h3>销售详情 - {{ detailData.flowNo }}</h3>
        <table class="data-table">
          <thead><tr><th>商品</th><th>数量</th><th>单价</th><th>小计</th></tr></thead>
          <tbody>
            <tr v-for="d in detailData.items" :key="d.id"><td>{{ d.name }}</td><td>{{ d.quantity }}</td><td>&yen;{{ d.price }}</td><td>&yen;{{ (d.quantity * d.price).toFixed(2) }}</td></tr>
          </tbody>
        </table>
        <p style="margin-top:16px;">合计：&yen;{{ detailData.totalAmount }} | 折扣：&yen;{{ detailData.discountAmount }} | 实收：&yen;{{ detailData.receivedAmount }} | 找零：&yen;{{ detailData.changeAmount }}</p>
        <div class="form-buttons"><button class="btn-cancel" @click="showDetail = false">关闭</button></div>
      </div>
    </div>

    <!-- 交班汇总弹窗 -->
    <div v-if="showShiftSummary" class="modal-overlay" @click.self="showShiftSummary = false">
      <div class="modal-card">
        <h3>当日交班汇总</h3>
        <div class="summary-cards">
          <div class="summary-card card-blue">
            <div class="summary-num">{{ shiftSummary.date }}</div>
            <div class="summary-label">日期</div>
          </div>
          <div class="summary-card card-green">
            <div class="summary-num">{{ shiftSummary.transactionCount }}</div>
            <div class="summary-label">交易笔数</div>
          </div>
          <div class="summary-card card-purple">
            <div class="summary-num">&yen;{{ shiftSummary.totalAmount }}</div>
            <div class="summary-label">交易总额</div>
          </div>
          <div class="summary-card card-orange">
            <div class="summary-num">&yen;{{ shiftSummary.averageAmount }}</div>
            <div class="summary-label">平均客单价</div>
          </div>
        </div>
        <div class="form-buttons"><button class="btn-cancel" @click="showShiftSummary = false">关闭</button></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { saleAPI } from '@/api'
const keyword = ref('')
const list = ref([])
const showDetail = ref(false)
const showShiftSummary = ref(false)
const detailData = ref({})
const shiftSummary = ref({ date: '', transactionCount: 0, totalAmount: '0.00', averageAmount: '0.00' })

async function fetchList() {
  try {
    const res = await saleAPI.list({ keyword: keyword.value })
    const rawList = res.data?.content || res.data?.data || res.data || []
    list.value = rawList.map(function(s) {
      return {
        id: s.id,
        flowNo: s.flowNo || ('SA' + String(s.id).padStart(6, '0')),
        totalAmount: Number(s.totalAmount || 0).toFixed(2),
        discountAmount: Number(s.discountAmount || 0).toFixed(2),
        receivedAmount: Number(s.receivedAmount || 0).toFixed(2),
        changeAmount: Number(s.changeAmount || 0).toFixed(2),
        cashier: (s.cashier && s.cashier.displayName) || (s.cashier && s.cashier.username) || (typeof s.cashier === 'string' ? s.cashier : ''),
        saleTime: (s.saleTime || s.createdAt || '').substring(0, 19)
      }
    })
  } catch (err) {
    console.error('获取销售记录失败', err)
  }
}

async function loadShiftSummary() {
  try {
    const res = await saleAPI.dailySummary()
    const data = res.data?.data || res.data || {}
    shiftSummary.value = {
      date: data.date || '',
      transactionCount: data.transactionCount || 0,
      totalAmount: Number(data.totalAmount || 0).toFixed(2),
      averageAmount: Number(data.averageAmount || 0).toFixed(2)
    }
    showShiftSummary.value = true
  } catch (err) {
    console.error('获取交班汇总失败', err)
    alert('获取交班汇总失败')
  }
}

function openDetail(item) {
  detailData.value = {
    ...item,
    items: (item.items && item.items.length > 0) ? item.items.map(function(i) {
      return {
        id: i.id || i.productId,
        name: i.productName || i.name || '商品',
        quantity: i.quantity || 0,
        price: Number(i.salePrice || i.price || 0).toFixed(2)
      }
    }) : [{ id: 1, name: '--', quantity: 0, price: '0.00' }]
  }
  showDetail.value = true
}

onMounted(function() { fetchList() })
</script>

<style scoped>
.page-container { background: #fff; border-radius: 8px; padding: 20px; }
.page-toolbar { display: flex; gap: 10px; margin-bottom: 20px; }
.page-toolbar input { flex: 1; padding: 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
.page-toolbar button { padding: 10px 20px; background: #667eea; color: #fff; border: none; border-radius: 6px; cursor: pointer; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { background: #f5f6fa; padding: 12px; text-align: left; font-size: 13px; color: #666; border-bottom: 2px solid #e8e8e8; }
.data-table td { padding: 12px; border-bottom: 1px solid #f0f0f0; font-size: 14px; }
.data-table td button { padding: 4px 12px; margin-right: 6px; border: 1px solid #ddd; border-radius: 4px; background: #fff; cursor: pointer; font-size: 12px; }
.empty-tip { text-align: center; padding: 60px; color: #999; font-size: 15px; }
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.4); display: flex; justify-content: center; align-items: center; z-index: 100; }
.modal-card { background: #fff; border-radius: 12px; padding: 30px; width: 600px; max-height: 80vh; overflow-y: auto; box-shadow: 0 10px 40px rgba(0,0,0,0.2); }
.modal-card h3 { margin-bottom: 24px; }
.form-buttons { display: flex; gap: 12px; margin-top: 24px; }
.btn-cancel { flex: 1; padding: 12px; border: 1px solid #ddd; border-radius: 8px; background: #fff; cursor: pointer; }
.summary-cards { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; margin-bottom: 8px; }
.summary-card { border-radius: 10px; padding: 20px; text-align: center; }
.summary-num { font-size: 24px; font-weight: bold; margin-bottom: 6px; }
.summary-label { font-size: 13px; color: #666; }
.card-blue { background: #eaf2fd; } .card-blue .summary-num { color: #3498db; }
.card-green { background: #eafaf1; } .card-green .summary-num { color: #27ae60; }
.card-purple { background: #f4ecf7; } .card-purple .summary-num { color: #8e44ad; }
.card-orange { background: #fef5e7; } .card-orange .summary-num { color: #e67e22; }
</style>
