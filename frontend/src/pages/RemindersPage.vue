<script setup>
import { computed } from 'vue'

const props = defineProps({
  notifications: { type: Array, default: () => [] },
  notifError: { type: String, default: '' },
  ladeNotifications: Function,
  markNotifSeen: Function,
  formatDateTime: Function
})

/* --- Helpers --- */
function cleanMessage(msg) {
  // Entfernt "UNGELESEN" aus dem Text, falls es irgendwo doch drin ist
  return String(msg ?? '').replace(/\bUNGELESEN\b/gi, '').trim()
}

function isUnread(n) {
  // Standard: unread = !seen
  // Falls Backend anders: hier anpassen (z.B. return !n.gelesen)
  return !n?.seen
}

const unreadCount = computed(() => props.notifications.filter(isUnread).length)
</script>

<template>
  <section class="panel">
    <div class="panel-head row">
      <div>
        <h2>Erinnerungen</h2>
        <p>
          {{ unreadCount }} ungelesen
        </p>
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
      <li
          v-for="n in notifications"
          :key="n.id"
          class="card notif"
          :class="{ unread: isUnread(n) }"
      >
        <div class="card-top">
          <div class="card-title">
            <strong>{{ cleanMessage(n.message) }}</strong>

            <span v-if="isUnread(n)" class="badge-unread">Ungelesen</span>
            <span v-else class="badge-read">Gesehen</span>
          </div>

          <div class="id">#{{ n.id }}</div>
        </div>

        <div class="card-body notif-body">
          <div class="rowline">
            <span class="k">Zeit:</span>
            <span class="v"><strong>{{ formatDateTime(n.createdAt) }}</strong></span>
          </div>

          <div class="rowline">
            <span class="k">Gegenstand-ID:</span>
            <span class="v"><strong>{{ n.gegenstandId ?? '—' }}</strong></span>
          </div>
        </div>

        <div class="actions" style="margin-top: 12px;">
          <button
              v-if="isUnread(n)"
              class="btn btn-primary"
              @click="markNotifSeen(n.id)"
          >
            Gesehen
          </button>
        </div>
      </li>
    </ul>
  </section>
</template>

<style scoped>
/* --- Label/Wert Darstellung wie gewünscht --- */
.rowline{
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: 10px;
  align-items: baseline;
}
.k{
  color: rgba(230,242,255,.65);
  font-size: 13px;
}
.v{
  color: rgba(230,242,255,.95);
  font-weight: 600;
  font-size: 13px;
  overflow-wrap: anywhere;
}

/* --- Notification-Card Styling --- */
.notif-body{
  display: flex;
  flex-direction: column;
  gap: 6px;
}

/* --- Ungelesen Highlight (Glow) --- */
.card.unread{
  border-color: rgba(255, 240, 170, .35) !important;
  background: rgba(255, 240, 170, .06) !important;
  box-shadow:
      0 0 0 1px rgba(255, 240, 170, .20),
      0 0 26px rgba(255, 240, 170, .12);
}

/* --- Badges statt "UNGELESEN" Text --- */
.badge-unread{
  margin-left: 10px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  border: 1px solid rgba(255, 240, 170, .35);
  background: rgba(255, 240, 170, .12);
  color: rgba(230,242,255,.98);
  font-weight: 800;
}

.badge-read{
  margin-left: 10px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  border: 1px solid rgba(230,242,255,.16);
  background: rgba(230,242,255,.06);
  color: rgba(230,242,255,.8);
}
</style>