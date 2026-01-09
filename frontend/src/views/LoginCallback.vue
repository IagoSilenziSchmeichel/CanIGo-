<template>
  <div style="padding: 40px 18px;">
    <h1>Logging in…</h1>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'


const route = useRoute()
const router = useRouter()

onMounted(async () => {
  const from = route.query.from || '/'
  router.replace(from)
})
</script>