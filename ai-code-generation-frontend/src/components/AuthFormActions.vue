<script setup lang="ts">
import type { RouteLocationRaw } from 'vue-router'

defineProps<{
  hint: string
  linkText: string
  linkTo: RouteLocationRaw
  submitText: string
  loading: boolean
}>()
</script>

<template>
  <div class="form-meta">
    <span>{{ hint }}</span>
    <RouterLink :to="linkTo">{{ linkText }}</RouterLink>
  </div>

  <a-button class="submit-button" type="primary" html-type="submit" :loading="loading" block>
    {{ submitText }}
  </a-button>
</template>

<style scoped>
.form-meta {
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: space-between;
  margin: 4px 0 24px;
  color: #94a3b8;
  font-size: 13px;
}

.submit-button {
  height: 48px;
  font-weight: 600;
  border-radius: 10px;
  box-shadow: 0 10px 24px rgba(22, 119, 255, 0.2);
}

@media (max-width: 576px) {
  .form-meta {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
