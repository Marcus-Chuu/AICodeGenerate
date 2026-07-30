<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowUpOutlined,
  AppstoreOutlined,
  LoginOutlined,
  SearchOutlined,
  ThunderboltFilled,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'

import { appApi } from '@/api/app'
import { getErrorMessage } from '@/api/http'
import AppCard from '@/components/AppCard.vue'
import { useUserStore } from '@/stores/user'
import type { AppVO } from '@/types/app'

const router = useRouter()
const userStore = useUserStore()

const prompt = ref('')
const creating = ref(false)
const myApps = ref<AppVO[]>([])
const featuredApps = ref<AppVO[]>([])
const myLoading = ref(false)
const featuredLoading = ref(false)

const myQuery = reactive({ page: 1, pageSize: 6, appName: '', total: 0 })
const featuredQuery = reactive({ page: 1, pageSize: 6, appName: '', total: 0 })

const promptExamples = [
  {
    label: '个人博客网站',
    prompt:
      '帮我创建一个现代简约的个人博客网站，包含首页、文章分类、文章详情、标签归档、关于我和联系方式；首页展示个人简介、精选文章与最新动态，支持深浅色切换和移动端适配，整体使用温暖克制的配色与清晰舒适的阅读排版。',
  },
  {
    label: '科技企业官网',
    prompt:
      '帮我创建一个专业的科技公司官网，包含品牌首页、产品服务、解决方案、客户案例、团队介绍和联系我们；首页突出核心价值、产品优势与合作客户，提供咨询预约入口，使用蓝紫科技感视觉、数据指标和流畅的响应式布局。',
  },
  {
    label: '电商商品网站',
    prompt:
      '帮我创建一个精致的电商商品展示网站，包含商品列表、分类筛选、搜索、商品详情、购物车和订单确认；重点展示商品图片、价格、规格、库存与用户评价，提供促销标签和相关推荐，整体风格现代、可信并完整适配手机端。',
  },
  {
    label: '在线课程平台',
    prompt:
      '帮我创建一个在线课程学习平台，包含课程首页、课程分类、课程详情、学习进度、讲师介绍和个人学习中心；首页展示热门课程与学习路径，课程详情包含章节目录、试看和评价，界面清爽有活力，并兼顾桌面端与移动端体验。',
  },
]

const loadMyApps = async () => {
  if (!userStore.isLoggedIn) return
  myLoading.value = true
  try {
    const result = await appApi.listMine({
      pageNum: myQuery.page,
      pageSize: myQuery.pageSize,
      appName: myQuery.appName.trim() || undefined,
    })
    myApps.value = result.records ?? []
    myQuery.total = Number(result.totalRow ?? 0)
  } catch (error) {
    myApps.value = []
    myQuery.total = 0
    void message.error(getErrorMessage(error, '我的应用加载失败'))
  } finally {
    myLoading.value = false
  }
}

const loadFeaturedApps = async () => {
  if (!userStore.isLoggedIn) return
  featuredLoading.value = true
  try {
    const result = await appApi.listFeatured({
      pageNum: featuredQuery.page,
      pageSize: featuredQuery.pageSize,
      appName: featuredQuery.appName.trim() || undefined,
    })
    featuredApps.value = result.records ?? []
    featuredQuery.total = Number(result.totalRow ?? 0)
  } catch (error) {
    featuredApps.value = []
    featuredQuery.total = 0
    void message.error(getErrorMessage(error, '精选应用加载失败'))
  } finally {
    featuredLoading.value = false
  }
}

const loadApplications = () => {
  void Promise.all([loadMyApps(), loadFeaturedApps()])
}

const createApplication = async () => {
  const initPrompt = prompt.value.trim()
  if (!initPrompt) {
    void message.warning('请先描述你想创建的网站应用')
    return
  }

  if (!userStore.isLoggedIn) {
    void message.info('登录后即可创建应用')
    await router.push({ path: '/user/login', query: { redirect: '/' } })
    return
  }

  creating.value = true
  try {
    const appId = await appApi.add(initPrompt)
    await router.push(`/app/chat/${appId}`)
  } catch (error) {
    void message.error(getErrorMessage(error, '应用创建失败'))
  } finally {
    creating.value = false
  }
}

