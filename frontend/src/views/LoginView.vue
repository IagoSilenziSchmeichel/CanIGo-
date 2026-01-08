<template>
  <div class="login-page">
    <h1>Login</h1>
    <p v-if="error" class="error">{{ error }}</p>
    <div id="okta-signin-container"></div>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref } from 'vue'
import OktaSignIn from '@okta/okta-signin-widget'


const error = ref('')
let widget = null

onMounted(() => {
  error.value = ''

  widget = new OktaSignIn({
    issuer: import.meta.env.VITE_OKTA_ISSUER,
    clientId: import.meta.env.VITE_OKTA_CLIENT_ID,
    redirectUri: window.location.origin + '/login/callback',
    scopes: ['openid', 'profile', 'email'],
    devMode: true
  })

  widget.renderEl(
      { el: '#okta-signin-container' },
      (res) => {
        console.log('WIDGET SUCCESS RES:', res)
      },
      (err) => {
        console.error('WIDGET ERROR OBJ:', err)
        error.value = err?.errorSummary || err?.message || JSON.stringify(err, null, 2)
      }
  )
})

onBeforeUnmount(() => {
  if (widget) widget.remove() // ✅ wichtig
})
</script>

<style scoped>
.login-page { padding: 40px 18px; }
.error { color: #ff8a8a; margin: 10px 0; white-space: pre-wrap; }
</style>