import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { setUnauthorizedHandler } from './services/apiClient'
import './styles/main.css'

setUnauthorizedHandler(() => {
  if (router.currentRoute.value.name !== 'login') {
    router.push({
      name: 'login',
      query: {
        redirect: router.currentRoute.value.fullPath
      }
    })
  }
})

createApp(App)
  .use(router)
  .mount('#app')
