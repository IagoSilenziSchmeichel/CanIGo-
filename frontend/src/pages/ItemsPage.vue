<script setup>
import { computed, reactive, ref, unref } from 'vue'
import { apiFetch } from '../api' // oder '@/api' je nachdem

const props = defineProps({
  liste: { type: Array, default: () => [] },
  ladeDaten: Function,
  searchQuery: { type: [String, Object], default: '' }
})

/* ---------- Suche ---------- */
const q = computed(() => String(unref(props.searchQuery) ?? '').trim().toLowerCase())
const filtered = computed(() => {
  if (!q.value) return props.liste
  return props.liste.filter(g => String(g?.name ?? '').toLowerCase().includes(q.value))
})

/* ---------- Anzeige Helpers ---------- */
function moneyOrDash(v) {
  if (v === null || v === undefined || v === '') return '—'
  const n = Number(v)
  if (Number.isNaN(n)) return '—'
  return new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR' }).format(n)
}
function dateOrDash(v) {
  return v ? v : '—'
}

/* ---------- Sortierung Wichtigkeit ---------- */
const prioRank = (w) => {
  if (w === 'WICHTIG') return 0
  if (w === 'MITTEL') return 1
  if (w === 'UNWICHTIG') return 2
  return 9
}

function sortByWichtigkeitThenName(a, b) {
  const ra = prioRank(a?.wichtigkeit)
  const rb = prioRank(b?.wichtigkeit)
  if (ra !== rb) return ra - rb
  return String(a?.name ?? '').localeCompare(String(b?.name ?? ''), 'de')
}

/* ---------- Kategorien ---------- */
const CATEGORY_ORDER = [
  { key: 'HAUSHALT', label: '🏠 Haushalt' },
  { key: 'DOKUMENTE', label: '📄 Dokumente' },
  { key: 'REISE', label: '🧳 Reise' },
  { key: 'TECH', label: '💻 Tech' },
  { key: 'SONSTIGES', label: '🧩 Sonstiges' }
]

/**
 * Baut pro Kategorie 3 Gruppen:
 * - WICHTIG
 * - MITTEL
 * - UNWICHTIG
 */
const categorized = computed(() => {
  const src = [...filtered.value]

  const byCategory = new Map()
  for (const cat of CATEGORY_ORDER) {
    byCategory.set(cat.key, {
      ...cat,
      groups: {
        WICHTIG: [],
        MITTEL: [],
        UNWICHTIG: []
      }
    })
  }

  // Falls Backend mal eine "unbekannte" Kategorie liefert:
  const ensureCategory = (key) => {
    if (!byCategory.has(key)) {
      byCategory.set(key, {
        key,
        label: key,
        groups: { WICHTIG: [], MITTEL: [], UNWICHTIG: [] }
      })
    }
    return byCategory.get(key)
  }

  for (const item of src) {
    const catKey = item?.kategorie ?? 'SONSTIGES'
    const groupKey = item?.wichtigkeit ?? 'MITTEL'
    const bucket = ensureCategory(catKey)
    if (!bucket.groups[groupKey]) bucket.groups[groupKey] = []
    bucket.groups[groupKey].push(item)
  }

  // Sortierung innerhalb jeder Wichtigkeitsgruppe
  for (const bucket of byCategory.values()) {
    bucket.groups.WICHTIG.sort(sortByWichtigkeitThenName)
    bucket.groups.MITTEL.sort(sortByWichtigkeitThenName)
    bucket.groups.UNWICHTIG.sort(sortByWichtigkeitThenName)
  }

  // In gewünschter Reihenfolge ausgeben
  const ordered = []
  for (const cat of CATEGORY_ORDER) ordered.push(byCategory.get(cat.key))

  // Unbekannte Kategorien hinten anhängen
  for (const [key, value] of byCategory.entries()) {
    if (!CATEGORY_ORDER.some(c => c.key === key)) ordered.push(value)
  }

  return ordered
})

/* ---------- Edit Modal State ---------- */
const showEdit = ref(false)
const busy = ref(false)
const error = ref('')

