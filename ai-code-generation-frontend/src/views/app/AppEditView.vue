<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeftOutlined, SaveOutlined } from '@ant-design/icons-vue'
import { message, type FormInstance } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'

import { appApi } from '@/api/app'
import { getErrorMessage } from '@/api/http'
import logoUrl from '@/assets/logo.png'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appId = computed(() => String(route.params.id))
const adminMode = computed(() => userStore.isAdmin && route.query.admin === '1')

const loading = ref(true)
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({ appName: '', cover: '', priority: 0 })

const rules: Record<string, Rule[]> = {
  appName: [
    { required: true, message: '请输入应用名称', trigger: 'blur' },
    { max: 128, message: '应用名称不能超过 128 个字符', trigger: 'blur' },
  ],
}

const loadApp = async () => {
  loading.value = true
  try {
    const detail = adminMode.value
      ? await appApi.getByAdmin(appId.value)
      : await appApi.get(appId.value)
    form.appName = detail.appName || ''
    form.cover = detail.cover || ''
    form.priority = detail.priority ?? 0
  } catch (error) {
    void message.error(getErrorMessage(error, '应用信息加载失败'))
    await router.replace(adminMode.value ? '/admin/apps' : '/')
  } finally {
    loading.value = false
  }
}

const saveApp = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  saving.value = true
  try {
    if (adminMode.value) {
      await appApi.updateByAdmin({
        id: appId.value,
        appName: form.appName.trim(),
        cover: form.cover.trim() || undefined,
        priority: form.priority,
      })
    } else {
      await appApi.update({ id: appId.value, appName: form.appName.trim() })
    }
    void message.success('应用信息已保存')
    await router.push(adminMode.value ? '/admin/apps' : `/app/chat/${appId.value}`)
  } catch (error) {
    void message.error(getErrorMessage(error, '应用信息保存失败'))
  } finally {
    saving.value = false
  }
}

onMounted(loadApp)
</script>

<template>
  <main class="edit-page">
    <div class="edit-page__inner">
      <a-button class="back-button" type="text" @click="router.back()">
        <template #icon><ArrowLeftOutlined /></template>
        返回
      </a-button>

      <section class="edit-card">
        <div class="edit-card__intro">
          <span>{{ adminMode ? 'ADMIN EDITOR' : 'APP SETTINGS' }}</span>
          <h1>编辑应用信息</h1>
          <p>
            {{ adminMode ? '管理员可以维护应用名称、封面与精选优先级。' : '你可以修改自己的应用名称。' }}
          </p>
        </div>

        <a-spin :spinning="loading">
          <div class="edit-card__content">
            <div class="cover-preview">
              <img :src="form.cover || logoUrl" :alt="form.appName || '应用封面'" />
              <span>应用封面预览</span>
            </div>

            <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
              <a-form-item label="应用名称" name="appName">
                <a-input v-model:value="form.appName" size="large" placeholder="请输入应用名称" />
              </a-form-item>

              <template v-if="adminMode">
                <a-form-item label="应用封面地址" name="cover">
                  <a-input
                    v-model:value="form.cover"
                    size="large"
                    placeholder="https://example.com/cover.png"
                  />
                </a-form-item>
                <a-form-item label="优先级" name="priority" extra="大于 0 的应用会进入精选列表，推荐设置为 99。">
                  <a-input-number v-model:value="form.priority" size="large" :min="0" :max="9999" />
                </a-form-item>
              </template>

              <a-button type="primary" size="large" :loading="saving" @click="saveApp">
                <template #icon><SaveOutlined /></template>
                保存修改
              </a-button>
            </a-form>
          </div>
        </a-spin>
      </section>
    </div>
  </main>
</template>

<style scoped>
.edit-page {
  min-height: calc(100vh - 164px);
  padding: 44px 24px 72px;
  background: #f5f7fb url('@/assets/hero-background.webp') center top / cover no-repeat;
}

.edit-page__inner {
  width: min(920px, 100%);
  margin: 0 auto;
}

.back-button {
  margin-bottom: 18px;
}

.edit-card {
  overflow: hidden;
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 22px;
  box-shadow: 0 24px 60px rgba(38, 71, 132, 0.1);
}

.edit-card__intro {
  padding: 34px 40px 30px;
  border-bottom: 1px solid #edf1f6;
}

.edit-card__intro > span {
  color: #1677ff;
  font-size: 12px;
  font-weight: 750;
  letter-spacing: 0.15em;
}

.edit-card__intro h1 {
  margin: 8px 0 6px;
  color: #0f172a;
  font-size: 30px;
}

.edit-card__intro p {
  margin: 0;
  color: #64748b;
}

.edit-card__content {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 42px;
  padding: 40px;
}

.cover-preview {
  display: flex;
  flex-direction: column;
  gap: 12px;
  color: #94a3b8;
  font-size: 12px;
  text-align: center;
}

.cover-preview img {
  width: 100%;
  aspect-ratio: 16 / 10;
  object-fit: contain;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
}

.edit-card :deep(.ant-input-number) {
  width: 100%;
}

@media (max-width: 700px) {
  .edit-card__intro,
  .edit-card__content {
    padding: 28px 22px;
  }

  .edit-card__content {
    grid-template-columns: 1fr;
    gap: 28px;
  }

  .cover-preview {
    max-width: 300px;
    margin: 0 auto;
  }
}
</style>
