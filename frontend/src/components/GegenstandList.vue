<template>
  <section>
    <h2>Liste der Gegenstände</h2>

    <!-- v-for rendert alle Gegenstände -->
    <ul>
      <li v-for="g in gegenstaende" :key="g.id">
        <strong>{{ g.name }}</strong><br />
        Ort: {{ g.ort }}<br />
        Status: {{ g.status }}
      </li>
    </ul>

    <!-- Falls keine Daten vorhanden sind -->
    <p v-if="gegenstaende.length === 0">Keine Gegenstände vorhanden.</p>
  </section>
</template>

<script setup>
// Props = Daten, die von der Elternkomponente (App.vue) übergeben werden
defineProps({
  gegenstaende: {
    type: Array,
    default: () => []
  }
});
</script>

<style scoped>
ul {
  list-style-type: none;
  padding: 0;
  margin: 0;
}

li {
  margin-bottom: 0.5rem;
  padding: 0;
  background: transparent; /* keine Box mehr */
  color: inherit;          /* übernimmt Standardtextfarbe */
}
</style>

