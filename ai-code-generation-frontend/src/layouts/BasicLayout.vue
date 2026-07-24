<script setup lang="ts">
import { RouterView } from 'vue-router'

import GlobalFooter from '@/components/GlobalFooter.vue'
import GlobalHeader from '@/components/GlobalHeader.vue'
</script>

<template>
  <a-layout class="basic-layout">
    <GlobalHeader />

    <a-layout-content class="basic-layout__content">
      <div class="basic-layout__content-inner">
        <RouterView />
      </div>
    </a-layout-content>

    <GlobalFooter />
  </a-layout>
</template>

<style scoped>
.basic-layout {
  min-height: 100vh;
  background: #fbfcfe;
}

.basic-layout__content {
  display: flex;
  flex: 1;
  width: 100%;
  padding: 0;
}

.basic-layout__content-inner {
  width: 100%;
  margin: 0 auto;
}
</style>
