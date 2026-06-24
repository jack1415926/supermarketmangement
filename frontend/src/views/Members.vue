<!-- 会员管理页面：会员列表、注册发卡、续期 -->
<template>
  <div class="page-container">
    <div class="page-toolbar">
      <input v-model="keyword" placeholder="搜索会员姓名或手机号..." @keyup.enter="fetchList" />
      <button @click="fetchList">搜索</button>
      <button class="btn-add" @click="openRegister">+ 注册会员</button>
    </div>

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
          <td>&yen;{{ Number(item.totalSpent || 0).toFixed(2) }}</td>
          <td><span :class="{ expired: isExpired(item.validUntil) }">{{ item.validUntil }}</span></td>
          <td>
            <button @click="openHistory(item)">消费记录</button>
            <button @click="handleRenew(item.id)">续期</button>
          </td>
        </tr>
      </tbody>
    </table>
    <div v-if="list.length === 0" class="empty-tip">暂无会员数据</div>

    <!-- 注册弹窗 -->
    <div v-if="showRegister" class="modal-overlay" @click.self="showRegister = false">
      <div class="modal-card">
        <h3>注册新会员</h3>
        <div class="form-row"><label>姓名</label><input v-model="form.name" placeholder="请输入姓名" /></div>
        <div class="form-row form-row-2col">
          <div><label>手机号</label><input v-model="form.phone" placeholder="11位手机号" /></div>
          <div><label>性别</label><select v-model="form.gender"><option value="男">男</option><option value="女">女</option></select></div>
        </div>
        <div class="form-row"><label>身份证号（选填）</label><input v-model="form.idCard" placeholder="选填" /></div>
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
        <p class="history-total">累计消费：<strong>&yen;{{ historyTotal }}</strong></p>
        <table class="data-table">
          <thead><tr><th>时间</th><th>流水号</th><th>金额</th></tr></thead>
          <tbody>
            <tr v-for="r in historyList" :key="r.id"><td>{{ r.time }}</td><td>{{ r.flowNo }}</td><td>&yen;{{ r.amount }}</td></tr>
          </tbody>
        </table>
        <div v-if="historyList.length === 0" class="empty-tip">暂无消费记录</div>
        <div class="form-buttons"><button class="btn-cancel" @click="showHistory = false">关闭</button></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { memberAPI } from '@/api'

const keyword = ref('')
const list = ref([])
const showRegister = ref(false)
const form = ref({ name: '', phone: '', gender: '男', idCard: '' })
const showHistory = ref(false)
const historyMember = ref('')
const historyList = ref([])
const historyTotal = ref('0')

function isExpired(s) { return s ? new Date(s) < new Date() : false }

async function fetchList() {
  try {
    const res = await memberAPI.list({ keyword: keyword.value })
    const raw = res.data?.content || res.data?.data || res.data || []
    list.value = raw.map(function(m) {
      var v = (m.validUntil || '').toString().substring(0, 10)
      return { id: m.id, cardNo: m.cardNo || '', name: m.name || '', phone: m.phone || '', totalSpent: m.totalSpent || 0, validUntil: v }
    })
  } catch (e) { console.error('获取会员列表失败', e) }
}

function openRegister() {
  form.value = { name: '', phone: '', gender: '男', idCard: '' }
  showRegister.value = true
}

async function handleRegister() {
  if (!form.value.name) { alert('请输入姓名'); return }
  if (!/^1[3-9]\d{9}$/.test(form.value.phone)) { alert('请输入正确的11位手机号'); return }
  try {
    var data = { name: form.value.name, phone: form.value.phone, gender: form.value.gender }
    if (form.value.idCard) data.idCard = form.value.idCard
    await memberAPI.create(data)
    showRegister.value = false
    await fetchList()
    alert('注册成功！')
  } catch (e) {
    var msg = (e.response && e.response.data && e.response.data.message) || e.message
    alert('注册失败：' + msg)
  }
}

function openHistory(item) {
  historyMember.value = item.name
  historyList.value = [
    { id: 1, time: '2026-06-20 14:30', flowNo: 'LS20240620001', amount: 156.50 },
    { id: 2, time: '2026-06-15 09:12', flowNo: 'LS20240615002', amount: 89.00 }
  ]
  historyTotal.value = historyList.value.reduce(function(s, r) { return s + r.amount }, 0).toFixed(2)
  showHistory.value = true
}

async function handleRenew(id) {
  if (!confirm('确认为该会员续期一年？')) return
  try {
    await memberAPI.renew(id)
    await fetchList()
    alert('续期成功！')
  } catch (e) {
    var msg = (e.response && e.response.data && e.response.data.message) || e.message
    alert('续期失败：' + msg)
  }
}

onMounted(function() { fetchList() })
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
.expired { color: #e74c3c; }
.empty-tip { text-align: center; padding: 60px; color: #999; font-size: 15px; }
.history-total { margin-bottom: 16px; font-size: 15px; color: #333; }
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
