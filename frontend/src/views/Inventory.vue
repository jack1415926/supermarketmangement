<!--
  库存管理页面
  功能：库存列表（高亮预警）、库存盘点录入、盘点报告
-->
<template>
  <div class="page-container">
    <div class="page-toolbar">
      <input v-model="keyword" placeholder="搜索商品名称..." @keyup.enter="fetchList" />
      <button @click="fetchList">搜索</button>
      <button class="btn-add" @click="openCheck">库存盘点</button>
    </div>
    <table class="data-table">
      <thead>
        <tr>
          <th>商品名称</th>
          <th>当前库存</th>
          <th>库存下限</th>
          <th>库存上限</th>
          <th>库存金额</th>
          <th>状态</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.id" :class="{ 'row-warn': item.currentStock <= item.stockMin, 'row-over': item.currentStock >= item.stockMax }">
          <td>{{ item.name }}</td>
          <td>{{ item.currentStock }}</td>
          <td>{{ item.stockMin }}</td>
          <td>{{ item.stockMax }}</td>
          <td>¥{{ (item.currentStock * item.purchasePrice).toFixed(2) }}</td>
          <td>
            <span v-if="item.currentStock <= item.stockMin" class="tag-warn">缺货预警</span>
            <span v-else-if="item.currentStock >= item.stockMax" class="tag-over">库存积压</span>
            <span v-else class="tag-normal">正常</span>
          </td>
        </tr>
      </tbody>
    </table>
    <div v-if="list.length === 0" class="empty-tip">暂无库存数据</div>

    <!-- 盘点弹窗 -->
    <div v-if="showCheck" class="modal-overlay" @click.self="showCheck = false">
      <div class="modal-card">
        <h3>库存盘点</h3>
        <div v-for="item in list" :key="item.id" class="check-row">
          <span>{{ item.name }}</span>
          <span>系统库存：{{ item.currentStock }}</span>
          <input v-model.number="item.actualStock" type="number" placeholder="实际库存" />
        </div>
        <div class="form-buttons">
          <button class="btn-cancel" @click="showCheck = false">取消</button>
          <button class="btn-save" @click="submitCheck">提交盘点</button>
        </div>
      </div>
    </div>

    <!-- 盘点报告弹窗 -->
    <div v-if="showReport" class="modal-overlay" @click.self="showReport = false">
      <div class="modal-card">
        <h3>盘点报告</h3>
        <table class="data-table">
          <thead><tr><th>商品</th><th>系统库存</th><th>实际库存</th><th>差异</th><th>差异金额</th></tr></thead>
          <tbody>
            <tr v-for="r in reportList" :key="r.id">
              <td>{{ r.name }}</td>
              <td>{{ r.systemStock }}</td>
              <td>{{ r.actualStock }}</td>
              <td :class="r.diff > 0 ? 'text-green' : r.diff < 0 ? 'text-red' : ''">{{ r.diff > 0 ? '+' + r.diff : r.diff }}</td>
              <td>¥{{ (Math.abs(r.diff) * r.purchasePrice).toFixed(2) }}</td>
            </tr>
          </tbody>
        </table>
        <div class="form-buttons"><button class="btn-cancel" @click="showReport = false">关闭</button></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { inventoryAPI } from '@/api'
const keyword = ref('')
const list = ref([])
const showCheck = ref(false)
const showReport = ref(false)
const reportList = ref([])

async function fetchList() {
  try {
    const res = await inventoryAPI.list({ keyword: keyword.value })
    const rawList = res.data?.content || res.data || []
    // 将后端返回的字段名映射为前端模板使用的字段名
    list.value = rawList.map(function(item) {
      return {
        id: item.productId,
        name: item.productName,
        currentStock: item.stock,
        stockMin: item.minStock,
        stockMax: item.maxStock,
        purchasePrice: item.purchasePrice
      }
    })
  } catch (err) {
    console.error('获取库存列表失败', err)
  }
}
function openCheck() { list.value.forEach(i => i.actualStock = i.currentStock); showCheck.value = true }
async function submitCheck() {
  try {
    // 收集盘点数据
    const checkItems = list.value.map(i => ({ productId: i.id, actualStock: i.actualStock || i.currentStock }))
    await inventoryAPI.check({ items: checkItems })
    reportList.value = list.value.map(i => ({ ...i, systemStock: i.currentStock, actualStock: i.actualStock || i.currentStock, diff: (i.actualStock || i.currentStock) - i.currentStock }))
    showCheck.value = false; showReport.value = true
  } catch (err) {
    console.error('盘点失败', err)
    alert('盘点提交失败')
  }
}
onMounted(() => fetchList())
</script>

<style scoped>
.page-container { background: #fff; border-radius: 8px; padding: 20px; }
.page-toolbar { display: flex; gap: 10px; margin-bottom: 20px; }
.page-toolbar input { flex: 1; padding: 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
.page-toolbar button { padding: 10px 20px; background: #667eea; color: #fff; border: none; border-radius: 6px; cursor: pointer; }
.btn-add { background: #e67e22 !important; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { background: #f5f6fa; padding: 12px; text-align: left; font-size: 13px; color: #666; border-bottom: 2px solid #e8e8e8; }
.data-table td { padding: 12px; border-bottom: 1px solid #f0f0f0; font-size: 14px; }
.row-warn { background: #fff5f5; }
.row-over { background: #fffff0; }
.tag-warn { padding: 2px 8px; background: #e74c3c; color: #fff; border-radius: 4px; font-size: 12px; }
.tag-over { padding: 2px 8px; background: #e67e22; color: #fff; border-radius: 4px; font-size: 12px; }
.tag-normal { padding: 2px 8px; background: #27ae60; color: #fff; border-radius: 4px; font-size: 12px; }
.empty-tip { text-align: center; padding: 60px; color: #999; font-size: 15px; }
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.4); display: flex; justify-content: center; align-items: center; z-index: 100; }
.modal-card { background: #fff; border-radius: 12px; padding: 30px; width: 600px; max-height: 80vh; overflow-y: auto; box-shadow: 0 10px 40px rgba(0,0,0,0.2); }
.modal-card h3 { margin-bottom: 24px; }
.check-row { display: flex; align-items: center; gap: 16px; padding: 10px 0; border-bottom: 1px solid #f0f0f0; }
.check-row span { font-size: 13px; }
.check-row input { width: 100px; padding: 6px; border: 1px solid #ddd; border-radius: 4px; text-align: center; }
.form-buttons { display: flex; gap: 12px; margin-top: 24px; }
.btn-cancel { flex: 1; padding: 12px; border: 1px solid #ddd; border-radius: 8px; background: #fff; cursor: pointer; }
.btn-save { flex: 1; padding: 12px; border: none; border-radius: 8px; background: #667eea; color: #fff; cursor: pointer; font-size: 15px; }
.text-green { color: #27ae60; }
.text-red { color: #e74c3c; }
</style>

