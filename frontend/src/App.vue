<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const liste = ref([])
const fehler = ref('')

const name = ref('')
const ort = ref('')
const wichtigkeit = ref('WICHTIG')
const kategorie = ref('HAUSHALT')
const lastUsed = ref('')
const wegwerfAm = ref('')
const kaufpreis = ref('')
const wunschVerkaufspreis = ref('')

const baseUrl = import.meta.env.VITE_APP_BACKEND_BASE_URL
const endpoint = `${baseUrl}/gegenstaende`

function formatMoney(v) {
  if (v === null || v === undefined || v === '') return '—'
  const n = Number(v)
  if (Number.isNaN(n)) return '—'
  return new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR' }).format(n)
}

function dateOrDash(v) {
  return v ? v : '—'
}

function formatDateTime(v) {
  if (!v) return '—'
  return String(v).replace('T', ' ').slice(0, 16)
}

async function ladeDaten() {
  fehler.value = ''
  try {
    const res = await fetch(endpoint)
    if (!res.ok) {
      const text = await res.text().catch(() => '')
      throw new Error(`GET fehlgeschlagen: HTTP ${res.status}${text ? ` – ${text}` : ''}`)
    }
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
    lastUsed: lastUsed.value ? lastUsed.value : null,
    wegwerfAm: wegwerfAm.value ? wegwerfAm.value : null,
    kaufpreis: kaufpreis.value !== '' ? Number(kaufpreis.value) : null,
    wunschVerkaufspreis: wunschVerkaufspreis.value !== '' ? Number(wunschVerkaufspreis.value) : null
  }

  try {
    const res = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(neuerGegenstand)
    })

    if (!res.ok) {
      const text = await res.text().catch(() => '')
      throw new Error(`POST fehlgeschlagen: HTTP ${res.status}${text ? ` – ${text}` : ''}`)
    }

    // Reset
    name.value = ''
    ort.value = ''
    lastUsed.value = ''
    wegwerfAm.value = ''
    kaufpreis.value = ''
    wunschVerkaufspreis.value = ''

    await ladeDaten()
    await ladeNotifications()
  } catch (e) {
    fehler.value = String(e)
  }
}

/* ---------------- Notifications ---------------- */
const notifications = ref([])
const notifError = ref('')

async function ladeNotifications() {
  notifError.value = ''
  try {
    const res = await fetch(`${baseUrl}/notifications?unseenOnly=true`)
    if (!res.ok) {
      const text = await res.text().catch(() => '')
      throw new Error(`Notifications GET fehlgeschlagen: HTTP ${res.status}${text ? ` – ${text}` : ''}`)
    }
    notifications.value = await res.json()
  } catch (e) {
    notifError.value = String(e)
  }
}

async function markNotifSeen(id) {
  notifError.value = ''
  try {
    const res = await fetch(`${baseUrl}/notifications/${id}/seen`, { method: 'PUT' })
    if (!res.ok) {
      const text = await res.text().catch(() => '')
      throw new Error(`seen PUT fehlgeschlagen: HTTP ${res.status}${text ? ` – ${text}` : ''}`)
    }
    await ladeNotifications()
  } catch (e) {
    notifError.value = String(e)
  }
}

let notifInterval = null

onMounted(() => {
  ladeDaten()
  ladeNotifications()
  notifInterval = setInterval(ladeNotifications, 30000)
})

onBeforeUnmount(() => {
  if (notifInterval) clearInterval(notifInterval)
})
</script>

