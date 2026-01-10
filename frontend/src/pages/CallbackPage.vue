<template>
  <div style="padding: 40px 18px;">
    <h1>Logging in…</h1>
    <p v-if="error" style="opacity:.8">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const error = ref('')

onMounted(() => {
  const token = localStorage.getItem('token')
  const from = route.query.from || '/'

  if (!token) {
    error.value = 'Kein Token gefunden. Bitte normal einloggen.'
    router.replace({ path: '/login', query: { from } })
    return
  }

  router.replace(from)
})
</script>