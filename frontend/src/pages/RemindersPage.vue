<script setup>
defineProps({
  notifications: { type: Array, default: () => [] },
  notifError: { type: String, default: '' },
  ladeNotifications: Function,
  markNotifSeen: Function,
  formatDateTime: Function
})
</script>

<template>
  <section class="panel">
    <div class="panel-head row">
      <div>
        <h2>Erinnerungen</h2>
        <p>{{ notifications.length }} ungelesen</p>
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
      <li v-for="n in notifications" :key="n.id" class="card">
        <div class="card-top">
          <div class="card-title">
            <strong>{{ n.message }}</strong>
            <span class="chip">UNGELESEN</span>
          </div>
          <div class="id">#{{ n.id }}</div>
        </div>

        <div class="card-body">
          <div class="kv"><span>Zeit</span><strong>{{ formatDateTime(n.createdAt) }}</strong></div>
          <div class="kv"><span>Gegenstand-ID</span><strong>{{ n.gegenstandId ?? '—' }}</strong></div>
        </div>

        <div class="actions" style="margin-top: 12px;">
          <button class="btn btn-primary" @click="markNotifSeen(n.id)">Gesehen</button>
        </div>
      </li>
    </ul>
  </section>
</template>