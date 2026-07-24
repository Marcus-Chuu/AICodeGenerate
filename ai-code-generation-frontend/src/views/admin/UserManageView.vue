<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  DeleteOutlined,
  EditOutlined,
  PlusOutlined,
  ReloadOutlined,
  SearchOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import {
  message,
  type FormInstance,
  type TableColumnsType,
  type TableProps,
} from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'

import { getErrorMessage } from '@/api/http'
import { userApi } from '@/api/user'
import type {
  UserAddRequest,
  UserQueryRequest,
  UserRole,
  UserUpdateRequest,
  UserVO,
} from '@/types/user'

type UserFormModel = {
  id?: number
  userAccount: string
  userName: string
  userAvatar: string
  userProfile: string
  userRole: UserRole
}

const loading = ref(false)
const saving = ref(false)
const modalOpen = ref(false)
const modalMode = ref<'create' | 'edit'>('create')
const formRef = ref<FormInstance>()
const records = ref<UserVO[]>([])
const total = ref(0)

const filters = reactive({
  userAccount: '',
  userName: '',
  userRole: undefined as UserRole | undefined,
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
})

const userForm = reactive<UserFormModel>({
  userAccount: '',
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user',
})

const roleOptions = [
  { label: '普通用户', value: 'user' },
  { label: '管理员', value: 'admin' },
  { label: '已封禁', value: 'ban' },
]

const formRules: Record<string, Rule[]> = {
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, message: '账号至少需要 4 个字符', trigger: 'blur' },
  ],
  userRole: [{ required: true, message: '请选择用户角色', trigger: 'change' }],
}

const columns: TableColumnsType<UserVO> = [
  { title: '用户', key: 'user', width: 220, fixed: 'left' },
  { title: '账号', dataIndex: 'userAccount', key: 'userAccount', width: 160 },
  { title: '角色', dataIndex: 'userRole', key: 'userRole', width: 110 },
  { title: '个人简介', dataIndex: 'userProfile', key: 'userProfile', ellipsis: true },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' },
]

const modalTitle = computed(() => (modalMode.value === 'create' ? '新增用户' : '编辑用户'))

const getRoleLabel = (role: UserRole) =>
  ({ user: '普通用户', admin: '管理员', ban: '已封禁' })[role] ?? role

const getRoleColor = (role: UserRole) =>
  ({ user: 'blue', admin: 'purple', ban: 'error' })[role] ?? 'default'

const formatTime = (value?: string) => {
  if (!value) return '-'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('zh-CN', { hour12: false })
}

const loadUsers = async () => {
  loading.value = true

  const query: UserQueryRequest = {
    pageNum: pagination.current,
    pageSize: pagination.pageSize,
    userAccount: filters.userAccount.trim() || undefined,
    userName: filters.userName.trim() || undefined,
    userRole: filters.userRole,
    sortField: 'createTime',
    sortOrder: 'descend',
  }

  try {
    const result = await userApi.listUsers(query)
    records.value = result.records ?? []
    total.value = Number(result.totalRow ?? 0)
  } catch (error) {
    records.value = []
    total.value = 0
    void message.error(getErrorMessage(error, '用户列表加载失败'))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  void loadUsers()
}

const handleReset = () => {
  filters.userAccount = ''
  filters.userName = ''
  filters.userRole = undefined
  pagination.current = 1
  void loadUsers()
}

const handleTableChange: TableProps<UserVO>['onChange'] = (page) => {
  pagination.current = page.current ?? 1
  pagination.pageSize = page.pageSize ?? 10
  void loadUsers()
}

const resetUserForm = () => {
  Object.assign(userForm, {
    id: undefined,
    userAccount: '',
    userName: '',
    userAvatar: '',
    userProfile: '',
    userRole: 'user' as UserRole,
  })
  formRef.value?.clearValidate()
}

const openCreateModal = () => {
  modalMode.value = 'create'
  resetUserForm()
  modalOpen.value = true
}

const openEditModal = (record: UserVO) => {
  modalMode.value = 'edit'
  Object.assign(userForm, {
    id: record.id,
    userAccount: record.userAccount,
    userName: record.userName ?? '',
    userAvatar: record.userAvatar ?? '',
    userProfile: record.userProfile ?? '',
    userRole: record.userRole,
  })
  formRef.value?.clearValidate()
  modalOpen.value = true
}

const saveUser = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  saving.value = true

  try {
    if (modalMode.value === 'create') {
      const payload: UserAddRequest = {
        userAccount: userForm.userAccount.trim(),
        userName: userForm.userName.trim() || undefined,
        userAvatar: userForm.userAvatar.trim() || undefined,
        userProfile: userForm.userProfile.trim() || undefined,
        userRole: userForm.userRole,
      }
      await userApi.addUser(payload)
      void message.success('用户创建成功，默认密码为 12345678')
    } else if (userForm.id) {
      const payload: UserUpdateRequest = {
        id: userForm.id,
        userName: userForm.userName.trim() || undefined,
        userAvatar: userForm.userAvatar.trim() || undefined,
        userProfile: userForm.userProfile.trim() || undefined,
        userRole: userForm.userRole,
      }
      await userApi.updateUser(payload)
      void message.success('用户信息已更新')
    }

    modalOpen.value = false
    await loadUsers()
  } catch (error) {
    void message.error(getErrorMessage(error, '保存失败'))
  } finally {
    saving.value = false
  }
}

