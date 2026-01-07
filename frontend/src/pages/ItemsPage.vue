<script setup>
defineProps({
  liste: { type: Array, default: () => [] },
  ladeDaten: Function
})
</script>

<template>
  <section class="panel">
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
        <p>Lege auf der Startseite deinen ersten Gegenstand an.</p>
      </div>
    </div>

    <ul v-else class="grid">
      <li v-for="g in liste" :key="g.id" class="card">
        <div class="card-top">
          <div class="card-title">
            <strong>{{ g.name }}</strong>
            <span class="chip">{{ g.wichtigkeit }}</span>
            <span class="chip ghost">{{ g.kategorie }}</span>
          </div>
          <div class="id">#{{ g.id }}</div>
        </div>

        <div class="card-body">
          <div class="kv"><span>Ort</span><strong>{{ g.ort }}</strong></div>
          <div class="kv"><span>Zuletzt</span><strong>{{ g.lastUsed ?? '—' }}</strong></div>
          <div class="kv"><span>Wegwerf am</span><strong>{{ g.wegwerfAm ?? '—' }}</strong></div>
          <div class="kv"><span>Kaufpreis</span><strong>{{ g.kaufpreis ?? '—' }}</strong></div>
          <div class="kv"><span>Wunschpreis</span><strong>{{ g.wunschVerkaufspreis ?? '—' }}</strong></div>
        </div>
      </li>
    </ul>
  </section>
</template>