<script setup>
import { ref, onMounted } from 'vue'

const liste = ref([])
const fehler = ref('')

const neuerName = ref('')
const neuerOrt = ref('')

const baseUrl = import.meta.env.VITE_APP_BACKEND_BASE_URL
const endpoint = `${baseUrl}/gegenstaende`

async function ladeDaten() {
  fehler.value = ''
  try {
    const res = await fetch(endpoint)
    if (!res.ok) throw new Error(`GET fehlgeschlagen: HTTP ${res.status}`)
    liste.value = await res.json()
  } catch (e) {
    fehler.value = String(e)
  }
}

async function speichern() {
  fehler.value = ''

  // Mini-Check: nicht leer speichern
  if (!neuerName.value.trim() || !neuerOrt.value.trim()) {
    fehler.value = 'Bitte Name und Ort ausfüllen.'
    return
  }

  const neuerGegenstand = {
    name: neuerName.value.trim(),
    ort: neuerOrt.value.trim(),
    status: 'Neu'
  }

  try {
    const res = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(neuerGegenstand)
    })
    if (!res.ok) throw new Error(`POST fehlgeschlagen: HTTP ${res.status}`)

    neuerName.value = ''
    neuerOrt.value = ''
    await ladeDaten()
  } catch (e) {
    fehler.value = String(e)
  }
}

onMounted(ladeDaten)
</script>

<template>
  <main class="app-container">
    <h1>$$$ Mein Inventar $$$</h1>

    <p v-if="fehler" class="error">{{ fehler }}</p>

    <div class="form-box">
      <input v-model="neuerName" placeholder="Name (z.B. Hammer)" />
      <input v-model="neuerOrt" placeholder="Ort (z.B. Werkbank)" />
      <button @click="speichern">Hinzufügen</button>
    </div>

    <ul class="grid">
      <li v-for="g in liste" :key="g.id" class="card">
        {{ g.name }} ({{ g.ort }})
      </li>
    </ul>
  </main>
</template>

<style scoped>
.app-container{
  min-height:100vh;
  width:100%;
  max-width:1100px;
  margin:0 auto;
  padding:2rem 1rem 3rem;
  display:flex;
  flex-direction:column;
  align-items:center;
}

.error{
  margin: .5rem 0 1rem;
  color: #b00020;
}

.form-box {
  margin-bottom: 2rem;
  display: flex;
  gap: 10px;
  justify-content: center;
}

input {
  padding: 8px;
  border-radius: 4px;
  border: 1px solid #ccc;
  color: black;
}

button {
  padding: 8px 16px;
  background: #2e7d32;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.grid{
  list-style:none;
  padding:0;
  margin:0;
  display:grid;
  gap: 12px;
}

.card{
  color:black;
}
</style>