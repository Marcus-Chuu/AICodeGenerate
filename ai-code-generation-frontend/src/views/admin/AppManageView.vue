<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  AppstoreOutlined,
  DeleteOutlined,
  EditOutlined,
  ReloadOutlined,
  SearchOutlined,
  StarFilled,
  StarOutlined,
} from '@ant-design/icons-vue'
import { message, type TableColumnsType, type TableProps } from 'ant-design-vue'

import { appApi } from '@/api/app'
import { getErrorMessage } from '@/api/http'
import logoUrl from '@/assets/logo.png'
import AdminListPage from '@/components/AdminListPage.vue'
import type { AppQueryRequest, AppVO } from '@/types/app'
import { formatDateTime } from '@/utils/date'

const router = useRouter()
const loading = ref(false)
const records = ref<AppVO[]>([])
const total = ref(0)

const filters = reactive({
  id: '',
  appName: '',
  cover: '',
  initPrompt: '',
  codeGenType: undefined as string | undefined,
  deployKey: '',
  priority: undefined as number | undefined,
  userId: '',
})

const pagination = reactive({ current: 1, pageSize: 10 })

const codeTypeOptions = [
  { label: '多文件应用', value: 'multi_file' },
  { label: '原生 HTML', value: 'html' },
]

const columns: TableColumnsType<AppVO> = [
  { title: '应用', key: 'app', width: 260, fixed: 'left' },
  { title: '生成类型', dataIndex: 'codeGenType', key: 'codeGenType', width: 120 },
  { title: '创建用户', dataIndex: 'userId', key: 'userId', width: 180 },
  { title: '部署标识', dataIndex: 'deployKey', key: 'deployKey', width: 130 },
  { title: '优先级', dataIndex: 'priority', key: 'priority', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 230, fixed: 'right' },
]

const compact = (value: string) => value.trim() || undefined

const loadApps = async () => {
  loading.value = true
  const query: AppQueryRequest = {
    pageNum: pagination.current,
    pageSize: pagination.pageSize,
    id: compact(filters.id),
    appName: compact(filters.appName),
    cover: compact(filters.cover),
    initPrompt: compact(filters.initPrompt),
    codeGenType: filters.codeGenType,
    deployKey: compact(filters.deployKey),
    priority: filters.priority,
    userId: compact(filters.userId),
    sortField: 'createTime',
    sortOrder: 'descend',
  }

  try {
    const result = await appApi.listByAdmin(query)
    records.value = result.records ?? []
    total.value = Number(result.totalRow ?? 0)
  } catch (error) {
    records.value = []
    total.value = 0
    void message.error(getErrorMessage(error, '应用列表加载失败'))
  } finally {
    loading.value = false
  }
}

const search = () => {
  pagination.current = 1
  void loadApps()
}

const reset = () => {
  Object.assign(filters, {
    id: '',
    appName: '',
    cover: '',
    initPrompt: '',
    codeGenType: undefined,
    deployKey: '',
    priority: undefined,
    userId: '',
  })
  pagination.current = 1
  void loadApps()
}

const handleTableChange: TableProps<AppVO>['onChange'] = (page) => {
  pagination.current = page.current ?? 1
  pagination.pageSize = page.pageSize ?? 10
  void loadApps()
}

const editApp = (record: AppVO) => {
  void router.push({ path: `/app/edit/${record.id}`, query: { admin: '1' } })
}

const featureApp = async (record: AppVO) => {
  try {
    await appApi.updateByAdmin({
      id: record.id,
      appName: record.appName,
      cover: record.cover,
      priority: 99,
    })
    void message.success('应用已设为精选')
    await loadApps()
  } catch (error) {
    void message.error(getErrorMessage(error, '设置精选失败'))
  }
}

const deleteApp = async (record: AppVO) => {
  try {
    await appApi.deleteByAdmin(record.id)
    void message.success('应用已删除')
    if (records.value.length === 1 && pagination.current > 1) pagination.current -= 1
    await loadApps()
  } catch (error) {
    void message.error(getErrorMessage(error, '删除应用失败'))
  }
}

onMounted(loadApps)
</script>

