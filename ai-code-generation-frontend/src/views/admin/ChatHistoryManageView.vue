<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  EyeOutlined,
  MessageOutlined,
  ReloadOutlined,
  RobotOutlined,
  SearchOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import { message, type TableColumnsType, type TableProps } from 'ant-design-vue'

import { chatHistoryApi } from '@/api/chatHistory'
import { getErrorMessage } from '@/api/http'
import logoUrl from '@/assets/logo.png'
import AdminListPage from '@/components/AdminListPage.vue'
import type {
  ChatHistoryAdminQueryRequest,
  ChatHistoryMessageType,
  ChatHistoryVO,
} from '@/types/chatHistory'
import { formatDateTime } from '@/utils/date'

const router = useRouter()
const loading = ref(false)
const detailOpen = ref(false)
const selectedHistory = ref<ChatHistoryVO>()
const records = ref<ChatHistoryVO[]>([])
const total = ref(0)

const filters = reactive({
  appId: '',
  userId: '',
  messageType: undefined as ChatHistoryMessageType | undefined,
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
})

const messageTypeOptions = [
  { label: '用户消息', value: 'user' },
  { label: 'AI 消息', value: 'ai' },
]

const columns: TableColumnsType<ChatHistoryVO> = [
  { title: '消息内容', key: 'message', width: 420, fixed: 'left' },
  { title: '消息类型', dataIndex: 'messageType', key: 'messageType', width: 120 },
  { title: '应用 ID', dataIndex: 'appId', key: 'appId', width: 190 },
  { title: '用户 ID', dataIndex: 'userId', key: 'userId', width: 190 },
  { title: '关联消息 ID', dataIndex: 'parentId', key: 'parentId', width: 190 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 110, fixed: 'right' },
]

const compact = (value: string) => value.trim() || undefined

const loadChatHistories = async () => {
  loading.value = true
  const query: ChatHistoryAdminQueryRequest = {
    pageNum: pagination.current,
    pageSize: pagination.pageSize,
    appId: compact(filters.appId),
    userId: compact(filters.userId),
    messageType: filters.messageType,
  }

  try {
    const result = await chatHistoryApi.listByAdmin(query)
    records.value = result.records ?? []
    total.value = Number(result.totalRow ?? 0)
  } catch (error) {
    records.value = []
    total.value = 0
    void message.error(getErrorMessage(error, '对话历史加载失败'))
  } finally {
    loading.value = false
  }
}

const search = () => {
  pagination.current = 1
  void loadChatHistories()
}

const reset = () => {
  filters.appId = ''
  filters.userId = ''
  filters.messageType = undefined
  pagination.current = 1
  void loadChatHistories()
}

const handleTableChange: TableProps<ChatHistoryVO>['onChange'] = (page) => {
  pagination.current = page.current ?? 1
  pagination.pageSize = page.pageSize ?? 10
  void loadChatHistories()
}

const showDetail = (record: ChatHistoryVO) => {
  selectedHistory.value = record
  detailOpen.value = true
}

const openApp = () => {
  if (!selectedHistory.value) return
  detailOpen.value = false
  void router.push(`/app/chat/${selectedHistory.value.appId}`)
}

onMounted(loadChatHistories)
</script>

