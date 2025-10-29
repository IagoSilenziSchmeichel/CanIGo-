<script setup>
import HelloWorld from './components/HelloWorld.vue'
import TheWelcome from './components/TheWelcome.vue'

// NEU: Unterkomponente + State + Fetch
import GegenstandList from './components/GegenstandList.vue'
import { ref, onMounted } from 'vue'

const liste = ref([])

onMounted(async () => {
  try {
    const res = await fetch('http://localhost:8080/gegenstaende')
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    liste.value = await res.json()
  } catch (err) {
    console.error('Fehler beim Laden / Fallback auf leere Liste:', err)
    liste.value = []
  }
})
</script>

<template>
  <header>
    <img alt="Vue logo" class="logo" src="./assets/logo.svg" width="125" height="125" />

    <div class="wrapper">
      <HelloWorld msg="You did it!" />
    </div>
  </header>

  <main>
    <!-- Belassen: Starter-Komponente -->
    <TheWelcome />

    <!-- NEU: Gegenstand-Liste mit v-for -->
    <section style="margin-top: 2rem;">
      <h1>Gegenstände</h1>
      <GegenstandList :gegenstaende="liste" />
    </section>
  </main>
</template>

<style scoped>
header {
  line-height: 1.5;
}

.logo {
  display: block;
  margin: 0 auto 2rem;
}

@media (min-width: 1024px) {
  header {
    display: flex;
    place-items: center;
    padding-right: calc(var(--section-gap) / 2);
  }

  .logo {
    margin: 0 2rem 0 0;
  }

  header .wrapper {
    display: flex;
    place-items: flex-start;
    flex-wrap: wrap;
  }
}
</style>
