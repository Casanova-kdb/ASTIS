<template>
  <main class="app-shell">
    <aside class="sidebar">
      <h1>ASTIS</h1>
      <nav>
        <RouterLink to="/">Dashboard</RouterLink>
        <RouterLink to="/tasks">Tasks</RouterLink>
        <RouterLink to="/recommendations">Recommendations</RouterLink>
      </nav>
    </aside>

    <section class="content">
      <RouterView />
    </section>
  </main>
</template>
