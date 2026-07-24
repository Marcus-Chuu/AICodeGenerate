<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { LockOutlined, UserOutlined } from '@ant-design/icons-vue'
import { message, type FormInstance } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'

import { getErrorMessage } from '@/api/http'
import UserAuthShell from '@/components/UserAuthShell.vue'
import { useUserStore } from '@/stores/user'
import type { UserLoginRequest } from '@/types/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const form = reactive<UserLoginRequest>({
  userAccount: typeof route.query.account === 'string' ? route.query.account : '',
  userPassword: '',
})

const rules: Record<keyof UserLoginRequest, Rule[]> = {
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, message: '账号至少需要 4 个字符', trigger: 'blur' },
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码至少需要 8 个字符', trigger: 'blur' },
  ],
}

const handleSubmit = async () => {
  submitting.value = true

  try {
    await userStore.login({ ...form })
    void message.success('登录成功')

    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.replace(redirect)
  } catch (error) {
    void message.error(getErrorMessage(error, '登录失败，请检查账号和密码'))
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <UserAuthShell eyebrow="WELCOME BACK" title="登录你的账号" description="继续创建和管理你的 AI 应用。">
    <a-form
      ref="formRef"
      :model="form"
      :rules="rules"
      layout="vertical"
      size="large"
      @finish="handleSubmit"
    >
      <a-form-item label="账号" name="userAccount">
        <a-input v-model:value="form.userAccount" autocomplete="username" placeholder="请输入账号">
          <template #prefix><UserOutlined /></template>
        </a-input>
      </a-form-item>

      <a-form-item label="密码" name="userPassword">
        <a-input-password
          v-model:value="form.userPassword"
          autocomplete="current-password"
          placeholder="请输入密码"
        >
          <template #prefix><LockOutlined /></template>
        </a-input-password>
      </a-form-item>

      <div class="form-meta">
        <span>使用平台账号安全登录</span>
        <RouterLink to="/user/register">还没有账号？去注册</RouterLink>
      </div>

      <a-button class="submit-button" type="primary" html-type="submit" :loading="submitting" block>
        登录
      </a-button>
    </a-form>
  </UserAuthShell>
</template>

<style scoped>
:deep(.ant-input-affix-wrapper) {
  min-height: 48px;
  border-radius: 10px;
}

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
