/**
 * Mock 数据模块
 * 当后端服务不可用时，拦截请求返回模拟数据，方便前端独立开发和演示
 * 后端启动后自动走真实 API，无需修改任何代码
 */
import request from './request'

// ==================== 模拟数据 ====================

/** 商品列表 */
const mockProducts = [
  { id: 1, name: '康师傅红烧牛肉面', category: { id: 1, name: '方便食品' }, barcode: '6901234567890', salePrice: 4.50, purchasePrice: 3.20, stock: 150 },
  { id: 2, name: '海飞丝去屑洗发水 400ml', category: { id: 2, name: '个人护理' }, barcode: '6902234567891', salePrice: 49.90, purchasePrice: 32.00, stock: 45 },
  { id: 3, name: '中华牙膏 120g', category: { id: 2, name: '个人护理' }, barcode: '6903234567892', salePrice: 12.80, purchasePrice: 8.50, stock: 80 },
  { id: 4, name: '农夫山泉 550ml', category: { id: 3, name: '饮料' }, barcode: '6904234567893', salePrice: 2.00, purchasePrice: 1.20, stock: 300 },
  { id: 5, name: '奥利奥原味饼干 97g', category: { id: 4, name: '零食' }, barcode: '6905234567894', salePrice: 9.90, purchasePrice: 6.50, stock: 60 },
  { id: 6, name: '蒙牛纯牛奶 250ml', category: { id: 5, name: '乳制品' }, barcode: '6906234567895', salePrice: 3.50, purchasePrice: 2.40, stock: 200 },
  { id: 7, name: '青岛啤酒 330ml', category: { id: 3, name: '饮料' }, barcode: '6907234567896', salePrice: 5.00, purchasePrice: 3.30, stock: 120 },
  { id: 8, name: '金龙鱼调和油 5L', category: { id: 6, name: '粮油调味' }, barcode: '6908234567897', salePrice: 69.90, purchasePrice: 52.00, stock: 35 },
  { id: 9, name: '心相印抽纸 3包装', category: { id: 7, name: '日用品' }, barcode: '6909234567898', salePrice: 15.90, purchasePrice: 10.50, stock: 90 },
  { id: 10, name: '旺旺大礼包 500g', category: { id: 4, name: '零食' }, barcode: '6910234567899', salePrice: 29.90, purchasePrice: 19.00, stock: 40 }
]

/** 分类列表 */
const mockCategories = [
  { id: 1, name: '方便食品' }, { id: 2, name: '个人护理' }, { id: 3, name: '饮料' },
  { id: 4, name: '零食' }, { id: 5, name: '乳制品' }, { id: 6, name: '粮油调味' }, { id: 7, name: '日用品' }
]

/** 会员列表 */
const mockMembers = [
  { id: 1, cardNo: 'M20240001', name: '张三', phone: '13800001111', totalSpent: 3580.50, validUntil: '2027-06-20' },
  { id: 2, cardNo: 'M20240002', name: '李四', phone: '13800002222', totalSpent: 1200.00, validUntil: '2026-12-15' },
  { id: 3, cardNo: 'M20240003', name: '王五', phone: '13800003333', totalSpent: 8900.00, validUntil: '2027-03-01' }
]

/** 库存列表 */
const mockInventory = [
  { id: 1, name: '康师傅红烧牛肉面', currentStock: 150, stockMin: 50, stockMax: 300, purchasePrice: 3.20 },
  { id: 2, name: '海飞丝去屑洗发水', currentStock: 8, stockMin: 20, stockMax: 100, purchasePrice: 32.00 },
  { id: 3, name: '中华牙膏 120g', currentStock: 80, stockMin: 30, stockMax: 150, purchasePrice: 8.50 },
  { id: 4, name: '农夫山泉 550ml', currentStock: 45, stockMin: 50, stockMax: 500, purchasePrice: 1.20 },
  { id: 5, name: '奥利奥原味饼干', currentStock: 60, stockMin: 25, stockMax: 200, purchasePrice: 6.50 },
  { id: 6, name: '蒙牛纯牛奶', currentStock: 200, stockMin: 50, stockMax: 400, purchasePrice: 2.40 },
  { id: 7, name: '青岛啤酒', currentStock: 120, stockMin: 30, stockMax: 300, purchasePrice: 3.30 },
  { id: 8, name: '金龙鱼调和油', currentStock: 35, stockMin: 20, stockMax: 100, purchasePrice: 52.00 },
  { id: 9, name: '心相印抽纸', currentStock: 90, stockMin: 30, stockMax: 200, purchasePrice: 10.50 },
  { id: 10, name: '旺旺大礼包', currentStock: 40, stockMin: 15, stockMax: 100, purchasePrice: 19.00 }
]

