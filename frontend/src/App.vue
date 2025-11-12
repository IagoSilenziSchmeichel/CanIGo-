<script setup>
import { ref, onMounted } from 'vue'
import GegenstandList from './components/GegenstandList.vue'

const liste = ref([])
const fehler = ref('')

onMounted(async () => {
  try {
    const res = await fetch('http://localhost:8080/gegenstaende')
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    liste.value = await res.json()
  } catch (err) {
    fehler.value = String(err)
    // Fallback-Daten, damit du sofort etwas siehst
    liste.value = [
      { id: 1, name: 'Rucksack', ort: 'Flur', status: 'wichtig' },
      { id: 2, name: 'Waffeleisen', ort: 'Küche', status: 'selten genutzt' }
    ]
  }
})
</script>

<template>
  <main class="app-container">
    <h1>$$$ Mein Inventar $$$</h1>

    <p v-if="fehler" style="opacity:.7;font-size:.9rem">
      (Hinweis: {{ fehler }} – zeige Demo-Daten)
    </p>

    <!-- WICHTIG: Prop übergeben -->
    <GegenstandList :gegenstaende="liste" />
  </main>
</template>

<style scoped>
.app-container{
  min-height:100vh; width:100%;
  max-width:1100px; margin:0 auto; padding:2rem 1rem 3rem;
  display:flex; flex-direction:column; align-items:center;
}
h1{ margin:0 0 1rem; color:#2e7d32 }
</style>
