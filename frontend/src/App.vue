<!-- frontend/src/App.vue -->
<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import {
  getToken,
  clearToken,
  getGegenstaende,
  createGegenstand,
  apiFetch
} from './api'

const route = useRoute()
const router = useRouter()

/* ---------------- Auth ---------------- */
const isLoggedIn = ref(false)

function refreshAuth() {
  isLoggedIn.value = !!getToken()
}

function goLogin() {
  router.push('/login')
}

function logout() {
  clearToken()
  refreshAuth()
  router.push('/')
}

function handleAuthError(e) {
  const msg = String(e?.message || e)
  if (/401|403|nicht eingeloggt|token/i.test(msg)) {
    logout()
    return 'Nicht eingeloggt oder Token abgelaufen.'
  }
  return msg
}

/* ---------------- Search ---------------- */
const searchQuery = ref('')
function onSearchEnter() {
  router.push({ path: '/items', query: { q: searchQuery.value } })
}

/* ---------------- State ---------------- */
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

/* ---------------- Helpers ---------------- */
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

/* ---------------- Data: Gegenstände ---------------- */
async function ladeDaten() {
  fehler.value = ''
  if (!isLoggedIn.value) {
    liste.value = []
    return
  }
  try {
    liste.value = await getGegenstaende()
  } catch (e) {
    fehler.value = handleAuthError(e)
  }
}

async function speichern() {
  fehler.value = ''
  if (!isLoggedIn.value) {
    fehler.value = 'Bitte zuerst einloggen.'
    return
  }
  if (!name.value.trim() || !ort.value.trim()) {
    fehler.value = 'Bitte Name und Ort ausfüllen.'
    return
  }

  const payload = {
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
    await createGegenstand(payload)

    name.value = ''
    ort.value = ''
    lastUsed.value = ''
    wegwerfAm.value = ''
    kaufpreis.value = ''
    wunschVerkaufspreis.value = ''

    await ladeDaten()
    await ladeNotifications()
  } catch (e) {
    fehler.value = handleAuthError(e)
  }
}

/* ---------------- Notifications ---------------- */
const notifications = ref([])
const notifError = ref('')

async function ladeNotifications() {
  notifError.value = ''
  if (!isLoggedIn.value) {
    notifications.value = []
    return
  }
  try {
    notifications.value = await apiFetch('/notifications?unseenOnly=true', { method: 'GET' })
  } catch (e) {
    notifError.value = handleAuthError(e)
  }
}

async function markNotifSeen(id) {
  notifError.value = ''
  if (!isLoggedIn.value) {
    notifError.value = 'Bitte zuerst einloggen.'
    return
  }
  try {
    await apiFetch(`/notifications/${id}/seen`, { method: 'PUT' })
    await ladeNotifications()
  } catch (e) {
    notifError.value = handleAuthError(e)
  }
}

/* ---------------- Home-only helper ---------------- */
function goToAddAndFocus() {
  if (route.path !== '/') router.push('/')
  setTimeout(() => {
    document.getElementById('add')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }, 50)
}

/* ---------------- Lifecycle ---------------- */
let notifInterval = null

function onStorageChange(e) {
  if (['token', 'user'].includes(e.key)) {
    refreshAuth()
    ladeDaten()
    ladeNotifications()
  }
}

function onAuthChanged() {
  refreshAuth()
  ladeDaten()
  ladeNotifications()
}

onMounted(async () => {
  refreshAuth()
  window.addEventListener('storage', onStorageChange)
  window.addEventListener('auth-changed', onAuthChanged)

  await ladeDaten()
  await ladeNotifications()

  notifInterval = setInterval(ladeNotifications, 30000)
})

onBeforeUnmount(() => {
  if (notifInterval) clearInterval(notifInterval)
  window.removeEventListener('storage', onStorageChange)
  window.removeEventListener('auth-changed', onAuthChanged)
})
</script>