/** 员工列表 */
const mockEmployees = [
  { id: 1, username: 'admin', displayName: '系统管理员', role: 'ROLE_ADMIN', phone: '13800000000' },
  { id: 2, username: 'manager', displayName: '张经理', role: 'ROLE_MANAGER', phone: '13811111111' },
  { id: 3, username: 'cashier', displayName: '李收银', role: 'ROLE_CASHIER', phone: '13822222222' }
]

/** 供应商列表 */
const mockSuppliers = [
  { id: 1, name: '统一食品批发', contact: '赵总', phone: '13900001111' },
  { id: 2, name: '日化用品供应商', contact: '钱经理', phone: '13900002222' },
  { id: 3, name: '饮料批发商', contact: '孙老板', phone: '13900003333' }
]

/** 进货记录 */
const mockPurchases = [
  { id: 1, purchaseNo: 'PO000001', supplier: '饮料批发商', itemCount: 4, totalAmount: 1240.00, purchaseDate: '2026-06-20' },
  { id: 2, purchaseNo: 'PO000002', supplier: '日用品批发市场', itemCount: 3, totalAmount: 850.00, purchaseDate: '2026-06-21' },
  { id: 3, purchaseNo: 'PO000003', supplier: '统一食品有限公司', itemCount: 5, totalAmount: 2100.00, purchaseDate: '2026-06-22' }
]

/** 销售记录 */
const mockSales = [
  { id: 1, flowNo: 'LS20260620001', totalAmount: 156.50, cashierName: '李收银', createdAt: '2026-06-20 14:30' },
  { id: 2, flowNo: 'LS20260620002', totalAmount: 89.00, cashierName: '李收银', createdAt: '2026-06-20 16:15' }
]

// ==================== Mock 拦截逻辑 ====================

/** 标记：true=后端已连通，不再使用 mock */
let backendOnline = false

/** 延迟模拟网络请求 */
function delay(ms = 300) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/** 包装成标准响应格式 { code: 200, message: 'success', data: ... } */
function ok(data, message = 'success') {
  return { code: 200, message, data }
}

/**
 * 给 request 实例添加 mock 响应拦截
 * 工作原理：在响应拦截器里，如果请求失败（后端不可达），返回匹配的 mock 数据
 */
