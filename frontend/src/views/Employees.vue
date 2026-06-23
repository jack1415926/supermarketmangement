<!--
  人员管理页面
  功能：员工列表展示、新增员工、编辑员工信息
-->
<template>
  <div class="page-container">
    <div class="page-toolbar">
      <input v-model="keyword" placeholder="搜索员工姓名或工号..." @keyup.enter="fetchList" />
      <button @click="fetchList">搜索</button>
      <button class="btn-add" @click="openAdd">+ 新增员工</button>
    </div>
    <table class="data-table">
      <thead>
        <tr>
          <th>工号</th>
          <th>姓名</th>
          <th>手机号</th>
          <th>职位</th>
          <th>入职日期</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.employeeNo }}</td>
          <td>{{ item.name }}</td>
          <td>{{ item.phone }}</td>
          <td>{{ item.position }}</td>
          <td>{{ item.hireDate }}</td>
          <td><span :class="item.status === '在职' ? 'status-active' : 'status-inactive'">{{ item.status }}</span></td>
          <td>
            <button @click="openEdit(item)">编辑</button>
          </td>
        </tr>
      </tbody>
    </table>
    <div v-if="list.length === 0" class="empty-tip">暂无员工数据</div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal-card">
        <h3>{{ isEdit ? '编辑员工' : '新增员工' }}</h3>
        <div class="form-row">
          <label>姓名</label>
          <input v-model="form.name" placeholder="请输入姓名" required />
        </div>
        <div class="form-row form-row-2col">
          <div><label>手机号</label><input v-model="form.phone" placeholder="请输入手机号" /></div>
          <div><label>职位</label><input v-model="form.position" placeholder="如：收银员" /></div>
        </div>
        <div class="form-row">
          <label>入职日期</label>
          <input v-model="form.hireDate" type="date" />
        </div>
        <div class="form-row">
          <label>状态</label>
          <select v-model="form.status"><option value="在职">在职</option><option value="离职">离职</option></select>
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
import { employeeAPI } from '@/api'
const keyword = ref('')
const list = ref([])
const showForm = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({ name: '', phone: '', position: '', hireDate: '', status: '在职' })

async function onMounted(() => fetchList()) {
  try {
    const res = await employeeAPI.list({ keyword: keyword.value })
    list.value = res.data || []
  } catch (err) {
    console.error('获取员工列表失败', err)
  }
}
function openAdd() {
  isEdit.value = false; editId.value = null
  form.value = { name: '', phone: '', position: '', hireDate: '', status: '在职' }
  showForm.value = true
}
function openEdit(item) {
  isEdit.value = true; editId.value = item.id
  form.value = { name: item.name, phone: item.phone, position: item.position, hireDate: item.hireDate, status: item.status }
  showForm.value = true
}
async function handleSave() {
  if (!form.value.name) { alert('请输入姓名'); return }
  try {
    if (isEdit.value) {
      await employeeAPI.update(editId.value, form.value)
    } else {
      await employeeAPI.create(form.value)
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
.page-container,.page-toolbar,.data-table,.empty-tip { /* same as products */ }
.page-container { background: #fff; border-radius: 8px; padding: 20px; }
.page-toolbar { display: flex; gap: 10px; margin-bottom: 20px; }
.page-toolbar input { flex: 1; padding: 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
.page-toolbar button { padding: 10px 20px; background: #667eea; color: #fff; border: none; border-radius: 6px; cursor: pointer; }
.btn-add { background: #27ae60 !important; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { background: #f5f6fa; padding: 12px; text-align: left; font-size: 13px; color: #666; border-bottom: 2px solid #e8e8e8; }
.data-table td { padding: 12px; border-bottom: 1px solid #f0f0f0; font-size: 14px; }
.data-table td button { padding: 4px 12px; margin-right: 6px; border: 1px solid #ddd; border-radius: 4px; background: #fff; cursor: pointer; font-size: 12px; }
.status-active { color: #27ae60; }
.status-inactive { color: #999; }
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
