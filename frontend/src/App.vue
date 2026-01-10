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

onMounted(async () => {
  refreshAuth()
  window.addEventListener('storage', onStorageChange)
  await ladeDaten()
  await ladeNotifications()
  notifInterval = setInterval(ladeNotifications, 30000)
})

onBeforeUnmount(() => {
  if (notifInterval) clearInterval(notifInterval)
  window.removeEventListener('storage', onStorageChange)
})
</script>

<template>
  <div class="page">
    <!-- NEU: Strukturierte Topbar -->
    <header class="topbar">
      <div class="topbar-inner">

        <!-- LINKS: Die Suche -->
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

        <!-- MITTE: Navigation -->
        <nav class="topbar-center">
          <RouterLink to="/" class="navlink" :class="{ active: route.path === '/' }">
            Home
          </RouterLink>

          <RouterLink to="/items" class="navlink" :class="{ active: route.path === '/items' }">
            Gegenstände
          </RouterLink>

          <RouterLink
              to="/notifications"
              class="navlink"
              :class="{ active: route.path === '/notifications' }"
          >
            Erinnerungen ({{ notifications.length }})
          </RouterLink>
        </nav>

        <!-- RECHTS: Login / Logout -->
        <div class="topbar-right">
          <button
              v-if="!isLoggedIn"
              class="navlink as-btn login-btn"
              type="button"
              @click="goLogin"
          >
            Login
          </button>

          <button
              v-else
              class="navlink as-btn logout-btn"
              type="button"
              @click="logout"
          >
            Logout
          </button>
        </div>

      </div>
    </header>

    <!-- HOME CONTENT -->
    <template v-if="route.path === '/'">
      <section class="hero">
        <div class="hero-inner">
          <div>
            <h1 class="hero-title">Can I Go?</h1>
            <p class="hero-sub">
              Speichere und ordne deine Gegenstände – und bekomme Erinnerungen, wenn etwas fällig ist.
            </p>

            <div class="hero-cta">
              <button class="btn btn-primary" type="button" @click="goToAddAndFocus">
                Neuen Gegenstand anlegen
              </button>
              <RouterLink class="btn btn-ghost" to="/items">Liste ansehen ({{ liste.length }})</RouterLink>
            </div>

            <div v-if="!isLoggedIn" class="alert">
              <strong>Hinweis:</strong>
              <span>Bitte einloggen, damit die Liste und Erinnerungen geladen werden können.</span>
            </div>

            <div v-else-if="fehler" class="alert">
              <strong>Fehler:</strong>
              <span>{{ fehler }}</span>
            </div>
          </div>

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

      <main class="content">
        <section class="panel">
          <div class="panel-head">
            <h2>Tipps: Was du mit alten Gegenständen tun kannst</h2>
            <p>Kurze Ideen – schnell entscheiden, bevor du es wegwirfst.</p>
          </div>

          <div class="tips">
            <div class="tip-card">
              <div class="tip-icon">♻️</div>
              <div>
                <strong>Spenden</strong>
                <p>Wenn’s noch funktioniert: Sozialkaufhaus, Freunde, Nachbarn.</p>
              </div>
            </div>

            <div class="tip-card">
              <div class="tip-icon">💶</div>
              <div>
                <strong>Verkaufen</strong>
                <p>Wunschpreis setzen und checken, ob sich’s lohnt.</p>
              </div>
            </div>

            <div class="tip-card">
              <div class="tip-icon">🗑️</div>
              <div>
                <strong>Wegwerfen</strong>
                <p>Mit Wegwerf-Datum planen – und rechtzeitig erinnert werden.</p>
              </div>
            </div>
          </div>
        </section>

        <section id="add" class="panel">
          <div class="panel-head">
            <h2>Neuen Gegenstand hinzufügen</h2>
            <p>Felder mit * sind Pflicht. Alles andere ist optional.</p>
          </div>

          <div class="form-grid">
            <!-- ... deine Inputs bleiben alle gleich ... -->
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
            <label class="field"><span>Wunsch-Verkaufspreis (€)</span><input v-model="wunschVerkaufspreis" type="number" step="0.01" /></label>
          </div>

          <!-- NEU: Fehlermeldung direkt im Formular anzeigen -->
          <div v-if="fehler" class="alert form-alert">
            <strong>Hinweis:</strong> {{ fehler }}
          </div>

          <div class="actions">
            <button class="btn btn-primary" type="button" @click="speichern">
              Hinzufügen
            </button>
          </div>
        </section>

        <footer class="footer">
          <span>© {{ new Date().getFullYear() }} Can I Go?</span>
          <span class="dot">•</span>
          <span>Minimal UI · Vue + Spring Boot</span>
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
          <span>© {{ new Date().getFullYear() }} Can I Go?</span>
          <span class="dot">•</span>
          <span>Minimal UI · Vue + Spring Boot</span>
        </footer>
      </main>
    </template>
  </div>
</template>

