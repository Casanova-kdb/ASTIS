<template>
  <section class="auth-frame">
    <header class="auth-brand">
      <span class="auth-brand-mark">
        <BookOpenCheck :size="22" :stroke-width="2" aria-hidden="true" />
      </span>
      <span>
        <strong>ASTIS</strong>
        <small>Study workspace</small>
      </span>
    </header>

    <div class="auth-intro">
      <p class="auth-kicker">{{ kicker }}</p>
      <h1>{{ title }}</h1>
      <p>{{ description }}</p>
    </div>

    <div class="auth-form-panel">
      <slot />
    </div>

    <p class="auth-switch">
      <slot name="footer" />
    </p>
  </section>
</template>

<script setup>
import { BookOpenCheck } from '@lucide/vue'

defineProps({
  kicker: {
    type: String,
    required: true
  },
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    required: true
  }
})
</script>