const usePromptExample = (value: string) => {
  prompt.value = value
}

const handlePromptKeydown = (event: KeyboardEvent) => {
  if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
    event.preventDefault()
    void createApplication()
  }
}

const openApp = (app: AppVO) => void router.push(`/app/chat/${app.id}`)
const editApp = (app: AppVO) => void router.push(`/app/edit/${app.id}`)

const deleteApp = (app: AppVO) => {
  Modal.confirm({
    title: `确认删除“${app.appName || '未命名应用'}”吗？`,
    content: '删除后无法恢复，已生成的应用记录也将不可再访问。',
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        await appApi.delete(app.id)
        void message.success('应用已删除')
        if (myApps.value.length === 1 && myQuery.page > 1) myQuery.page -= 1
        await loadMyApps()
      } catch (error) {
        void message.error(getErrorMessage(error, '删除应用失败'))
      }
    },
  })
}

const searchMine = () => {
  myQuery.page = 1
  void loadMyApps()
}

const searchFeatured = () => {
  featuredQuery.page = 1
  void loadFeaturedApps()
}

watch(
  () => userStore.isLoggedIn,
  (loggedIn) => {
    if (loggedIn) loadApplications()
    else {
      myApps.value = []
      featuredApps.value = []
    }
  },
)

onMounted(loadApplications)
</script>

<template>
  <main class="home-page">
    <section class="hero" aria-labelledby="home-title">
      <div class="hero__heading">
        <h1 id="home-title">AI 应用生成平台</h1>
        <p>一句话轻松创建网站应用</p>
      </div>

      <div class="prompt-box">
        <a-textarea
          v-model:value="prompt"
          class="prompt-box__input"
          :bordered="false"
          :maxlength="1000"
          :auto-size="{ minRows: 4, maxRows: 8 }"
          placeholder="帮我创建个人博客网站"
          @keydown="handlePromptKeydown"
        />
        <div class="prompt-box__toolbar">
          <span class="prompt-box__tip"><ThunderboltFilled /> 描述越具体，生成效果越好</span>
          <a-button
            class="prompt-box__submit"
            type="primary"
            shape="circle"
            size="large"
            :loading="creating"
            aria-label="创建应用"
            @click="createApplication"
          >
            <template #icon><ArrowUpOutlined /></template>
          </a-button>
        </div>
      </div>

      <div class="prompt-examples" aria-label="提示词示例">
        <button
          v-for="item in promptExamples"
          :key="item.label"
          type="button"
          :title="item.prompt"
          :aria-label="`${item.label}：${item.prompt}`"
          @click="usePromptExample(item.prompt)"
        >
          <span>{{ item.label }}</span>
        </button>
      </div>
    </section>

    <section class="application-showcase">
      <div class="showcase-section">
        <div class="section-heading">
          <div>
            <span class="section-heading__eyebrow"><AppstoreOutlined /> MY APPS</span>
            <h2>我的应用</h2>
          </div>
          <a-input-search
            v-if="userStore.isLoggedIn"
            v-model:value="myQuery.appName"
            allow-clear
            placeholder="按名称搜索"
            @search="searchMine"
          >
            <template #enterButton><SearchOutlined /></template>
          </a-input-search>
        </div>

        <div v-if="!userStore.isLoggedIn" class="login-empty">
          <LoginOutlined />
          <h3>登录后查看和管理你的应用</h3>
          <a-button type="primary" @click="router.push('/user/login')">立即登录</a-button>
        </div>
        <a-spin v-else :spinning="myLoading">
          <div v-if="myApps.length" class="app-grid">
            <AppCard
              v-for="app in myApps"
              :key="app.id"
              :app="app"
              editable
              @open="openApp"
              @edit="editApp"
              @delete="deleteApp"
            />
          </div>
          <a-empty v-else-if="!myLoading" description="还没有应用，试试用上面的提示词创建一个" />
        </a-spin>
        <a-pagination
          v-if="userStore.isLoggedIn && myQuery.total > myQuery.pageSize"
          v-model:current="myQuery.page"
          class="section-pagination"
          :page-size="myQuery.pageSize"
          :total="myQuery.total"
          hide-on-single-page
          @change="loadMyApps"
        />
      </div>

      <div class="showcase-section showcase-section--featured">
        <div class="section-heading">
          <div>
            <span class="section-heading__eyebrow section-heading__eyebrow--featured">
              <ThunderboltFilled /> FEATURED
            </span>
            <h2>精选应用</h2>
          </div>
          <a-input-search
            v-if="userStore.isLoggedIn"
            v-model:value="featuredQuery.appName"
            allow-clear
            placeholder="搜索精选应用"
            @search="searchFeatured"
          >
            <template #enterButton><SearchOutlined /></template>
          </a-input-search>
        </div>

        <a-spin v-if="userStore.isLoggedIn" :spinning="featuredLoading">
          <div v-if="featuredApps.length" class="app-grid">
            <AppCard v-for="app in featuredApps" :key="app.id" :app="app" @open="openApp" />
          </div>
          <a-empty v-else-if="!featuredLoading" description="暂时还没有精选应用" />
        </a-spin>
        <div v-else class="featured-guest-tip">登录后即可浏览平台精选应用</div>
        <a-pagination
          v-if="userStore.isLoggedIn && featuredQuery.total > featuredQuery.pageSize"
          v-model:current="featuredQuery.page"
          class="section-pagination"
          :page-size="featuredQuery.pageSize"
          :total="featuredQuery.total"
          hide-on-single-page
          @change="loadFeaturedApps"
        />
      </div>
    </section>
  </main>
