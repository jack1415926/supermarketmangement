/**
 * API 接口层
 * 按业务模块组织，所有接口对应后端 RESTful API
 * 统一响应格式：{ code: 200, message: "success", data: {} }
 */
import request from './request'

// ==================== 认证模块 ====================
export const authAPI = {
  login: (data) => request.post('/auth/login', data),       // 登录
  logout: () => request.post('/auth/logout'),                // 登出
  getMe: () => request.get('/auth/me')                       // 获取当前用户信息
}

// ==================== 商品模块 ====================
export const productAPI = {
  list: (params) => request.get('/products', { params }),                    // 商品列表（分页+筛选）
  getById: (id) => request.get(`/products/${id}`),                           // 商品详情
  create: (data) => request.post('/products', data),                         // 新增商品
  update: (id, data) => request.put(`/products/${id}`, data),                // 编辑商品
  remove: (id) => request.delete(`/products/${id}`),                         // 删除/下架商品
  search: (keyword) => request.get('/products/search', { params: { keyword } }) // 模糊搜索
}

// ==================== 分类模块 ====================
export const categoryAPI = {
  list: () => request.get('/categories')  // 获取分类树
}

// ==================== 销售模块 ====================
export const saleAPI = {
  settle: (data) => request.post('/sales', data),            // POS 结算（创建销售单）
  list: (params) => request.get('/sales', { params }),       // 销售记录列表
  getById: (id) => request.get(`/sales/${id}`),              // 销售单详情
  dailySummary: () => request.get('/sales/daily-summary')    // 当日交班汇总
}

// ==================== 会员模块 ====================
export const memberAPI = {
  list: (params) => request.get('/members', { params }),               // 会员列表
  getById: (id) => request.get(`/members/${id}`),                      // 会员详情
  create: (data) => request.post('/members', data),                    // 注册发卡
  renew: (id) => request.put(`/members/${id}/renew`),                  // 会员续期
  verify: (cardNo) => request.get(`/members/verify/${cardNo}`)         // 刷卡验证
}

// ==================== 进货模块 ====================
export const purchaseAPI = {
  list: (params) => request.get('/purchases', { params }),  // 进货记录列表
  create: (data) => request.post('/purchases', data),       // 创建进货单（入库）
  plan: () => request.get('/purchases/plan')                // 进货建议计划
}

// ==================== 库存模块 ====================
export const inventoryAPI = {
  list: (params) => request.get('/inventory', { params }),                      // 库存列表
  setThreshold: (productId, data) => request.put(`/inventory/${productId}/threshold`, data), // 设置库存预警阈值
  check: (data) => request.post('/inventory/check', data)                       // 库存盘点
}

// ==================== 报表模块 ====================
export const reportAPI = {
  salesRanking: (params) => request.get('/reports/sales-ranking', { params }),      // 销售排行榜
  salesDaily: (params) => request.get('/reports/sales-daily', { params }),          // 销售日报
  salesMonthly: (params) => request.get('/reports/sales-monthly', { params }),      // 销售月报
  cashierPerformance: (params) => request.get('/reports/cashier-performance', { params }) // 收银员业绩统计
}

// ==================== 人员模块 ====================
export const employeeAPI = {
  list: (params) => request.get('/employees', { params }),            // 员工列表
  create: (data) => request.post('/employees', data),                // 新增员工
  update: (id, data) => request.put(`/employees/${id}`, data)        // 编辑员工
}

export const supplierAPI = {
  list: (params) => request.get('/suppliers', { params }),            // 供应商列表
  create: (data) => request.post('/suppliers', data),                // 新增供应商
  update: (id, data) => request.put(`/suppliers/${id}`, data)        // 编辑供应商
}
