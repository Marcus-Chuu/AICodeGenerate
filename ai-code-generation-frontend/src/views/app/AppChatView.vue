<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeftOutlined,
  CloudUploadOutlined,
  EditOutlined,
  InfoCircleOutlined,
  ReloadOutlined,
  SendOutlined,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'

import { appApi } from '@/api/app'
import { chatHistoryApi } from '@/api/chatHistory'
import { getErrorMessage } from '@/api/http'
import logoUrl from '@/assets/logo.png'
import AppDetailModal from '@/components/AppDetailModal.vue'
import MarkdownMessage from '@/components/MarkdownMessage.vue'
import { buildAppPreviewUrl } from '@/config/app'
import { useUserStore } from '@/stores/user'
import type { AppVO } from '@/types/app'
import type { ChatHistoryVO } from '@/types/chatHistory'
import { formatDateTime } from '@/utils/date'

interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  createdAt?: string
  streaming?: boolean
  failed?: boolean
}

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appId = computed(() => String(route.params.id))

const app = ref<AppVO | null>(null)
const loading = ref(true)
const loadingMore = ref(false)
const sending = ref(false)
const deploying = ref(false)
const deleting = ref(false)
const detailOpen = ref(false)
const inputMessage = ref('')
const previewReady = ref(false)
const previewVersion = ref(Date.now())
const messages = ref<ChatMessage[]>([])
const historyCursor = ref<string>()
const hasMoreHistory = ref(false)
const messageListRef = ref<HTMLElement>()
let closeStream: (() => void) | undefined

const appName = computed(() => app.value?.appName || '未命名应用')
const isOwner = computed(
  () =>
    Boolean(app.value && userStore.currentUser) &&
    String(userStore.currentUser?.id) === String(app.value?.userId),
)
const canManage = computed(() => {
  if (!app.value || !userStore.currentUser) return false
  return userStore.isAdmin || isOwner.value
})
const previewUrl = computed(() => {
  if (!app.value?.codeGenType) return ''
  return `${buildAppPreviewUrl(app.value.codeGenType, appId.value)}?v=${previewVersion.value}`
})

const scrollToBottom = async () => {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

const toChatMessage = (history: ChatHistoryVO): ChatMessage => ({
  id: String(history.id),
  role: history.messageType === 'user' ? 'user' : 'assistant',
  content: history.message,
  createdAt: history.createTime,
  failed:
    history.messageType === 'ai' &&
    (history.message.includes('AI 回复失败') || history.message.includes('AI 回复中断')),
})

const loadInitialHistory = async () => {
  const result = await chatHistoryApi.listByApp({ appId: appId.value })
  messages.value = (result.records ?? []).map(toChatMessage)
  historyCursor.value = result.nextCursor
  hasMoreHistory.value = result.hasMore
  previewReady.value = messages.value.length >= 2
  await scrollToBottom()
  return messages.value.length
}

const loadMoreHistory = async () => {
  if (!hasMoreHistory.value || !historyCursor.value || loadingMore.value) return

  const messageList = messageListRef.value
  const previousScrollHeight = messageList?.scrollHeight ?? 0
  const previousScrollTop = messageList?.scrollTop ?? 0
  loadingMore.value = true

  try {
    const result = await chatHistoryApi.listByApp({
      appId: appId.value,
      lastId: historyCursor.value,
    })
    const loadedIds = new Set(messages.value.map((item) => item.id))
    const olderMessages = (result.records ?? [])
      .map(toChatMessage)
      .filter((item) => !loadedIds.has(item.id))
    messages.value = [...olderMessages, ...messages.value]
    historyCursor.value = result.nextCursor
    hasMoreHistory.value = result.hasMore

    await nextTick()
    if (messageListRef.value) {
      messageListRef.value.scrollTop =
        previousScrollTop + messageListRef.value.scrollHeight - previousScrollHeight
    }
  } catch (error) {
    void message.error(getErrorMessage(error, '更早的对话历史加载失败'))
  } finally {
    loadingMore.value = false
  }
}

const startGeneration = (rawMessage: string) => {
  const content = rawMessage.trim()
  if (!content || sending.value || !isOwner.value) return

  const assistantMessage: ChatMessage = {
    id: `assistant-${Date.now()}`,
    role: 'assistant',
    content: '',
    streaming: true,
  }
  messages.value.push({ id: `user-${Date.now()}`, role: 'user', content }, assistantMessage)
  inputMessage.value = ''
  sending.value = true
  void scrollToBottom()

  closeStream?.()
  closeStream = appApi.createChatStream(appId.value, content, {
    onMessage(chunk) {
      assistantMessage.content += chunk
      void scrollToBottom()
    },
    onDone() {
      assistantMessage.streaming = false
      sending.value = false
      previewReady.value = true
      previewVersion.value = Date.now()
      void message.success('网站生成完成，右侧预览已更新')
      void scrollToBottom()
    },
    onError() {
      assistantMessage.streaming = false
      assistantMessage.failed = true
      if (!assistantMessage.content) assistantMessage.content = '生成过程中连接中断，请稍后重试。'
      sending.value = false
      void message.error('AI 生成连接中断')
      void scrollToBottom()
    },
  })
}

const sendMessage = () => {
  if (!isOwner.value) {
    void message.warning('只有应用创建者可以继续生成应用')
    return
  }
  if (!inputMessage.value.trim()) {
    void message.warning('请输入你的需求')
    return
  }
  startGeneration(inputMessage.value)
}

const handleMessageKeydown = (event: KeyboardEvent) => {
  if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
    event.preventDefault()
    sendMessage()
  }
}