export function installMock() {
  request.interceptors.response.use(
    // 成功响应：标记后端在线，正常返回
    (response) => {
      backendOnline = true
      return response
    },
    // 失败响应：后端不可达时，尝试返回 mock 数据
    async (error) => {
      // 如果后端曾经连通，说明是真错误，不 mock
      if (backendOnline) return Promise.reject(error)

      const config = error.config || {}
      const url = config.url || ''
      const method = (config.method || 'get').toLowerCase()

      await delay(200 + Math.random() * 300)

      // ========== 认证模块 ==========
      if (url === '/auth/login' && method === 'post') {
        const body = JSON.parse(config.data || '{}')
        const users = {
          admin:   { username: 'admin', displayName: '系统管理员', role: 'ROLE_ADMIN', password: 'admin123' },
          manager: { username: 'manager', displayName: '张经理', role: 'ROLE_MANAGER', password: 'manager123' },
          cashier: { username: 'cashier', displayName: '李收银', role: 'ROLE_CASHIER', password: 'cashier123' }
        }
        const u = users[body.username]
        if (u && body.password === u.password) {
          return { data: ok({ token: 'mock-token-' + Date.now(), ...u }) }
        }
        return Promise.reject({ response: { status: 401, data: { code: 401, message: '用户名或密码错误' } } })
      }
      if (url === '/auth/me' && method === 'get') return { data: ok({ username: 'admin', displayName: '系统管理员', role: 'ROLE_ADMIN' }) }

      // ========== 商品模块 ==========
      if (url === '/products' && method === 'get') return { data: ok(mockProducts) }
      if (url === '/products/search' && method === 'get') {
        const kw = (config.params?.keyword || '').toLowerCase()
        const filtered = kw ? mockProducts.filter(p => p.name.toLowerCase().includes(kw) || p.barcode.includes(kw)) : mockProducts
        return { data: ok(filtered) }
      }
      if (url.startsWith('/products/') && method === 'get') {
        const id = parseInt(url.split('/').pop())
        const p = mockProducts.find(p => p.id === id)
        return p ? { data: ok(p) } : Promise.reject({ response: { status: 404 } })
      }
      if (url === '/products' && method === 'post') {
        const body = JSON.parse(config.data || '{}')
        const newProduct = { id: Date.now(), ...body, stock: 0 }
        mockProducts.unshift(newProduct)
        return { data: ok(newProduct, '新增成功') }
      }
      if (url.startsWith('/products/') && method === 'put') return { data: ok(null, '更新成功') }
      if (url.startsWith('/products/') && method === 'delete') return { data: ok(null, '删除成功') }

      // ========== 分类 ==========
      if (url === '/categories' && method === 'get') return { data: ok(mockCategories) }

      // ========== 会员 ==========
      if (url === '/members' && method === 'get') return { data: ok(mockMembers) }
      if (url.startsWith('/members/verify/') && method === 'get') {
        const cardNo = url.split('/verify/').pop()
        const m = mockMembers.find(m => m.cardNo === cardNo)
        return m ? { data: ok(m) } : Promise.reject({ response: { status: 404, data: { message: '未找到该会员' } } })
      }
      if (url === '/members' && method === 'post') {
        const body = JSON.parse(config.data || '{}')
        const cardNo = 'M' + Date.now().toString().slice(-8)
        const today = new Date()
        const year = new Date(today.setFullYear(today.getFullYear() + 1))
        const newMember = { id: Date.now(), cardNo, ...body, totalSpent: 0, validUntil: year.toISOString().split('T')[0] }
        mockMembers.push(newMember)
        return { data: ok(newMember) }
      }

      // ========== 库存 ==========
      if (url === '/inventory' && method === 'get') return { data: ok(mockInventory) }
      if (url.startsWith('/inventory/') && url.includes('/threshold') && method === 'put') return { data: ok(null, '阈值更新成功') }
      if (url === '/inventory/check' && method === 'post') return { data: ok(null, '盘点提交成功') }

      // ========== 员工 ==========
      if (url === '/employees' && method === 'get') return { data: ok(mockEmployees) }
      if (url === '/employees' && method === 'post') return { data: ok(null, '新增成功') }
      if (url.startsWith('/employees/') && method === 'put') return { data: ok(null, '更新成功') }

      // ========== 供应商 ==========
      if (url === '/suppliers' && method === 'get') return { data: ok(mockSuppliers) }
      if (url === '/suppliers' && method === 'post') return { data: ok(null, '新增成功') }
      if (url.startsWith('/suppliers/') && method === 'put') return { data: ok(null, '更新成功') }

      // ========== 进货 ==========
      if (url === '/purchases' && method === 'get') return { data: ok(mockPurchases) }

      // ========== 销售 ==========
      if (url === '/sales' && method === 'get') return { data: ok(mockSales) }

      // ========== 报表 ==========
      if (url === '/reports/sales-daily' && method === 'get') {
        return { data: ok({ totalAmount: 2456.80, orderCount: 18 }) }
      }

      if (url === '/sales' && method === 'post') return { data: ok(null, '结算成功') }

      if (url === '/purchases' && method === 'post') return { data: ok(null, '进货单创建成功') }

      // 未匹配的接口，返回空的成功响应，避免页面报错
      console.warn('[Mock] 未匹配的接口:', method, url)
      return { data: ok(null) }
    }
  )
  console.log('[Mock] Mock 拦截器已启用，后端不可达时将返回模拟数据')
}
