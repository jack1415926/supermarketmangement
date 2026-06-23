<!--
  POS 收银台页面
  功能：商品搜索扫码录入、购物车管理、金额计算、会员折扣、收款结算
-->
<template>
  <div class="pos-layout">
    <!-- 左侧：商品搜索与录入 -->
    <div class="pos-left">
      <!-- 会员验证 -->
      <div class="member-bar">
        <input v-model="memberCard" placeholder="输入会员卡号享受折扣" @keyup.enter="verifyMember" />
        <button @click="verifyMember">验证</button>
      </div>
      <div v-if="memberInfo" class="member-info">
        ✅ 会员：{{ memberInfo.name }}
        <span class="discount-tag">{{ discountRate * 100 }}折</span>
      </div>

      <!-- 搜索框 -->
      <div class="search-bar">
        <input v-model="keyword" placeholder="输入商品名或扫码..." @keyup.enter="searchProduct" />
        <button @click="searchProduct">搜索</button>
      </div>

      <!-- 搜索结果列表 -->
      <div class="search-results">
        <div
          v-for="item in results"
          :key="item.id"
          class="search-item"
          @click="addToCart(item)"
        >
          <span>{{ item.name }}</span>
          <span>¥{{ item.price }}</span>
        </div>
      </div>
    </div>

    <!-- 右侧：购物车列表 + 结算栏 -->
    <div class="pos-right">
      <div class="cart-area">
        <!-- 表头 -->
        <div class="cart-header">
          <span class="col-name">商品名称</span>
          <span class="col-qty">数量</span>
          <span class="col-price">单价</span>
          <span class="col-subtotal">小计</span>
          <span class="col-action">操作</span>
        </div>

        <!-- 购物车为空 -->
        <div v-if="cart.length === 0" class="cart-empty">
          购物车为空，请扫描或搜索商品
        </div>

        <!-- 购物车商品列表 -->
        <div v-for="item in cart" :key="item.id" class="cart-item">
          <span class="col-name">{{ item.name }}</span>
          <span class="col-qty">
            <button @click="decreaseQty(item)">-</button>
            {{ item.quantity }}
            <button @click="increaseQty(item)">+</button>
          </span>
          <span class="col-price">¥{{ item.price }}</span>
          <span class="col-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          <span class="col-action">
            <button @click="removeFromCart(item)">删除</button>
          </span>
        </div>
      </div>

      <div class="summary-area">
        <!-- 数量与金额汇总 -->
        <div class="summary-row">
          <span>共 <strong>{{ totalCount }}</strong> 件商品</span>
          <span>合计：<strong class="total-amount">¥{{ totalAmount }}</strong></span>
        </div>
        <!-- 收款按钮 -->
        <button class="checkout-btn" @click="checkout">收 款 (F8)</button>
      </div>
    </div>

    <!-- 收款弹窗 -->
    <div v-if="showCheckout" class="checkout-modal">
      <div class="checkout-dialog">
        <h3>收款结算</h3>
        <div class="checkout-row">
          <span>应收金额</span>
          <span class="amount-red">¥{{ totalAmount }}</span>
        </div>
        <div class="checkout-row">
          <span>实收金额</span>
          <input v-model="receivedAmount" type="number" placeholder="请输入客户付款金额" @keyup.enter="confirmCheckout" />
        </div>
        <div v-if="receivedAmount > 0" class="checkout-row">
          <span>找零</span>
          <span class="amount-green">¥{{ changeAmount }}</span>
        </div>
        <div class="checkout-buttons">
          <button class="btn-cancel" @click="showCheckout = false">取消</button>
          <button class="btn-confirm" @click="confirmCheckout">确认收款</button>
        </div>
      </div>
    </div>
  </div>
  <!-- 小票预览（收款成功后显示） -->
<div v-if="showReceipt" class="checkout-modal">
  <div class="receipt-paper">
    <h3>超市管理系统</h3>
    <p class="receipt-subtitle">=== 购物小票 ===</p>

    <!-- 商品明细 -->
    <div class="receipt-items">
      <div v-for="item in receiptData.items" :key="item.id" class="receipt-line">
        <span>{{ item.name }}</span>
        <span>x{{ item.quantity }}</span>
        <span>¥{{ (item.price * item.quantity).toFixed(2) }}</span>
      </div>
    </div>

    <div class="receipt-divider">----------------------</div>

    <!-- 汇总信息 -->
    <div class="receipt-line">
      <span>合计</span>
      <span>¥{{ receiptData.total }}</span>
    </div>
    <div v-if="receiptData.discount < 1" class="receipt-line">
      <span>会员折扣</span>
      <span>-¥{{ receiptData.discountAmount }}</span>
    </div>
    <div class="receipt-line receipt-bold">
      <span>实收</span>
      <span>¥{{ receiptData.received }}</span>
    </div>
    <div class="receipt-line receipt-bold">
      <span>找零</span>
      <span>¥{{ receiptData.change }}</span>
    </div>

    <div class="receipt-divider">----------------------</div>
    <p class="receipt-time">{{ receiptData.time }}</p>
    <p class="receipt-thanks">谢谢惠顾！</p>

    <button class="btn-confirm" style="margin-top:16px;width:100%;" @click="showReceipt = false">关闭</button>
  </div>
