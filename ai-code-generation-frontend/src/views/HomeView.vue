<script setup lang="ts">
import { computed, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  AreaChartOutlined,
  CalendarOutlined,
  CheckCircleFilled,
  RobotOutlined,
  SendOutlined,
  ShoppingCartOutlined,
  SlidersOutlined,
  StarFilled,
} from '@ant-design/icons-vue'

const prompt = ref('')
const selectedMode = ref('smart')
const isGenerating = ref(false)
const generationStatus = ref<'ready' | 'generating' | 'created'>('ready')
const autoSelectStack = ref(true)
const generateMockData = ref(true)

const modelOptions = [
  { label: '智能模式', value: 'smart' },
  { label: '极速模式', value: 'fast' },
  { label: '深度模式', value: 'deep' },
]

const inspirationItems = [
  {
    title: '一个简约的在线书店',
    prompt: '创建一个简约的在线书店，支持图书浏览、分类筛选、购物车和订单结算。',
    icon: ShoppingCartOutlined,
    tone: 'purple',
  },
  {
    title: '团队任务管理工具',
    prompt: '创建一个团队任务管理工具，包含看板、任务分配、进度追踪和成员协作。',
    icon: CalendarOutlined,
    tone: 'teal',
  },
  {
    title: '数据可视化分析看板',
    prompt: '创建一个数据可视化分析看板，展示核心指标、趋势图表和多维筛选器。',
    icon: AreaChartOutlined,
    tone: 'blue',
  },
]

const statusText = computed(() => {
  if (generationStatus.value === 'generating') return '正在创建'
  if (generationStatus.value === 'created') return '任务已创建'
  return '就绪'
})

const statusType = computed(() =>
  generationStatus.value === 'generating' ? 'processing' : 'success',
)

const useInspiration = (examplePrompt: string) => {
  prompt.value = examplePrompt
  generationStatus.value = 'ready'
}

const handleGenerate = () => {
  if (!prompt.value.trim()) {
    void message.warning('请先描述你想创建的网站或应用')
    return
  }

  isGenerating.value = true
  generationStatus.value = 'generating'

  window.setTimeout(() => {
    isGenerating.value = false
    generationStatus.value = 'created'
    void message.success('应用生成任务已创建')
  }, 1200)
}
</script>

<template>
  <main class="home-page">
    <section class="hero" aria-labelledby="hero-title">
      <div class="hero__intro">
        <div class="hero__sparkles" aria-hidden="true">
          <StarFilled />
          <StarFilled />
        </div>
        <h1 id="hero-title" class="hero__title">
          <span>一句话，</span><span class="hero__title-second">生成<span
              class="hero__title-accent"
              >你的应</span
            ><span class="hero__title-accent-secondary">用</span></span
          >
        </h1>
        <p class="hero__description">
          描述你想要的网站或应用，AI 将自动为你生成完整的页面、功能与逻辑，快速落地你的想法。
        </p>
      </div>

      <div class="composer" :class="{ 'composer--created': generationStatus === 'created' }">
        <div class="composer__input-area">
          <StarFilled class="composer__ai-icon" aria-hidden="true" />
          <a-textarea
            v-model:value="prompt"
            class="composer__textarea"
            :bordered="false"
            :maxlength="600"
            placeholder="描述你想创建的网站或应用..."
            aria-label="描述你想创建的网站或应用"
            @keydown.ctrl.enter.prevent="handleGenerate"
            @keydown.meta.enter.prevent="handleGenerate"
          />
        </div>

        <div class="composer__toolbar">
          <div class="composer__tools-left">
            <div class="model-control">
              <RobotOutlined aria-hidden="true" />
              <span class="model-control__label">AI 模型：</span>
              <a-select
                v-model:value="selectedMode"
                class="model-control__select"
                :options="modelOptions"
                :bordered="false"
                aria-label="选择 AI 模型"
              />
            </div>

            <a-badge :status="statusType" :text="`状态：${statusText}`" />
          </div>

          <div class="composer__actions">
            <a-popover placement="topRight" trigger="click">
              <template #content>
                <div class="generation-settings">
                  <div class="generation-settings__title">生成偏好</div>
                  <label class="generation-settings__row">
                    <span>自动选择技术栈</span>
                    <a-switch v-model:checked="autoSelectStack" size="small" />
                  </label>
                  <label class="generation-settings__row">
                    <span>生成示例数据</span>
                    <a-switch v-model:checked="generateMockData" size="small" />
                  </label>
                </div>
              </template>
              <a-button class="settings-button" size="large" aria-label="打开生成设置">
                <template #icon><SlidersOutlined /></template>
              </a-button>
            </a-popover>

            <a-button
              class="generate-button"
              type="primary"
              size="large"
              :loading="isGenerating"
              @click="handleGenerate"
            >
              <template #icon>
                <CheckCircleFilled v-if="generationStatus === 'created'" />
                <SendOutlined v-else />
              </template>
              {{ generationStatus === 'created' ? '已创建任务' : '开始生成' }}
            </a-button>
          </div>
        </div>
      </div>

      <div class="inspiration" aria-labelledby="inspiration-title">
        <div class="inspiration__heading">
          <StarFilled aria-hidden="true" />
          <span id="inspiration-title">灵感示例</span>
          <StarFilled aria-hidden="true" />
        </div>

        <div class="inspiration__list">
          <button
            v-for="item in inspirationItems"
            :key="item.title"
            class="inspiration-card"
            type="button"
            @click="useInspiration(item.prompt)"
          >
            <component :is="item.icon" :class="`inspiration-card__icon--${item.tone}`" />
            <span>{{ item.title }}</span>
          </button>
        </div>
      </div>
    </section>
  </main>
