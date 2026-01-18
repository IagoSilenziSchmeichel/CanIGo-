<!-- frontend/src/App.vue -->
<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { getToken, clearToken, getGegenstaende, createGegenstand, apiFetch } from './api'

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
    wunschVerkaufspreis:
        wunschVerkaufspreis.value !== '' ? Number(wunschVerkaufspreis.value) : null
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
          <RouterLink to="/items" class="navlink" :class="{ active: route.path === '/items' }"
          >Gegenstände</RouterLink
          >
          <RouterLink
              to="/notifications"
              class="navlink"
              :class="{ active: route.path === '/notifications' }"
          >
            Erinnerungen ({{ notifications.length }})
          </RouterLink>
        </nav>

        <div class="topbar-right">
          <button v-if="!isLoggedIn" class="navlink as-btn login-btn" type="button" @click="goLogin">
            Login
          </button>
          <button v-else class="navlink as-btn logout-btn" type="button" @click="logout">
            Logout
          </button>
        </div>
      </div>
    </header>

    <!-- HOME -->
    <template v-if="route.path === '/'">
      <!-- ✅ Hero & Quick Insight: gleicher Container + gleiche Kanten wie unten -->
      <section class="hero hero-min">
        <div class="wrap hero-grid">
          <div class="hero-copy">
            <h1 class="hero-title hero-title-big">Can I Go?</h1>
            <p class="hero-sub hero-sub-big">
              Speichere und ordne deine Gegenstände – und bekomme Erinnerungen, wenn etwas fällig ist.
            </p>
          </div>

          <aside class="hero-card">
            <div class="hero-card-top">
              <span class="mini-title">Quick Insight</span>
              <span class="mini-badge">{{ liste.length }} Items</span>
            </div>
            <div class="hero-card-row">
              <span>Wichtig:</span
              ><strong>{{ liste.filter((x) => x.wichtigkeit === 'WICHTIG').length }}</strong>
            </div>
            <div class="hero-card-row">
              <span>Wegwerf-Datum:</span><strong>{{ liste.filter((x) => x.wegwerfAm).length }}</strong>
            </div>
            <div class="hero-card-row">
              <span>Erinnerungen:</span><strong>{{ notifications.length }}</strong>
            </div>
          </aside>
        </div>
      </section>

      <main class="content">
        <!-- ✅ Add Panel: exakt gleiche Kante wie Hero/Quick Insight -->
        <section id="add" class="panel panel-big panel-tight">
          <div class="panel-head panel-head-big">
            <h2>Neuen Gegenstand hinzufügen</h2>
            <p>Felder mit * sind Pflicht. Alles andere ist optional.</p>
          </div>

          <div class="form-grid form-grid-big">
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
            <label class="field"
            ><span>Wunschpreis (€)</span><input v-model="wunschVerkaufspreis" type="number" step="0.01"
            /></label>
          </div>

          <div v-if="fehler" class="alert form-alert">
            <strong>Hinweis:</strong> {{ fehler }}
          </div>

          <!-- ✅ Button sitzt IM Panel sauber unterhalb, ohne Überlappung -->
          <div class="actions actions-big">
            <button class="btn btn-primary btn-big" type="button" @click="speichern">Hinzufügen</button>
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
/* ---------- Base / Theme ---------- */
.page {
  --br-bg: #070A12;
  --br-ink: #E6F2FF;
  --br-muted: rgba(230,242,255,.72);
  --br-cyan: #29F3FF;
  --br-border: rgba(41,243,255,.14);
  --br-pink: #FF3DAE;
  --br-glass: rgba(10,14,28,.65);
  --br-shadow: 0 22px 70px rgba(0,0,0,.55);

  min-height: 100vh;
  color: var(--br-ink);
  background:
      radial-gradient(1100px 700px at 18% 20%, rgba(41,243,255,.14), transparent 55%),
      linear-gradient(180deg, rgba(7,10,18,1) 100%),
      var(--br-bg);
}

/* box sizing */
.page :deep(*),
.page :deep(*::before),
.page :deep(*::after) {
  box-sizing: border-box;
}

/* ---------- Topbar ---------- */
.topbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(7, 10, 18, 0.75);
  backdrop-filter: blur(15px);
  border-bottom: 1px solid var(--br-border);
}

.topbar-inner {
  max-width: 100%;
  padding: 10px 24px;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 10px;
}

.nav-search-input {
  width: 260px;
  padding: 10px 16px;
  border-radius: 12px;
  border: 1px solid rgba(230,242,255,.12);
  background: rgba(230,242,255,.05);
  color: #fff;
  outline: none;
}

.topbar-center {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
}
.topbar-right {
  display: flex;
  justify-content: flex-end;
}

