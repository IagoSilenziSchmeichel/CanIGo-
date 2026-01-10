<script setup>
import { ref } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'

const route = useRoute()
const router = useRouter()

const baseUrl = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '')

const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

async function doLogin() {
  error.value = ''
  loading.value = true

  try {
    const res = await fetch(`${baseUrl}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: email.value.trim(),
        password: password.value
      })
    })

    if (!res.ok) {
      const text = await res.text().catch(() => '')
      throw new Error(`Login fehlgeschlagen: HTTP ${res.status}${text ? ` – ${text}` : ''}`)
    }

    const data = await res.json()
    if (!data?.token) throw new Error('Kein Token erhalten')

    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify({
      userId: data.userId,
      email: data.email,
      name: data.name
    }))

    const from = route.query.from || '/'
    router.replace(from)
  } catch (e) {
    error.value = String(e?.message || e)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="card">
      <h1>Login</h1>

      <p v-if="error" class="error">{{ error }}</p>

      <label class="field">
        <span>Email</span>
        <input v-model="email" type="email" autocomplete="email" />
      </label>

      <label class="field">
        <span>Passwort</span>
        <input v-model="password" type="password" autocomplete="current-password" />
      </label>

      <button class="btn btn-primary" @click="doLogin" :disabled="loading">
        {{ loading ? 'Logging in…' : 'Login' }}
      </button>

      <p class="hint">
        Noch kein Account?
        <RouterLink class="link" to="/register">Jetzt registrieren</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
/* dein Style bleibt gleich */
.link {
  color: rgba(230,242,255,.9);
  text-decoration: underline;
}
</style>