const loadApp = async () => {
  loading.value = true
  try {
    app.value = await appApi.get(appId.value)
  } catch (error) {
    void message.error(getErrorMessage(error, '应用信息加载失败'))
    loading.value = false
    await router.replace('/')
    return
  }

  try {
    const historyCount = await loadInitialHistory()
    if (isOwner.value && historyCount === 0 && app.value?.initPrompt) {
      startGeneration(app.value.initPrompt)
    }
  } catch (error) {
    void message.error(getErrorMessage(error, '对话历史加载失败'))
  } finally {
    loading.value = false
  }
}

const deployApp = async () => {
  const deployWindow = window.open('', '_blank')
  if (deployWindow) {
    deployWindow.document.title = '正在部署应用'
    deployWindow.document.body.textContent = '正在部署应用，请稍候……'
  }

  deploying.value = true
  try {
    const url = await appApi.deploy(appId.value)
    void message.success('应用部署成功')
    const deployUrl = new URL(url, window.location.origin).toString()
    if (deployWindow) {
      deployWindow.location.replace(deployUrl)
    } else {
      window.location.assign(deployUrl)
    }
  } catch (error) {
    deployWindow?.close()
    void message.error(getErrorMessage(error, '应用部署失败'))
  } finally {
    deploying.value = false
  }
}

const refreshPreview = () => {
  previewReady.value = true
  previewVersion.value = Date.now()
}

const editApp = () => {
  detailOpen.value = false
  void router.push({
    path: `/app/edit/${appId.value}`,
    query: userStore.isAdmin ? { admin: '1' } : undefined,
  })
}

const deleteApp = () => {
  Modal.confirm({
    title: `确认删除“${appName.value}”吗？`,
    content: '删除后应用记录将无法恢复。',
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      deleting.value = true
      try {
        if (userStore.isAdmin) await appApi.deleteByAdmin(appId.value)
        else await appApi.delete(appId.value)
        detailOpen.value = false
        void message.success('应用已删除')
        await router.replace(userStore.isAdmin ? '/admin/apps' : '/')
      } catch (error) {
        void message.error(getErrorMessage(error, '删除应用失败'))
        throw error
      } finally {
        deleting.value = false
      }
    },
  })
}

onMounted(loadApp)
onBeforeUnmount(() => closeStream?.())
</script>

