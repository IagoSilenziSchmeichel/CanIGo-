<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

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

const baseUrl = import.meta.env.VITE_APP_BACKEND_BASE_URL
const endpoint = `${baseUrl}/gegenstaende`

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

/* ---------------- Data ---------------- */
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

/* ---------------- Home-only helper ---------------- */
function goToAddAndFocus() {
  // Home ist in App.vue, wir bleiben dort und scrollen zum Formular
  if (route.path !== '/') router.push('/')
  // wait one tick-ish for route render; simple timeout is enough
  setTimeout(() => {
    document.getElementById('add')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }, 50)
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
          <RouterLink to="/" class="navlink" :class="{ active: route.path === '/' }">Home</RouterLink>
          <RouterLink to="/reminders" class="navlink" :class="{ active: route.path === '/reminders' }">
            Erinnerungen ({{ notifications.length }})
          </RouterLink>
          <button class="navlink as-btn" type="button" @click="goToAddAndFocus">Hinzufügen</button>
          <RouterLink to="/items" class="navlink" :class="{ active: route.path === '/items' }">Gegenstände</RouterLink>
        </nav>

        <div class="topbar-actions">
          <button class="btn btn-primary" type="button" @click="goToAddAndFocus">Start</button>
        </div>
      </div>
    </header>

    <!-- HOME: Hero + Tipps + Add bleibt auf / -->
    <template v-if="route.path === '/'">
      <!-- Hero -->
      <section class="hero">
        <div class="hero-inner">
          <div>
            <p class="pill">Weniger Chaos. Mehr Überblick.</p>
            <h1 class="hero-title">Entscheide smarter, ob dein Nachbar es besser gebrauchen könnte als der Mülleimer.</h1>
            <p class="hero-sub">
              Speichere und ordne deine Gegenstände – und bekomme Erinnerungen, wenn etwas fällig ist.
            </p>

            <div class="hero-cta">
              <button class="btn btn-primary" type="button" @click="goToAddAndFocus">Neuen Gegenstand anlegen</button>
              <RouterLink class="btn btn-ghost" to="/items">Liste ansehen ({{ liste.length }})</RouterLink>
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
        <!-- TIPPS / "Startseite Content" -->
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

        <!-- FORM (bleibt auf Home) -->
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

        <footer class="footer">
          <span>© {{ new Date().getFullYear() }} Can I Go?</span>
          <span class="dot">•</span>
          <span>Minimal UI · Vue + Spring Boot</span>
        </footer>
      </main>
    </template>

    <!-- ROUTED PAGES (Items / Reminders) -->
    <template v-else>
      <main class="content">
        <!-- wir geben wichtige Daten/Funktionen als Props an die Pages -->
        <RouterView
            :liste="liste"
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
/* ---------- Blade Runner 2049 Theme ---------- */
.page{
  --br-bg: #070A12;
  --br-ink: #E6F2FF;
  --br-muted: rgba(230,242,255,.72);

  --br-cyan: #29F3FF;
  --br-teal: #00B7B4;
  --br-amber: #FFB000;
  --br-orange: #FF6A00;
  --br-pink: #FF3DAE;

  --br-glass: rgba(10,14,28,.55);
  --br-glass2: rgba(10,14,28,.38);
  --br-border: rgba(41,243,255,.14);

  --br-shadow: 0 22px 70px rgba(0,0,0,.55);
  --br-glow-cyan: 0 0 0 1px rgba(41,243,255,.18), 0 0 22px rgba(41,243,255,.12);
  --br-glow-amber: 0 0 0 1px rgba(255,176,0,.18), 0 0 22px rgba(255,176,0,.10);

  min-height: 100vh;
  color: var(--br-ink);
  background:
      radial-gradient(1100px 700px at 18% 20%, rgba(41,243,255,.14), transparent 55%),
      radial-gradient(900px 650px at 82% 25%, rgba(255,176,0,.12), transparent 55%),
      radial-gradient(900px 700px at 55% 90%, rgba(255,61,174,.06), transparent 60%),
      linear-gradient(180deg, rgba(0,183,180,.06) 0%, rgba(255,106,0,.05) 55%, rgba(7,10,18,1) 100%),
      var(--br-bg);
}

/* ---------- Topbar ---------- */
.topbar{
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgba(7,10,18,.55);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--br-border);
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
  background: var(--br-ink);
  box-shadow: 0 0 0 6px rgba(41,243,255,.10);
}

