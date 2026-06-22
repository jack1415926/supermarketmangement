<!--
  系统管理页面
  功能：用户管理、角色权限分配（仅系统管理员可访问）
-->
<template>
  <div class="page-container">
    <div class="page-toolbar">
      <button class="btn-add" @click="openAdd">+ 新增用户</button>
    </div>
    <table class="data-table">
      <thead>
        <tr>
          <th>用户名</th>
          <th>角色</th>
          <th>状态</th>
          <th>创建时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.username }}</td>
          <td>{{ item.role === 'cashier' ? '收银员' : item.role === 'admin' ? '管理员' : '系统管理员' }}</td>
          <td><span :class="item.status === 'active' ? 'tag-normal' : 'tag-warn'">{{ item.status === 'active' ? '正常' : '已禁用' }}</span></td>
          <td>{{ item.createdAt }}</td>
          <td>
            <button @click="toggleStatus(item)">{{ item.status === 'active' ? '禁用' : '启用' }}</button>
          </td>
        </tr>
      </tbody>
    </table>
    <div v-if="list.length === 0" class="empty-tip">暂无用户数据</div>

    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal-card">
        <h3>新增用户</h3>
        <div class="form-row"><label>用户名</label><input v-model="form.username" placeholder="请输入用户名" /></div>
        <div class="form-row"><label>密码</label><input v-model="form.password" type="password" placeholder="请输入密码" /></div>
        <div class="form-row">
          <label>角色</label>
          <select v-model="form.role">
            <option value="cashier">收银员</option>
            <option value="admin">管理员</option>
            <option value="superadmin">系统管理员</option>
          </select>
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
import { ref } from 'vue'
const list = ref([])
const showForm = ref(false)
const form = ref({ username: '', password: '', role: 'cashier' })

list.value = [
  { id: 1, username: 'admin', role: 'superadmin', status: 'active', createdAt: '2026-06-01' },
  { id: 2, username: 'cashier01', role: 'cashier', status: 'active', createdAt: '2026-06-10' },
  { id: 3, username: 'cashier02', role: 'cashier', status: 'disabled', createdAt: '2026-06-15' },
]

function openAdd() { form.value = { username: '', password: '', role: 'cashier' }; showForm.value = true }
function handleSave() {
  if (!form.value.username) { alert('请输入用户名'); return }
  list.value.push({ id: Date.now(), ...form.value, status: 'active', createdAt: new Date().toISOString().split('T')[0] })
  showForm.value = false
}
function toggleStatus(item) { item.status = item.status === 'active' ? 'disabled' : 'active' }
</script>

<style scoped>
.page-container { background: #fff; border-radius: 8px; padding: 20px; }
.page-toolbar { display: flex; gap: 10px; margin-bottom: 20px; }
.page-toolbar button { padding: 10px 20px; background: #667eea; color: #fff; border: none; border-radius: 6px; cursor: pointer; }
.btn-add { background: #27ae60 !important; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { background: #f5f6fa; padding: 12px; text-align: left; font-size: 13px; color: #666; border-bottom: 2px solid #e8e8e8; }
.data-table td { padding: 12px; border-bottom: 1px solid #f0f0f0; font-size: 14px; }
.data-table td button { padding: 4px 12px; margin-right: 6px; border: 1px solid #ddd; border-radius: 4px; background: #fff; cursor: pointer; font-size: 12px; }
.tag-normal { padding: 2px 8px; background: #27ae60; color: #fff; border-radius: 4px; font-size: 12px; }
.tag-warn { padding: 2px 8px; background: #e74c3c; color: #fff; border-radius: 4px; font-size: 12px; }
.empty-tip { text-align: center; padding: 60px; color: #999; font-size: 15px; }
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.4); display: flex; justify-content: center; align-items: center; z-index: 100; }
.modal-card { background: #fff; border-radius: 12px; padding: 30px; width: 480px; box-shadow: 0 10px 40px rgba(0,0,0,0.2); }
.modal-card h3 { margin-bottom: 24px; }
.form-row { margin-bottom: 16px; }
.form-row label { display: block; margin-bottom: 6px; font-size: 13px; color: #666; }
.form-row input, .form-row select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
.form-buttons { display: flex; gap: 12px; margin-top: 24px; }
.btn-cancel { flex: 1; padding: 12px; border: 1px solid #ddd; border-radius: 8px; background: #fff; cursor: pointer; }
.btn-save { flex: 1; padding: 12px; border: none; border-radius: 8px; background: #667eea; color: #fff; cursor: pointer; font-size: 15px; }
</style>
