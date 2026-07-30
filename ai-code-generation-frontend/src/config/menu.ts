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
  {
    key: 'app-manage',
    label: '应用管理',
    path: '/admin/apps',
    requiresAdmin: true,
  },
  {
    key: 'chat-history-manage',
    label: '对话管理',
    path: '/admin/chat-histories',
    requiresAdmin: true,
  },
]
