# Atlas · 寰宇全息投研助手（正式版）

对接 `atlas_backend` 的微信小程序开发工程。UI 从 `atlas_demo` fork，逐步替换 Mock 为真实 API。

## 与 demo 的差异

| 项 | atlas_demo | atlas_wechat |
|----|-----------|--------------|
| 数据 | 全 Mock | API 优先（`utils/config.js` 可切 `useMock`） |
| 市场 Tab | 全部可点 | 非 A 股 **置灰**，点击提示「即将开放」 |
| 自选/历史 | 本地 storage | **必须微信登录** |
| 网络层 | 无 | `utils/api.js` · `auth.js` · `adapter.js` |

## 运行

1. 微信开发者工具 → 导入 **`d:\workspaces\huanyu\atlas_wechat`**
2. 本地联调 backend 时：`utils/config.js` 设 `useMock: false`，并勾选「不校验合法域名」
3. Backend：`cd atlas_backend && mvn spring-boot:run`（端口 9010，路径 `/tts`）

## 配置

`utils/config.js`：

- `enabledMarkets: ['cn']` — Phase 1 仅 A 股可用
- `requireLoginForWatchlist: true` — 自选需登录
- `useMock: true` — Phase 0 默认 Mock，backend 就绪后改 `false`

## 目录

```
utils/
  config.js    # 环境与产品开关
  api.js       # wx.request 封装
  auth.js      # 微信登录 / token
  adapter.js   # 后端 DTO 映射
  markets.js   # 市场 Tab 启用/置灰
  mock.js      # 过渡 Mock（逐步废弃）
```

详见 [`docs/项目实施方案.md`](../docs/项目实施方案.md)
