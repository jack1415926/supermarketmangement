<!--
  进货管理页面
  功能：进货记录列表、创建进货入库单、查看进货计划建议
-->
<template>
  <div class="page-container">
    <div class="page-toolbar">
      <input v-model="keyword" placeholder="搜索进货单号..." @keyup.enter="fetchList" />
      <button @click="fetchList">搜索</button>
      <button class="btn-add" @click="openAdd">+ 新建入库</button>
    </div>
    <table class="data-table">
      <thead>
        <tr>
          <th>进货单号</th>
          <th>供应商</th>
          <th>商品数量</th>
          <th>总金额</th>
          <th>进货日期</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.purchaseNo }}</td>
          <td>{{ item.supplier }}</td>
          <td>{{ item.itemCount }}</td>
          <td>¥{{ item.totalAmount }}</td>
          <td>{{ item.purchaseDate }}</td>
          <td>
            <button @click="openDetail(item)">详情</button>
          </td>
        </tr>
      </tbody>
    </table>
    <div v-if="list.length === 0" class="empty-tip">暂无进货记录</div>

    <!-- 新建入库弹窗 -->
    <div v-if="showAdd" class="modal-overlay" @click.self="showAdd = false">
      <div class="modal-card">
        <h3>新建进货入库</h3>
        <div class="form-row">
          <label>供应商</label>
          <select v-model="purchaseForm.supplier">
            <option value="">请选择供应商</option>
            <option value="统一食品有限公司">统一食品有限公司</option>
            <option value="日用品批发市场">日用品批发市场</option>
          </select>
        </div>
        <div class="form-row">
          <label>商品名称</label>
          <input v-model="purchaseForm.product" placeholder="请输入商品名称" />
        </div>
        <div class="form-row form-row-2col">
          <div><label>数量</label><input v-model.number="purchaseForm.quantity" type="number" /></div>
          <div><label>进价 (¥)</label><input v-model.number="purchaseForm.price" type="number" step="0.01" /></div>
        </div>
        <div class="form-row">
          <label>进货日期</label>
          <input v-model="purchaseForm.purchaseDate" type="date" />
        </div>
        <div class="form-buttons">
          <button class="btn-cancel" @click="showAdd = false">取消</button>
          <button class="btn-save" @click="handlePurchase">确认入库</button>
        </div>
      </div>
    </div>

    <!-- 进货详情弹窗 -->
    <div v-if="showDetail" class="modal-overlay" @click.self="showDetail = false">
      <div class="modal-card">
        <h3>进货详情 - {{ detailData.purchaseNo }}</h3>
        <p>供应商：{{ detailData.supplier }}</p>
        <p>总金额：¥{{ detailData.totalAmount }}</p>
        <p>日期：{{ detailData.purchaseDate }}</p>
        <table class="data-table" style="margin-top:16px;">
          <thead><tr><th>商品</th><th>数量</th><th>进价</th><th>小计</th></tr></thead>
          <tbody>
            <tr v-for="d in detailData.items" :key="d.id"><td>{{ d.product }}</td><td>{{ d.quantity }}</td><td>¥{{ d.price }}</td><td>¥{{ (d.quantity * d.price).toFixed(2) }}</td></tr>
          </tbody>
        </table>
        <div class="form-buttons"><button class="btn-cancel" @click="showDetail = false">关闭</button></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const keyword = ref('')
const list = ref([])
const showAdd = ref(false)
const showDetail = ref(false)
const detailData = ref({})
const purchaseForm = ref({ supplier: '', product: '', quantity: 1, price: 0, purchaseDate: new Date().toISOString().split('T')[0] })

function fetchList() {
  list.value = [
    { id: 1, purchaseNo: 'JH202406001', supplier: '统一食品有限公司', itemCount: 3, totalAmount: 5600.00, purchaseDate: '2026-06-15' },
    { id: 2, purchaseNo: 'JH202406002', supplier: '日用品批发市场', itemCount: 5, totalAmount: 3200.00, purchaseDate: '2026-06-18' },
  ]
}
function openAdd() { purchaseForm.value = { supplier: '', product: '', quantity: 1, price: 0, purchaseDate: new Date().toISOString().split('T')[0] }; showAdd.value = true }
function handlePurchase() {
  if (!purchaseForm.value.supplier || !purchaseForm.value.product) { alert('请填写供应商和商品名称'); return }
  const total = purchaseForm.value.quantity * purchaseForm.value.price
  list.value.unshift({ id: Date.now(), purchaseNo: 'JH' + Date.now().toString().slice(-6), supplier: purchaseForm.value.supplier, itemCount: 1, totalAmount: total.toFixed(2), purchaseDate: purchaseForm.value.purchaseDate })
  showAdd.value = false
  alert('入库成功')
}
function openDetail(item) {
  detailData.value = { ...item, items: [{ id: 1, product: '商品详情待开发', quantity: item.itemCount, price: (item.totalAmount / item.itemCount).toFixed(2) }] }
  showDetail.value = true
}
fetchList()
</script>

<style scoped>
.page-container { background: #fff; border-radius: 8px; padding: 20px; }
.page-toolbar { display: flex; gap: 10px; margin-bottom: 20px; }
.page-toolbar input { flex: 1; padding: 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
.page-toolbar button { padding: 10px 20px; background: #667eea; color: #fff; border: none; border-radius: 6px; cursor: pointer; }
.btn-add { background: #27ae60 !important; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { background: #f5f6fa; padding: 12px; text-align: left; font-size: 13px; color: #666; border-bottom: 2px solid #e8e8e8; }
.data-table td { padding: 12px; border-bottom: 1px solid #f0f0f0; font-size: 14px; }
.data-table td button { padding: 4px 12px; margin-right: 6px; border: 1px solid #ddd; border-radius: 4px; background: #fff; cursor: pointer; font-size: 12px; }
.empty-tip { text-align: center; padding: 60px; color: #999; font-size: 15px; }
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.4); display: flex; justify-content: center; align-items: center; z-index: 100; }
.modal-card { background: #fff; border-radius: 12px; padding: 30px; width: 520px; max-height: 80vh; overflow-y: auto; box-shadow: 0 10px 40px rgba(0,0,0,0.2); }
.modal-card h3 { margin-bottom: 24px; }
.form-row { margin-bottom: 16px; }
.form-row label { display: block; margin-bottom: 6px; font-size: 13px; color: #666; }
.form-row input, .form-row select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
.form-row-2col { display: flex; gap: 16px; }
.form-row-2col > div { flex: 1; }
.form-buttons { display: flex; gap: 12px; margin-top: 24px; }
.btn-cancel { flex: 1; padding: 12px; border: 1px solid #ddd; border-radius: 8px; background: #fff; cursor: pointer; }
.btn-save { flex: 1; padding: 12px; border: none; border-radius: 8px; background: #667eea; color: #fff; cursor: pointer; font-size: 15px; }
</style>
