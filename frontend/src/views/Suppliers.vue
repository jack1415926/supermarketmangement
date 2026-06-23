<!--
  供应商管理页面
  功能：供应商列表、新增供应商、编辑供应商信息
-->
<template>
  <div class="page-container">
    <div class="page-toolbar">
      <input v-model="keyword" placeholder="搜索供应商名称..." @keyup.enter="fetchList" />
      <button @click="fetchList">搜索</button>
      <button class="btn-add" @click="openAdd">+ 新增供应商</button>
    </div>
    <table class="data-table">
      <thead>
        <tr>
          <th>供应商名称</th>
          <th>联系人</th>
          <th>联系电话</th>
          <th>地址</th>
          <th>供应品类</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.name }}</td>
          <td>{{ item.contact }}</td>
          <td>{{ item.phone }}</td>
          <td>{{ item.address }}</td>
          <td>{{ item.category }}</td>
          <td>
            <button @click="openEdit(item)">编辑</button>
          </td>
        </tr>
      </tbody>
    </table>
    <div v-if="list.length === 0" class="empty-tip">暂无供应商数据</div>

    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal-card">
        <h3>{{ isEdit ? '编辑供应商' : '新增供应商' }}</h3>
        <div class="form-row">
          <label>供应商名称</label>
          <input v-model="form.name" placeholder="请输入供应商名称" required />
        </div>
        <div class="form-row form-row-2col">
          <div><label>联系人</label><input v-model="form.contact" placeholder="请输入联系人" /></div>
          <div><label>联系电话</label><input v-model="form.phone" placeholder="请输入联系电话" /></div>
        </div>
        <div class="form-row">
          <label>地址</label>
          <input v-model="form.address" placeholder="请输入地址" />
        </div>
        <div class="form-row">
          <label>供应商品类别</label>
          <input v-model="form.category" placeholder="如：饮料、食品" />
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
import { supplierAPI } from '@/api'
const keyword = ref('')
const list = ref([])
const showForm = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({ name: '', contact: '', phone: '', address: '', category: '' })

async function onMounted(() => fetchList()) {
  try {
    const res = await supplierAPI.list({ keyword: keyword.value })
    list.value = res.data || []
  } catch (err) {
    console.error('获取供应商列表失败', err)
  }
}
function openAdd() { isEdit.value = false; editId.value = null; form.value = { name: '', contact: '', phone: '', address: '', category: '' }; showForm.value = true }
function openEdit(item) { isEdit.value = true; editId.value = item.id; form.value = { name: item.name, contact: item.contact, phone: item.phone, address: item.address, category: item.category }; showForm.value = true }
async function handleSave() {
  if (!form.value.name) { alert('请输入供应商名称'); return }
  try {
    if (isEdit.value) {
      await supplierAPI.update(editId.value, form.value)
    } else {
      await supplierAPI.create(form.value)
    }
    showForm.value = false
    onMounted(() => fetchList())
  } catch (err) {
    console.error('保存失败', err)
    alert('保存失败')
  }
}
onMounted(() => fetchList())
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