<template>
  <div class="page">
    <!-- Sticky Top Nav -->
    <header class="topbar">
      <div class="topbar-inner">
        <div class="brand">
          <div class="logo-dot" />
          <span>Can I Go?</span>
        </div>

        <nav class="nav">
          <a href="#home">Home</a>
          <a href="#notifications">Erinnerungen ({{ notifications.length }})</a>
          <a href="#add">Hinzufügen</a>
          <a href="#items">Gegenstände</a>
        </nav>

        <div class="topbar-actions">
          <a class="btn btn-primary" href="#add">Start</a>
        </div>
      </div>
    </header>

    <!-- Hero -->
    <section id="home" class="hero">
      <div class="hero-inner">
        <div>
          <p class="pill">Weniger Chaos. Mehr Überblick.</p>
          <h1 class="hero-title">
            Entscheide smarter, ob dein Nachbar es besser gebrauchen könnte als der Mülleimer.
          </h1>
          <p class="hero-sub">
            Speichere und ordne deine Gegenstände – und bekomme Erinnerungen, wenn etwas fällig ist.
          </p>

          <div class="hero-cta">
            <a class="btn btn-primary" href="#add">Neuen Gegenstand anlegen</a>
            <a class="btn btn-ghost" href="#items">Liste ansehen ({{ liste.length }})</a>
          </div>

          <div v-if="fehler" class="alert">
            <strong>Fehler:</strong>
            <span>{{ fehler }}</span>
          </div>
        </div>

        <!-- Decorative Card -->
        <div class="hero-card">
          <div class="hero-card-top">
            <span class="mini-title">Quick Insight</span>
            <span class="mini-badge">{{ liste.length }} Items</span>
          </div>
          <div class="hero-card-row">
            <span>Wichtig:</span>
            <strong>{{ liste.filter(x => x.wichtigkeit === 'WICHTIG').length }}</strong>
          </div>
          <div class="hero-card-row">
            <span>Mit Wegwerf-Datum:</span>
            <strong>{{ liste.filter(x => x.wegwerfAm).length }}</strong>
          </div>
          <div class="hero-card-row">
            <span>Ungelesene Erinnerungen:</span>
            <strong>{{ notifications.length }}</strong>
          </div>
        </div>
      </div>
    </section>

    <!-- Content -->
    <main class="content">
      <!-- NOTIFICATIONS -->
      <section id="notifications" class="panel">
        <div class="panel-head row">
          <div>
            <h2>Erinnerungen</h2>
            <p>{{ notifications.length }} ungelesen</p>
          </div>
          <button class="btn btn-ghost" @click="ladeNotifications">Neu laden</button>
        </div>

        <div v-if="notifError" class="alert">
          <strong>Fehler:</strong>
          <span>{{ notifError }}</span>
        </div>

        <div v-if="notifications.length === 0" class="empty">
          <div class="empty-card">
            <strong>Keine neuen Erinnerungen 🎉</strong>
            <p>Wenn ein Gegenstand fällig ist, erscheint hier eine Benachrichtigung.</p>
          </div>
        </div>

        <ul v-else class="grid">
          <li v-for="n in notifications" :key="n.id" class="card">
            <div class="card-top">
              <div class="card-title">
                <strong>{{ n.message }}</strong>
                <span class="chip">UNGELESEN</span>
              </div>
              <div class="id">#{{ n.id }}</div>
            </div>

            <div class="card-body">
              <div class="kv"><span>Zeit</span><strong>{{ formatDateTime(n.createdAt) }}</strong></div>
              <div class="kv"><span>Gegenstand-ID</span><strong>{{ n.gegenstandId ?? '—' }}</strong></div>
            </div>

            <div class="actions" style="margin-top: 12px;">
              <button class="btn btn-primary" @click="markNotifSeen(n.id)">Gesehen</button>
            </div>
          </li>
        </ul>
      </section>

      <!-- FORM -->
      <section id="add" class="panel">
        <div class="panel-head">
          <h2>Neuen Gegenstand hinzufügen</h2>
          <p>Felder mit * sind Pflicht. Alles andere ist optional.</p>
        </div>

        <div class="form-grid">
          <label class="field">
            <span>Name*</span>
            <input v-model="name" placeholder="z.B. Hammer" />
          </label>

          <label class="field">
            <span>Ort*</span>
            <input v-model="ort" placeholder="z.B. Werkbank" />
          </label>

          <label class="field">
            <span>Wichtigkeit</span>
            <select v-model="wichtigkeit">
              <option value="WICHTIG">Wichtig</option>
              <option value="MITTEL">Mittel</option>
              <option value="UNWICHTIG">Unwichtig</option>
            </select>
          </label>

          <label class="field">
            <span>Kategorie</span>
            <select v-model="kategorie">
              <option value="HAUSHALT">Haushalt</option>
              <option value="DOKUMENTE">Dokumente</option>
              <option value="REISE">Reise</option>
              <option value="TECH">Tech</option>
              <option value="SONSTIGES">Sonstiges</option>
            </select>
          </label>

          <label class="field">
            <span>Zuletzt benutzt</span>
            <input v-model="lastUsed" type="date" />
          </label>

          <label class="field">
            <span>Wegwerf-Datum</span>
            <input v-model="wegwerfAm" type="date" />
          </label>

          <label class="field">
            <span>Kaufpreis (€)</span>
            <input v-model="kaufpreis" type="number" step="0.01" placeholder="z.B. 49.99" />
          </label>

          <label class="field">
            <span>Wunsch-Verkaufspreis (€)</span>
            <input v-model="wunschVerkaufspreis" type="number" step="0.01" placeholder="z.B. 25.00" />
          </label>
        </div>

        <div class="actions">
          <button class="btn btn-primary" @click="speichern">Hinzufügen</button>
        </div>
      </section>

      <!-- LISTE -->
      <section id="items" class="panel">
        <div class="panel-head row">
          <div>
            <h2>Deine Gegenstände</h2>
            <p>{{ liste.length }} Einträge</p>
          </div>
          <button class="btn btn-ghost" @click="ladeDaten">Neu laden</button>
        </div>

        <div v-if="liste.length === 0" class="empty">
          <div class="empty-card">
            <strong>Noch keine Gegenstände.</strong>
            <p>Lege oben deinen ersten Gegenstand an.</p>
            <a class="btn btn-primary" href="#add">Jetzt starten</a>
          </div>
        </div>

        <ul v-else class="grid">
          <li v-for="g in liste" :key="g.id" class="card">
            <div class="card-top">
              <div class="card-title">
                <strong>{{ g.name }}</strong>
                <span class="chip" :data-imp="g.wichtigkeit">{{ g.wichtigkeit }}</span>
                <span class="chip ghost">{{ g.kategorie }}</span>
              </div>
              <div class="id">#{{ g.id }}</div>
            </div>

            <div class="card-body">
              <div class="kv"><span>Ort</span><strong>{{ g.ort }}</strong></div>
              <div class="kv"><span>Zuletzt</span><strong>{{ dateOrDash(g.lastUsed) }}</strong></div>
              <div class="kv"><span>Wegwerf am</span><strong>{{ dateOrDash(g.wegwerfAm) }}</strong></div>
              <div class="kv"><span>Kaufpreis</span><strong>{{ formatMoney(g.kaufpreis) }}</strong></div>
              <div class="kv"><span>Wunschpreis</span><strong>{{ formatMoney(g.wunschVerkaufspreis) }}</strong></div>
            </div>
          </li>
        </ul>
      </section>

      <footer class="footer">
        <span>© {{ new Date().getFullYear() }} Can I Go?</span>
        <span class="dot">•</span>
        <span>Minimal UI · Vue + Spring Boot</span>
      </footer>
    </main>
  </div>