.navlink {
  color: var(--br-muted);
  text-decoration: none;
  font-size: 14px;
  padding: 8px 18px;
  border-radius: 999px;
  transition: 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.navlink:hover,
.navlink.active {
  color: var(--br-cyan);
  background: rgba(41,243,255,.10);
}

.as-btn {
  background: none;
  border: none;
  cursor: pointer;
}
.login-btn {
  border: 1px solid rgba(41,243,255,.30) !important;
  color: var(--br-cyan);
}
.logout-btn {
  color: var(--br-pink);
}

/* ---------- Shared Container (WICHTIG fürs “gerade” Layout) ---------- */
.wrap {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 20px; /* gleiche Kante wie .content */
}

/* ---------- Layout ---------- */
.content {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px 20px 80px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.panel {
  border-radius: 24px;
  border: 1px solid var(--br-border);
  background: var(--br-glass);
  padding: 28px;
  overflow: hidden; /* nichts darf raus */
}

.panel-head {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}
.panel-head h2 { margin: 0; }
.panel-head p { margin: 0; color: var(--br-muted); }

/* ---------- Hero ---------- */
.hero-min {
  padding: 54px 0 10px; /* links/rechts handled durch .wrap */
}

.hero-grid {
  display: grid;
  grid-template-columns: 1fr 420px;
  gap: 32px;
  align-items: start;
}

.hero-copy {
  max-width: 720px;
}

.hero-title-big {
  font-size: 72px;
  letter-spacing: .5px;
  margin: 0;
}

.hero-sub-big {
  font-size: 18px;
  line-height: 1.5;
  max-width: 640px;
  color: var(--br-muted);
  margin: 10px 0 0;
}

/* Quick Insight */
.hero-card {
  border-radius: 24px;
  border: 1px solid var(--br-border);
  background: rgba(10,14,28,.50);
  padding: 24px;
  overflow: hidden; /* Chips/Badges niemals raus */
}

.hero-card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.mini-title { color: var(--br-muted); font-size: 12px; }
.mini-badge {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(41,243,255,.18);
  background: rgba(41,243,255,.08);
  color: var(--br-ink);
  white-space: nowrap;
}

.hero-card-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-top: 1px solid rgba(230,242,255,.08);
}

/* ---------- Bigger Add Panel ---------- */
.panel-big { padding: 34px; }
.panel-head-big h2 { font-size: 26px; }
.panel-head-big p { font-size: 14px; }

/* ✅ optional: Panel wirkt “breiter”, aber bleibt exakt auf der gleichen Kante */
.panel-tight {
  /* nichts — absichtlich: exakt gleiche Kanten wie wrap/content */
}

/* ---------- Form ---------- */
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  align-items: start;
  padding-bottom: 6px;
}

.form-grid-big {
  gap: 18px;
  margin-top: 10px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 13px;
  color: var(--br-muted);
  min-width: 0;
}

input,
select {
  width: 100%;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid rgba(230,242,255,.15);
  background: rgba(230,242,255,.05);
  color: #fff;
  outline: none;
  min-height: 44px;
}

input:focus,
select:focus {
  border-color: rgba(41,243,255,.35);
  box-shadow: 0 0 0 3px rgba(41,243,255,.10);
}

/* ---------- Actions ---------- */
.actions {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  gap: 12px;

  margin-top: 22px;
  padding-top: 16px;
  border-top: 1px solid rgba(230,242,255,.10);
}

.actions .btn { min-width: 160px; }

/* ---------- Buttons ---------- */
.btn {
  padding: 12px 24px;
  border-radius: 999px;
  font-weight: 700;
  cursor: pointer;
  border: 1px solid var(--br-border);
  background: rgba(230,242,255,.06);
  color: var(--br-ink);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.btn-primary {
  background: linear-gradient(90deg, rgba(41,243,255,.25), rgba(255,176,0,.15));
  color: #fff;
}

.btn-big {
  min-width: 190px;
  padding: 14px 28px;
  font-size: 14px;
}

/* ---------- Alerts / Footer ---------- */
.alert {
  padding: 16px;
  border-radius: 12px;
  background: rgba(255, 106, 0, 0.10);
  border: 1px solid rgba(255, 106, 0, 0.20);
  color: #ffd0b0;
  margin-top: 16px;
}

.footer {
  text-align: center;
  padding: 40px;
  font-size: 12px;
  color: rgba(230,242,255, 0.35);
}

/* ---------- Responsive ---------- */
@media (max-width: 900px) {
  .topbar-inner { grid-template-columns: 1fr; }
  .nav-search-input { width: 100%; }

  .hero-grid { grid-template-columns: 1fr; }
  .hero-title-big { font-size: 46px; }
  .hero-sub-big { font-size: 16px; }

  .panel-big { padding: 26px; }
  .form-grid { grid-template-columns: 1fr; }
}
</style>