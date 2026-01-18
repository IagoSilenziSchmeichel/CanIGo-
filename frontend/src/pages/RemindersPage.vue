<script setup>
import { computed } from 'vue'

const props = defineProps({
  notifications: { type: Array, default: () => [] },
  notifError: { type: String, default: '' },
  ladeNotifications: Function,
  markNotifSeen: Function,
  formatDateTime: Function
})

const unreadCount = computed(() => props.notifications?.length ?? 0)
</script>

<template>
  <section class="panel">
    <div class="panel-head row">
      <div>
        <h2>Erinnerungen</h2>
        <p>{{ unreadCount }} ungelesen</p>
      </div>
      <button class="btn btn-ghost" @click="ladeNotifications">Neu laden</button>
    </div>

    <div v-if="notifError" class="alert">
      <strong>Fehler:</strong>
      <span>{{ notifError }}</span>
    </div>

    <div v-if="unreadCount === 0" class="empty">
      <div class="empty-card">
        <strong>Keine neuen Erinnerungen 🎉</strong>
        <p>Wenn ein Gegenstand fällig ist, erscheint hier eine Benachrichtigung.</p>
      </div>
    </div>

    <ul v-else class="grid">
      <li v-for="n in notifications" :key="n.id" class="card unread">
        <div class="card-top">
          <div class="title-wrap">
            <strong class="title">{{ n.message }}</strong>
            <span class="chip chip-unread">Ungelesen</span>
          </div>
          <div class="id">#{{ n.id }}</div>
        </div>

        <div class="card-body">
          <div class="kv">
            <span>Zeit</span>
            <strong>{{ formatDateTime(n.createdAt) }}</strong>
          </div>
          <div class="kv">
            <span>Gegenstand-ID</span>
            <strong>{{ n.gegenstandId ?? '—' }}</strong>
          </div>
        </div>

        <div class="card-actions">
          <button class="btn btn-primary" @click="markNotifSeen(n.id)">Gesehen</button>
        </div>
      </li>
    </ul>
  </section>
</template>

<style scoped>
/* Layout */
.grid{
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

@media (max-width: 900px){
  .grid{ grid-template-columns: 1fr; }
}

/* Card */
.card{
  border-radius: 18px;
  border: 1px solid rgba(230,242,255,.10);
  background: rgba(10,14,28,.22);
  padding: 14px;
  overflow: hidden; /* wichtig: nichts ragt raus */
}

.card-top{
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 10px;
}

.title-wrap{
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap; /* verhindert rauslaufen */
  min-width: 0;
}

.title{
  display: inline-block;
  max-width: 100%;
  word-break: break-word;
}

.id{
  opacity: .7;
  font-size: 12px;
  white-space: nowrap;
}

/* Unread = visuell hervorheben */
.unread{
  border-color: rgba(255, 240, 170, .30);
  background: rgba(255, 240, 170, .05);
  box-shadow:
      0 0 0 1px rgba(255, 240, 170, .18),
      0 0 26px rgba(255, 240, 170, .10);
}

/* Chip */
.chip{
  display: inline-flex;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  border: 1px solid rgba(230,242,255,.14);
  background: rgba(230,242,255,.06);
  color: rgba(230,242,255,.88);
  white-space: nowrap;
}

.chip-unread{
  border-color: rgba(255, 240, 170, .26);
  background: rgba(255, 240, 170, .14);
  color: rgba(230,242,255,.98);
}

/* KV Zeilen mit : */
.kv{
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: 10px;
  align-items: baseline;
  padding: 2px 0;
}

.kv span{
  color: rgba(230,242,255,.72);
  white-space: nowrap;
}
.kv span::after{
  content: ":";
  margin-left: 2px;
  color: rgba(230,242,255,.55);
}
.kv strong{
  color: rgba(230,242,255,.95);
}

/* Actions sauber in Box */
.card-actions{
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px solid rgba(230,242,255,.08);
}

/* Deine bestehenden Buttons nutzen, nur falls nötig: */
.btn{ border-radius: 999px; }
</style>