<template>
  <div class="page">
    <header class="topbar">
      <div class="topbar-inner">
        <div class="topbar-left">
          <div class="nav-search">
            <input
                v-model="searchQuery"
                class="nav-search-input"
                type="search"
                placeholder="Suche nach Gegenstand…"
                @keydown.enter.prevent="onSearchEnter"
            />
          </div>
        </div>

        <nav class="topbar-center">
          <RouterLink to="/" class="navlink" :class="{ active: route.path === '/' }">Home</RouterLink>
          <RouterLink to="/items" class="navlink" :class="{ active: route.path === '/items' }">Gegenstände</RouterLink>
          <RouterLink to="/notifications" class="navlink" :class="{ active: route.path === '/notifications' }">
            Erinnerungen ({{ notifications.length }})
          </RouterLink>
        </nav>

        <div class="topbar-right">
          <button v-if="!isLoggedIn" class="navlink as-btn login-btn" type="button" @click="goLogin">Login</button>
          <button v-else class="navlink as-btn logout-btn" type="button" @click="logout">Logout</button>
        </div>
      </div>
    </header>

    <!-- HOME -->
    <template v-if="route.path === '/'">
      <section class="hero">
        <div class="wrap">
          <div class="hero-grid">
            <div class="hero-copy">
              <h1 class="hero-title">Can I Go?</h1>
              <p class="hero-sub">
                'Speichere und ordne deine Gegenstände – und bekomme Erinnerungen, wenn etwas fällig ist.'
              </p>
            </div>

            <aside class="insight">
              <div class="insight-top">
                <span class="insight-kicker">Quick Insight</span>
                <span class="insight-pill">{{ liste.length }} Items</span>
              </div>
              <div class="insight-row"><span>Wichtig</span><strong>{{ liste.filter(x => x.wichtigkeit === 'WICHTIG').length }}</strong></div>
              <div class="insight-row"><span>Wegwerf-Datum</span><strong>{{ liste.filter(x => x.wegwerfAm).length }}</strong></div>
              <div class="insight-row"><span>Erinnerungen</span><strong>{{ notifications.length }}</strong></div>
            </aside>
          </div>
        </div>
      </section>

      <main class="content">
        <section id="add" class="panel panel-big">
          <div class="panel-head">
            <h2>Neuen Gegenstand hinzufügen</h2>
            <p>Felder mit * sind Pflicht. Alles andere ist optional.</p>
          </div>

          <div class="form-grid">
            <label class="field"><span>Name*</span><input v-model="name" placeholder="z.B. Hammer" /></label>
            <label class="field"><span>Ort*</span><input v-model="ort" placeholder="z.B. Werkbank" /></label>

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

            <label class="field"><span>Zuletzt benutzt</span><input v-model="lastUsed" type="date" /></label>
            <label class="field"><span>Wegwerf-Datum</span><input v-model="wegwerfAm" type="date" /></label>
            <label class="field"><span>Kaufpreis (€)</span><input v-model="kaufpreis" type="number" step="0.01" /></label>
            <label class="field"><span>Wunschpreis (€)</span><input v-model="wunschVerkaufspreis" type="number" step="0.01" /></label>
          </div>

          <div v-if="fehler" class="alert">
            <strong>Hinweis:</strong> {{ fehler }}
          </div>

          <div class="actions">
            <button class="btn btn-primary btn-big" type="button" @click="speichern">Hinzufügen</button>
          </div>
        </section>

        <!-- FREE TYPOGRAPHY SECTION (no boxes, no borders) -->
        <section class="scroll-notes">
          <div class="notes-wrap">
            <div class="notes-head">
              <div class="notes-kicker">IDEEN</div>
              <h2 class="notes-title">Was du mit Gegenständen machen kannst</h2>
              <p class="notes-sub">Kein To-Do-Overload. Einfach klare Optionen – wie Sätze, die im Raum hängen.</p>
            </div>

            <div class="notes-cloud">
              <p class="cloud c1"><span class="lead">Verkaufen</span> – Preis kurz prüfen, 2–3 gute Fotos machen, Abholung/Versand sofort festlegen.</p>
              <p class="cloud c2"><span class="lead">Spenden</span> – In eine Kiste sammeln, Stelle auswählen, festen Abgabetermin setzen.</p>
              <p class="cloud c3"><span class="lead">Schnellentscheidung</span> – 30 Tage ungenutzt? Heute entscheiden: behalten, verkaufen, spenden oder entsorgen.</p>
              <p class="cloud c4"><span class="lead">Reparieren</span> – Defekt notieren, Teil besorgen, 30 Minuten testen. Wenn’s nicht klappt: Plan B.</p>
              <p class="cloud c5"><span class="lead">Verschenken</span> – 1–2 konkrete Personen fragen, Frist setzen. Danach automatisch weiter.</p>
              <p class="cloud c6"><span class="lead">Tauschen</span> – Klar sagen, was du suchst. Nur tauschen, wenn es wirklich passt.</p>
              <p class="cloud c7"><span class="lead">Upcycling</span> – Nur starten, wenn Idee + Material da sind. Sonst wird es ein Dauerprojekt.</p>
              <p class="cloud c8"><span class="lead">Entsorgen</span> – Richtig trennen, Sondermüll prüfen, in einem Gang erledigen.</p>
              <p class="cloud c9"><span class="lead">Einlagern</span> – Box + Label + fester Platz. Dazu ein Datum, wann du es wieder prüfst.</p>
              <p class="cloud c10"><span class="lead">Digitalisieren</span> – Scannen, sinnvoll benennen, Backup. Papier danach leichter reduzieren.</p>
              <p class="cloud c11"><span class="lead">Set komplettieren</span> – Zubehör zusammensuchen und als Set anbieten: wirkt wertiger, geht schneller weg.</p>
            </div>
          </div>
        </section>

        <footer class="footer">
          <span>© {{ new Date().getFullYear() }} Can I Go? • Minimal UI</span>
        </footer>
      </main>
    </template>

    <!-- ROUTED PAGES -->
    <template v-else>
      <main class="content">
        <RouterView
            :liste="liste"
            :searchQuery="route.query.q ?? searchQuery"
            :notifications="notifications"
            :notifError="notifError"
            :formatMoney="formatMoney"
            :dateOrDash="dateOrDash"
            :formatDateTime="formatDateTime"
            :ladeDaten="ladeDaten"
            :ladeNotifications="ladeNotifications"
            :markNotifSeen="markNotifSeen"
        />

        <footer class="footer">
          <span>© {{ new Date().getFullYear() }} Can I Go? • Minimal UI</span>
        </footer>
      </main>
    </template>
  </div>
