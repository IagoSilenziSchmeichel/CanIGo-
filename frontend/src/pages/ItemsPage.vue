<script setup>
import { computed, reactive, ref, unref } from 'vue'
import { apiFetch } from '../api'

// WICHTIG: App.vue übergibt "liste", nicht "gegenstaende"
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

/* ---------- Kategorisierung ---------- */
function actionOf(item) {
  if (item?.wegwerfAm) return 'WEGWERFEN'
  const wunsch = Number(item?.wunschVerkaufspreis)
  if (wunsch > 0) return 'VERKAUFEN'
  return 'SPENDEN'
}

const spenden = computed(() => filtered.value.filter(x => actionOf(x) === 'SPENDEN'))
const verkaufen = computed(() => filtered.value.filter(x => actionOf(x) === 'VERKAUFEN'))
const wegwerfen = computed(() => filtered.value.filter(x => actionOf(x) === 'WEGWERFEN'))

/* ---------- Helpers ---------- */
function moneyOrDash(v) {
  if (v === null || v === undefined || v === '') return '—'
  const n = Number(v)
  return isNaN(n) ? '—' : new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR' }).format(n)
}
function dateOrDash(v) { return v ? v : '—' }

/* ---------- Edit Modal State ---------- */
const showEdit = ref(false)
const busy = ref(false)
const error = ref('')

const editForm = reactive({
  id: null, name: '', ort: '', wichtigkeit: 'WICHTIG', kategorie: 'HAUSHALT',
  lastUsed: '', wegwerfAm: '', kaufpreis: '', wunschVerkaufspreis: ''
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

function closeEdit() { showEdit.value = false }

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
    lastUsed: editForm.lastUsed || null,
    wegwerfAm: editForm.wegwerfAm || null,
    kaufpreis: editForm.kaufpreis ? Number(editForm.kaufpreis) : null,
    wunschVerkaufspreis: editForm.wunschVerkaufspreis ? Number(editForm.wunschVerkaufspreis) : null
  }

  busy.value = true
  try {
    await apiFetch(`/gegenstaende/${editForm.id}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    })
    closeEdit()
    // Daten in App.vue neu laden
    if (props.ladeDaten) await props.ladeDaten()
  } catch (e) {
    error.value = String(e?.message || e)
  } finally {
    busy.value = false
  }
}

async function deleteItem(item) {
  if (!confirm(`Wirklich löschen?\n${item.name}`)) return
  busy.value = true
  try {
    await apiFetch(`/gegenstaende/${item.id}`, { method: 'DELETE' })
    if (props.ladeDaten) await props.ladeDaten()
  } catch (e) {
    alert(e.message)
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <section class="panel">
    <div class="panel-head row">
      <div>
        <h2>Gegenstände</h2>
        <p>{{ filtered.length }} Treffer <span v-if="q">für „{{ q }}“</span></p>
      </div>
      <button class="btn btn-ghost" :disabled="busy" @click="ladeDaten">Neu laden</button>
    </div>

    <!-- Listen-Ansicht -->
    <div v-if="filtered.length === 0" class="empty-msg">
      Keine Gegenstände gefunden.
    </div>

    <div v-else class="lists">
      <!-- Helper Component für eine Kategorie -->
      <template v-for="(group, title) in { '♻️ Spenden': spenden, '💶 Verkaufen': verkaufen, '🗑️ Wegwerfen': wegwerfen }" :key="title">
        <section class="subpanel" v-if="group.length > 0">
          <div class="subhead"><h3>{{ title }}</h3> <span class="count">{{ group.length }}</span></div>
          <ul class="grid">
            <li v-for="g in group" :key="g.id" class="card">
              <div class="card-top">
                <strong>{{ g.name }}</strong>
                <div class="chips">
                  <span class="chip">{{ g.wichtigkeit }}</span>
                  <span class="chip ghost">{{ g.kategorie }}</span>
                </div>
              </div>
              <div class="card-body">
                <div class="kv"><span>Ort:</span> {{ g.ort }}</div>
                <div v-if="g.wunschVerkaufspreis" class="kv"><span>Wunsch:</span> {{ moneyOrDash(g.wunschVerkaufspreis) }}</div>
                <div v-if="g.wegwerfAm" class="kv"><span>Wegwerfen:</span> {{ dateOrDash(g.wegwerfAm) }}</div>
              </div>
              <div class="card-actions">
                <button class="btn btn-ghost small" @click="openEdit(g)">Edit</button>
                <button class="btn btn-danger small" @click="deleteItem(g)">Löschen</button>
              </div>
            </li>
          </ul>
        </section>
      </template>
    </div>

    <!-- Edit Modal -->
    <div v-if="showEdit" class="modal-overlay" @click.self="closeEdit">
      <div class="modal">
        <h3>Bearbeiten</h3>
        <p v-if="error" class="err">{{ error }}</p>
        <div class="form-grid">
          <!-- Felder hier analog zu deinem Code (gekürzt für Übersicht) -->
          <label>Name <input v-model="editForm.name"></label>
          <label>Ort <input v-model="editForm.ort"></label>
          <label>Wunschpreis <input type="number" step="0.01" v-model="editForm.wunschVerkaufspreis"></label>
          <!-- ... weitere Felder ... -->
        </div>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="closeEdit">Abbrechen</button>
          <button class="btn btn-primary" @click="saveEdit">Speichern</button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
/* Hier deine CSS Styles aus Block 3 einfügen */
.lists { display: flex; flex-direction: column; gap: 20px; }
.subpanel { background: rgba(255,255,255,0.05); padding: 15px; border-radius: 12px; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 10px; list-style: none; padding: 0; }
.card { background: rgba(0,0,0,0.3); padding: 10px; border-radius: 8px; border: 1px solid rgba(255,255,255,0.1); }
.card-top { display: flex; justify-content: space-between; margin-bottom: 8px; }
.card-actions { margin-top: 10px; display: flex; gap: 5px; justify-content: flex-end; }
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.8); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #1a1e2e; padding: 20px; border-radius: 12px; width: 100%; max-width: 600px; border: 1px solid #29F3FF; }
.form-grid { display: grid; gap: 10px; margin: 20px 0; }
input { background: rgba(255,255,255,0.1); border: 1px solid #444; color: white; padding: 8px; border-radius: 4px; }
.err { color: #ff6b6b; }
</style>