<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
</script>

<template>
  <BasicLayout />
</template>

<style>
html,
body,
#app {
  min-height: 100%;
  margin: 0;
}

*,
*::before,
*::after {
  box-sizing: border-box;
}

#app {
  min-height: 100vh;
}

body {
  color: #1f2937;
  background: #fbfcfe;
  font-family:
    Inter, "PingFang SC", "Microsoft YaHei", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, "Helvetica Neue", Arial, sans-serif;
  text-rendering: optimizeLegibility;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
