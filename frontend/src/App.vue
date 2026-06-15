<template>
  <main class="app-shell">
    <section class="hero">
      <p class="eyebrow">blog-planet</p>
      <h1>Admin UI build pipeline is connected.</h1>
      <p class="description">
        Vue assets will be bundled by Vite and copied into Spring static resources during the Gradle build.
      </p>
    </section>
  </main>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 32px;
  background: #f4f7fb;
  color: #172033;
  font-family: Arial, Helvetica, sans-serif;
}

.hero {
  width: min(720px, 100%);
  padding: 32px;
  background: #ffffff;
  border: 1px solid #d8e0ef;
  border-radius: 8px;
  box-shadow: 0 12px 32px rgba(23, 32, 51, 0.08);
}

.eyebrow {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 700;
  text-transform: uppercase;
  color: #4a6cf7;
}

h1 {
  margin: 0 0 12px;
  font-size: 32px;
  line-height: 1.2;
}

.description {
  margin: 0;
  font-size: 16px;
  line-height: 1.6;
  color: #4a5568;
}
</style>
