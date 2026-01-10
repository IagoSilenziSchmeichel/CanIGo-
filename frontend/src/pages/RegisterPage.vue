<script setup>
import { ref } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { register } from '../api' // ✅ nutzt api.js (speichert token automatisch)

const route = useRoute()
const router = useRouter()

const name = ref('')
const email = ref('')
const password = ref('')
const password2 = ref('')

const loading = ref(false)
const error = ref('')

async function doRegister() {
  error.value = ''

  const n = name.value.trim()
  const e = email.value.trim().toLowerCase()

  if (!n || !e || !password.value) {
    error.value = 'Bitte Name, Email und Passwort ausfüllen.'
    return
  }
  if (password.value.length < 6) {
    error.value = 'Passwort muss mindestens 6 Zeichen haben.'
    return
  }
  if (password.value !== password2.value) {
    error.value = 'Passwörter stimmen nicht überein.'
    return
  }

  loading.value = true
  try {
    await register(n, e, password.value)

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
  <div class="register-page">
    <div class="card">
      <h1>Registrieren</h1>

      <p v-if="error" class="error">{{ error }}</p>

      <label class="field">
        <span>Name</span>
        <input v-model="name" autocomplete="name" placeholder="z.B. Iago" />
      </label>

      <label class="field">
        <span>Email</span>
        <input v-model="email" type="email" autocomplete="email" placeholder="name@mail.de" />
      </label>

      <label class="field">
        <span>Passwort</span>
        <input v-model="password" type="password" autocomplete="new-password" />
      </label>

      <label class="field">
        <span>Passwort wiederholen</span>
        <input v-model="password2" type="password" autocomplete="new-password" />
      </label>

      <button class="btn btn-primary" type="button" @click="doRegister" :disabled="loading">
        {{ loading ? 'Erstelle Account…' : 'Account erstellen' }}
      </button>

      <p class="hint">
        Schon einen Account?
        <RouterLink class="link" to="/login">Zum Login</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.register-page {
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