<template>
  <AdminListPage
    title="对话管理"
    description="查看平台应用的用户与 AI 对话记录，便于内容审核和问题排查。"
    list-title="对话历史列表"
    count-unit="条消息"
    max-width="1380px"
    :total="total"
    :loading="loading"
    @refresh="loadChatHistories"
  >
    <template #headerIcon><MessageOutlined /></template>
    <template #refreshIcon><ReloadOutlined /></template>

    <template #filters>
      <a-form layout="vertical" :model="filters">
        <div class="filter-grid">
          <a-form-item label="应用 ID">
            <a-input
              v-model:value="filters.appId"
              allow-clear
              placeholder="按应用 ID 查询"
              @press-enter="search"
            />
          </a-form-item>
          <a-form-item label="用户 ID">
            <a-input
              v-model:value="filters.userId"
              allow-clear
              placeholder="按用户 ID 查询"
              @press-enter="search"
            />
          </a-form-item>
          <a-form-item label="消息类型">
            <a-select
              v-model:value="filters.messageType"
              allow-clear
              :options="messageTypeOptions"
              placeholder="全部消息类型"
            />
          </a-form-item>
        </div>
        <div class="filter-actions">
          <a-button type="primary" @click="search">
            <template #icon><SearchOutlined /></template>
            查询
          </a-button>
          <a-button @click="reset">
            <template #icon><ReloadOutlined /></template>
            重置
          </a-button>
        </div>
      </a-form>
    </template>

    <a-table
      row-key="id"
      :columns="columns"
      :data-source="records"
      :loading="loading"
      :scroll="{ x: 1410 }"
      :pagination="{
        current: pagination.current,
        pageSize: pagination.pageSize,
        total,
        pageSizeOptions: ['10', '20', '50', '100'],
        showSizeChanger: true,
        showQuickJumper: true,
        showTotal: (value: number) => `共 ${value} 条`,
      }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'message'">
          <div class="message-cell">
            <a-avatar v-if="record.messageType === 'user'" :size="38">
              <template #icon><UserOutlined /></template>
            </a-avatar>
            <a-avatar v-else :size="38" :src="logoUrl" />
            <div>
              <strong>{{ record.messageType === 'user' ? '用户消息' : 'AI 回复' }}</strong>
              <span>{{ record.message }}</span>
              <small>ID: {{ record.id }}</small>
            </div>
          </div>
        </template>
        <template v-else-if="column.key === 'messageType'">
          <a-tag :color="record.messageType === 'user' ? 'blue' : 'purple'">
            <UserOutlined v-if="record.messageType === 'user'" />
            <RobotOutlined v-else />
            {{ record.messageType === 'user' ? '用户' : 'AI' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'parentId'">
          {{ record.parentId || '-' }}
        </template>
        <template v-else-if="column.key === 'createTime'">
          {{ formatDateTime(record.createTime) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-button type="link" size="small" @click="showDetail(record)">
            <template #icon><EyeOutlined /></template>
            查看
          </a-button>
        </template>
      </template>
    </a-table>

    <template #overlay>
      <a-modal v-model:open="detailOpen" title="对话消息详情" :footer="null" width="720px">
        <a-descriptions v-if="selectedHistory" bordered :column="1" size="small">
          <a-descriptions-item label="消息 ID">{{ selectedHistory.id }}</a-descriptions-item>
          <a-descriptions-item label="应用 ID">{{ selectedHistory.appId }}</a-descriptions-item>
          <a-descriptions-item label="用户 ID">{{ selectedHistory.userId }}</a-descriptions-item>
          <a-descriptions-item label="消息类型">
            {{ selectedHistory.messageType === 'user' ? '用户消息' : 'AI 消息' }}
          </a-descriptions-item>
          <a-descriptions-item label="关联消息 ID">
            {{ selectedHistory.parentId || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ formatDateTime(selectedHistory.createTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="消息内容">
            <pre class="message-detail">{{ selectedHistory.message }}</pre>
          </a-descriptions-item>
        </a-descriptions>
        <div class="detail-actions">
          <a-button @click="detailOpen = false">关闭</a-button>
          <a-button type="primary" @click="openApp">进入应用对话</a-button>
        </div>
      </a-modal>
    </template>
  </AdminListPage>
</template>

<style scoped>
.filter-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0 16px;
}

.filter-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.message-cell {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.message-cell > div {
  display: grid;
  min-width: 0;
}

.message-cell strong {
  color: #1e293b;
  font-size: 13px;
}

.message-cell span {
  max-width: 330px;
  overflow: hidden;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-cell small {
  color: #a0aec0;
  font-size: 10px;
}

.message-detail {
  max-height: 360px;
  padding: 14px;
  margin: 0;
  overflow: auto;
  color: #334155;
  font: inherit;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  background: #f8fafc;
  border-radius: 8px;
}

.detail-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .filter-grid {
    grid-template-columns: 1fr;
  }
}
</style>
