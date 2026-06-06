# Atlas monorepo

| 目录 | 定位 |
|------|------|
| `atlas_demo/` | MVP 演示版（Mock 数据，产品形态已验证，冻结维护） |
| `atlas_wechat/` | 正式微信小程序（Phase 0 已初始化，对接 `atlas_backend`） |
| `atlas_backend/` | Java 后端（Spring Boot，原 tts） |

## 项目实施方案

详见 [`docs/项目实施方案.md`](docs/项目实施方案.md)

## 微信开发者工具

- **演示：** `d:\workspaces\huanyu\atlas_demo`
- **开发：** `d:\workspaces\huanyu\atlas_wechat`（从 demo 复制后启动，见实施方案 Phase 0）

## 后端

```bash
cd atlas_backend
mvn compile -DskipTests
```

入口类：`com.yh.bigdata.tts.spider.ApplicationStarter` · 默认端口 `9010`，context-path `/tts`

新增 Atlas API（Phase 1）：

```text
GET  /tts/stock/health
GET  /tts/stock/search?q=茅台&limit=20
GET  /tts/stock/{code}
GET  /tts/stock/{code}/klines?period=week&limit=50
GET  /tts/stock/{code}/detail
GET  /tts/stock/{code}/compass
POST /tts/auth/wx/login  { "code": "..." }
```

启动前需本地 MySQL `tts` 库有数据；`application.properties` 已默认 `atlas.cache.enabled=true`。