<template>
  <main class="chat-page">
    <header class="chat-header">
      <div class="chat-header__app">
        <a-button type="text" aria-label="返回首页" @click="router.push('/')">
          <template #icon><ArrowLeftOutlined /></template>
        </a-button>
        <img :src="logoUrl" alt="" />
        <div>
          <strong>{{ appName }}</strong>
          <span>AI 应用工作台</span>
        </div>
        <a-button
          v-if="canManage"
          type="text"
          size="small"
          aria-label="编辑应用信息"
          @click="router.push(`/app/edit/${appId}`)"
        >
          <template #icon><EditOutlined /></template>
        </a-button>
      </div>

      <div class="chat-header__actions">
        <a-button @click="detailOpen = true">
          <template #icon><InfoCircleOutlined /></template>
          <span class="button-label">应用详情</span>
        </a-button>
        <a-button type="primary" :loading="deploying" :disabled="sending" @click="deployApp">
          <template #icon><CloudUploadOutlined /></template>
          <span class="button-label">部署应用</span>
        </a-button>
      </div>
    </header>

    <a-spin :spinning="loading" class="chat-page__loading">
      <div class="workspace">
        <section class="conversation" aria-label="AI 对话区域">
          <div ref="messageListRef" class="message-list">
            <div v-if="hasMoreHistory" class="history-loader">
              <a-button type="link" size="small" :loading="loadingMore" @click="loadMoreHistory">
                加载更多历史消息
              </a-button>
            </div>

            <div v-if="!messages.length && !loading" class="message-empty">
              <img :src="logoUrl" alt="" />
              <strong>{{ isOwner ? '开始生成你的应用' : '暂无可查看的对话历史' }}</strong>
              <span>
                {{
                  isOwner
                    ? '发送一条具体需求，AI 会在这里持续回复。'
                    : '只有应用创建者和管理员可以查看应用对话。'
                }}
              </span>
            </div>

            <div
              v-for="item in messages"
              :key="item.id"
              class="message-row"
              :class="`message-row--${item.role}`"
            >
              <img v-if="item.role === 'assistant'" :src="logoUrl" alt="AI" />
              <div class="message-bubble" :class="{ 'message-bubble--failed': item.failed }">
                <MarkdownMessage
                  v-if="item.role === 'assistant' && item.content"
                  :content="item.content"
                />
                <p v-else>{{ item.content || '正在思考并生成代码…' }}</p>
                <span v-if="item.streaming" class="typing-indicator">生成中</span>
                <time v-else-if="item.createdAt" class="message-time">
                  {{ formatDateTime(item.createdAt) }}
                </time>
              </div>
            </div>
          </div>

          <div class="chat-composer">
            <a-textarea
              v-model:value="inputMessage"
              :bordered="false"
              :auto-size="{ minRows: 3, maxRows: 7 }"
              :disabled="sending || !isOwner"
              :placeholder="
                isOwner ? '请描述你想生成的网站，越详细效果越好哦' : '仅应用创建者可以继续对话'
              "
              @keydown="handleMessageKeydown"
            />
            <div class="chat-composer__footer">
              <span>Ctrl / ⌘ + Enter 发送</span>
              <a-button
                type="primary"
                shape="circle"
                :loading="sending"
                :disabled="!isOwner"
                @click="sendMessage"
              >
                <template #icon><SendOutlined /></template>
              </a-button>
            </div>
          </div>
        </section>

        <section class="preview-panel" aria-label="生成的网站预览">
          <div class="preview-panel__toolbar">
            <div>
              <span
                class="preview-panel__status"
                :class="{ 'preview-panel__status--ready': previewReady }"
              />
              {{ sending ? '正在生成网站' : previewReady ? '实时预览' : '等待生成' }}
            </div>
            <a-button
              type="text"
              size="small"
              :disabled="!app?.codeGenType"
              @click="refreshPreview"
            >
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
          </div>

          <div class="preview-panel__viewport">
            <iframe
              v-if="previewReady && previewUrl"
              :key="previewVersion"
              :src="previewUrl"
              :title="`${appName}网页预览`"
            />
            <div v-else class="preview-empty">
              <img :src="logoUrl" alt="" />
              <h2>{{ sending ? 'AI 正在构建你的应用' : '网页预览将在这里展示' }}</h2>
              <p>
                {{
                  sending
                    ? '生成完成后会自动刷新，无需手动操作。'
                    : '发送一条需求，开始生成或优化应用。'
                }}
              </p>
            </div>
          </div>
        </section>
      </div>
    </a-spin>

    <AppDetailModal
      v-model:open="detailOpen"
      :app="app"
      :can-manage="canManage"
      :deleting="deleting"
      @edit="editApp"
      @delete="deleteApp"
    />
  </main>
</template>

<style scoped>
.chat-page {
  height: 100vh;
  overflow: hidden;
  background: #f5f7fb;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 68px;
  padding: 0 24px 0 14px;
  background: #fff;
  border-bottom: 1px solid #e6ebf2;
}

.chat-header__app {
  display: flex;
  gap: 10px;
  align-items: center;
  min-width: 0;
}

.chat-header__app img {
  width: 38px;
  height: 38px;
}

.chat-header__app > div {
  display: grid;
  min-width: 0;
}

.chat-header__app strong {
  max-width: 260px;
  overflow: hidden;
  color: #172033;
  font-size: 15px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-header__app span {
  color: #94a3b8;
  font-size: 11px;
}

.chat-header__actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.chat-page__loading,
.chat-page__loading :deep(.ant-spin-container) {
  height: calc(100vh - 68px);
}

.workspace {
  display: grid;
  grid-template-columns: minmax(340px, 2fr) minmax(0, 3fr);
  gap: 0;
  height: calc(100vh - 68px);
  padding: 0;
}

.conversation,
.preview-panel {
  min-width: 0;
  overflow: hidden;
  background: #fff;
  border: 0;
  border-radius: 0;
  box-shadow: none;
}

.conversation {
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e4e9f1;
}

.message-list {
  flex: 1;
  padding: 24px 20px;
  overflow-y: auto;
}

.history-loader {
  display: flex;
  justify-content: center;
  margin: -8px 0 16px;
}

.message-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100%;
  color: #94a3b8;
  text-align: center;
}

