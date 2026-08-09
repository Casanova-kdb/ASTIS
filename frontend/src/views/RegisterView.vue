<template>
  <AuthFrame
    kicker="New workspace"
    title="Create an account"
    description="Set up your account before adding academic tasks and study preferences."
  >
    <form class="auth-form" @submit.prevent="handleSubmit">
      <label>
        Username
        <input v-model.trim="form.username" type="text" autocomplete="username" required />
      </label>

      <label>
        Email
        <input v-model.trim="form.email" type="email" autocomplete="email" required />
      </label>

      <label>
        Password
        <input
          v-model="form.password"
          type="password"
          autocomplete="new-password"
          minlength="8"
          required
        />
      </label>

      <p v-if="errorMessage" class="form-error" role="alert">{{ errorMessage }}</p>

      <button type="submit" class="primary-button auth-submit" :disabled="isSubmitting">
        {{ isSubmitting ? 'Creating account...' : 'Register' }}
      </button>
    </form>

    <template #footer>
      Already have an account?
      <RouterLink to="/login">Login</RouterLink>
    </template>
  </AuthFrame>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AuthFrame from '../components/layout/AuthFrame.vue'
import { getApiErrorMessage } from '../services/apiClient'
import { register } from '../services/authService'

const router = useRouter()
const isSubmitting = ref(false)
const errorMessage = ref('')

const form = reactive({
  username: '',
  email: '',
  password: ''
})

async function handleSubmit() {
  errorMessage.value = ''
  isSubmitting.value = true

  try {
    await register({
      username: form.username,
      email: form.email,
      password: form.password
    })

    router.push({ name: 'dashboard' })
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error)
  } finally {
    isSubmitting.value = false
  }
}
</script>
