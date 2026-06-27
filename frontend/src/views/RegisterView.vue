<template>
  <section class="auth-card">
    <p class="eyebrow">Start planning</p>
    <h2>Create your ASTIS account</h2>

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

      <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>

      <button type="submit" class="primary-button" :disabled="isSubmitting">
        {{ isSubmitting ? 'Creating account...' : 'Register' }}
      </button>
    </form>

    <p class="auth-switch">
      Already have an account?
      <RouterLink to="/login">Login</RouterLink>
    </p>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
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