</template>

<style scoped>
.home-page {
  min-height: calc(100vh - 164px);
  background: #fbfcfe url('@/assets/hero-background.webp') center top / cover no-repeat;
}

.hero {
  width: 100%;
  max-width: 1046px;
  padding: 156px 32px 72px;
  margin: 0 auto;
  text-align: center;
}

.hero__intro {
  position: relative;
  z-index: 1;
}

.hero__sparkles {
  position: absolute;
  top: -18px;
  right: 8%;
  display: flex;
  gap: 10px;
  align-items: flex-end;
  color: #18bfa8;
}

.hero__sparkles > :first-child {
  font-size: 13px;
}

.hero__sparkles > :last-child {
  font-size: 9px;
}

.hero__title {
  margin: 0;
  color: #07142f;
  font-size: clamp(52px, 5.6vw, 80px);
  font-weight: 750;
  line-height: 1.12;
  letter-spacing: -0.045em;
}

.hero__title-second {
  margin-left: 0.55em;
}

.hero__title-accent {
  color: #2878ef;
}

.hero__title-accent-secondary {
  color: #18bfa8;
}

.hero__description {
  max-width: 780px;
  margin: 24px auto 0;
  color: #64748b;
  font-size: 17px;
  line-height: 1.8;
}

.composer {
  width: 100%;
  margin: 58px auto 0;
  overflow: hidden;
  text-align: left;
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 24px;
  box-shadow: 0 24px 70px rgba(38, 71, 132, 0.12);
  transition:
    border-color 0.25s ease,
    box-shadow 0.25s ease,
    transform 0.25s ease;
}

.composer:focus-within {
  border-color: #80b5ff;
  box-shadow:
    0 24px 70px rgba(38, 71, 132, 0.14),
    0 0 0 4px rgba(22, 119, 255, 0.08);
  transform: translateY(-2px);
}

.composer--created {
  border-color: #86efac;
}

.composer__input-area {
  display: flex;
  gap: 16px;
  min-height: 144px;
  padding: 36px 30px 20px;
}

.composer__ai-icon {
  flex: none;
  margin-top: 4px;
  color: #2878ef;
  font-size: 24px;
}

.composer__textarea {
  flex: 1;
  min-height: 90px !important;
  padding: 0 !important;
  color: #172033;
  font-size: 18px;
  line-height: 1.7;
  resize: none;
  box-shadow: none !important;
}

.composer__textarea::placeholder {
  color: #94a3b8;
}

.composer__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 78px;
  padding: 16px 24px 20px 30px;
  border-top: 1px solid #f1f5f9;
}

.composer__tools-left,
.composer__actions {
  display: flex;
  gap: 22px;
  align-items: center;
}

