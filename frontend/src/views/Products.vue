<!--
  商品管理页面
  功能：商品列表分页展示、关键字搜索、新增商品、编辑商品、删除/下架
  API对接：productAPI、categoryAPI
-->
<template>
  <div class="products-page">
    <div class="page-toolbar">
      <input v-model="keyword" placeholder="搜索商品名称或条形码..." @keyup.enter="fetchList" />
      <button @click="fetchList">搜索</button>
      <button class="btn-add" @click="openAdd">+ 新增商品</button>
    </div>

    <table class="data-table">
      <thead>
        <tr>
          <th>商品编号</th>
          <th>商品名称</th>
          <th>分类</th>
          <th>条形码</th>
          <th>售价</th>
          <th>进价</th>
          <th>库存</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.id }}</td>
          <td>{{ item.name }}</td>
          <td>{{ item.categoryName || item.category?.name || '' }}</td>
          <td>{{ item.barcode }}</td>
          <td>&yen;{{ item.salePrice }}</td>
          <td>&yen;{{ item.purchasePrice }}</td>
          <td>{{ item.stock }}</td>
          <td>
            <button @click="openEdit(item)">编辑</button>
            <button @click="handleDelete(item.id)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="list.length === 0" class="empty-tip">暂无商品数据</div>

    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal-card">
        <h3>{{ isEdit ? '编辑商品' : '新增商品' }}</h3>
        <div class="form-row">
          <label>商品名称</label>
          <input v-model="form.name" placeholder="请输入商品名称" required />
        </div>
        <div class="form-row">
          <label>商品分类</label>
          <select v-model="form.categoryId">
            <option value="">请选择分类</option>
            <option v-for="cat in categoryList" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
          </select>
        </div>
        <div class="form-row form-row-2col">
          <div><label>条形码</label><input v-model="form.barcode" placeholder="扫码或手动输入" /></div>
          <div><label>售价 (&yen;)</label><input v-model.number="form.salePrice" type="number" step="0.01" placeholder="零售单价" /></div>
        </div>
        <div class="form-row">
          <label>进价 (&yen;)</label>
          <input v-model.number="form.purchasePrice" type="number" step="0.01" placeholder="进货单价" />
        </div>
        <div class="form-buttons">
          <button class="btn-cancel" @click="showForm = false">取消</button>
          <button class="btn-save" @click="handleSave">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { productAPI, categoryAPI } from '@/api'

const keyword = ref('')
const list = ref([])
const showForm = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const categoryList = ref([])
const form = ref({ name: '', categoryId: '', barcode: '', salePrice: 0, purchasePrice: 0 })

/** 拉取商品列表（对接后端分页 API） */
async function fetchList() {
  try {
    const res = await productAPI.list({ keyword: keyword.value })
    // 后端返回 PageDTO: { code: 200, data: { content: [...], totalElements: 12 } }
    list.value = res.data?.content || []
  } catch (err) {
    console.error('获取商品列表失败', err)
  }
}

/** 拉取分类列表 */
async function fetchCategories() {
  try {
    const res = await categoryAPI.list()
    // 兼容分页和非分页两种返回格式
    categoryList.value = res.data?.content || res.data || []
  } catch (err) {
    console.error('获取分类列表失败', err)
  }
}

/** 打开新增弹窗 */
function openAdd() {
  isEdit.value = false
  editId.value = null
  fetchCategories()
  form.value = { name: '', categoryId: '', barcode: '', salePrice: 0, purchasePrice: 0 }
  showForm.value = true
}

/** 打开编辑弹窗 */
function openEdit(item) {
  isEdit.value = true
  editId.value = item.id
  fetchCategories()
  form.value = {
    name: item.name || '',
    categoryId: item.categoryId || item.category?.id || '',
    barcode: item.barcode || '',
    salePrice: item.salePrice || 0,
    purchasePrice: item.purchasePrice || 0
  }
  showForm.value = true
}

/** 保存商品（调用后端 API） */
async function handleSave() {
  if (!form.value.name) { alert('请输入商品名称'); return }
  try {
    // 后端期望 category 是嵌套对象 { id: categoryId }
    const payload = {
      name: form.value.name,
      barcode: form.value.barcode,
      salePrice: form.value.salePrice,
      purchasePrice: form.value.purchasePrice,
      category: form.value.categoryId ? { id: form.value.categoryId } : null,
      stock: 0
    }
    if (isEdit.value) {
      await productAPI.update(editId.value, payload)
    } else {
      await productAPI.create(payload)
    }
    showForm.value = false
    fetchList()
  } catch (err) {
    console.error('保存失败', err)
    const msg = err.response?.data?.message || err.message || '保存失败'
    alert(msg)
  }
}

/** 删除商品（调用后端 API） */
async function handleDelete(id) {
  if (!confirm('确定删除该商品？')) return
  try {
    await productAPI.remove(id)
    fetchList()
  } catch (err) {
    console.error('删除失败', err)
    alert('删除失败，请检查是否存在关联数据')
  }
}

onMounted(() => {
  fetchList()
  fetchCategories()
})
</script>

<style scoped>
.products-page { background: #fff; border-radius: 8px; padding: 20px; }
.page-toolbar { display: flex; gap: 10px; margin-bottom: 20px; }
.page-toolbar input { flex: 1; padding: 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
.page-toolbar button { padding: 10px 20px; background: #667eea; color: #fff; border: none; border-radius: 6px; cursor: pointer; }
.btn-add { background: #27ae60 !important; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { background: #f5f6fa; padding: 12px; text-align: left; font-size: 13px; color: #666; border-bottom: 2px solid #e8e8e8; }
.data-table td { padding: 12px; border-bottom: 1px solid #f0f0f0; font-size: 14px; }
.data-table td button { padding: 4px 12px; margin-right: 6px; border: 1px solid #ddd; border-radius: 4px; background: #fff; cursor: pointer; font-size: 12px; }
.data-table td button:hover { border-color: #667eea; color: #667eea; }
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
