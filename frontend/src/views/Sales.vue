<!--
  销售记录页面
  功能：交易记录列表、查看销售单详情、收银员交班汇总
-->
<template>
  <div class="page-container">
    <div class="page-toolbar">
      <input v-model="keyword" placeholder="搜索流水号..." @keyup.enter="fetchList" />
      <button @click="fetchList">搜索</button>
      <button @click="showShiftSummary = true">交班汇总</button>
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
          <td>¥{{ item.totalAmount }}</td>
          <td>¥{{ item.discountAmount }}</td>
          <td>¥{{ item.receivedAmount }}</td>
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
            <tr v-for="d in detailData.items" :key="d.id"><td>{{ d.name }}</td><td>{{ d.quantity }}</td><td>¥{{ d.price }}</td><td>¥{{ (d.quantity * d.price).toFixed(2) }}</td></tr>
          </tbody>
        </table>
        <p style="margin-top:16px;">合计：¥{{ detailData.totalAmount }} | 折扣：¥{{ detailData.discountAmount }} | 实收：¥{{ detailData.receivedAmount }} | 找零：¥{{ detailData.changeAmount }}</p>
        <div class="form-buttons"><button class="btn-cancel" @click="showDetail = false">关闭</button></div>
      </div>
    </div>

    <!-- 交班汇总弹窗 -->
    <div v-if="showShiftSummary" class="modal-overlay" @click.self="showShiftSummary = false">
      <div class="modal-card">
        <h3>收银员交班汇总</h3>
        <table class="data-table">
          <thead><tr><th>收银员</th><th>交易笔数</th><th>交易总额</th></tr></thead>
          <tbody>
            <tr v-for="s in shiftList" :key="s.name"><td>{{ s.name }}</td><td>{{ s.count }}</td><td>¥{{ s.total }}</td></tr>
          </tbody>
        </table>
        <div class="form-buttons"><button class="btn-cancel" @click="showShiftSummary = false">关闭</button></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const keyword = ref('')
const list = ref([])
const showDetail = ref(false)
const showShiftSummary = ref(false)
const detailData = ref({})
const shiftList = ref([])

function fetchList() {
  list.value = [
    { id: 1, flowNo: 'LS20240620001', totalAmount: '156.50', discountAmount: '7.83', receivedAmount: '160.00', changeAmount: '3.50', cashier: '张三', saleTime: '2026-06-20 14:30' },
    { id: 2, flowNo: 'LS20240620002', totalAmount: '89.00', discountAmount: '0', receivedAmount: '100.00', changeAmount: '11.00', cashier: '李四', saleTime: '2026-06-20 15:12' },
    { id: 3, flowNo: 'LS20240620003', totalAmount: '320.00', discountAmount: '16.00', receivedAmount: '320.00', changeAmount: '0', cashier: '张三', saleTime: '2026-06-20 16:45' },
  ]
  shiftList.value = [
    { name: '张三', count: 25, total: '4560.00' },
    { name: '李四', count: 18, total: '3200.00' },
  ]
}
function openDetail(item) {
  detailData.value = { ...item, items: [{ id: 1, name: '可口可乐 330ml', quantity: 3, price: 3.5 }, { id: 2, name: '康师傅方便面', quantity: 2, price: 4 }] }
  showDetail.value = true
}
fetchList()
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
</style>
