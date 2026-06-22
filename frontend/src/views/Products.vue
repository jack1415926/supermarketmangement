<!--
  商品管理页面
  功能：商品列表分页展示、关键字搜索、新增商品、编辑商品、删除/下架
-->
<template>
  <div class="products-page">
    <!-- 顶部操作栏：搜索 + 新增按钮 -->
    <div class="page-toolbar">
      <input v-model="keyword" placeholder="搜索商品名称或条形码..." @keyup.enter="fetchList" />
      <button @click="fetchList">搜索</button>
      <button class="btn-add" @click="openAdd">+ 新增商品</button>
    </div>

    <!-- 商品表格 -->
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
          <td>{{ item.category }}</td>
          <td>{{ item.barcode }}</td>
          <td>¥{{ item.retailPrice }}</td>
          <td>¥{{ item.purchasePrice }}</td>
          <td>{{ item.currentStock }}</td>
          <td>
            <button @click="openEdit(item)">编辑</button>
            <button @click="handleDelete(item.id)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- 新增/编辑弹窗 -->
<div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
  <div class="modal-card">
    <h3>{{ isEdit ? '编辑商品' : '新增商品' }}</h3>

    <!-- 商品名称 -->
    <div class="form-row">
      <label>商品名称</label>
      <input v-model="form.name" placeholder="请输入商品名称" required />
    </div>

    <!-- 分类 -->
    <div class="form-row">
      <label>商品分类</label>
      <select v-model="form.category">
        <option value="">请选择分类</option>
        <option value="饮料">饮料</option>
        <option value="食品">食品</option>
        <option value="日用品">日用品</option>
        <option value="烟酒">烟酒</option>
      </select>
    </div>

    <!-- 条形码 + 售价一行 -->
    <div class="form-row form-row-2col">
      <div>
        <label>条形码</label>
        <input v-model="form.barcode" placeholder="扫码或手动输入" />
      </div>
      <div>
        <label>售价 (¥)</label>
        <input v-model.number="form.retailPrice" type="number" step="0.01" placeholder="零售单价" />
      </div>
    </div>

    <!-- 进价 -->
    <div class="form-row">
      <label>进价 (¥)</label>
      <input v-model.number="form.purchasePrice" type="number" step="0.01" placeholder="进货单价" />
    </div>

    <!-- 底部按钮 -->
    <div class="form-buttons">
      <button class="btn-cancel" @click="showForm = false">取消</button>
      <button class="btn-save" @click="handleSave">保存</button>
    </div>
  </div>
</div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 搜索关键字
const keyword = ref('')
// 商品列表
const list = ref([])
// 控制弹窗显示
const showForm = ref(false)
// 是否编辑模式（false = 新增，true = 编辑）
const isEdit = ref(false)
// 当前编辑的商品 id（新增时为 null）
const editId = ref(null)
// 表单数据
const form = ref({
  name: '',
  category: '',
  barcode: '',
  retailPrice: 0,
  purchasePrice: 0
})

/** 拉取商品列表（当前用假数据） */
function fetchList() {
  list.value = [
    { id: 1, name: '可口可乐 330ml', category: '饮料', barcode: '6901234567890', retailPrice: 3.5, purchasePrice: 2.8, currentStock: 200 },
    { id: 2, name: '康师傅方便面', category: '食品', barcode: '6901234567891', retailPrice: 4.0, purchasePrice: 3.2, currentStock: 150 },
    { id: 3, name: '农夫山泉 550ml', category: '饮料', barcode: '6901234567892', retailPrice: 2.0, purchasePrice: 1.5, currentStock: 300 }
  ]
}

/** 打开新增弹窗（清空表单） */
function openAdd() {
  isEdit.value = false
  editId.value = null
  form.value = { name: '', category: '', barcode: '', retailPrice: 0, purchasePrice: 0 }
  showForm.value = true
}

/** 打开编辑弹窗（回填当前商品数据） */
function openEdit(item) {
  isEdit.value = true
  editId.value = item.id
  // 把商品信息填到表单里
  form.value = {
    name: item.name,
    category: item.category,
    barcode: item.barcode,
    retailPrice: item.retailPrice,
    purchasePrice: item.purchasePrice
  }
  showForm.value = true
}

/** 保存商品（新增或编辑） */
function handleSave() {
  if (!form.value.name) { alert('请输入商品名称'); return }
  if (isEdit.value) {
    // 编辑：找到列表里对应商品更新
    const target = list.value.find(item => item.id === editId.value)
    if (target) Object.assign(target, form.value)
  } else {
    // 新增：塞入列表（id 暂用时间戳，后续后端生成）
    list.value.push({ id: Date.now(), ...form.value, currentStock: 0 })
  }
  showForm.value = false
}

/** 删除商品 */
function handleDelete(id) {
  if (confirm('确定删除该商品？')) {
    list.value = list.value.filter(item => item.id !== id)
  }
}

// 页面加载时拉取数据
fetchList()
</script>

<style scoped>
.products-page { background: #fff; border-radius: 8px; padding: 20px; }

/* 工具栏 */
.page-toolbar { display: flex; gap: 10px; margin-bottom: 20px; }
.page-toolbar input { flex: 1; padding: 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
.page-toolbar button { padding: 10px 20px; background: #667eea; color: #fff; border: none; border-radius: 6px; cursor: pointer; }
.btn-add { background: #27ae60 !important; }

/* 表格 */
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { background: #f5f6fa; padding: 12px; text-align: left; font-size: 13px; color: #666; border-bottom: 2px solid #e8e8e8; }
.data-table td { padding: 12px; border-bottom: 1px solid #f0f0f0; font-size: 14px; }
.data-table td button { padding: 4px 12px; margin-right: 6px; border: 1px solid #ddd; border-radius: 4px; background: #fff; cursor: pointer; font-size: 12px; }
.data-table td button:hover { border-color: #667eea; color: #667eea; }

/* 空状态 */
.empty-tip { text-align: center; padding: 60px; color: #999; font-size: 15px; }
/* 弹窗遮罩 */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.4); display: flex; justify-content: center; align-items: center; z-index: 100; }
/* 弹窗卡片 */
.modal-card { background: #fff; border-radius: 12px; padding: 30px; width: 520px; max-height: 80vh; overflow-y: auto; box-shadow: 0 10px 40px rgba(0,0,0,0.2); }
.modal-card h3 { margin-bottom: 24px; }
/* 表单行 */
.form-row { margin-bottom: 16px; }
.form-row label { display: block; margin-bottom: 6px; font-size: 13px; color: #666; }
.form-row input, .form-row select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
/* 两列布局 */
.form-row-2col { display: flex; gap: 16px; }
.form-row-2col > div { flex: 1; }
/* 按钮 */
.form-buttons { display: flex; gap: 12px; margin-top: 24px; }
.btn-cancel { flex: 1; padding: 12px; border: 1px solid #ddd; border-radius: 8px; background: #fff; cursor: pointer; }
.btn-save { flex: 1; padding: 12px; border: none; border-radius: 8px; background: #667eea; color: #fff; cursor: pointer; font-size: 15px; }
</style>
