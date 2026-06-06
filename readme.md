# Atlas monorepo

| 目录 | 定位 |
|------|------|
| `atlas_demo/` | MVP 演示版（Mock 数据，产品形态已验证，冻结维护） |
| `atlas_wechat/` | 正式微信小程序（对接 `atlas_backend`，待初始化） |
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