</div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { productAPI, memberAPI, saleAPI } from '@/api'

// 搜索框输入的关键字
const keyword = ref('')
// 搜索结果列表
const results = ref([])
// 购物车数组，每项包含 id、name、price、quantity
const cart = ref([])

// 会员卡号输入框
const memberCard = ref('')
// 当前验证通过的会员信息（null 表示未验证）
const memberInfo = ref(null)
// 控制收款弹窗显示
const showCheckout = ref(false)
// 客户实付金额
const receivedAmount = ref(0)

// 折扣率（默认无折扣，验证会员后 95 折）
const discountRate = computed(() => {
  return memberInfo.value ? 0.95 : 1
})

// 购物车总件数
const totalCount = computed(() => {
  return cart.value.reduce((sum, item) => sum + item.quantity, 0)
})

// 购物车合计金额（已考虑折扣）
const totalAmount = computed(() => {
  const raw = cart.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  return (raw * discountRate.value).toFixed(2)
})

// 找零金额
const changeAmount = computed(() => {
  const change = receivedAmount.value - totalAmount.value
  return change > 0 ? change.toFixed(2) : '0.00'
})

// 搜索商品（当前用假数据，后续对接后端 API）
/** 搜索商品（对接后端真实 API） */
async function searchProduct() {
  if (!keyword.value) return
  try {
    const res = await productAPI.search(keyword.value)
    results.value = (res.data || []).map(item => ({
      id: item.id,
      name: item.name,
      price: item.salePrice || item.retailPrice || item.price
    }))
  } catch (err) {
    console.error('搜索失败', err)
  }
}

// 将商品加入购物车
function addToCart(product) {
  const item = cart.value.find(c => c.id === product.id)
  if (item) {
    item.quantity++
  } else {
    cart.value.push({ ...product, quantity: 1 })
  }
}

// 数量 +1
function increaseQty(item) {
  item.quantity++
}

// 数量 -1，减到 0 自动移除
function decreaseQty(item) {
  item.quantity--
  if (item.quantity <= 0) {
    cart.value = cart.value.filter(c => c.id !== item.id)
  }
}

// 从购物车删除
function removeFromCart(item) {
  cart.value = cart.value.filter(c => c.id !== item.id)
}

// 验证会员卡号（当前用假数据）
/** 验证会员卡号（对接后端真实 API） */
async function verifyMember() {
  if (!memberCard.value) return
  try {
    const res = await memberAPI.verify(memberCard.value)
    memberInfo.value = { name: res.data.name, cardNo: res.data.cardNo }
    alert('会员验证成功')
  } catch (err) {
    console.error('会员验证失败', err)
    alert('会员卡号无效')
  }
}

// 打开收款弹窗
function checkout() {
  if (cart.value.length === 0) {
    alert('购物车为空，请先添加商品')
    return
  }
  receivedAmount.value = 0
  showCheckout.value = true
}

// 控制小票弹窗
const showReceipt = ref(false)
// 小票数据
const receiptData = ref({ items: [], total: '0', discount: 1, discountAmount: '0', received: '0', change: '0', time: '' })

/** 确认收款，生成小票数据并展示 */
/** 确认收款，调用后端结算 API */
async function confirmCheckout() {
  if (receivedAmount.value < totalAmount.value) {
    alert('实收金额不足')
    return
  }
  try {
    // 构造结算请求：商品明细 + 会员折扣 + 实收金额
    const saleData = {
      items: cart.value.map(item => ({
        productId: item.id,
        quantity: item.quantity
      })),
      receivedAmount: Number(receivedAmount.value),
      memberId: memberInfo.value?.id || null
    }
    const res = await saleAPI.settle(saleData)

    // 快照购物车到小票
    const rawTotal = cart.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
    receiptData.value = {
      items: [...cart.value],
      total: totalAmount.value,
      discount: discountRate.value,
      discountAmount: (rawTotal - Number(totalAmount.value)).toFixed(2),
      received: Number(receivedAmount.value).toFixed(2),
      change: changeAmount.value,
      time: new Date().toLocaleString()
    }
    showCheckout.value = false
    showReceipt.value = true
    cart.value = []
    memberInfo.value = null
    memberCard.value = ''
  } catch (err) {
    console.error('结算失败', err)
    alert('结算失败：' + (err.response?.data?.message || err.message))
  }
}
</script>