</template>

<style scoped>
.home-page {
  min-height: calc(100vh - 164px);
  overflow: hidden;
  background:
    radial-gradient(circle at 12% 10%, rgba(35, 211, 255, 0.22), transparent 30%),
    radial-gradient(circle at 88% 18%, rgba(127, 90, 240, 0.28), transparent 34%),
    radial-gradient(circle at 52% 54%, rgba(30, 102, 255, 0.16), transparent 40%),
    linear-gradient(145deg, #071225 0%, #0a1d3c 42%, #132d58 72%, #12203d 100%);
}

.hero {
  position: relative;
  width: 100%;
  min-height: 650px;
  padding: 112px 24px 98px;
  text-align: center;
}

.hero__heading h1 {
  margin: 0;
  color: #fff;
  font-size: clamp(44px, 5vw, 68px);
  font-weight: 800;
  letter-spacing: -0.045em;
  text-shadow: 0 8px 36px rgba(45, 155, 255, 0.24);
}

.hero__heading p {
  margin: 20px 0 0;
  color: rgba(226, 240, 255, 0.76);
  font-size: clamp(17px, 2vw, 22px);
  font-weight: 400;
  letter-spacing: 0.12em;
}

.prompt-box {
  width: min(1040px, 100%);
  min-height: 220px;
  padding: 28px 30px 20px;
  margin: 52px auto 0;
  text-align: left;
  background: rgba(255, 255, 255, 0.96);
  border: 0;
  border-radius: 24px;
  box-shadow:
    0 30px 80px rgba(0, 8, 30, 0.38),
    0 0 48px rgba(41, 171, 255, 0.12);
  backdrop-filter: blur(20px);
  transition:
    box-shadow 0.25s ease,
    transform 0.25s ease;
}

.prompt-box:focus-within {
  box-shadow:
    0 34px 90px rgba(0, 8, 30, 0.44),
    0 0 0 4px rgba(71, 184, 255, 0.16),
    0 0 56px rgba(99, 102, 241, 0.2);
  transform: translateY(-2px);
}

.prompt-box__input {
  min-height: 132px;
  padding: 0 !important;
  color: #17233a;
  font-size: 18px;
  line-height: 1.8;
  resize: none;
}

.prompt-box__input::placeholder {
  color: #9aa7b9;
}

.prompt-box__toolbar {
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
}

.prompt-box__tip {
  display: inline-flex;
  gap: 7px;
  align-items: center;
  color: #94a3b8;
  font-size: 13px;
}

.prompt-box__tip :deep(svg) {
  color: #14b8a6;
}

.prompt-box__submit {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #1677ff, #6758ee);
  border: 0;
  box-shadow: 0 12px 28px rgba(43, 101, 255, 0.34);
}

.prompt-examples {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  width: min(920px, 100%);
  margin-top: 28px;
  margin-right: auto;
  margin-left: auto;
}

.prompt-examples button {
  min-width: 0;
  padding: 12px 18px;
  overflow: hidden;
  color: rgba(235, 244, 255, 0.9);
  font: inherit;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.09);
  border: 1px solid rgba(163, 205, 255, 0.2);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  transition:
    color 0.2s ease,
    background-color 0.2s ease,
    border-color 0.2s ease,
    transform 0.2s ease;
}