const deleteUser = async (record: UserVO) => {
  try {
    await userApi.deleteUser(record.id)
    void message.success('用户已删除')

    if (records.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    await loadUsers()
  } catch (error) {
    void message.error(getErrorMessage(error, '删除失败'))
  }
}

onMounted(() => {
  void loadUsers()
})
</script>

<template>
  <main class="admin-page">
    <div class="admin-page__inner">
      <header class="page-header">
        <div>
          <div class="page-header__eyebrow"><TeamOutlined /> ADMIN CONSOLE</div>
          <h1>用户管理</h1>
          <p>查看平台用户、调整角色与维护账号信息。</p>
        </div>
        <a-button type="primary" size="large" @click="openCreateModal">
          <template #icon><PlusOutlined /></template>
          新增用户
        </a-button>
      </header>

      <section class="filter-panel" aria-label="用户筛选">
        <a-form layout="inline" :model="filters">
          <a-form-item label="账号">
            <a-input
              v-model:value="filters.userAccount"
              allow-clear
              placeholder="搜索用户账号"
              @press-enter="handleSearch"
            />
          </a-form-item>
          <a-form-item label="昵称">
            <a-input
              v-model:value="filters.userName"
              allow-clear
              placeholder="搜索用户昵称"
              @press-enter="handleSearch"
            />
          </a-form-item>
          <a-form-item label="角色">
            <a-select
              v-model:value="filters.userRole"
              allow-clear
              :options="roleOptions"
              placeholder="全部角色"
              style="width: 140px"
            />
          </a-form-item>
          <a-form-item class="filter-panel__actions">
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-form-item>
        </a-form>
      </section>

      <section class="table-panel" aria-label="用户列表">
        <div class="table-panel__header">
          <div>
            <h2>用户列表</h2>
            <span>共 {{ total }} 位用户</span>
          </div>
          <a-button :loading="loading" @click="loadUsers">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </div>

        <a-table
          row-key="id"
          :columns="columns"
          :data-source="records"
          :loading="loading"
          :scroll="{ x: 980 }"
          :pagination="{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (value: number) => `共 ${value} 条`,
          }"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'user'">
              <div class="user-cell">
                <a-avatar :src="record.userAvatar" :size="40">
                  <template #icon><UserOutlined /></template>
                </a-avatar>
                <div>
                  <strong>{{ record.userName || '未设置昵称' }}</strong>
                  <span>ID: {{ record.id }}</span>
                </div>
              </div>
            </template>

            <template v-else-if="column.key === 'userRole'">
              <a-tag :color="getRoleColor(record.userRole)">{{ getRoleLabel(record.userRole) }}</a-tag>
            </template>

            <template v-else-if="column.key === 'userProfile'">
              <span class="profile-text">{{ record.userProfile || '-' }}</span>
            </template>

            <template v-else-if="column.key === 'createTime'">
              {{ formatTime(record.createTime) }}
            </template>

            <template v-else-if="column.key === 'action'">
              <div class="table-actions">
                <a-button type="link" size="small" @click="openEditModal(record)">
                  <template #icon><EditOutlined /></template>
                  编辑
                </a-button>
                <a-popconfirm
                  title="确认删除该用户吗？"
                  description="删除后无法恢复。"
                  ok-text="删除"
                  cancel-text="取消"
                  ok-type="danger"
                  @confirm="deleteUser(record)"
                >
                  <a-button type="link" danger size="small">
                    <template #icon><DeleteOutlined /></template>
                    删除
                  </a-button>
                </a-popconfirm>
              </div>
            </template>
          </template>
        </a-table>
      </section>
    </div>

    <a-modal
      v-model:open="modalOpen"
      :title="modalTitle"
      :confirm-loading="saving"
      ok-text="保存"
      cancel-text="取消"
      width="620px"
      @ok="saveUser"
    >
      <a-alert
        v-if="modalMode === 'create'"
        class="default-password-tip"
        type="info"
        show-icon
        message="新用户默认密码为 12345678"
      />

      <a-form ref="formRef" :model="userForm" :rules="formRules" layout="vertical">
        <div class="form-grid">
          <a-form-item label="账号" name="userAccount">
            <a-input
              v-model:value="userForm.userAccount"
              :disabled="modalMode === 'edit'"
              placeholder="请输入用户账号"
            />
          </a-form-item>
          <a-form-item label="角色" name="userRole">
            <a-select v-model:value="userForm.userRole" :options="roleOptions" />
          </a-form-item>
        </div>

        <a-form-item label="昵称" name="userName">
          <a-input v-model:value="userForm.userName" placeholder="请输入用户昵称" />
        </a-form-item>
        <a-form-item label="头像地址" name="userAvatar">
          <a-input v-model:value="userForm.userAvatar" placeholder="https://example.com/avatar.png" />
        </a-form-item>
        <a-form-item label="个人简介" name="userProfile">
          <a-textarea
            v-model:value="userForm.userProfile"
            :rows="3"
            :maxlength="200"
            show-count
            placeholder="简单介绍一下这位用户"
          />
        </a-form-item>
      </a-form>
    </a-modal>
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
  max-width: 1280px;
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

