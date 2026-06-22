import request from './request'

// 认证
export const authAPI = {
  login: (data) => request.post('/auth/login', data),
  logout: () => request.post('/auth/logout'),
  getMe: () => request.get('/auth/me')
}

// 商品
export const productAPI = {
  list: (params) => request.get('/products', { params }),
  getById: (id) => request.get(`/products/${id}`),
  create: (data) => request.post('/products', data),
  update: (id, data) => request.put(`/products/${id}`, data),
  remove: (id) => request.delete(`/products/${id}`),
  search: (keyword) => request.get('/products/search', { params: { keyword } })
}

// 分类
export const categoryAPI = {
  list: () => request.get('/categories')
}

// 销售
export const saleAPI = {
  settle: (data) => request.post('/sales', data),
  list: (params) => request.get('/sales', { params }),
  getById: (id) => request.get(`/sales/${id}`),
  dailySummary: () => request.get('/sales/daily-summary')
}

// 会员
export const memberAPI = {
  list: (params) => request.get('/members', { params }),
  getById: (id) => request.get(`/members/${id}`),
  create: (data) => request.post('/members', data),
  renew: (id) => request.put(`/members/${id}/renew`),
  verify: (cardNo) => request.get(`/members/verify/${cardNo}`)
}

// 进货
export const purchaseAPI = {
  list: (params) => request.get('/purchases', { params }),
  create: (data) => request.post('/purchases', data),
  plan: () => request.get('/purchases/plan')
}

// 库存
export const inventoryAPI = {
  list: (params) => request.get('/inventory', { params }),
  setThreshold: (productId, data) => request.put(`/inventory/${productId}/threshold`, data),
  check: (data) => request.post('/inventory/check', data)
}

// 报表
export const reportAPI = {
  salesRanking: (params) => request.get('/reports/sales-ranking', { params }),
  salesDaily: (params) => request.get('/reports/sales-daily', { params }),
  salesMonthly: (params) => request.get('/reports/sales-monthly', { params }),
  cashierPerformance: (params) => request.get('/reports/cashier-performance', { params })
}

// 人员
export const employeeAPI = {
  list: (params) => request.get('/employees', { params }),
  create: (data) => request.post('/employees', data),
  update: (id, data) => request.put(`/employees/${id}`, data)
}

export const supplierAPI = {
  list: (params) => request.get('/suppliers', { params }),
  create: (data) => request.post('/suppliers', data),
  update: (id, data) => request.put(`/suppliers/${id}`, data)
}
