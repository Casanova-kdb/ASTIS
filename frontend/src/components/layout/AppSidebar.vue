<template>
  <aside :class="['sidebar', { 'sidebar-open': isOpen }]" aria-label="Application navigation">
    <div class="sidebar-brand">
      <span class="brand-mark" aria-hidden="true">
        <BookOpenCheck :size="20" :stroke-width="2" />
      </span>
      <div>
        <strong>ASTIS</strong>
        <span>Study workspace</span>
      </div>

      <button
        ref="closeButton"
        type="button"
        class="icon-button sidebar-close"
        aria-label="Close navigation"
        @click="$emit('close')"
      >
        <X :size="20" aria-hidden="true" />
      </button>
    </div>

    <div class="sidebar-navigation">
      <span class="navigation-label">Workspace</span>
      <nav>
        <RouterLink
          v-for="item in navigationItems"
          :key="item.to"
          :to="item.to"
          @click="$emit('close')"
        >
          <component :is="item.icon" :size="18" :stroke-width="1.9" aria-hidden="true" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>
    </div>

    <div class="sidebar-footer">
      <div class="account-summary">
        <span class="account-initial" aria-hidden="true">{{ accountInitial }}</span>
        <div>
          <strong>{{ accountName }}</strong>
          <span>{{ currentUser?.email || 'Student account' }}</span>
        </div>
      </div>

      <button type="button" class="sidebar-command" @click="$emit('logout')">
        <LogOut :size="17" :stroke-width="1.9" aria-hidden="true" />
        <span>Log out</span>
      </button>
    </div>
  </aside>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import {
  BookOpenCheck,
  CalendarRange,
  FileUp,
  LayoutDashboard,
  ListChecks,
  ListOrdered,
  LogOut,
  Settings,
  X
} from '@lucide/vue'

const props = defineProps({
  currentUser: {
    type: Object,
    default: null
  },
  isOpen: {
    type: Boolean,
    default: false
  }
})

defineEmits(['close', 'logout'])

const closeButton = ref(null)

const navigationItems = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/tasks', label: 'Tasks', icon: ListChecks },
  { to: '/handbook-import', label: 'Handbook import', icon: FileUp },
  { to: '/recommendations', label: 'Recommendations', icon: ListOrdered },
  { to: '/study-plan', label: 'Study plan', icon: CalendarRange },
  { to: '/settings', label: 'Settings', icon: Settings }
]

const accountName = computed(() => {
  return props.currentUser?.displayName || props.currentUser?.username || 'Student'
})

const accountInitial = computed(() => accountName.value.charAt(0).toUpperCase())

watch(() => props.isOpen, async (isOpen) => {
  if (isOpen) {
    await nextTick()
    closeButton.value?.focus()
  }
})
</script>
