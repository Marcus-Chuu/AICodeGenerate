import type { Rule } from 'ant-design-vue/es/form'

export const userAccountRules: Rule[] = [
  { required: true, message: '请输入账号', trigger: 'blur' },
  { min: 4, message: '账号至少需要 4 个字符', trigger: 'blur' },
]

export const userPasswordRules: Rule[] = [
  { required: true, message: '请输入密码', trigger: 'blur' },
  { min: 8, message: '密码至少需要 8 个字符', trigger: 'blur' },
]