const editForm = reactive({
  id: null,
  name: '',
  ort: '',
  wichtigkeit: 'WICHTIG',
  kategorie: 'HAUSHALT',
  lastUsed: '',
  wegwerfAm: '',
  kaufpreis: '',
  wunschVerkaufspreis: ''
})

function openEdit(item) {
  error.value = ''
  editForm.id = item.id
  editForm.name = item.name ?? ''
  editForm.ort = item.ort ?? ''
  editForm.wichtigkeit = item.wichtigkeit ?? 'WICHTIG'
  editForm.kategorie = item.kategorie ?? 'HAUSHALT'
  editForm.lastUsed = item.lastUsed ?? ''
  editForm.wegwerfAm = item.wegwerfAm ?? ''
  editForm.kaufpreis = item.kaufpreis ?? ''
  editForm.wunschVerkaufspreis = item.wunschVerkaufspreis ?? ''
  showEdit.value = true
}
function closeEdit() {
  showEdit.value = false
}

/* ---------- API Calls ---------- */
async function saveEdit() {
  error.value = ''

  if (!editForm.name.trim() || !editForm.ort.trim()) {
    error.value = 'Bitte Name und Ort ausfüllen.'
    return
  }

  const payload = {
    name: editForm.name.trim(),
    ort: editForm.ort.trim(),
    wichtigkeit: editForm.wichtigkeit,
    kategorie: editForm.kategorie,
    lastUsed: editForm.lastUsed ? editForm.lastUsed : null,
    wegwerfAm: editForm.wegwerfAm ? editForm.wegwerfAm : null,
    kaufpreis: editForm.kaufpreis !== '' ? Number(editForm.kaufpreis) : null,
    wunschVerkaufspreis: editForm.wunschVerkaufspreis !== '' ? Number(editForm.wunschVerkaufspreis) : null
  }

  busy.value = true
  try {
    await apiFetch(`/gegenstaende/${editForm.id}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    })

    closeEdit()
    await props.ladeDaten?.()
  } catch (e) {
    error.value = String(e?.message || e)
  } finally {
    busy.value = false
  }
}

async function deleteItem(item) {
  error.value = ''
  const ok = window.confirm(`Wirklich löschen?\n\n${item.name} (#${item.id})`)
  if (!ok) return

  busy.value = true
  try {
    await apiFetch(`/gegenstaende/${item.id}`, { method: 'DELETE' })
    await props.ladeDaten?.()
  } catch (e) {
    error.value = String(e?.message || e)
  } finally {
    busy.value = false
  }
}

/* ---------- Kleine Helper fürs Template ---------- */
const groupTitle = (key) => {
  if (key === 'WICHTIG') return 'Wichtig'
  if (key === 'MITTEL') return 'Mittel'
  if (key === 'UNWICHTIG') return 'Unwichtig'
  return key
}
</script>

<template>
  <section class="panel">
    <div class="panel-head row">
      <div>
        <h2>Gegenstände</h2>
        <p>
          {{ filtered.length }} Treffer
          <span v-if="q">für „{{ q }}“</span>
        </p>
        <p v-if="error" class="err">{{ error }}</p>
      </div>

      <button class="btn btn-ghost" :disabled="busy" @click="ladeDaten">Neu laden</button>
    </div>

    <div v-if="filtered.length === 0" class="empty">
      <div class="empty-card">
        <strong>Keine Treffer.</strong>
        <p v-if="q">Ändere den Suchbegriff oder lösche ihn.</p>
        <p v-else>Lege auf der Startseite deinen ersten Gegenstand an.</p>
      </div>
    </div>

    <div v-else class="lists">
      <section v-for="cat in categorized" :key="cat.key" class="subpanel">
        <div class="subhead">
          <h3>{{ cat.label }}</h3>
          <span class="count">
            {{ (cat.groups.WICHTIG.length + cat.groups.MITTEL.length + cat.groups.UNWICHTIG.length) }}
          </span>
        </div>

        <!-- Gruppen innerhalb Kategorie -->
        <div class="prio-sections">
          <div v-for="prio in ['WICHTIG','MITTEL','UNWICHTIG']" :key="prio" class="prio-block">
            <div class="prio-head">
              <span class="prio-title">{{ groupTitle(prio) }}</span>
              <span class="prio-count">{{ cat.groups[prio].length }}</span>
            </div>

            <div v-if="cat.groups[prio].length === 0" class="muted">Keine Gegenstände.</div>

            <ul v-else class="grid">
              <li v-for="g in cat.groups[prio]" :key="g.id" class="card">
                <div class="card-top">
                  <div class="card-title">
                    <strong>{{ g.name }}</strong>

                    <!-- ✅ Chips mit Abstand -->
                    <span class="chips">
                      <span class="chip" :class="`prio-${String(g.wichtigkeit||'').toLowerCase()}`">
                        {{ g.wichtigkeit }}
                      </span>
                      <span class="chip ghost">{{ g.kategorie }}</span>
                    </span>
                  </div>

                  <div class="id">#{{ g.id }}</div>
                </div>

                <div class="card-body">
                  <div class="kv"><span>Ort</span><strong>{{ g.ort }}</strong></div>
                  <div class="kv"><span>Zuletzt</span><strong>{{ dateOrDash(g.lastUsed) }}</strong></div>
                  <div class="kv"><span>Wegwerf am</span><strong>{{ dateOrDash(g.wegwerfAm) }}</strong></div>
                  <div class="kv"><span>Kaufpreis</span><strong>{{ moneyOrDash(g.kaufpreis) }}</strong></div>
                  <div class="kv"><span>Wunschpreis</span><strong>{{ moneyOrDash(g.wunschVerkaufspreis) }}</strong></div>
                </div>

                <div class="card-actions">
                  <button class="btn btn-ghost" :disabled="busy" @click="openEdit(g)">Bearbeiten</button>
                  <button class="btn btn-danger" :disabled="busy" @click="deleteItem(g)">Löschen</button>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </section>
    </div>
  </section>

  <!-- EDIT MODAL -->
  <div v-if="showEdit" class="modal-overlay" @click.self="closeEdit">
    <div class="modal">
      <div class="modal-head">
        <h3>Gegenstand bearbeiten</h3>
        <button class="btn btn-ghost" @click="closeEdit">✕</button>
      </div>

      <p v-if="error" class="err">{{ error }}</p>

      <div class="form-grid">
        <label class="field">
          <span>Name*</span>
          <input v-model="editForm.name" />
        </label>

        <label class="field">
          <span>Ort*</span>
          <input v-model="editForm.ort" />
        </label>

        <label class="field">
          <span>Wichtigkeit</span>
          <select v-model="editForm.wichtigkeit">
            <option value="WICHTIG">Wichtig</option>
            <option value="MITTEL">Mittel</option>
            <option value="UNWICHTIG">Unwichtig</option>
          </select>
        </label>

        <label class="field">
          <span>Kategorie</span>
          <select v-model="editForm.kategorie">
            <option value="HAUSHALT">Haushalt</option>
            <option value="DOKUMENTE">Dokumente</option>
            <option value="REISE">Reise</option>
            <option value="TECH">Tech</option>
            <option value="SONSTIGES">Sonstiges</option>
          </select>
        </label>

        <label class="field">
          <span>Zuletzt benutzt</span>
          <input v-model="editForm.lastUsed" type="date" />
        </label>

        <label class="field">
          <span>Wegwerf-Datum</span>
          <input v-model="editForm.wegwerfAm" type="date" />
        </label>

        <label class="field">
          <span>Kaufpreis (€)</span>
          <input v-model="editForm.kaufpreis" type="number" step="0.01" />
        </label>

        <label class="field">
          <span>Wunsch-Verkaufspreis (€)</span>
          <input v-model="editForm.wunschVerkaufspreis" type="number" step="0.01" />
        </label>
      </div>

      <div class="modal-actions">
        <button class="btn btn-ghost" :disabled="busy" @click="closeEdit">Abbrechen</button>
        <button class="btn btn-primary" :disabled="busy" @click="saveEdit">
          {{ busy ? 'Speichert…' : 'Speichern' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.err { color: #ff8a8a; margin: 8px 0 0; }

.lists { display: flex; flex-direction: column; gap: 14px; }
.subpanel { border-radius: 18px; border: 1px solid rgba(230,242,255,.10); background: rgba(10,14,28,.30); padding: 14px; }
.subhead { display:flex; align-items:center; justify-content:space-between; margin-bottom:10px; }
.count { font-size: 12px; padding: 4px 10px; border-radius: 999px; border: 1px solid rgba(41,243,255,.18); background: rgba(41,243,255,.08); }
.muted { color: rgba(230,242,255,.55); font-size: 13px; }

/* ✅ Gruppenlayout innerhalb Kategorie */
.prio-sections{
  display:flex;
  flex-direction: column;
  gap: 12px;
}

.prio-block{
  border-radius: 16px;
  border: 1px solid rgba(230,242,255,.10);
  background: rgba(10,14,28,.20);
  padding: 12px;
}

.prio-head{
  display:flex;
  align-items:center;
  justify-content:space-between;
  margin-bottom: 10px;
}

.prio-title{
  font-weight: 800;
  color: rgba(230,242,255,.9);
}

.prio-count{
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(230,242,255,.14);
  background: rgba(230,242,255,.06);
}

/* ✅ Chips: Abstand + Zeilenumbruch */
.chips{
  display: inline-flex;
  gap: 8px;            /* wichtig: damit NICHT "WICHTIGDOKUMENTE" entsteht */
  flex-wrap: wrap;
  align-items: center;
  margin-left: 10px;
}

.chip{
  display:inline-flex;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  border: 1px solid rgba(41,243,255,.18);
  background: rgba(41,243,255,.08);
}

.chip.ghost{
  border-color: rgba(230,242,255,.14);
  background: rgba(230,242,255,.06);
  color: rgba(230,242,255,.88);
}

/* Optional: Wichtigkeit farblich subtil unterscheiden */
.chip.prio-wichtig{ border-color: rgba(255,176,0,.22); background: rgba(255,176,0,.10); }
.chip.prio-mittel{ border-color: rgba(41,243,255,.18); background: rgba(41,243,255,.08); }
.chip.prio-unwichtig{ border-color: rgba(255,61,174,.18); background: rgba(255,61,174,.08); }

.card-actions{
  display:flex;
  justify-content:flex-end;
  gap: 10px;
  margin-top: 12px;
}

.btn-danger{
  border-color: rgba(255,106,0,.28);
  background: rgba(255,106,0,.10);
  color: rgba(255,240,230,.92);
}
.btn-danger:hover{ filter: brightness(1.08); }

/* Modal */
.modal-overlay{
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.55);
  display:flex;
  align-items:center;
  justify-content:center;
  padding: 18px;
  z-index: 999;
}
.modal{
  width: min(820px, 100%);
  border-radius: 22px;
  border: 1px solid rgba(41,243,255,.14);
  background: rgba(10,14,28,.88);
  backdrop-filter: blur(14px);
  padding: 16px;
  box-shadow: 0 22px 70px rgba(0,0,0,.55);
}
.modal-head{
  display:flex;
  align-items:center;
  justify-content:space-between;
  margin-bottom: 10px;
}
.modal-actions{
  display:flex;
  justify-content:flex-end;
  gap: 10px;
  margin-top: 14px;
}

/* Form Grid im Modal */
.form-grid{
  display:grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}
.field{ display:flex; flex-direction:column; gap: 8px; font-size: 13px; color: rgba(230,242,255,.72); }
input, select{ padding: 12px; border-radius: 16px; border: 1px solid rgba(230,242,255,.16); background: rgba(230,242,255,.06); color: rgba(230,242,255,.95); outline: none; }

@media (max-width: 720px){
  .form-grid{ grid-template-columns: 1fr; }
}
</style>