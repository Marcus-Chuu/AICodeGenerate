<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { LockOutlined, UserOutlined } from '@ant-design/icons-vue'
import { message, type FormInstance } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'

import { getErrorMessage } from '@/api/http'
import AuthFormActions from '@/components/AuthFormActions.vue'
import UserAuthShell from '@/components/UserAuthShell.vue'
import { userAccountRules, userPasswordRules } from '@/config/validation'
import { useUserStore } from '@/stores/user'
import type { UserRegisterRequest } from '@/types/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const form = reactive<UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const validateConfirmPassword = async (_rule: Rule, value: string) => {
  if (!value) return Promise.reject(new Error('请再次输入密码'))
  if (value !== form.userPassword) return Promise.reject(new Error('两次输入的密码不一致'))
  return Promise.resolve()
}

const rules: Record<keyof UserRegisterRequest, Rule[]> = {
  userAccount: userAccountRules,
  userPassword: userPasswordRules,
  checkPassword: [
    { required: true, validator: validateConfirmPassword, trigger: ['blur', 'change'] },
  ],
}

const handleSubmit = async () => {
  submitting.value = true

  try {
    await userStore.register({ ...form })
    void message.success('注册成功，请登录')
    await router.replace({ path: '/user/login', query: { account: form.userAccount } })
  } catch (error) {
    void message.error(getErrorMessage(error, '注册失败，请稍后重试'))
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <UserAuthShell
    eyebrow="CREATE ACCOUNT"
    title="创建平台账号"
    description="注册后即可保存应用并管理生成记录。"
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
        <a-input
          v-model:value="form.userAccount"
          autocomplete="username"
          placeholder="至少 4 个字符"
        >
          <template #prefix><UserOutlined /></template>
        </a-input>
      </a-form-item>

      <a-form-item label="密码" name="userPassword">
        <a-input-password
          v-model:value="form.userPassword"
          autocomplete="new-password"
          placeholder="至少 8 个字符"
        >
          <template #prefix><LockOutlined /></template>
        </a-input-password>
      </a-form-item>

      <a-form-item label="确认密码" name="checkPassword">
        <a-input-password
          v-model:value="form.checkPassword"
          autocomplete="new-password"
          placeholder="请再次输入密码"
        >
          <template #prefix><LockOutlined /></template>
        </a-input-password>
      </a-form-item>

      <AuthFormActions
        hint="注册即表示你同意平台使用规范"
        link-text="已有账号？去登录"
        link-to="/user/login"
        submit-text="注册"
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
