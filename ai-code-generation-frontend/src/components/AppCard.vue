<script setup lang="ts">
import { computed } from 'vue'
import { DeleteOutlined, EditOutlined, EyeOutlined, UserOutlined } from '@ant-design/icons-vue'

import logoUrl from '@/assets/logo.png'
import type { AppVO } from '@/types/app'

const props = withDefaults(
  defineProps<{
    app: AppVO
    editable?: boolean
  }>(),
  { editable: false },
)

const emit = defineEmits<{
  open: [app: AppVO]
  edit: [app: AppVO]
  delete: [app: AppVO]
}>()

const title = computed(() => props.app.appName || '未命名应用')
const creatorName = computed(
  () => props.app.user?.userName || props.app.user?.userAccount || '平台用户',
)
</script>

<template>
  <article class="app-card">
    <button class="app-card__preview" type="button" @click="emit('open', app)">
      <img v-if="app.cover" :src="app.cover" :alt="`${title}预览图`" />
      <span v-else class="app-card__fallback">
        <img :src="logoUrl" alt="" />
        <span>{{ title }}</span>
      </span>
      <span class="app-card__view"><EyeOutlined /> 查看应用</span>
    </button>

    <div class="app-card__body">
      <div class="app-card__info">
        <a-avatar
          class="app-card__avatar"
          :size="48"
          :src="app.user?.userAvatar"
          :alt="creatorName"
        >
          <template v-if="!app.user?.userAvatar" #icon><UserOutlined /></template>
        </a-avatar>

        <div class="app-card__info-content">
          <div class="app-card__title-row">
            <h3>{{ title }}</h3>
            <a-tag v-if="(app.priority ?? 0) > 0" color="gold">精选</a-tag>
          </div>
          <p class="app-card__creator">{{ creatorName }}</p>
        </div>
      </div>

      <div class="app-card__meta">
        <span>{{ app.codeGenType === 'multi_file' ? '多文件应用' : '网页应用' }}</span>
        <span v-if="app.deployedTime" class="app-card__deployed">已部署</span>
      </div>

      <div v-if="editable" class="app-card__actions">
        <a-button type="text" size="small" @click="emit('edit', app)">
          <template #icon><EditOutlined /></template>
          编辑
        </a-button>
        <a-button type="text" danger size="small" @click="emit('delete', app)">
          <template #icon><DeleteOutlined /></template>
          删除
        </a-button>
      </div>
    </div>
  </article>
</template>

<style scoped>
.app-card {
  min-width: 0;
  overflow: hidden;
  background: #fff;
  border: 1px solid #e7ecf3;
  border-radius: 18px;
  box-shadow: 0 12px 32px rgba(38, 71, 132, 0.07);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.app-card:hover {
  box-shadow: 0 18px 44px rgba(38, 71, 132, 0.13);
  transform: translateY(-4px);
}

.app-card__preview {
  position: relative;
  display: block;
  width: 100%;
  aspect-ratio: 16 / 9;
  padding: 0;
  overflow: hidden;
  cursor: pointer;
  background: #f6f8fb;
  border: 0;
}

.app-card__preview > img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
}

.app-card:hover .app-card__preview > img {
  transform: scale(1.025);
}

.app-card__fallback {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #3c4a62;
  background: url('@/assets/hero-background.webp') center / cover no-repeat;
}

.app-card__fallback img {
  width: 58px;
  height: 58px;
}

.app-card__fallback span {
  max-width: 80%;
  overflow: hidden;
  font-size: 16px;
  font-weight: 650;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-card__view {
  position: absolute;
  right: 14px;
  bottom: 14px;
  display: inline-flex;
  gap: 6px;
  align-items: center;
  padding: 7px 12px;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  visibility: hidden;
  background: rgba(15, 23, 42, 0.76);
  border-radius: 20px;
  opacity: 0;
  transition:
    opacity 0.2s ease,
    visibility 0.2s ease;
}

.app-card:hover .app-card__view,
.app-card__preview:focus-visible .app-card__view {
  visibility: visible;
  opacity: 1;
}

.app-card__body {
  padding: 18px 18px 14px;
}

.app-card__info {
  display: flex;
  gap: 13px;
  align-items: center;
}

.app-card__avatar {
  flex: 0 0 auto;
  color: #4f6f9d;
  background: #edf4ff;
  border: 2px solid #fff;
  box-shadow: 0 4px 12px rgba(38, 71, 132, 0.14);
}

.app-card__info-content {
  min-width: 0;
  flex: 1;
}

.app-card__title-row {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: space-between;
}

.app-card__title-row h3 {
  min-width: 0;
  margin: 0;
  overflow: hidden;
  color: #111827;
  font-size: 18px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-card__title-row :deep(.ant-tag) {
  flex: 0 0 auto;
  margin-inline-end: 0;
}

.app-card__creator {
  margin: 5px 0 0;
  overflow: hidden;
  color: #64748b;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-card__meta {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-top: 14px;
  color: #64748b;
  font-size: 12px;
}

.app-card__deployed {
  color: #0f9f78;
}

.app-card__actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 11px;
  margin-top: 12px;
  border-top: 1px solid #f0f2f5;
}
</style>