<style scoped>
.pos-layout { display: flex; height: 100%; gap: 20px; }
.pos-left { width: 400px; background: #fff; border-radius: 8px; padding: 20px; overflow-y: auto; }
.pos-right { flex: 1; display: flex; flex-direction: column; gap: 20px; }
.cart-area { flex: 1; background: #fff; border-radius: 8px; padding: 20px; overflow-y: auto; }
.summary-area { background: #fff; border-radius: 8px; padding: 20px; }

/* 会员栏 */
.member-bar { display: flex; gap: 10px; margin-bottom: 16px; }
.member-bar input { flex: 1; padding: 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
.member-bar button { padding: 10px 16px; background: #27ae60; color: #fff; border: none; border-radius: 6px; cursor: pointer; }
.member-info { padding: 10px; background: #eafaf1; border-radius: 6px; margin-bottom: 16px; font-size: 14px; }
.discount-tag { margin-left: 10px; padding: 2px 8px; background: #27ae60; color: #fff; border-radius: 4px; font-size: 12px; }

/* 搜索栏 */
.search-bar { display: flex; gap: 10px; margin-bottom: 16px; }
.search-bar input { flex: 1; padding: 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
.search-bar button { padding: 10px 20px; background: #667eea; color: #fff; border: none; border-radius: 6px; cursor: pointer; }

/* 搜索结果 */
.search-item { display: flex; justify-content: space-between; padding: 12px; border-bottom: 1px solid #f0f0f0; cursor: pointer; }
.search-item:hover { background: #f5f6fa; }

/* 购物车 */
.cart-header { display: flex; padding: 8px 0; border-bottom: 2px solid #e8e8e8; font-weight: bold; color: #666; font-size: 13px; }
.cart-empty { text-align: center; padding: 40px; color: #999; }
.cart-item { display: flex; align-items: center; padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.col-name { flex: 2; }
.col-qty { flex: 1; text-align: center; }
.col-price { flex: 1; text-align: center; }
.col-subtotal { flex: 1; text-align: center; color: #e74c3c; font-weight: bold; }
.col-action { flex: 0.8; text-align: center; }
.col-qty button { width: 24px; height: 24px; border: 1px solid #ddd; border-radius: 4px; background: #fff; cursor: pointer; }
.col-action button { padding: 4px 10px; border: 1px solid #ff4d4f; border-radius: 4px; color: #ff4d4f; background: #fff; cursor: pointer; font-size: 12px; }

/* 结算区 */
.summary-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; font-size: 16px; }
.total-amount { color: #e74c3c; font-size: 24px; }
.checkout-btn { width: 100%; padding: 14px; background: #e74c3c; color: #fff; border: none; border-radius: 8px; font-size: 18px; cursor: pointer; }
.checkout-btn:hover { background: #c0392b; }

/* 收款弹窗 */
.checkout-modal { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.4); display: flex; justify-content: center; align-items: center; z-index: 100; }
.checkout-dialog { background: #fff; border-radius: 12px; padding: 30px; width: 420px; box-shadow: 0 10px 40px rgba(0,0,0,0.2); }
.checkout-dialog h3 { margin-bottom: 24px; text-align: center; }
.checkout-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; font-size: 16px; }
.checkout-row input { width: 180px; padding: 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 16px; text-align: right; }
.amount-red { color: #e74c3c; font-size: 24px; font-weight: bold; }
.amount-green { color: #27ae60; font-size: 24px; font-weight: bold; }
.checkout-buttons { display: flex; gap: 12px; margin-top: 24px; }
.btn-cancel { flex: 1; padding: 12px; border: 1px solid #ddd; border-radius: 8px; background: #fff; cursor: pointer; }
.btn-confirm { flex: 1; padding: 12px; border: none; border-radius: 8px; background: #27ae60; color: #fff; cursor: pointer; font-size: 16px; }
/* 小票纸张样式 */
.receipt-paper { background: #fffbe6; border-radius: 8px; padding: 24px; width: 360px; box-shadow: 0 10px 40px rgba(0,0,0,0.2); font-family: 'Courier New', monospace; }
.receipt-paper h3 { text-align: center; margin-bottom: 4px; }
.receipt-subtitle { text-align: center; font-size: 12px; color: #666; margin-bottom: 16px; }
.receipt-items { margin-bottom: 8px; }
.receipt-line { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 4px; }
.receipt-divider { text-align: center; color: #ccc; font-size: 12px; margin: 8px 0; }
.receipt-bold { font-weight: bold; font-size: 15px; }
.receipt-time { text-align: center; font-size: 11px; color: #999; margin-top: 4px; }
.receipt-thanks { text-align: center; font-size: 13px; margin-top: 8px; }
</style>
