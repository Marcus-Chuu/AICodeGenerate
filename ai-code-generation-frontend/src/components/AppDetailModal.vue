<script setup lang="ts">
import { computed } from 'vue'
import { DeleteOutlined, EditOutlined, UserOutlined } from '@ant-design/icons-vue'

import type { AppVO } from '@/types/app'
import { formatDateTime } from '@/utils/date'

const props = withDefaults(
  defineProps<{
    open: boolean
    app: AppVO | null
    canManage?: boolean
    deleting?: boolean
  }>(),
  {
    canManage: false,
    deleting: false,
  },
)

const emit = defineEmits<{
  'update:open': [value: boolean]
  edit: []
  delete: []
}>()

const creatorName = computed(
  () =>
    props.app?.user?.userName ||
    props.app?.user?.userAccount ||
    (props.app?.userId ? `用户 ${props.app.userId}` : '未知用户'),
)
</script>

<template>
  <a-modal
    :open="open"
    title="应用详情"
    :footer="null"
    :width="480"
    centered
    @update:open="emit('update:open', $event)"
  >
    <div class="app-detail-modal">
      <section class="app-detail-section">
        <h3>应用基础信息</h3>
        <div class="app-detail-row">
          <span>创建者</span>
          <div class="creator-info">
            <a-avatar :size="42" :src="app?.user?.userAvatar">
              <template #icon><UserOutlined /></template>
            </a-avatar>
            <div>
              <strong>{{ creatorName }}</strong>
              <small v-if="app?.user?.userAccount">{{ app.user.userAccount }}</small>
            </div>
          </div>
        </div>
        <div class="app-detail-row">
          <span>创建时间</span>
          <time>{{ formatDateTime(app?.createTime) }}</time>
        </div>
      </section>

      <section v-if="canManage" class="app-detail-section app-detail-actions">
        <h3>操作</h3>
        <div>
          <a-button type="primary" @click="emit('edit')">
            <template #icon><EditOutlined /></template>
            修改
          </a-button>
          <a-button danger :loading="deleting" @click="emit('delete')">
            <template #icon><DeleteOutlined /></template>
            删除
          </a-button>
        </div>
      </section>
    </div>
  </a-modal>
</template>

<style scoped>
.app-detail-modal {
  display: grid;
  gap: 20px;
  padding-top: 8px;
}

.app-detail-section {
  overflow: hidden;
  border: 1px solid #e6ebf2;
  border-radius: 14px;
}

.app-detail-section h3 {
  padding: 13px 16px;
  margin: 0;
  color: #1e293b;
  font-size: 14px;
  background: #f8fafc;
  border-bottom: 1px solid #edf1f6;
}

.app-detail-row {
  display: flex;
  gap: 20px;
  align-items: center;
  justify-content: space-between;
  min-height: 68px;
  padding: 12px 16px;
}

.app-detail-row + .app-detail-row {
  border-top: 1px solid #f0f2f5;
}

.app-detail-row > span {
  flex: 0 0 auto;
  color: #64748b;
  font-size: 13px;
}

.app-detail-row time {
  color: #334155;
  font-size: 13px;
}

.creator-info {
  display: flex;
  gap: 11px;
  align-items: center;
  min-width: 0;
}

.creator-info > div {
  display: grid;
  min-width: 0;
}

.creator-info strong,
.creator-info small {
  max-width: 260px;
  overflow: hidden;
  text-align: right;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.creator-info strong {
  color: #1e293b;
}

.creator-info small {
  color: #94a3b8;
}

.app-detail-actions > div {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding: 16px;
}
</style>