</template>

<style scoped>
/* ---------- Page Background (Slite-ish) ---------- */
.page{
  min-height: 100vh;
  background:
      radial-gradient(1200px 700px at 20% 10%, rgba(255, 210, 170, .28), transparent 55%),
      radial-gradient(900px 600px at 80% 20%, rgba(160, 210, 255, .22), transparent 55%),
      radial-gradient(900px 700px at 50% 90%, rgba(210, 190, 255, .18), transparent 55%),
      #f6f1e8;
  color: #111827;
}

/* ---------- Topbar ---------- */
.topbar{
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgba(246, 241, 232, .75);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(17,24,39,.08);
}

.topbar-inner{
  max-width: 1100px;
  margin: 0 auto;
  padding: 14px 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.brand{
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.logo-dot{
  width: 12px;
  height: 12px;
  border-radius: 999px;
  background: #111827;
  box-shadow: 0 0 0 6px rgba(17,24,39,.08);
}

.nav{
  display: flex;
  gap: 14px;
  align-items: center;
  flex-wrap: wrap;
}

.nav a{
  color: rgba(17,24,39,.72);
  text-decoration: none;
  font-size: 14px;
  padding: 8px 10px;
  border-radius: 999px;
}

.nav a:hover{
  background: rgba(17,24,39,.06);
  color: rgba(17,24,39,.9);
}

.topbar-actions{
  display: flex;
  gap: 10px;
  align-items: center;
}

/* ---------- Hero ---------- */
.hero{
  padding: 48px 18px 18px;
}

.hero-inner{
  max-width: 1100px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.2fr .8fr;
  gap: 16px;
  align-items: start;
}

.pill{
  display: inline-flex;
  width: fit-content;
  padding: 7px 12px;
  border-radius: 999px;
  border: 1px solid rgba(17,24,39,.10);
  background: rgba(255,255,255,.55);
  color: rgba(17,24,39,.75);
  font-size: 13px;
  margin: 0 0 14px;
}

.hero-title{
  margin: 0 0 10px;
  font-size: 54px;
  line-height: 1.05;
  letter-spacing: -0.03em;
}

.hero-sub{
  margin: 0 0 16px;
  max-width: 62ch;
  color: rgba(17,24,39,.72);
  font-size: 16px;
  line-height: 1.55;
}

.hero-cta{
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.alert{
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: 16px;
  border: 1px solid rgba(255, 120, 120, .35);
  background: rgba(255, 235, 235, .65);
  color: #7a1212;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* right hero card */
.hero-card{
  border-radius: 22px;
  border: 1px solid rgba(17,24,39,.10);
  background: rgba(255,255,255,.60);
  backdrop-filter: blur(10px);
  box-shadow: 0 24px 70px rgba(16,24,40,.10);
  padding: 16px;
  align-self: start;
}

.hero-card-top{
  display:flex;
  align-items:center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.mini-title{
  font-size: 13px;
  color: rgba(17,24,39,.70);
  font-weight: 600;
}

.mini-badge{
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(17,24,39,.10);
  background: rgba(17,24,39,.04);
  color: rgba(17,24,39,.75);
}

.hero-card-row{
  display:flex;
  justify-content: space-between;
  padding: 10px 0;
  border-top: 1px solid rgba(17,24,39,.08);
  color: rgba(17,24,39,.78);
}

/* ---------- Content Panels ---------- */
.content{
  max-width: 1100px;
  margin: 0 auto;
  padding: 18px 18px 70px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel{
  border-radius: 22px;
  border: 1px solid rgba(17,24,39,.10);
  background: rgba(255,255,255,.62);
  backdrop-filter: blur(10px);
  box-shadow: 0 22px 60px rgba(16,24,40,.08);
  padding: 18px;
}

.panel-head{
  margin-bottom: 12px;
}

.panel-head.row{
  display:flex;
  align-items:center;
  justify-content: space-between;
  gap: 12px;
}

.panel h2{
  margin: 0;
  font-size: 18px;
  letter-spacing: -0.01em;
}

.panel p{
  margin: 6px 0 0;
  color: rgba(17,24,39,.68);
  font-size: 13px;
}

/* ---------- Form ---------- */
.form-grid{
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.field{
  display:flex;
  flex-direction: column;
  gap: 8px;
  font-size: 13px;
  color: rgba(17,24,39,.75);
}

input, select{
  padding: 12px 12px;
  border-radius: 16px;
  border: 1px solid rgba(17,24,39,.14);
  background: rgba(255,255,255,.88);
  color: #111827;
  outline: none;
}

input:focus, select:focus{
  border-color: rgba(17,24,39,.28);
  box-shadow: 0 0 0 4px rgba(17,24,39,.06);
}

.actions{
  margin-top: 14px;
  display:flex;
  justify-content:flex-end;
}

/* ---------- Buttons ---------- */
.btn{
  display:inline-flex;
  align-items:center;
  justify-content:center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 999px;
  font-weight: 700;
  text-decoration: none;
  border: 1px solid rgba(17,24,39,.14);
  cursor: pointer;
  user-select: none;
  font-size: 14px;
}

.btn-primary{
  background: #111827;
  color: white;
  border-color: rgba(17,24,39,.14);
}

.btn-primary:hover{ opacity: .92; }

.btn-ghost{
  background: rgba(255,255,255,.55);
  color: rgba(17,24,39,.85);
}

.btn-ghost:hover{
  background: rgba(17,24,39,.06);
}

/* ---------- List ---------- */
.grid{
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(290px, 1fr));
  gap: 14px;
}

.card{
  border-radius: 20px;
  border: 1px solid rgba(17,24,39,.10);
  background: rgba(255,255,255,.72);
  padding: 14px;
}

.card-top{
  display:flex;
  align-items:flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.card-title{
  display:flex;
  gap: 8px;
  align-items:center;
  flex-wrap: wrap;
}

.id{
  font-size: 12px;
  color: rgba(17,24,39,.55);
}

.chip{
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(17,24,39,.10);
  background: rgba(17,24,39,.05);
  color: rgba(17,24,39,.8);
}

.chip.ghost{
  background: rgba(255,255,255,.55);
}

.chip[data-imp="WICHTIG"]{
  border-color: rgba(16,185,129,.25);
  background: rgba(16,185,129,.10);
}

.chip[data-imp="UNWICHTIG"]{
  border-color: rgba(239,68,68,.22);
  background: rgba(239,68,68,.08);
}

.card-body{
  display:grid;
  gap: 8px;
}

.kv{
  display:flex;
  justify-content: space-between;
  gap: 10px;
  font-size: 13px;
  color: rgba(17,24,39,.72);
}

.kv strong{
  color: rgba(17,24,39,.92);
}

/* ---------- Empty state ---------- */
.empty{
  padding: 12px 0 0;
}

.empty-card{
  border-radius: 18px;
  border: 1px dashed rgba(17,24,39,.18);
  background: rgba(255,255,255,.55);
  padding: 18px;
  text-align: center;
}

.empty-card p{
  margin: 8px 0 12px;
}

/* ---------- Footer ---------- */
.footer{
  margin-top: 8px;
  display:flex;
  justify-content:center;
  gap: 10px;
  color: rgba(17,24,39,.55);
  font-size: 12px;
  padding: 10px 0 0;
}

.dot{ opacity: .6; }

/* ---------- Responsive ---------- */
@media (max-width: 980px){
  .hero-inner{
    grid-template-columns: 1fr;
  }
  .hero-title{
    font-size: 44px;
  }
}

@media (max-width: 720px){
  .form-grid{
    grid-template-columns: 1fr;
  }
  .topbar-actions{
    display:none;
  }
  .hero-title{
    font-size: 38px;
  }
}
</style>