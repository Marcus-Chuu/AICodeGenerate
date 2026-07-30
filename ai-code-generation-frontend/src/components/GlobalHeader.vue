<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  DownOutlined,
  AppstoreOutlined,
  LogoutOutlined,
  MessageOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import { message, type MenuProps } from 'ant-design-vue'

import logoUrl from '@/assets/logo.png'
import { getErrorMessage } from '@/api/http'
import { globalMenuItems } from '@/config/menu'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const visibleMenuItems = computed(() =>
  globalMenuItems.filter((item) => !item.requiresAdmin || userStore.isAdmin),
)

const menuItems = computed<MenuProps['items']>(() =>
  visibleMenuItems.value.map(({ key, label }) => ({ key, label })),
)

const selectedKeys = computed(() => {
  const activeItem = visibleMenuItems.value.find(({ path }) =>
    path === '/' ? route.path === path : route.path.startsWith(path),
  )

  return activeItem ? [activeItem.key] : []
})

const displayName = computed(
  () => userStore.currentUser?.userName || userStore.currentUser?.userAccount || '平台用户',
)

const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
  const targetItem = visibleMenuItems.value.find((item) => item.key === String(key))

  if (targetItem && targetItem.path !== route.path) {
    void router.push(targetItem.path)
  }
}

const handleLogin = () => {
  const query = route.path.startsWith('/user/') ? undefined : { redirect: route.fullPath }
  void router.push({ path: '/user/login', query })
}

const handleUserMenuClick: MenuProps['onClick'] = async ({ key }) => {
  if (key === 'admin') {
    await router.push('/admin/users')
    return
  }

  if (key === 'admin-apps') {
    await router.push('/admin/apps')
    return
  }

  if (key === 'admin-chat-histories') {
    await router.push('/admin/chat-histories')
    return
  }

  if (key === 'logout') {
    try {
      await userStore.logout()
      void message.success('已安全退出登录')
      await router.push('/')
    } catch (error) {
      void message.error(getErrorMessage(error, '退出登录失败'))
    }
  }
}
</script>

<template>
  <a-layout-header class="global-header">
    <div class="global-header__inner">
      <RouterLink class="global-header__brand" to="/" aria-label="返回首页">
        <img class="global-header__logo" :src="logoUrl" alt="AI 零代码生成平台 Logo" />
        <span class="global-header__title">AI 零代码生成平台</span>
      </RouterLink>

      <a-menu
        class="global-header__menu"
        mode="horizontal"
        :items="menuItems"
        :selected-keys="selectedKeys"
        @click="handleMenuClick"
      />

      <a-dropdown v-if="userStore.isLoggedIn" placement="bottomRight" :trigger="['click']">
        <button class="user-trigger" type="button" aria-label="打开用户菜单">
          <a-avatar :src="userStore.currentUser?.userAvatar" :size="34">
            <template #icon><UserOutlined /></template>
          </a-avatar>
          <span class="user-trigger__name">{{ displayName }}</span>
          <DownOutlined class="user-trigger__arrow" />
        </button>

        <template #overlay>
          <a-menu @click="handleUserMenuClick">
            <a-menu-item v-if="userStore.isAdmin" key="admin">
              <TeamOutlined />
              用户管理
            </a-menu-item>
            <a-menu-item v-if="userStore.isAdmin" key="admin-apps">
              <AppstoreOutlined />
              应用管理
            </a-menu-item>
            <a-menu-item v-if="userStore.isAdmin" key="admin-chat-histories">
              <MessageOutlined />
              对话管理
            </a-menu-item>
            <a-menu-divider v-if="userStore.isAdmin" />
            <a-menu-item key="logout">
              <LogoutOutlined />
              退出登录
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>

      <a-button
        v-else
        class="global-header__login"
        type="primary"
        :loading="userStore.loading"
        @click="handleLogin"
      >
        登录
      </a-button>
    </div>
  </a-layout-header>
</template>

<style scoped>
.global-header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: 68px;
  padding: 0;
  line-height: normal;
  background: rgba(255, 255, 255, 0.94);
  border-bottom: 1px solid #e9eef5;
  box-shadow: 0 1px 0 rgba(15, 23, 42, 0.02);
  backdrop-filter: blur(12px);
}

.global-header__inner {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: none;
  height: 100%;
  padding: 0 62px;
  margin: 0 auto;
}

.global-header__brand {
  display: inline-flex;
  flex-shrink: 0;
  gap: 10px;
  align-items: center;
  color: #111827;
  text-decoration: none;
}

.global-header__logo {
  display: block;
  width: 38px;
  height: 38px;
  object-fit: contain;
}

.global-header__title {
  color: #0f172a;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -0.02em;
  white-space: nowrap;
}

.global-header__menu {
  flex: 1;
  min-width: 0;
  margin-left: 38px;
  background: transparent;
  border-bottom: 0;
}

:deep(.global-header__menu.ant-menu-horizontal) {
  line-height: 67px;
}

.global-header__login {
  flex-shrink: 0;
  min-width: 72px;
  height: 40px;
  margin-left: 24px;
  font-weight: 600;
  border-radius: 8px;
  box-shadow: 0 7px 18px rgba(22, 119, 255, 0.2);
}

.user-trigger {
  display: inline-flex;
  flex-shrink: 0;
  gap: 10px;
  align-items: center;
  min-height: 44px;
  padding: 5px 10px 5px 6px;
  color: #334155;
  font: inherit;
  cursor: pointer;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 24px;
  transition:
    border-color 0.2s ease,
    background-color 0.2s ease;
}

.user-trigger:hover {
  background: #ffffff;
  border-color: #91caff;
}

.user-trigger:focus-visible {
  outline: 3px solid rgba(22, 119, 255, 0.2);
  outline-offset: 2px;
}

.user-trigger__name {
  max-width: 120px;
  overflow: hidden;
  font-size: 14px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-trigger__arrow {
  color: #94a3b8;
  font-size: 11px;
}

@media (max-width: 768px) {
  .global-header {
    height: 56px;
  }

  .global-header__inner {
    padding: 0 12px;
  }

  .global-header__logo {
    width: 34px;
    height: 34px;
  }

  .global-header__title {
    font-size: 16px;
  }

  .global-header__menu {
    margin-left: 12px;
  }

  :deep(.global-header__menu.ant-menu-horizontal) {
    line-height: 55px;
  }

  .global-header__login {
    min-width: 58px;
    height: 36px;
    margin-left: 8px;
  }

  .user-trigger {
    min-height: 40px;
  }

  .user-trigger__name {
    display: none;
  }
}

@media (max-width: 576px) {
  .global-header__title {
    display: none;
  }
}
</style>
