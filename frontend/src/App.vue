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

async function speichern() {
  fehler.value = ''

  if (!name.value.trim() || !ort.value.trim()) {
    fehler.value = 'Bitte Name und Ort ausfüllen.'
    return
  }

  const neuerGegenstand = {
    name: name.value.trim(),
    ort: ort.value.trim(),
    wichtigkeit: wichtigkeit.value,
    kategorie: kategorie.value,

    // wenn leer -> null, sonst Datum-String "YYYY-MM-DD"
    lastUsed: lastUsed.value ? lastUsed.value : null,
    wegwerfAm: wegwerfAm.value ? wegwerfAm.value : null,

    // wenn leer -> null, sonst Zahl
    kaufpreis: kaufpreis.value !== '' ? Number(kaufpreis.value) : null,
    wunschVerkaufpreis: wunschVerkaufpreis.value !== '' ? Number(wunschVerkaufpreis.value) : null
  }

  try {
    const res = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(neuerGegenstand)
    })

    // Wenn Backend Fehlertext/JSON zurückgibt, zeigen wir es an:
    if (!res.ok) {
      const text = await res.text()
      throw new Error(`POST fehlgeschlagen: HTTP ${res.status} – ${text}`)
    }

    // Felder leeren
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
    <h1>Can I Go?</h1>

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

          <div class="card-row">Ort: <span>{{ g.ort }}</span></div>
          <div class="card-row">Zuletzt: <span>{{ g.lastUsed ?? '—' }}</span></div>
          <div class="card-row">Wegwerf am: <span>{{ g.wegwerfAm ?? '—' }}</span></div>
          <div class="card-row">Kaufpreis: <span>{{ g.kaufpreis ?? '—' }}</span></div>
          <div class="card-row">Wunschpreis: <span>{{ g.wunschVerkaufpreis ?? '—' }}</span></div>
        </li>
      </ul>
    </section>
  </main>
</template>

<style scoped>
.app-container{
  width: 100%;
  min-height: 100vh;
  padding: 48px 16px;
  display: flex;
  justify-content: center;
}

.shell{
  width: 100%;
  max-width: 920px;
  display: grid;
  gap: 18px;
}

.header{
  display:flex;
  align-items:flex-end;
  justify-content:space-between;
  gap: 12px;
  padding: 6px 2px;
}

.title{
  font-size: 34px;
  letter-spacing: -0.02em;
  margin: 0;
}

.subtitle{
  margin: 6px 0 0;
  color: rgba(15,23,42,.6);
  font-size: 14px;
}

.panel{
  background: rgba(255,255,255,.75);
  border: 1px solid rgba(15,23,42,.08);
  border-radius: 18px;
  box-shadow: 0 12px 30px rgba(15,23,42,.08);
  backdrop-filter: blur(10px);
  padding: 16px;
}

.form-box{
  display:flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items:center;
}

input{
  height: 42px;
  padding: 0 12px;
  border-radius: 12px;
  border: 1px solid rgba(15,23,42,.10);
  background: #fff;
  color: #0f172a;
  outline: none;
  min-width: 180px;
}

input:focus{
  border-color: rgba(109,40,217,.35);
  box-shadow: 0 0 0 4px rgba(109,40,217,.12);
}

button{
  height: 42px;
  padding: 0 14px;
  border-radius: 12px;
  border: none;
  background: #6d28d9;
  color: white;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 8px 18px rgba(109,40,217,.20);
}

button:hover{ filter: brightness(1.02); }

.grid{
  list-style:none;
  padding: 0;
  margin: 0;
  display:grid;
  gap: 10px;
}

.card{
  display:flex;
  justify-content:space-between;
  align-items:center;
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid rgba(15,23,42,.08);
  background: rgba(255,255,255,.9);
  box-shadow: 0 6px 16px rgba(15,23,42,.06);
}
</style>