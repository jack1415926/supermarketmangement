import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
// 后端不可达时自动启用 Mock 数据，后端启动后自动切换为真实 API
import { installMock } from './api/mock'

const app = createApp(App)

app.use(createPinia())
app.use(router)

// 启用 Mock 拦截（仅在 API 请求失败时生效，不影响正常后端联调）
installMock()

app.mount('#app')
