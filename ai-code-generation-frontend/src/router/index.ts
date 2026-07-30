import { createRouter, createWebHistory } from 'vue-router'

import { useUserStore } from '@/stores/user'
import HomeView from '@/views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('@/views/AboutView.vue'),
    },
    {
      path: '/user/login',
      name: 'user-login',
      component: () => import('@/views/user/UserLoginView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/user/register',
      name: 'user-register',
      component: () => import('@/views/user/UserRegisterView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/admin/users',
      name: 'admin-user-manage',
      component: () => import('@/views/admin/UserManageView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/apps',
      name: 'admin-app-manage',
      component: () => import('@/views/admin/AppManageView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/chat-histories',
      name: 'admin-chat-history-manage',
      component: () => import('@/views/admin/ChatHistoryManageView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/app/chat/:id',
      name: 'app-chat',
      component: () => import('@/views/app/AppChatView.vue'),
      meta: { requiresAuth: true, immersive: true },
    },
    {
      path: '/app/edit/:id',
      name: 'app-edit',
      component: () => import('@/views/app/AppEditView.vue'),
      meta: { requiresAuth: true },
    },
  ],
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()

  if (!userStore.initialized) {
    await userStore.fetchLoginUser()
  }

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return {
      path: '/user/login',
      query: { redirect: to.fullPath },
    }
  }

  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    return { path: '/' }
  }

  if (to.meta.guestOnly && userStore.isLoggedIn) {
    return { path: '/' }
  }

  return true
})

export default router
