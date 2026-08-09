import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { setUnauthorizedHandler } from './services/apiClient'
import '@fontsource-variable/ibm-plex-sans'
import './styles/tokens.css'
import './styles/base.css'
import './styles/shell.css'
import './styles/main.css'
import './styles/core-pages.css'

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