.prompt-examples button span {
  display: block;
  overflow: hidden;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.prompt-examples button:hover {
  color: #fff;
  background: rgba(91, 150, 255, 0.18);
  border-color: rgba(123, 190, 255, 0.62);
  transform: translateY(-2px);
}

.application-showcase {
  position: relative;
  width: min(1340px, calc(100% - 48px));
  padding: 58px 54px 72px;
  margin: -30px auto 64px;
  background: rgba(248, 251, 255, 0.97);
  border: 1px solid rgba(205, 224, 248, 0.48);
  border-radius: 32px;
  box-shadow: 0 30px 90px rgba(0, 8, 30, 0.34);
  backdrop-filter: blur(20px);
}

.showcase-section + .showcase-section {
  padding-top: 62px;
  margin-top: 62px;
  border-top: 1px solid #edf1f6;
}

.section-heading {
  display: flex;
  gap: 24px;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 26px;
}

.section-heading h2 {
  margin: 7px 0 0;
  color: #0f172a;
  font-size: 32px;
  letter-spacing: -0.03em;
}

.section-heading__eyebrow {
  display: flex;
  gap: 7px;
  align-items: center;
  color: #1677ff;
  font-size: 12px;
  font-weight: 750;
  letter-spacing: 0.14em;
}

.section-heading__eyebrow--featured {
  color: #d99000;
}

.section-heading :deep(.ant-input-search) {
  width: 250px;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 24px;
}

.login-empty,
.featured-guest-tip {
  display: flex;
  flex-direction: column;
  gap: 14px;
  align-items: center;
  justify-content: center;
  min-height: 220px;
  color: #64748b;
  text-align: center;
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 18px;
}

.login-empty > :first-child {
  color: #1677ff;
  font-size: 30px;
}

.login-empty h3 {
  margin: 0;
  color: #334155;
  font-size: 17px;
}

.featured-guest-tip {
  min-height: 130px;
}

.section-pagination {
  margin-top: 32px;
  text-align: center;
}

@media (max-width: 960px) {
  .prompt-examples {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .app-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .application-showcase {
    padding: 44px 32px 56px;
  }
}

@media (max-width: 640px) {
  .hero {
    min-height: 580px;
    padding: 82px 16px 68px;
  }

  .hero__heading h1 {
    font-size: 38px;
  }

  .hero__heading p {
    margin-top: 14px;
    font-size: 16px;
    letter-spacing: 0.08em;
  }

  .prompt-box {
    min-height: 220px;
    padding: 22px 20px 18px;
    margin-top: 40px;
    border-radius: 20px;
  }

  .prompt-box__input {
    font-size: 16px;
  }

  .prompt-box__tip {
    max-width: 210px;
  }

  .prompt-examples {
    gap: 10px;
  }

  .prompt-examples button {
    padding: 11px 10px;
    font-size: 13px;
  }

  .application-showcase {
    width: calc(100% - 24px);
    padding: 34px 18px 46px;
    border-radius: 22px;
  }

  .section-heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .section-heading h2 {
    font-size: 27px;
  }

  .section-heading :deep(.ant-input-search) {
    width: 100%;
  }

  .app-grid {
    grid-template-columns: 1fr;
  }
}
</style>
