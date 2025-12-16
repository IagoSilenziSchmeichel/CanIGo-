<script setup>
import { ref, onMounted } from 'vue'

const liste = ref([])
const fehler = ref('')

const name = ref('')
const ort = ref('')
const wichtigkeit = ref('WICHTIG')         // default
const kategorie = ref('HAUSHALT')           // default
const lastUsed = ref('')                    // yyyy-mm-dd (Input type=date)
const wegwerfAm = ref('')                   // yyyy-mm-dd
const kaufpreis = ref('')                   // number
const wunschVerkaufpreis = ref('')

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
function toNumberOrNull(v) {
  if (v === '' || v === null || v === undefined) return null
  const n = Number(v)
  return Number.isFinite(n) ? n : null
}

function toStringOrNull(v) {
  return v && String(v).trim() ? String(v).trim() : null
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
    status: 'Neu' ,

    wichtigkeit: "WICHTIG",
    kategorie: "HAUSHALT",

    // optional (kann null sein)
    lastUsed: null,
    wegwerfAm: null,
    kaufpreis: null,
    wunschVerkaufpreis: null
  }

  try {
    const res = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(neuerGegenstand)
    })
    if (!res.ok) throw new Error(`POST fehlgeschlagen: HTTP ${res.status}`)

    name.value = ''
    ort.value = ''
    lastUsed.value = ''
    wegwerfAm.value = ''
    kaufpreis.value = ''
    wunschVerkaufpreis.value = ''
    await ladeDaten()
  } catch (e) {
    fehler.value = String(e)
  }
}

onMounted(() => {
  ladeDaten().catch(e => (fehler.value = String(e)))
})
</script>

<template>
  <main class="app-container">
    <h1>$$$ Mein Inventar $$$</h1>

    <p v-if="fehler" class="error">{{ fehler }}</p>

    <!-- FORMULAR -->
    <section class="panel">
      <h2 class="panel-title">Neuen Gegenstand hinzufügen</h2>

      <div class="form-grid">
        <label>
          Name*
          <input v-model="name" placeholder="z.B. Hammer" />
        </label>

        <label>
          Ort*
          <input v-model="ort" placeholder="z.B. Werkbank" />
        </label>

        <label>
          Wichtigkeit
          <select v-model="wichtigkeit">
            <option value="WICHTIG">Wichtig</option>
            <option value="MITTEL">Mittel</option>
            <option value="UNWICHTIG">Unwichtig</option>
          </select>
        </label>

        <label>
          Kategorie
          <select v-model="kategorie">
            <option value="HAUSHALT">Haushalt</option>
            <option value="DOKUMENTE">Dokumente</option>
            <option value="REISE">Reise</option>
            <option value="TECH">Tech</option>
            <option value="SONSTIGES">Sonstiges</option>
          </select>
        </label>

        <label>
          Zuletzt benutzt
          <input v-model="lastUsed" type="date" />
        </label>

        <label>
          Wegwerf-Datum
          <input v-model="wegwerfAm" type="date" />
        </label>

        <label>
          Kaufpreis (€)
          <input v-model="kaufpreis" type="number" step="0.01" placeholder="z.B. 49.99" />
        </label>

        <label>
          Wunsch-Verkaufspreis (€)
          <input v-model="wunschVerkaufpreis" type="number" step="0.01" placeholder="z.B. 25.00" />
        </label>
      </div>

      <div class="actions">
        <button @click="speichern">Hinzufügen</button>
      </div>
    </section>

    <!-- LISTE -->
    <section class="panel">
      <h2 class="panel-title">Deine Gegenstände ({{ liste.length }})</h2>

      <ul class="grid">
        <li v-for="g in liste" :key="g.id" class="card">
          <div class="card-title">
            <strong>{{ g.name }}</strong>
            <span class="badge">{{ g.wichtigkeit }}</span>
            <span class="badge ghost">{{ g.kategorie }}</span>
          </div>

          <div class="card-row">📍 Ort: <span>{{ g.ort }}</span></div>
          <div class="card-row">🗓️ Zuletzt: <span>{{ g.lastUsed ?? '—' }}</span></div>
          <div class="card-row">🗑️ Wegwerf am: <span>{{ g.wegwerfAm ?? '—' }}</span></div>
          <div class="card-row">💶 Kaufpreis: <span>{{ g.kaufpreis ?? '—' }}</span></div>
          <div class="card-row">🏷️ Wunschpreis: <span>{{ g.wunschVerkaufpreis ?? '—' }}</span></div>
        </li>
      </ul>
    </section>
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
  gap: 18px;
  align-items:stretch;
}

h1{
  text-align:center;
  margin: 0 0 10px;
}

.error{
  margin: 0 auto;
  max-width: 900px;
  padding: 10px 12px;
  border: 1px solid #ffb4b4;
  background: #ffecec;
  color: #8a0000;
  border-radius: 10px;
  white-space: pre-wrap;
}

.panel{
  margin: 0 auto;
  width: 100%;
  max-width: 900px;
  padding: 16px;
  border: 1px solid rgba(0,0,0,.08);
  border-radius: 14px;
  background: rgba(255,255,255,.75);
  backdrop-filter: blur(10px);
}

.panel-title{
  margin: 0 0 12px;
  font-size: 1rem;
  opacity: .85;
}

.form-grid{
  display:grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

label{
  display:flex;
  flex-direction:column;
  gap: 6px;
  font-size: .9rem;
  opacity: .9;
}

input, select{
  padding: 10px;
  border-radius: 10px;
  border: 1px solid rgba(0,0,0,.15);
  background: white;
  color: black;
}

.actions{
  margin-top: 12px;
  display:flex;
  justify-content:flex-end;
}

button{
  padding: 10px 14px;
  background: #2e7d32;
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
}

.grid{
  list-style:none;
  padding:0;
  margin:0;
  display:grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 12px;
}

.card{
  padding: 12px;
  border-radius: 14px;
  border: 1px solid rgba(0,0,0,.08);
  background: rgba(255,255,255,.85);
}

.card-title{
  display:flex;
  gap: 8px;
  align-items:center;
  flex-wrap:wrap;
  margin-bottom: 8px;
}

.badge{
  font-size: .75rem;
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(46,125,50,.12);
  border: 1px solid rgba(46,125,50,.25);
}

.badge.ghost{
  background: rgba(0,0,0,.06);
  border: 1px solid rgba(0,0,0,.10);
}

.card-row{
  font-size: .9rem;
  opacity: .9;
  display:flex;
  gap: 6px;
}
</style>