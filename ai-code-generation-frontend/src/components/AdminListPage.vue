<script setup lang="ts">
withDefaults(
  defineProps<{
    title: string
    description: string
    listTitle: string
    total: number
    loading: boolean
    countUnit?: string
    maxWidth?: string
  }>(),
  {
    countUnit: '条记录',
    maxWidth: '1280px',
  },
)

const emit = defineEmits<{
  refresh: []
}>()
</script>

<template>
  <main class="admin-page">
    <div class="admin-page__inner" :style="{ maxWidth }">
      <header class="page-header">
        <div>
          <div class="page-header__eyebrow">
            <slot name="headerIcon" />
            ADMIN CONSOLE
          </div>
          <h1>{{ title }}</h1>
          <p>{{ description }}</p>
        </div>
        <div v-if="$slots.headerAction" class="page-header__action">
          <slot name="headerAction" />
        </div>
      </header>

      <section class="filter-panel" :aria-label="`${title}筛选`">
        <slot name="filters" />
      </section>

      <section class="table-panel" :aria-label="listTitle">
        <div class="table-panel__header">
          <div>
            <h2>{{ listTitle }}</h2>
            <span>共 {{ total }} {{ countUnit }}</span>
          </div>
          <a-button :loading="loading" @click="emit('refresh')">
            <template #icon><slot name="refreshIcon" /></template>
            刷新
          </a-button>
        </div>
        <slot />
      </section>

      <slot name="overlay" />
    </div>
  </main>
</template>

<style scoped>
.admin-page {
  min-height: calc(100vh - 164px);
  padding: 48px 32px 64px;
  background: #f5f7fb;
}

.admin-page__inner {
  width: 100%;
  margin: 0 auto;
}

.page-header {
  display: flex;
  gap: 24px;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 28px;
}

.page-header__eyebrow {
  display: flex;
  gap: 8px;
  align-items: center;
  color: #1677ff;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
}

.page-header h1 {
  margin: 8px 0 6px;
  color: #0f172a;
  font-size: 34px;
  font-weight: 700;
  letter-spacing: -0.03em;
}

.page-header p {
  margin: 0;
  color: #64748b;
}

.page-header__action :deep(.ant-btn) {
  height: 44px;
  border-radius: 10px;
}

.filter-panel,
.table-panel {
  background: #fff;
  border: 1px solid #e7ecf3;
  border-radius: 16px;
  box-shadow: 0 10px 34px rgba(38, 71, 132, 0.06);
}

.filter-panel {
  padding: 22px 24px;
  margin-bottom: 20px;
}

.table-panel {
  padding: 0 24px 24px;
  overflow: hidden;
}

.table-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 76px;
}

.table-panel__header > div {
  display: flex;
  gap: 12px;
  align-items: baseline;
}

.table-panel__header h2 {
  margin: 0;
  color: #172033;
  font-size: 18px;
  font-weight: 650;
}

.table-panel__header span {
  color: #94a3b8;
  font-size: 13px;
}

@media (max-width: 768px) {
  .admin-page {
    min-height: calc(100vh - 117px);
    padding: 30px 16px 48px;
  }

  .page-header {
    align-items: flex-start;
  }

  .page-header h1 {
    font-size: 28px;
  }

  .filter-panel,
  .table-panel {
    border-radius: 12px;
  }
}

@media (max-width: 576px) {
  .page-header {
    flex-direction: column;
  }

  .page-header__action,
  .page-header__action :deep(.ant-btn) {
    width: 100%;
  }

  .table-panel {
    padding: 0 14px 14px;
  }
}
</style>
