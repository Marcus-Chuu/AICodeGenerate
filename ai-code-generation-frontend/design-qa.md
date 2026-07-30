# App 模块设计验收报告

## 验收范围

- 首页：提示词创建应用、我的应用、精选应用、名称搜索与分页。
- 应用生成对话页：流式消息区域、完成后的网站预览、部署入口。
- 应用信息修改页：普通用户仅编辑名称；管理员可编辑名称、封面和优先级。
- 应用管理页：管理员筛选、分页、编辑、删除、设置精选。

## 设计来源

- `design-qa-assets/reference-home-hero.png`（原型图 1，2000 × 1117）
- `design-qa-assets/reference-home-lists.png`（原型图 2，1843 × 1244）
- `design-qa-assets/reference-chat.png`（原型图 3，1870 × 1258）

## 浏览器证据

- `design-qa-assets/app-home-exact.png`：登录态首页，同尺寸桌面视口。
- `design-qa-assets/app-home-lists-exact.png`：登录态应用列表区域。
- `design-qa-assets/app-home-mobile.png`：390 × 844 移动端断点。
- `design-qa-assets/app-chat-exact.png`：已有应用的对话与真实静态网站预览。
- `design-qa-assets/app-edit-user.png`：普通用户编辑权限状态。
- `design-qa-assets/comparison-app-home.png`：首页原型与实现并排对照。
- `design-qa-assets/comparison-app-lists.png`：列表原型与实现并排对照。
- `design-qa-assets/comparison-app-chat.png`：对话页原型与实现并排对照。

## 交互与功能检查

- 提示词示例可以回填输入框；未登录创建会跳转登录页。
- 登录态成功请求自己的应用列表，应用卡片可进入对话页和编辑页。
- 对话页加载真实应用详情，并在右侧 iframe 展示 `/api/static/{codeGenType}_{appId}/`。
- 普通用户编辑页只渲染应用名称字段，没有封面与优先级字段。
- 390px 视口下首页使用单列卡片，没有横向溢出。
- 管理员路由、菜单可见性和接口分支已做静态检查；当前浏览器账号不是管理员，因此没有执行管理员写操作。

## 原型差异评估

- P3：沿用项目现有品牌 Logo，而不是原型中的黑猫标识。
- P3：首页保留现有全局导航栏，原型图 1 没有展示该区域。
- P3：应用封面由后端数据决定；当前测试数据没有封面，因此使用品牌背景作为回退图。
- P3：对话页不会伪造历史 AI 消息；当前会话只显示欢迎消息，新的 SSE 内容会在用户真实发送后出现。

上述差异来自现有产品品牌、真实数据状态和业务约束，不影响核心流程。

## 修复记录

- P1（已修复）：首次检查时对话工作区依赖父级百分比高度，导致下方出现大块空白。工作区改为 `calc(100vh - 68px)` 后，1870 × 1258 视口中从顶部栏下方连续铺满到窗口底部。
- P2（已修复）：输入框同时注册多个键盘监听导致 Vue 警告。合并为单一键盘处理器后警告消失。

## 工程检查

- `npm run build`：通过（包含 `vue-tsc --build` 与 Vite 生产构建）。
- ESLint（本次变更文件）：通过。
- `git diff --check`：通过。
- 构建仅保留 Vite 的大分包提示，不影响功能和发布。

final result: passed
