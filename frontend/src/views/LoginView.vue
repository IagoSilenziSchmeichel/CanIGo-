<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { oktaAuth } from '../okta'

const route = useRoute()
const error = ref('')

async function doLogin() {
  error.value = ''
  try {
    const from = route.query.from || '/'
    await oktaAuth.signInWithRedirect({ originalUri: String(from) })
  } catch (e) {
    error.value = String(e?.message || e)
  }
}
</script>

<template>
  <div class="login-page">
    <h1>Login</h1>
    <p v-if="error" class="error">{{ error }}</p>
    <button class="btn btn-primary" type="button" @click="doLogin">
      Mit Okta einloggen
    </button>
  </div>
</template>