</template>

<style scoped>
.page{
  --bg:#070A12;
  --ink:#E6F2FF;
  --muted:rgba(230,242,255,.72);
  --border:rgba(41,243,255,.14);
  --glass:rgba(10,14,28,.65);
  --cyan:#29F3FF;
  --pink:#FF3DAE;

  min-height:100vh;
  color:var(--ink);
  background:
      radial-gradient(1100px 700px at 18% 20%, rgba(41,243,255,.14), transparent 55%),
      linear-gradient(180deg, rgba(7,10,18,1) 100%),
      var(--bg);
}

.page :deep(*),
.page :deep(*::before),
.page :deep(*::after){ box-sizing:border-box; }

/* Topbar */
.topbar{
  position:sticky; top:0; z-index:100;
  background: rgba(7, 10, 18, 0.75);
  backdrop-filter: blur(15px);
  border-bottom: 1px solid var(--border);
}
.topbar-inner{
  padding: 10px 24px;
  display:grid;
  grid-template-columns: 1fr auto 1fr;
  align-items:center;
  gap: 10px;
}
.nav-search-input{
  width:260px;
  padding:10px 16px;
  border-radius:12px;
  border:1px solid rgba(230,242,255,.12);
  background: rgba(230,242,255,.05);
  color:#fff;
  outline:none;
}
.topbar-center{ display:flex; gap:8px; justify-content:center; flex-wrap:wrap; }
.topbar-right{ display:flex; justify-content:flex-end; }
.navlink{
  color: var(--muted);
  text-decoration:none;
  font-size:14px;
  padding:8px 18px;
  border-radius:999px;
  transition:.2s;
  display:inline-flex;
  align-items:center;
  justify-content:center;
}
.navlink:hover,.navlink.active{
  color: var(--cyan);
  background: rgba(41,243,255,.10);
}
.as-btn{ background:none; border:none; cursor:pointer; }
.login-btn{ border:1px solid rgba(41,243,255,.30) !important; color: var(--cyan); }
.logout-btn{ color: var(--pink); }

