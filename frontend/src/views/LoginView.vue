<template>
  <AuthFrame
    kicker="Welcome back"
    title="Log in"
    description="Continue to your tasks, recommendations, and study plan."
  >
    <form class="auth-form" @submit.prevent="handleSubmit">
      <label>
        Email
        <input v-model.trim="form.email" type="email" autocomplete="email" required />
      </label>

      <label>
        Password
        <input v-model="form.password" type="password" autocomplete="current-password" required />
      </label>

      <p v-if="errorMessage" class="form-error" role="alert">{{ errorMessage }}</p>

      <button type="submit" class="primary-button auth-submit" :disabled="isSubmitting">
        {{ isSubmitting ? 'Logging in...' : 'Login' }}
      </button>
    </form>

    <template #footer>
      New to ASTIS?
      <RouterLink to="/register">Create an account</RouterLink>
    </template>
  </AuthFrame>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AuthFrame from '../components/layout/AuthFrame.vue'
import { getApiErrorMessage } from '../services/apiClient'
import { login } from '../services/authService'

const route = useRoute()
const router = useRouter()
const isSubmitting = ref(false)
const errorMessage = ref('')

const form = reactive({
  email: '',
  password: ''
})

async function handleSubmit() {
  errorMessage.value = ''
  isSubmitting.value = true

  try {
    await login({
      email: form.email,
      password: form.password
    })

    router.push(route.query.redirect || { name: 'dashboard' })
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error)
  } finally {
    isSubmitting.value = false
  }
}
</script>
