<!--
  进货管理页面
  功能：进货记录列表、创建进货入库单
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
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.purchaseNo }}</td>
          <td>{{ item.supplier }}</td>
          <td>{{ item.itemCount }}</td>
          <td>&yen;{{ item.totalAmount }}</td>
          <td>{{ item.purchaseDate }}</td>
        </tr>
      </tbody>
    </table>
    <div v-if="list.length === 0" class="empty-tip">暂无进货记录</div>

    <!-- 新建入库弹窗 -->
    <div v-if="showAdd" class="modal-overlay" @click.self="showAdd = false">
      <div class="modal-card" style="width:600px;">
        <h3>新建进货入库</h3>
        <!-- 选择供应商 -->
        <div class="form-row">
          <label>供应商</label>
          <select v-model="purchaseForm.supplierId">
            <option :value="null">请选择供应商</option>
            <option v-for="s in suppliers" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
        </div>
        <!-- 进货商品列表 -->
        <div class="form-row">
          <label>进货商品</label>
          <div v-for="(item, index) in purchaseForm.items" :key="index" class="purchase-item-row">
            <select v-model="item.productId" style="flex:2;">
              <option :value="null">选择商品</option>
              <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }}</option>
            </select>
            <input v-model.number="item.quantity" type="number" min="1" placeholder="数量" style="flex:1;margin:0 8px;" />
            <input v-model.number="item.purchasePrice" type="number" step="0.01" min="0" placeholder="进价" style="flex:1;" />
            <button class="btn-remove" @click="removePurchaseItem(index)">删除</button>
          </div>
          <button class="btn-add-item" @click="addPurchaseItem">+ 添加商品</button>
        </div>
        <div class="form-row">
          <label>备注</label>
          <input v-model="purchaseForm.remark" placeholder="可选备注" />
        </div>
        <div class="form-buttons">
          <button class="btn-cancel" @click="showAdd = false">取消</button>
          <button class="btn-save" @click="handlePurchase">确认入库</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { purchaseAPI, supplierAPI, productAPI } from '@/api'

const keyword = ref('')
const list = ref([])
const showAdd = ref(false)
const suppliers = ref([])
const products = ref([])

// 入库表单：符合后端 PurchaseRequest 格式
const purchaseForm = ref({
  supplierId: null,
  items: [{ productId: null, quantity: 1, purchasePrice: 0 }],
  remark: ''
})

/** 加载供应商和商品列表（供下拉选择） */
async function loadOptions() {
  try {
    const supRes = await supplierAPI.list({})
    suppliers.value = supRes.data?.data || supRes.data || []
  } catch (e) { console.error('加载供应商失败', e) }
  try {
    const prodRes = await productAPI.list({})
    products.value = prodRes.data?.content || prodRes.data?.data || prodRes.data || []
  } catch (e) { console.error('加载商品失败', e) }
}

/** 拉取进货记录列表 */
async function fetchList() {
  try {
    const res = await purchaseAPI.list({ keyword: keyword.value })
    const rawList = res.data?.content || res.data?.data || res.data || []
    list.value = rawList.map(function(p) {
      return {
        id: p.id,
        purchaseNo: p.purchaseNo || ('PO' + String(p.id).padStart(6, '0')),
        supplier: (p.supplier && p.supplier.name) || p.supplierName || '未知',
        itemCount: (p.items && p.items.length) || p.itemCount || 0,
        totalAmount: Number(p.totalAmount || 0).toFixed(2),
        purchaseDate: (p.purchaseDate || p.createdAt || '').substring(0, 10)
      }
    })
  } catch (err) {
    console.error('获取进货列表失败', err)
  }
}

function openAdd() {
  purchaseForm.value = {
    supplierId: null,
    items: [{ productId: null, quantity: 1, purchasePrice: 0 }],
    remark: ''
  }
  showAdd.value = true
}

function addPurchaseItem() {
  purchaseForm.value.items.push({ productId: null, quantity: 1, purchasePrice: 0 })
}
function removePurchaseItem(index) {
  if (purchaseForm.value.items.length > 1) {
    purchaseForm.value.items.splice(index, 1)
  }
}

async function handlePurchase() {
  if (!purchaseForm.value.supplierId) { alert('请选择供应商'); return }
  // 过滤掉未选择商品的条目
  const validItems = purchaseForm.value.items.filter(function(i) { return i.productId != null && i.quantity > 0 })
  if (validItems.length === 0) { alert('请至少选择一个商品并填写数量'); return }
  try {
    await purchaseAPI.create({
      supplierId: purchaseForm.value.supplierId,
      items: validItems.map(function(i) {
        return {
          productId: i.productId,
          quantity: i.quantity,
          purchasePrice: i.purchasePrice > 0 ? i.purchasePrice : null
        }
      }),
      remark: purchaseForm.value.remark || undefined
    })
    showAdd.value = false
    await fetchList()
    alert('入库成功')
  } catch (err) {
    console.error('入库失败', err)
    alert('入库失败：' + ((err.response && err.response.data && err.response.data.message) || err.message))
  }
}

onMounted(function() {
  loadOptions()
  fetchList()
})
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
.purchase-item-row { display: flex; align-items: center; margin-bottom: 8px; }
.purchase-item-row select, .purchase-item-row input { padding: 8px; border: 1px solid #ddd; border-radius: 4px; font-size: 13px; }
.btn-remove { padding: 6px 12px; border: 1px solid #e74c3c; border-radius: 4px; background: #fff; color: #e74c3c; cursor: pointer; font-size: 12px; white-space: nowrap; }
.btn-add-item { padding: 8px 16px; border: 1px dashed #667eea; border-radius: 4px; background: #f8f9ff; color: #667eea; cursor: pointer; font-size: 13px; width: 100%; margin-top: 8px; }
</style>