<template>
  <AdminListPage
    title="应用管理"
    description="检索平台应用、维护展示信息并设置精选内容。"
    list-title="应用列表"
    count-unit="个应用"
    max-width="1380px"
    :total="total"
    :loading="loading"
    @refresh="loadApps"
  >
    <template #headerIcon><AppstoreOutlined /></template>
    <template #refreshIcon><ReloadOutlined /></template>
    <template #filters>
      <a-form layout="vertical" :model="filters">
        <div class="filter-grid">
          <a-form-item label="应用 ID">
            <a-input v-model:value="filters.id" allow-clear placeholder="精确查询 ID" />
          </a-form-item>
          <a-form-item label="应用名称">
            <a-input v-model:value="filters.appName" allow-clear placeholder="按名称查询" />
          </a-form-item>
          <a-form-item label="创建用户 ID">
            <a-input v-model:value="filters.userId" allow-clear placeholder="用户 ID" />
          </a-form-item>
          <a-form-item label="生成类型">
            <a-select
              v-model:value="filters.codeGenType"
              allow-clear
              :options="codeTypeOptions"
              placeholder="全部类型"
            />
          </a-form-item>
          <a-form-item label="部署标识">
            <a-input v-model:value="filters.deployKey" allow-clear placeholder="deployKey" />
          </a-form-item>
          <a-form-item label="优先级">
            <a-input-number v-model:value="filters.priority" :min="0" placeholder="优先级" />
          </a-form-item>
          <a-form-item label="封面地址">
            <a-input v-model:value="filters.cover" allow-clear placeholder="封面 URL" />
          </a-form-item>
          <a-form-item label="初始提示词">
            <a-input
              v-model:value="filters.initPrompt"
              allow-clear
              placeholder="提示词关键词"
              @press-enter="search"
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
      :scroll="{ x: 1200 }"
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
        <template v-if="column.key === 'app'">
          <div class="app-cell">
            <img :src="record.cover || logoUrl" alt="" />
            <div>
              <strong>{{ record.appName || '未命名应用' }}</strong
              ><span>ID: {{ record.id }}</span>
            </div>
          </div>
        </template>
        <template v-else-if="column.key === 'codeGenType'">
          <a-tag color="blue">{{ record.codeGenType === 'multi_file' ? '多文件' : 'HTML' }}</a-tag>
        </template>
        <template v-else-if="column.key === 'deployKey'">
          {{ record.deployKey || '-' }}
        </template>
        <template v-else-if="column.key === 'priority'">
          <span class="priority-cell"
            ><StarFilled v-if="(record.priority ?? 0) > 0" /> {{ record.priority ?? 0 }}</span
          >
        </template>
        <template v-else-if="column.key === 'createTime'">
          {{ formatDateTime(record.createTime) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <div class="table-actions">
            <a-button type="link" size="small" @click="editApp(record)">
              <template #icon><EditOutlined /></template>编辑
            </a-button>
            <a-button
              type="link"
              size="small"
              :disabled="record.priority === 99"
              @click="featureApp(record)"
            >
              <template #icon><StarOutlined /></template>精选
            </a-button>
            <a-popconfirm
              title="确认删除该应用吗？"
              ok-text="删除"
              cancel-text="取消"
              ok-type="danger"
              @confirm="deleteApp(record)"
            >
              <a-button type="link" danger size="small">
                <template #icon><DeleteOutlined /></template>删除
              </a-button>
            </a-popconfirm>
          </div>
        </template>
      </template>
    </a-table>
  </AdminListPage>
</template>

<style scoped>
.filter-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0 16px;
}
.filter-grid :deep(.ant-input-number) {
  width: 100%;
}
.filter-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
.app-cell {
  display: flex;
  gap: 12px;
  align-items: center;
}
.app-cell img {
  width: 48px;
  height: 36px;
  object-fit: cover;
  background: #f8fafc;
  border: 1px solid #edf1f5;
  border-radius: 7px;
}
.app-cell > div {
  display: grid;
  min-width: 0;
}
.app-cell strong {
  max-width: 170px;
  overflow: hidden;
  color: #1e293b;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.app-cell span {
  color: #94a3b8;
  font-size: 11px;
}
.priority-cell {
  color: #d99000;
}
.table-actions {
  display: flex;
  align-items: center;
}
@media (max-width: 1000px) {
  .filter-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 640px) {
  .filter-grid {
    grid-template-columns: 1fr;
  }
}
</style>
