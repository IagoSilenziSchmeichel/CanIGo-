<script setup>
import { ref, onMounted } from 'vue'

const liste = ref([])
const fehler = ref('')
// Variablen für das Formular
const neuerName = ref('')
const neuerOrt = ref('')

const baseUrl = import.meta.env.VITE_APP_BACKEND_BASE_URL
const endpoint = baseUrl + '/gegenstaende'

// Daten laden (GET)
async function ladeDaten() {
  try {
    const res = await fetch(endpoint)
    liste.value = await res.json()
  } catch (e) {
    fehler.value = 'Fehler beim Laden.'
  }
}

// Daten speichern (POST) - Das ist neu für M4!
async function speichern() {
  const neuerGegenstand = {
    name: neuerName.value,
    ort: neuerOrt.value,
    status: 'Neu'
  }

  await fetch(endpoint, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(neuerGegenstand)
  })

  // Felder leeren und Liste neu laden
  neuerName.value = ''
  neuerOrt.value = ''
  ladeDaten()
}

onMounted(() => {
  ladeDaten()
})
</script>

<template>
  <main class="app-container">
    <h1>$$$ Mein Inventar $$$</h1>

    <!-- Das neue Formular -->
    <div class="form-box">
      <input v-model="neuerName" placeholder="Name (z.B. Hammer)" />
      <input v-model="neuerOrt" placeholder="Ort (z.B. Werkbank)" />
      <button @click="speichern">Hinzufügen</button>
    </div>

    <!-- Deine Liste (bleibt fast gleich) -->
    <ul class="grid">
      <li v-for="g in liste" :key="g.id" class="card">
        {{ g.name }} ({{ g.ort }})
      </li>
    </ul>
  </main>
</template>

<style>
/* Kleines bisschen Style für das Formular */
.form-box { margin-bottom: 2rem; display: flex; gap: 10px; justify-content: center; }
input { padding: 8px; border-radius: 4px; border: 1px solid #ccc; }
button { padding: 8px 16px; background: #2e7d32; color: white; border: none; border-radius: 4px; cursor: pointer; }
</style>