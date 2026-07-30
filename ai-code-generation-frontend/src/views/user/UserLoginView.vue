<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { LockOutlined, UserOutlined } from '@ant-design/icons-vue'
import { message, type FormInstance } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'

import { getErrorMessage } from '@/api/http'
import AuthFormActions from '@/components/AuthFormActions.vue'
import UserAuthShell from '@/components/UserAuthShell.vue'
import { userAccountRules, userPasswordRules } from '@/config/validation'
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
  userAccount: userAccountRules,
  userPassword: userPasswordRules,
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
  <UserAuthShell
    eyebrow="WELCOME BACK"
    title="登录你的账号"
    description="继续创建和管理你的 AI 应用。"
  >
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

      <AuthFormActions
        hint="使用平台账号安全登录"
        link-text="还没有账号？去注册"
        link-to="/user/register"
        submit-text="登录"
        :loading="submitting"
      />
    </a-form>
  </UserAuthShell>
</template>

<style scoped>
:deep(.ant-input-affix-wrapper) {
  min-height: 48px;
  border-radius: 10px;
}
</style>