.message-empty img {
  width: 52px;
  height: 52px;
  margin-bottom: 14px;
  opacity: 0.78;
}

.message-empty strong {
  margin-bottom: 6px;
  color: #475569;
  font-size: 15px;
}

.message-empty span {
  max-width: 280px;
  font-size: 12px;
  line-height: 1.6;
}

.message-row {
  display: flex;
  gap: 9px;
  align-items: flex-start;
  margin-bottom: 20px;
}

.message-row--user {
  justify-content: flex-end;
}

.message-row > img {
  width: 30px;
  height: 30px;
  margin-top: 2px;
  border-radius: 9px;
}

.message-bubble {
  max-width: 86%;
  padding: 13px 15px;
  color: #334155;
  background: #f4f7fb;
  border: 1px solid #e8edf4;
  border-radius: 4px 16px 16px;
}

.message-row--user .message-bubble {
  color: #fff;
  background: #1677ff;
  border-color: #1677ff;
  border-radius: 16px 4px 16px 16px;
}

.message-bubble--failed {
  color: #b42318;
  background: #fff5f5;
  border-color: #fecaca;
}

.message-bubble p {
  margin: 0;
  font-size: 14px;
  line-height: 1.75;
  white-space: pre-wrap;
}

.typing-indicator {
  display: block;
  margin-top: 8px;
  color: #1677ff;
  font-size: 11px;
}

.message-time {
  display: block;
  margin-top: 8px;
  color: #94a3b8;
  font-size: 10px;
}

.message-row--user .message-time {
  color: rgba(255, 255, 255, 0.72);
  text-align: right;
}

.chat-composer {
  padding: 14px;
  margin: 0 14px 14px;
  background: #f8fafc;
  border: 1px solid #dfe6ef;
  border-radius: 16px;
}

.chat-composer :deep(textarea) {
  padding: 0 !important;
  background: transparent !important;
  resize: none;
}

.chat-composer__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 10px;
}

.chat-composer__footer span {
  color: #a0aec0;
  font-size: 11px;
}

.preview-panel {
  display: flex;
  flex-direction: column;
}

.preview-panel__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 52px;
  padding: 0 16px;
  color: #64748b;
  font-size: 13px;
  border-bottom: 1px solid #e8edf4;
}

.preview-panel__toolbar > div {
  display: flex;
  gap: 8px;
  align-items: center;
}

.preview-panel__status {
  width: 8px;
  height: 8px;
  background: #f59e0b;
  border-radius: 50%;
}

.preview-panel__status--ready {
  background: #10b981;
}

.preview-panel__viewport {
  flex: 1;
  min-height: 0;
  padding: 0;
  background: #eef2f7;
}

.preview-panel__viewport iframe {
  width: 100%;
  height: 100%;
  background: #fff;
  border: 0;
  border-radius: 0;
  box-shadow: none;
}

.preview-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #64748b;
  text-align: center;
  background: #fff;
  border: 1px dashed #cbd5e1;
  border-radius: 10px;
}

.preview-empty img {
  width: 72px;
  height: 72px;
}

.preview-empty h2 {
  margin: 20px 0 8px;
  color: #334155;
  font-size: 20px;
}

.preview-empty p {
  margin: 0;
}

@media (max-width: 900px) {
  .chat-page {
    height: auto;
    min-height: 100vh;
    overflow: visible;
  }

  .chat-page__loading,
  .chat-page__loading :deep(.ant-spin-container) {
    height: auto;
  }

  .workspace {
    grid-template-columns: 1fr;
    height: auto;
  }

  .conversation,
  .preview-panel {
    height: calc(100vh - 96px);
    min-height: 600px;
  }
}

@media (max-width: 576px) {
  .chat-header {
    padding: 0 12px 0 4px;
  }

  .chat-header__app span,
  .chat-header__app > .ant-btn:last-child {
    display: none;
  }

  .chat-header__app strong {
    max-width: 130px;
  }

  .chat-header__actions .ant-btn {
    padding-inline: 9px;
  }

  .chat-header__actions .button-label {
    display: none;
  }

  .workspace {
    padding: 8px;
  }

  .conversation,
  .preview-panel {
    border-radius: 12px;
  }
}
</style>
