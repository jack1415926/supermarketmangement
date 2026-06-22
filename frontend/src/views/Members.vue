<!--
  会员管理页面
  功能：会员列表、注册发卡、会员续期、消费记录查看
-->
<template>
  <div class="page-container">
    <!-- 顶部工具栏 -->
    <div class="page-toolbar">
      <input v-model="keyword" placeholder="搜索会员姓名或手机号..." @keyup.enter="fetchList" />
      <button @click="fetchList">搜索</button>
      <button class="btn-add" @click="openRegister">+ 注册会员</button>
    </div>

    <!-- 会员表格 -->
    <table class="data-table">
      <thead>
        <tr>
          <th>会员卡号</th>
          <th>姓名</th>
          <th>手机号</th>
          <th>累计消费</th>
          <th>有效期至</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.cardNo }}</td>
          <td>{{ item.name }}</td>
          <td>{{ item.phone }}</td>
          <td>¥{{ item.totalSpent }}</td>
          <td>
            <span :class="{ 'expired': isExpired(item.validUntil) }">
              {{ item.validUntil }}
            </span>
          </td>
          <td>
            <button @click="openHistory(item)">消费记录</button>
            <button @click="handleRenew(item.id)">续期</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="list.length === 0" class="empty-tip">暂无会员数据</div>
  </div>
  <!-- 注册会员弹窗 -->
<div v-if="showRegister" class="modal-overlay" @click.self="showRegister = false">
  <div class="modal-card">
    <h3>注册新会员</h3>

    <!-- 姓名 -->
    <div class="form-row">
      <label>会员姓名</label>
      <input v-model="registerForm.name" placeholder="请输入姓名" required />
    </div>

    <!-- 手机号 + 性别 -->
    <div class="form-row form-row-2col">
      <div>
        <label>手机号</label>
        <input v-model="registerForm.phone" placeholder="请输入手机号" required />
      </div>
      <div>
        <label>性别</label>
        <select v-model="registerForm.gender">
          <option value="男">男</option>
          <option value="女">女</option>
        </select>
      </div>
    </div>

    <!-- 身份证号 -->
    <div class="form-row">
      <label>身份证号</label>
      <input v-model="registerForm.idCard" placeholder="请输入身份证号" />
    </div>

    <!-- 出生日期 -->
    <div class="form-row">
      <label>出生日期</label>
      <input v-model="registerForm.birthday" type="date" />
    </div>

    <!-- 按钮 -->
    <div class="form-buttons">
      <button class="btn-cancel" @click="showRegister = false">取消</button>
      <button class="btn-save" @click="handleRegister">确认注册</button>
    </div>
  </div>
</div>
<!-- 消费记录弹窗 -->
<div v-if="showHistory" class="modal-overlay" @click.self="showHistory = false">
  <div class="modal-card">
    <h3>{{ historyMember }} 的消费记录</h3>

    <!-- 累计消费 -->
    <p class="history-total">累计消费：<strong>¥{{ historyTotal }}</strong></p>

    <!-- 消费记录列表 -->
    <table class="data-table">
      <thead>
        <tr>
          <th>时间</th>
          <th>流水号</th>
          <th>金额</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="record in historyList" :key="record.id">
          <td>{{ record.time }}</td>
          <td>{{ record.flowNo }}</td>
          <td>¥{{ record.amount }}</td>
        </tr>
      </tbody>
    </table>

    <div v-if="historyList.length === 0" class="empty-tip">暂无消费记录</div>

    <div class="form-buttons">
      <button class="btn-cancel" @click="showHistory = false">关闭</button>
    </div>
  </div>
</div>
</template>
<script setup>
import { ref } from 'vue'

const keyword = ref('')
const list = ref([])
// 控制注册弹窗
const showRegister = ref(false)
// 注册表单数据
const registerForm = ref({
  name: '',
  phone: '',
  gender: '男',
  idCard: '',
  birthday: ''
})
// 消费记录弹窗相关
const showHistory = ref(false)
// 当前查看的会员姓名
const historyMember = ref('')
// 消费记录列表
const historyList = ref([])
// 累计消费金额
const historyTotal = ref('0')

/** 拉取会员列表（当前假数据） */
function fetchList() {
  list.value = [
    { id: 1, cardNo: 'M20240001', name: '张三', phone: '13800001111', totalSpent: 2560.00, validUntil: '2027-06-15' },
    { id: 2, cardNo: 'M20240002', name: '李四', phone: '13800002222', totalSpent: 890.50, validUntil: '2026-01-20' },
    { id: 3, cardNo: 'M20240003', name: '王五', phone: '13800003333', totalSpent: 5200.00, validUntil: '2027-12-01' }
  ]
}

/** 判断有效期是否已过期 */
function isExpired(dateStr) {
  return new Date(dateStr) < new Date()
}

/** 打开注册弹窗（清空表单） */
function openRegister() {
  registerForm.value = { name: '', phone: '', gender: '男', idCard: '', birthday: '' }
  showRegister.value = true
}

/** 提交注册 */
function handleRegister() {
  if (!registerForm.value.name || !registerForm.value.phone) {
    alert('姓名和手机号不能为空')
    return
  }
  // 生成会员卡号（当前本地生成，后续后端生成）
  const cardNo = 'M' + Date.now().toString().slice(-8)
  // 有效期一年
  const today = new Date()
  const validUntil = new Date(today.setFullYear(today.getFullYear() + 1)).toISOString().split('T')[0]
  // 加入列表
  list.value.push({
    id: Date.now(),
    cardNo: cardNo,
    name: registerForm.value.name,
    phone: registerForm.value.phone,
    totalSpent: 0,
    validUntil: validUntil
  })
  showRegister.value = false
  alert('注册成功！会员卡号：' + cardNo)
}

/** 打开消费记录弹窗 */
function openHistory(item) {
  historyMember.value = item.name
  showHistory.value = true
  // 当前用假数据，后续对接后端 API
  historyList.value = [
    { id: 1, time: '2026-06-20 14:30', flowNo: 'LS20240620001', amount: 156.50 },
    { id: 2, time: '2026-06-15 09:12', flowNo: 'LS20240615002', amount: 89.00 },
    { id: 3, time: '2026-06-10 18:45', flowNo: 'LS20240610003', amount: 320.00 }
  ]
  // 累计 = 列表金额加总
  historyTotal.value = historyList.value.reduce((sum, r) => sum + r.amount, 0).toFixed(2)
}

/** 续期一年 */
function handleRenew(id) {
  if (confirm('确认为该会员续期一年？')) {
    const member = list.value.find(m => m.id === id)
    if (member) {
      // 有效期延长一年
      const d = new Date(member.validUntil)
      d.setFullYear(d.getFullYear() + 1)
      member.validUntil = d.toISOString().split('T')[0]
    }
  }
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
/* 过期标红 */
.expired { color: #e74c3c; }
.empty-tip { text-align: center; padding: 60px; color: #999; font-size: 15px; }
.history-total { margin-bottom: 16px; font-size: 15px; color: #333; }
</style>