<style scoped>
.page {
  --br-bg: #070A12;
  --br-ink: #E6F2FF;
  --br-muted: rgba(230,242,255,.72);
  --br-cyan: #29F3FF;
  --br-border: rgba(41,243,255,.14);
  --br-pink: #FF3DAE;
  --br-shadow: 0 22px 70px rgba(0,0,0,.55);
  --br-glow-cyan: 0 0 0 1px rgba(41,243,255,.18), 0 0 22px rgba(41,243,255,.12);

  min-height: 100vh;
  color: var(--br-ink);
  background:
      radial-gradient(1100px 700px at 18% 20%, rgba(41,243,255,.14), transparent 55%),
      radial-gradient(900px 650px at 82% 25%, rgba(255,176,0,.12), transparent 55%),
      radial-gradient(900px 700px at 55% 90%, rgba(255,61,174,.06), transparent 60%),
      linear-gradient(180deg, rgba(0,183,180,.06) 0%, rgba(255,106,0,.05) 55%, rgba(7,10,18,1) 100%),
      var(--br-bg);
}

/* ---------- Topbar Redesign ---------- */
.topbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(7, 10, 18, 0.7);
  backdrop-filter: blur(15px);
  border-bottom: 1px solid var(--br-border);
}

.topbar-inner {
  max-width: 1400px; /* Breiteres Layout für die Nav */
  margin: 0 auto;
  padding: 12px 24px;
  display: grid;
  grid-template-columns: 1fr auto 1fr; /* Links - Mitte - Rechts */
  align-items: center;
}

.topbar-left {
  display: flex;
  justify-content: flex-start;
}

.topbar-center {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.topbar-right {
  display: flex;
  justify-content: flex-end;
}

/* Suche */
.nav-search-input {
  width: 260px;
  padding: 10px 16px;
  border-radius: 12px;
  border: 1px solid rgba(230,242,255,.12);
  background: rgba(230,242,255,.05);
  color: #fff;
  outline: none;
  font-size: 14px;
  transition: all 0.3s ease;
}

.nav-search-input:focus {
  border-color: var(--br-cyan);
  box-shadow: var(--br-glow-cyan);
}

/* Nav Links */
.navlink {
  color: var(--br-muted);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 999px;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.navlink:hover {
  color: #fff;
  background: rgba(41,243,255,.08);
}

.navlink.active {
  color: var(--br-cyan);
  background: rgba(41,243,255,.1);
  border-color: rgba(41,243,255,.2);
}

.as-btn {
  background: none;
  cursor: pointer;
  border: none;
}

.login-btn {
  color: var(--br-cyan);
  border: 1px solid rgba(41,243,255, 0.3);
}

.logout-btn {
  color: var(--br-pink);
}

/* ---------- Restliche Styles (Hero, Panels, Form) ---------- */
.hero { padding: 60px 18px 20px; }
.hero-inner {
  max-width: 1100px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.2fr .8fr;
  gap: 24px;
}
.hero-title { font-size: 54px; margin: 0 0 10px; letter-spacing: -0.03em; }
.hero-sub { color: var(--br-muted); margin-bottom: 24px; line-height: 1.6; }
.hero-cta { display: flex; gap: 12px; margin-bottom: 20px; }

.hero-card {
  border-radius: 22px;
  border: 1px solid var(--br-border);
  background: rgba(10,14,28,.45);
  padding: 20px;
  box-shadow: var(--br-shadow);
}
.hero-card-row {
  display: flex; justify-content: space-between;
  padding: 12px 0; border-top: 1px solid rgba(230,242,255,.1);
}

.content { max-width: 1100px; margin: 0 auto; padding: 20px 18px 80px; display: flex; flex-direction: column; gap: 20px; }
.panel {
  border-radius: 22px; border: 1px solid var(--br-border);
  background: rgba(10,14,28,.55); padding: 24px; backdrop-filter: blur(12px);
}

.tips { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.tip-card {
  border-radius: 20px; border: 1px solid rgba(230,242,255,.1);
  background: rgba(10,14,28,.3); padding: 16px; display: flex; gap: 12px;
}
.tip-icon { font-size: 24px; }

.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.field { display: flex; flex-direction: column; gap: 6px; font-size: 13px; color: var(--br-muted); }
input, select {
  padding: 12px; border-radius: 12px; border: 1px solid rgba(230,242,255,.15);
  background: rgba(230,242,255,.05); color: #fff; outline: none;
}
input:focus { border-color: var(--br-cyan); }

.btn { padding: 12px 20px; border-radius: 999px; font-weight: 700; cursor: pointer; border: 1px solid var(--br-border); }
.btn-primary { background: linear-gradient(90deg, rgba(41,243,255,.2), rgba(255,176,0,.15)); color: #fff; box-shadow: var(--br-glow-cyan); }
.btn-ghost { background: rgba(230,242,255,.05); color: var(--br-muted); }

.alert { padding: 16px; border-radius: 12px; background: rgba(255,106,0,.1); border: 1px solid rgba(255,106,0,.2); margin-top: 16px; }
.footer { text-align: center; padding: 40px; font-size: 12px; color: rgba(230,242,255,.4); }

@media (max-width: 900px) {
  .topbar-inner { grid-template-columns: 1fr; gap: 15px; }
  .topbar-left, .topbar-center, .topbar-right { justify-content: center; }
  .hero-inner, .tips, .form-grid { grid-template-columns: 1fr; }
}
</style>