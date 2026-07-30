<script setup lang="ts">
import { computed, ref } from 'vue'
import { CheckOutlined, CopyOutlined } from '@ant-design/icons-vue'
import DOMPurify from 'dompurify'
import { marked, Renderer, type Tokens } from 'marked'

const props = defineProps<{
  content: string
}>()

const copiedTarget = ref<string>()
let copiedTimer: ReturnType<typeof setTimeout> | undefined

const escapeHtml = (value: string) =>
  value.replace(
    /[&<>"]/g,
    (character) =>
      ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
      })[character] ?? character,
  )

const codeBlocks = computed(() => {
  const blocks: string[] = []
  const renderer = new Renderer()
  renderer.code = ({ text, lang }: Tokens.Code) => {
    const index = blocks.push(text) - 1
    const language = lang?.trim().split(/\s+/)[0] || 'code'
    return `
        <div class="markdown-code-block">
          <div class="markdown-code-block__toolbar">
            <span>${escapeHtml(language)}</span>
            <button type="button" data-copy-code-index="${index}">复制代码</button>
          </div>
          <pre><code class="language-${escapeHtml(language)}">${escapeHtml(text)}</code></pre>
        </div>
      `
  }

  const html = marked.parse(props.content || '', {
    breaks: true,
    gfm: true,
    renderer,
  }) as string

  return {
    blocks,
    html: DOMPurify.sanitize(html, {
      ADD_ATTR: ['data-copy-code-index'],
    }),
  }
})

const copyText = async (text: string, target: string) => {
  try {
    await navigator.clipboard.writeText(text)
  } catch {
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    textarea.remove()
  }

  copiedTarget.value = target
  if (copiedTimer) window.clearTimeout(copiedTimer)
  copiedTimer = window.setTimeout(() => {
    copiedTarget.value = undefined
  }, 1600)
}

const copyAll = () => copyText(props.content, 'all')

const handleContentClick = (event: MouseEvent) => {
  const button = (event.target as HTMLElement).closest<HTMLButtonElement>('[data-copy-code-index]')
  if (!button) return

  const index = Number(button.dataset.copyCodeIndex)
  const code = codeBlocks.value.blocks[index]
  if (code === undefined) return

  void copyText(code, `code-${index}`)
  button.textContent = '已复制'
  window.setTimeout(() => {
    button.textContent = '复制代码'
  }, 1600)
}
</script>

<template>
  <div class="markdown-message">
    <button
      class="markdown-message__copy"
      type="button"
      :aria-label="copiedTarget === 'all' ? '已复制全部回复' : '复制全部回复'"
      @click="copyAll"
    >
      <CheckOutlined v-if="copiedTarget === 'all'" />
      <CopyOutlined v-else />
      {{ copiedTarget === 'all' ? '已复制' : '复制' }}
    </button>
    <div
      class="markdown-message__content"
      @click="handleContentClick"
      v-html="codeBlocks.html"
    ></div>
  </div>
</template>

<style scoped>
.markdown-message {
  position: relative;
  min-width: 0;
}

.markdown-message__copy {
  position: absolute;
  top: -7px;
  right: -8px;
  z-index: 1;
  display: inline-flex;
  gap: 5px;
  align-items: center;
  padding: 4px 8px;
  color: #64748b;
  font: inherit;
  font-size: 11px;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid #dbe3ee;
  border-radius: 7px;
  opacity: 0;
  transition:
    color 0.2s ease,
    opacity 0.2s ease;
}

.markdown-message:hover .markdown-message__copy,
.markdown-message__copy:focus-visible {
  opacity: 1;
}

.markdown-message__copy:hover {
  color: #1677ff;
}

.markdown-message__content {
  min-width: 0;
  overflow-wrap: anywhere;
}

.markdown-message__content :deep(> :first-child) {
  margin-top: 0;
}

.markdown-message__content :deep(> :last-child) {
  margin-bottom: 0;
}

.markdown-message__content :deep(p),
.markdown-message__content :deep(li) {
  font-size: 14px;
  line-height: 1.75;
}

.markdown-message__content :deep(p) {
  margin: 0 0 10px;
}

.markdown-message__content :deep(h1),
.markdown-message__content :deep(h2),
.markdown-message__content :deep(h3),
.markdown-message__content :deep(h4) {
  margin: 18px 0 8px;
  color: #172033;
  line-height: 1.4;
}

.markdown-message__content :deep(h1) {
  font-size: 20px;
}
.markdown-message__content :deep(h2) {
  font-size: 18px;
}
.markdown-message__content :deep(h3),
.markdown-message__content :deep(h4) {
  font-size: 16px;
}

.markdown-message__content :deep(ul),
.markdown-message__content :deep(ol) {
  padding-left: 22px;
  margin: 8px 0 12px;
}

.markdown-message__content :deep(a) {
  color: #1677ff;
}

.markdown-message__content :deep(blockquote) {
  padding: 8px 12px;
  margin: 10px 0;
  color: #64748b;
  background: #f8fafc;
  border-left: 3px solid #91caff;
}

.markdown-message__content :deep(code:not(pre code)) {
  padding: 2px 5px;
  color: #d4380d;
  font-family: 'Cascadia Code', Consolas, monospace;
  font-size: 0.9em;
  background: #fff2e8;
  border-radius: 5px;
}

.markdown-message__content :deep(.markdown-code-block) {
  margin: 12px 0;
  overflow: hidden;
  background: #111827;
  border: 1px solid #243147;
  border-radius: 10px;
}

.markdown-message__content :deep(.markdown-code-block__toolbar) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 36px;
  padding: 0 10px 0 13px;
  color: #94a3b8;
  font-size: 11px;
  background: #1e293b;
  border-bottom: 1px solid #334155;
}

.markdown-message__content :deep(.markdown-code-block__toolbar button) {
  padding: 4px 8px;
  color: #cbd5e1;
  font: inherit;
  cursor: pointer;
  background: transparent;
  border: 1px solid #475569;
  border-radius: 6px;
}

.markdown-message__content :deep(.markdown-code-block__toolbar button:hover) {
  color: #fff;
  border-color: #60a5fa;
}

.markdown-message__content :deep(pre) {
  max-height: 440px;
  padding: 16px;
  margin: 0;
  overflow: auto;
}

.markdown-message__content :deep(pre code) {
  color: #e2e8f0;
  font-family: 'Cascadia Code', Consolas, monospace;
  font-size: 12px;
  line-height: 1.65;
  white-space: pre;
}

@media (max-width: 640px) {
  .markdown-message__copy {
    position: static;
    float: right;
    margin: 0 0 6px 8px;
    opacity: 1;
  }
}
</style>