.page-header > .ant-btn {
  height: 44px;
  border-radius: 10px;
}

.filter-panel,
.table-panel {
  background: #ffffff;
  border: 1px solid #e7ecf3;
  border-radius: 16px;
  box-shadow: 0 10px 34px rgba(38, 71, 132, 0.06);
}

.filter-panel {
  padding: 22px 24px 6px;
  margin-bottom: 20px;
}

.filter-panel :deep(.ant-form-inline) {
  row-gap: 0;
}

.filter-panel__actions :deep(.ant-form-item-control-input-content) {
  display: flex;
  gap: 10px;
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

.user-cell {
  display: flex;
  gap: 12px;
  align-items: center;
}

.user-cell > div {
  display: grid;
  gap: 2px;
  min-width: 0;
}

.user-cell strong {
  overflow: hidden;
  color: #1e293b;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-cell span {
  color: #94a3b8;
  font-size: 12px;
}

.profile-text {
  color: #64748b;
}

.table-actions {
  display: flex;
  align-items: center;
}

.default-password-tip {
  margin-bottom: 22px;
}

.form-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 180px;
  gap: 16px;
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

  .filter-panel :deep(.ant-form-item) {
    width: 100%;
    margin-right: 0;
  }

  .filter-panel :deep(.ant-form-item-control),
  .filter-panel :deep(.ant-input),
  .filter-panel :deep(.ant-select) {
    width: 100% !important;
  }
}

@media (max-width: 576px) {
  .page-header {
    flex-direction: column;
  }

  .page-header > .ant-btn {
    width: 100%;
  }

  .table-panel {
    padding: 0 14px 14px;
  }

  .form-grid {
    grid-template-columns: 1fr;
    gap: 0;
  }
}
</style>
