<template>
  <main v-if="isAuthPage" class="auth-shell">
    <RouterView />
  </main>

  <main v-else class="app-shell">
    <aside class="sidebar">
      <div>
        <h1>ASTIS</h1>
        <p class="sidebar-subtitle">Study planning workspace</p>
      </div>

      <nav>
        <RouterLink to="/">Dashboard</RouterLink>
        <RouterLink to="/tasks">Tasks</RouterLink>
        <RouterLink to="/recommendations">Recommendations</RouterLink>
      </nav>

      <div class="sidebar-footer">
        <p>{{ currentUser?.username || 'Student' }}</p>
        <button type="button" class="ghost-button" @click="handleLogout">Logout</button>
      </div>
    </aside>

    <section class="content">
      <RouterView />
    </section>
  </main>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCurrentUser, logout } from './services/authService'

const route = useRoute()
const router = useRouter()

const isAuthPage = computed(() => route.meta.layout === 'auth')
const currentUser = computed(() => {
  route.fullPath
  return getCurrentUser()
})

function handleLogout() {
  logout()
  router.push({ name: 'login' })
}
</script>
