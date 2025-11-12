<script setup>
const props = defineProps({
  gegenstaende: { type: Array, default: () => [] }
})
</script>

<template>
  <section class="list-wrap">
    <h2 class="list-title">Deine Gegenstände</h2>

    <p v-if="props.gegenstaende.length === 0" class="empty">
      Keine Gegenstände vorhanden.
    </p>

    <ul v-else class="grid">
      <li v-for="g in props.gegenstaende" :key="g.id" class="card">
        <div class="title">{{ g.name }}</div>
        <div class="meta">
          <span>📍 {{ g.ort || '—' }}</span>
          <span>🛈 {{ g.status || '—' }}</span>
        </div>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.list-wrap{ width:100%; max-width:900px; padding:1rem }
.list-title{ text-align:center; margin-bottom:1rem; color:#2e7d32 }
.empty{ text-align:center; opacity:.8; margin-top:2rem }
.grid{ display:grid; grid-template-columns:repeat(auto-fill,minmax(240px,1fr));
  gap:1.2rem; list-style:none; padding:0; margin:0 }
.card{
  background:rgba(255,255,255,.55);
  backdrop-filter:blur(12px) saturate(120%); -webkit-backdrop-filter:blur(12px) saturate(120%);
  border:1px solid rgba(70,90,60,.15); border-radius:16px; padding:1rem;
  box-shadow:0 6px 16px rgba(56,82,61,.15); transition:.25s;
}
.card:hover{ transform:translateY(-4px); background:rgba(255,255,255,.75) }
.title{ font-weight:600; margin-bottom:.4rem; color:#1b3b22 }
.meta{ display:flex; gap:.5rem; flex-wrap:wrap; color:#3c4f3c }
</style>