/* Layout */
.wrap{ max-width:1100px; margin:0 auto; padding:0 20px; }
.content{
  max-width:1100px;
  margin:0 auto;
  padding: 18px 20px 80px;
  display:flex;
  flex-direction:column;
  gap: 20px;
}

/* Hero */
.hero{ padding: 56px 0 10px; }
.hero-grid{
  display:grid;
  grid-template-columns: 1fr 420px;
  gap: 32px;
  align-items:start;
}
.hero-title{
  font-size: 84px;
  margin:0;
  letter-spacing:.3px;
}
.hero-sub{
  margin: 10px 0 0;
  max-width: 620px;
  color: var(--muted);
  font-size: 18px;
  line-height: 1.6;
}

/* Buttons */
.btn{
  padding: 12px 24px;
  border-radius: 999px;
  font-weight: 800;
  cursor:pointer;
  border: 1px solid var(--border);
  background: rgba(230,242,255,.06);
  color: var(--ink);
  display:inline-flex;
  align-items:center;
  justify-content:center;
  line-height: 1;
}
.btn-primary{
  margin-top: 16px;
  background: linear-gradient(90deg, rgba(41,243,255,.25), rgba(255,176,0,.15));
  color:#fff;
}
.btn-big{ min-width: 190px; padding: 14px 28px; font-size: 14px; }

/* Insight */
.insight{
  border-radius: 24px;
  border: 1px solid var(--border);
  background: rgba(10,14,28,.50);
  padding: 22px;
}
.insight-top{
  display:flex; align-items:center; justify-content:space-between; gap:12px;
  margin-bottom: 8px;
}
.insight-kicker{ font-size: 12px; color: var(--muted); }
.insight-pill{
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(41,243,255,.18);
  background: rgba(41,243,255,.08);
}
.insight-row{
  display:flex; justify-content:space-between;
  padding: 12px 0;
  border-top: 1px solid rgba(230,242,255,.08);
}
.insight-row span{ color: rgba(230,242,255,.78); }

/* Panel (Add) */
.panel{
  border-radius: 24px;
  border: 1px solid var(--border);
  background: var(--glass);
  padding: 34px;
  overflow:hidden;
}
.panel-big{ padding: 38px; }
.panel-head{ display:flex; flex-direction:column; gap:6px; margin-bottom: 18px; }
.panel-head h2{ margin:0; font-size: 28px; }
.panel-head p{ margin:0; color: var(--muted); }

.form-grid{
  display:grid;
  grid-template-columns: repeat(2, minmax(0,1fr));
  gap: 18px;
  align-items:start;
}
.field{ display:flex; flex-direction:column; gap:8px; font-size:13px; color: var(--muted); }
input,select{
  width:100%;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid rgba(230,242,255,.15);
  background: rgba(230,242,255,.05);
  color:#fff;
  outline:none;
  min-height: 44px;
}
input:focus,select:focus{
  border-color: rgba(41,243,255,.35);
  box-shadow: 0 0 0 3px rgba(41,243,255,.10);
}
.actions{
  display:flex;
  margin-top: 22px;
  padding-top: 16px;
  border-top: 1px solid rgba(230,242,255,.10);
}
.alert{
  margin-top: 16px;
  padding: 16px;
  border-radius: 12px;
  background: rgba(255, 106, 0, 0.10);
  border: 1px solid rgba(255, 106, 0, 0.20);
  color: #ffd0b0;
}

