<template>
  <header class="page-header">
    <div>
      <h2>{{ title }}</h2>
      <p v-if="description" class="page-copy">{{ description }}</p>
    </div>

    <div v-if="$slots.actions" class="page-header-actions">
      <slot name="actions" />
    </div>
  </header>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  }
})
</script>