.nav{
  display: flex;
  gap: 14px;
  align-items: center;
  flex-wrap: wrap;
}

.navlink{
  color: rgba(230,242,255,.72);
  text-decoration: none;
  font-size: 14px;
  padding: 8px 10px;
  border-radius: 999px;
  border: 1px solid transparent;
  background: transparent;
  line-height: 1;
}

.navlink:hover{
  background: rgba(41,243,255,.08);
  color: rgba(230,242,255,.92);
}

.navlink.active{
  border-color: rgba(41,243,255,.22);
  background: rgba(41,243,255,.08);
  box-shadow: 0 0 0 1px rgba(41,243,255,.10);
}

.navlink.as-btn{
  cursor: pointer;
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
  border: 1px solid rgba(230,242,255,.16);
  background: rgba(10,14,28,.45);
  color: var(--br-muted);
  font-size: 13px;
  margin: 0 0 14px;
}

.hero-title{
  margin: 0 0 10px;
  font-size: 54px;
  line-height: 1.05;
  letter-spacing: -0.03em;
  color: rgba(230,242,255,.98);
}

.hero-sub{
  margin: 0 0 16px;
  max-width: 62ch;
  color: var(--br-muted);
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
  border: 1px solid rgba(255,106,0,.25);
  background: rgba(255,106,0,.10);
  color: rgba(255,240,230,.92);
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* right hero card */
.hero-card{
  border-radius: 22px;
  border: 1px solid rgba(41,243,255,.14);
  background: rgba(10,14,28,.45);
  backdrop-filter: blur(12px);
  box-shadow: var(--br-shadow);
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
  color: var(--br-muted);
  font-weight: 600;
}

.mini-badge{
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(255,176,0,.18);
  background: rgba(255,176,0,.08);
  color: rgba(255,226,180,.95);
}

.hero-card-row{
  display:flex;
  justify-content: space-between;
  padding: 10px 0;
  border-top: 1px solid rgba(230,242,255,.10);
  color: var(--br-muted);
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
  border: 1px solid var(--br-border);
  background: var(--br-glass);
  backdrop-filter: blur(12px);
  box-shadow: var(--br-shadow);
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
  color: rgba(230,242,255,.96);
}

.panel p{
  margin: 6px 0 0;
  color: var(--br-muted);
  font-size: 13px;
}

/* ---------- Tips (Home only) ---------- */
.tips{
  display:grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.tip-card{
  border-radius: 20px;
  border: 1px solid rgba(230,242,255,.12);
  background: rgba(10,14,28,.35);
  padding: 14px;
  display:flex;
  gap: 12px;
  align-items:flex-start;
}

.tip-icon{
  width: 38px;
  height: 38px;
  border-radius: 14px;
  display:flex;
  align-items:center;
  justify-content:center;
  background: rgba(41,243,255,.10);
  border: 1px solid rgba(41,243,255,.18);
  box-shadow: var(--br-glow-cyan);
  flex: 0 0 auto;
  font-size: 18px;
}

.tip-card p{
  margin: 6px 0 0;
  color: var(--br-muted);
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
  color: var(--br-muted);
}

input, select{
  padding: 12px 12px;
  border-radius: 16px;
  border: 1px solid rgba(230,242,255,.16);
  background: rgba(230,242,255,.06);
  color: var(--br-ink);
  outline: none;
}

input:focus, select:focus{
  border-color: rgba(41,243,255,.30);
  box-shadow: var(--br-glow-cyan);
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
  border: 1px solid rgba(230,242,255,.14);
  cursor: pointer;
  user-select: none;
  font-size: 14px;
  background: transparent;
}

.btn-primary{
  background: linear-gradient(90deg, rgba(41,243,255,.22), rgba(255,176,0,.18));
  color: var(--br-ink);
  border-color: rgba(41,243,255,.22);
  box-shadow: var(--br-glow-cyan);
}

.btn-primary:hover{
  filter: brightness(1.08);
}

.btn-ghost{
  background: rgba(230,242,255,.06);
  color: rgba(230,242,255,.90);
  border-color: rgba(230,242,255,.14);
}

.btn-ghost:hover{
  background: rgba(41,243,255,.10);
  border-color: rgba(41,243,255,.18);
}

/* ---------- Footer ---------- */
.footer{
  margin-top: 8px;
  display:flex;
  justify-content:center;
  gap: 10px;
  color: rgba(230,242,255,.55);
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
  .tips{
    grid-template-columns: 1fr;
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