/* Footer */
.footer{
  text-align:center;
  padding: 40px 0 0;
  font-size: 12px;
  color: rgba(230,242,255, 0.35);
}

/* ---------------- FREE TYPO “QUOTES IN SPACE” ---------------- */
.scroll-notes{
  padding-top: 10px;
}

.notes-wrap{
  max-width: 1100px;
  margin: 0 auto;
  padding: 10px 20px 0;
}

.notes-head{
  padding: 26px 0 12px;
}

.notes-kicker{
  font-size: 12px;
  letter-spacing: .14em;
  text-transform: uppercase;
  color: rgba(230,242,255,.55);
  margin-bottom: 10px;
}

.notes-title{
  margin: 0;
  font-size: 34px;
  line-height: 1.15;
}

.notes-sub{
  margin: 12px 0 0;
  max-width: 760px;
  color: rgba(230,242,255,.70);
  font-size: 16px;
  line-height: 1.65;
}

/* RANDOM WALL */
.notes-cloud{
  position: relative;
  margin-top: 18px;
  min-height: 760px;         /* mehr Fläche => weniger Überlappung */
  padding: 30px 0 260px;     /* Platz für top>100% */
}

.cloud{
  position: absolute;
  margin: 0;
  max-width: 560px;
  font-size: 20px;
  line-height: 2.05;         /* mehr Abstand in sich */
  color: rgba(230,242,255,.86);
}

.lead{
  font-weight: 900;
  color: rgba(230,242,255,.96);
}

/* Positionen */
.c1{ left: 4%;  top: 0%; }
.c2{ right: 4%; top: 6%; text-align:right; }

.c3{ left: 18%; top: 20%; max-width: 720px; font-size: 21px; line-height: 2.15; }
.c4{ right: 10%; top: 31%; text-align:right; }

.c5{ left: 6%;  top: 44%; }
.c6{ right: 6%; top: 52%; text-align:right; }

.c7{ left: 20%; top: 62%; max-width: 700px; line-height: 2.15; }
.c8{ right: 18%; top: 73%; text-align:right; }

.c9{ left: 5%;  top: 84%; }
.c10{ right: 5%; top: 90%; text-align:right; }

.c11{ left: 22%; top: 102%; max-width: 760px; line-height: 2.15; }
.c12{ right: 22%; top: 114%; text-align:right; }

/* Mobile: sauber untereinander */
@media (max-width: 980px){
  .notes-cloud{ min-height: auto; padding: 18px 0 0; }
  .cloud{
    position: static;
    max-width: none;
    font-size: 18px;
    line-height: 1.85;
    text-align: left !important;
    margin: 0 0 16px;
  }
}

/* Responsive: auf Mobile wieder sauber untereinander (ohne Chaos) */
@media (max-width: 980px){
  .topbar-inner{ grid-template-columns: 1fr; }
  .nav-search-input{ width:100%; }
  .hero-grid{ grid-template-columns: 1fr; }
  .hero-title{ font-size: 56px; }
  .hero-sub{ font-size: 16px; }
  .form-grid{ grid-template-columns: 1fr; }

  .notes-title{ font-size: 28px; }

  .notes-cloud{
    min-height: auto;
    padding: 18px 0 0;
  }
  .cloud{
    position: static;
    max-width: none;
    font-size: 18px;
    line-height: 1.85;
    text-align: left !important;
    margin: 0 0 16px;
  }
}
</style>