.model-control {
  display: flex;
  align-items: center;
  min-height: 46px;
  padding: 0 8px 0 15px;
  color: #475569;
  border: 1px solid #dbe3ee;
  border-radius: 13px;
}

.model-control__label {
  margin-left: 8px;
  white-space: nowrap;
}

.model-control__select {
  width: 112px;
}

.settings-button {
  width: 48px;
  height: 48px;
  padding: 0;
  color: #475569;
  border-color: #dbe3ee;
  border-radius: 12px;
}

.generate-button {
  min-width: 154px;
  height: 48px;
  padding: 0 24px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  box-shadow: 0 10px 24px rgba(22, 119, 255, 0.22);
}

.generation-settings {
  width: 220px;
}

.generation-settings__title {
  margin-bottom: 10px;
  color: #172033;
  font-weight: 600;
}

.generation-settings__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  color: #475569;
}

.inspiration {
  margin-top: 72px;
}

.inspiration__heading {
  display: flex;
  gap: 14px;
  align-items: center;
  justify-content: center;
  color: #53617a;
  font-size: 16px;
  font-weight: 600;
}

.inspiration__heading > :first-child,
.inspiration__heading > :last-child {
  color: #bdc7d8;
  font-size: 12px;
}

.inspiration__list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 28px;
  max-width: 870px;
  margin-top: 30px;
  margin-right: auto;
  margin-left: auto;
}

.inspiration-card {
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: center;
  min-height: 76px;
  padding: 16px 22px;
  color: #3d4a61;
  font: inherit;
  font-size: 16px;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid #dce4ef;
  border-radius: 12px;
  transition:
    color 0.2s ease,
    background-color 0.2s ease,
    border-color 0.2s ease,
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.inspiration-card:hover {
  color: #1677ff;
  background: #ffffff;
  border-color: #9bc5ff;
  box-shadow: 0 12px 30px rgba(38, 71, 132, 0.09);
  transform: translateY(-3px);
}

.inspiration-card:focus-visible {
  outline: 3px solid rgba(22, 119, 255, 0.22);
  outline-offset: 3px;
}

.inspiration-card > :first-child {
  flex: none;
  font-size: 25px;
}

.inspiration-card__icon--purple {
  color: #8b5cf6;
}

.inspiration-card__icon--teal {
  color: #14b8a6;
}

.inspiration-card__icon--blue {
  color: #3b82f6;
}

@media (max-width: 900px) {
  .hero {
    padding-top: 112px;
  }

  .composer__toolbar {
    align-items: flex-start;
  }

  .composer__tools-left {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .inspiration__list {
    gap: 14px;
  }

  .inspiration-card {
    padding: 14px;
    font-size: 14px;
  }
}

@media (max-width: 680px) {
  .home-page {
    min-height: calc(100vh - 117px);
  }

  .hero {
    padding: 78px 16px 52px;
  }

  .hero__title {
    font-size: clamp(39px, 11vw, 52px);
    letter-spacing: -0.055em;
  }

  .hero__title-second {
    display: block;
    margin-top: 8px;
    margin-left: 0;
  }

  .hero__sparkles {
    display: none;
  }

  .hero__description {
    margin-top: 18px;
    font-size: 15px;
    line-height: 1.7;
  }

  .composer {
    margin-top: 42px;
    border-radius: 18px;
  }

  .composer__input-area {
    min-height: 142px;
    padding: 24px 20px 14px;
  }

  .composer__textarea {
    font-size: 16px;
  }

  .composer__toolbar {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
    padding: 16px 18px 18px;
  }

  .composer__tools-left {
    flex-direction: row;
    justify-content: space-between;
  }

  .composer__actions {
    gap: 10px;
  }

  .generate-button {
    flex: 1;
  }

  .model-control {
    min-width: 0;
  }

  .model-control__label {
    display: none;
  }

  .model-control__select {
    width: 100px;
  }

  .inspiration {
    margin-top: 52px;
  }

  .inspiration__list {
    grid-template-columns: 1fr;
    margin-top: 22px;
  }

  .inspiration-card {
    justify-content: flex-start;
    min-height: 64px;
    padding: 14px 20px;
  }
}
</style>
