export interface GlobalMenuItem {
  key: string
  label: string
  path: string
  requiresAdmin?: boolean
}

export const globalMenuItems: GlobalMenuItem[] = [
  {
    key: 'home',
    label: '首页',
    path: '/',
  },
  {
    key: 'about',
    label: '关于平台',
    path: '/about',
  },
  {
    key: 'user-manage',
    label: '用户管理',
    path: '/admin/users',
    requiresAdmin: true,
  },
]
