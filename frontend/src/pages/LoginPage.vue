<script setup>
import { ref } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { login } from '../api' // ✅ nutzt api.js (speichert token automatisch)

const route = useRoute()
const router = useRouter()

const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

async function doLogin() {
  error.value = ''
  loading.value = true

  try {
    await login(email.value.trim(), password.value)

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

      <button class="btn btn-primary" type="button" @click="doLogin" :disabled="loading">
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
/* Wenn du schon global/card styles hast, kannst du das minimal lassen */
.login-page {
  max-width: 520px;
  margin: 0 auto;
  padding: 40px 18px;
}

.card {
  border-radius: 22px;
  border: 1px solid rgba(230, 242, 255, 0.14);
  background: rgba(10, 14, 28, 0.45);
  padding: 18px;
}

h1 {
  margin: 0 0 12px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
  color: rgba(230, 242, 255, 0.72);
  font-size: 13px;
}

input {
  padding: 12px;
  border-radius: 16px;
  border: 1px solid rgba(230, 242, 255, 0.16);
  background: rgba(230, 242, 255, 0.06);
  color: rgba(230, 242, 255, 0.92);
  outline: none;
}

input:focus {
  border-color: rgba(41, 243, 255, 0.3);
  box-shadow: 0 0 0 1px rgba(41, 243, 255, 0.18), 0 0 22px rgba(41, 243, 255, 0.12);
}

.error {
  padding: 10px 12px;
  border-radius: 16px;
  border: 1px solid rgba(255, 106, 0, 0.25);
  background: rgba(255, 106, 0, 0.1);
  margin: 0 0 12px;
}

.hint {
  margin-top: 12px;
  color: rgba(230, 242, 255, 0.55);
  font-size: 12px;
}

.link {
  color: rgba(230, 242, 255, 0.9);
  text-decoration: underline;
}
</style>