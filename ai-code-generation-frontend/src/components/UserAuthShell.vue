<script setup lang="ts">
import { CodeOutlined, SafetyCertificateOutlined, ThunderboltOutlined } from '@ant-design/icons-vue'

defineProps<{
  eyebrow: string
  title: string
  description: string
}>()

const highlights = [
  { icon: ThunderboltOutlined, text: '一句话描述需求，快速开始创作' },
  { icon: CodeOutlined, text: 'AI 自动生成页面、功能与业务逻辑' },
  { icon: SafetyCertificateOutlined, text: '账号与作品数据由平台安全保护' },
]
</script>

<template>
  <main class="auth-page">
    <div class="auth-page__inner">
      <section class="auth-intro" aria-label="平台介绍">
        <div class="auth-intro__eyebrow">AI PRODUCT STUDIO</div>
        <h1>让想法更快成为<br /><span>可用的产品</span></h1>
        <p>登录后继续管理你的生成任务、应用项目和团队协作空间。</p>

        <ul class="auth-intro__highlights">
          <li v-for="item in highlights" :key="item.text">
            <span class="auth-intro__icon"><component :is="item.icon" /></span>
            <span>{{ item.text }}</span>
          </li>
        </ul>
      </section>

      <section class="auth-card" :aria-label="title">
        <div class="auth-card__eyebrow">{{ eyebrow }}</div>
        <h2>{{ title }}</h2>
        <p class="auth-card__description">{{ description }}</p>
        <slot />
      </section>
    </div>
  </main>
</template>

<style scoped>
.auth-page {
  display: flex;
  min-height: calc(100vh - 164px);
  padding: 72px 32px;
  background: #fbfcfe url('@/assets/hero-background.webp') center / cover no-repeat;
}

.auth-page__inner {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 460px;
  gap: 100px;
  align-items: center;
  width: 100%;
  max-width: 1100px;
  margin: auto;
}

.auth-intro__eyebrow,
.auth-card__eyebrow {
  color: #1677ff;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
}

.auth-intro h1 {
  margin: 18px 0 20px;
  color: #07142f;
  font-size: clamp(42px, 4vw, 60px);
  font-weight: 750;
  line-height: 1.18;
  letter-spacing: -0.04em;
}

.auth-intro h1 span {
  color: #1677ff;
}

.auth-intro > p {
  max-width: 500px;
  margin: 0;
  color: #64748b;
  font-size: 16px;
  line-height: 1.8;
}

.auth-intro__highlights {
  display: grid;
  gap: 18px;
  padding: 0;
  margin: 40px 0 0;
  list-style: none;
}

.auth-intro__highlights li {
  display: flex;
  gap: 14px;
  align-items: center;
  color: #475569;
}

.auth-intro__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  color: #1677ff;
  background: #eef6ff;
  border: 1px solid #d7e9ff;
  border-radius: 10px;
}

.auth-card {
  padding: 42px;
  background: rgba(255, 255, 255, 0.97);
  border: 1px solid #e5ebf3;
  border-radius: 24px;
  box-shadow: 0 28px 80px rgba(38, 71, 132, 0.13);
}

.auth-card h2 {
  margin: 10px 0 8px;
  color: #0f172a;
  font-size: 30px;
  font-weight: 700;
  letter-spacing: -0.03em;
}

.auth-card__description {
  margin: 0 0 30px;
  color: #64748b;
  line-height: 1.7;
}

@media (max-width: 900px) {
  .auth-page__inner {
    grid-template-columns: 1fr;
    max-width: 500px;
  }

  .auth-intro {
    display: none;
  }
}

@media (max-width: 576px) {
  .auth-page {
    min-height: calc(100vh - 117px);
    padding: 32px 16px;
  }

  .auth-card {
    padding: 30px 22px;
    border-radius: 18px;
  }

  .auth-card h2 {
    font-size: 26px;
  }
}
</style>
