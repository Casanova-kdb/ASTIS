<template>
  <main v-if="isAuthPage" class="auth-shell">
    <RouterView />
  </main>

  <div v-else class="app-shell">
    <a class="skip-link" href="#main-content">Skip to main content</a>

    <AppSidebar
      :current-user="currentUser"
      :is-open="isNavigationOpen"
      @close="closeNavigation"
      @logout="handleLogout"
    />

    <div class="app-workspace">
      <header class="mobile-header">
        <div class="mobile-brand">
          <BookOpenCheck :size="19" :stroke-width="2" aria-hidden="true" />
          <strong>ASTIS</strong>
        </div>

        <button
          ref="navigationButton"
          type="button"
          class="icon-button"
          aria-label="Open navigation"
          :aria-expanded="isNavigationOpen"
          @click="openNavigation"
        >
          <Menu :size="21" aria-hidden="true" />
        </button>
      </header>

      <main id="main-content" class="content" tabindex="-1">
        <RouterView />
      </main>
    </div>

    <button
      v-if="isNavigationOpen"
      type="button"
      class="sidebar-scrim"
      aria-label="Close navigation"
      @click="closeNavigation"
    ></button>
  </div>
</template>

<script setup>
import { BookOpenCheck, Menu } from '@lucide/vue'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppSidebar from './components/layout/AppSidebar.vue'
import { getCurrentUser, logout } from './services/authService'

const route = useRoute()
const router = useRouter()
const isNavigationOpen = ref(false)
const navigationButton = ref(null)

const isAuthPage = computed(() => route.meta.layout === 'auth')
const currentUser = computed(() => {
  route.fullPath
  return getCurrentUser()
})

watch(() => route.fullPath, closeNavigation)

watch(isNavigationOpen, (isOpen) => {
  document.body.classList.toggle('navigation-open', isOpen)
})

onMounted(() => {
  document.addEventListener('keydown', handleGlobalKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleGlobalKeydown)
  document.body.classList.remove('navigation-open')
})

async function handleGlobalKeydown(event) {
  if (event.key === 'Escape' && isNavigationOpen.value) {
    closeNavigation()
    await nextTick()
    navigationButton.value?.focus()
  }
}

function openNavigation() {
  isNavigationOpen.value = true
}

function closeNavigation() {
  isNavigationOpen.value = false
}

function handleLogout() {
  closeNavigation()
  logout()
  router.push({ name: 'login' })
}
</script>
