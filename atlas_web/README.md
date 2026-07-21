# Atlas Web（Vue 3）

寰宇投研 Web 版，对接 `atlas_backend` REST API，支持 **微信扫码登录**。

## 技术栈

- Vue 3 + Vite
- Vue Router + Pinia
- Axios（Bearer Token）

## 快速开始

```bash
# 1. 启动后端
cd atlas_backend
mvn -pl tts-spider spring-boot:run

# 2. 安装并启动 Web
cd atlas_web
npm install
npm run dev
```

浏览器打开 http://localhost:5173

### 开发环境登录

未配置微信开放平台时，后端 `atlas.auth.wx.dev-mode=true`（默认）：

1. 打开 `/login`
2. 点击 **「开发环境：模拟扫码成功」**

## 微信扫码登录（生产）

1. 在 [微信开放平台](https://open.weixin.qq.com/) 创建 **网站应用**
2. 配置授权回调域与回调 URL：`https://your-domain/tts/auth/wx/callback`
3. 在 `atlas_backend/tts-spider/src/main/resources/application.properties` 设置：

```properties
atlas.auth.wx.dev-mode=false
atlas.auth.wx.app-id=你的AppId
atlas.auth.wx.app-secret=你的AppSecret
atlas.auth.wx.redirect-uri=https://your-domain/tts/auth/wx/callback
atlas.auth.wx.web-front-url=https://your-domain
```

4. 用户访问 `/login`，扫描页面二维码，微信确认后自动跳转并完成登录

## API 端点（Auth）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/wx/qr/create` | 创建扫码会话 |
| GET | `/auth/wx/qr/poll?state=` | 轮询登录状态 |
| POST | `/auth/wx/qr/dev-confirm` | 开发模拟确认 |
| GET | `/auth/wx/callback` | 微信 OAuth 回调 |

## 目录

```
src/
  api/          HTTP 客户端、auth、stock
  domain/       策略导航（从小程序精简迁移）
  views/        页面
  layouts/      布局
  stores/       Pinia 状态
```

## 与小程序关系

- **共享后端** `atlas_backend`，同一套 findMy / klines / watchlist API
- Web 不受小程序内存限制，后续可接 ECharts 全量 K 线、虚拟列表
- 小程序 `atlas_wechat` 可继续保留移动端入口
