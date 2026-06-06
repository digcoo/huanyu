# Atlas · 寰宇全息投研助手（MVP 演示版）

> **冻结维护**：产品形态已验证，仅修 bug，不叠加新需求。正式开发见 `atlas_wechat/`。

个人选股助手微信小程序 Demo（100% Mock 数据）
## 功能概览

- **策略**：多市场、策略推荐、K 线、搜索
- **自选**：跟踪列表、周期同步、左滑移除
- **历史**：自选持有期盈亏复盘 + K 线
- **详情**：价格/K 线、企业速览、画像、行业对标、四维罗盘

## 运行方式

1. 下载并安装 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 打开微信开发者工具 → 导入项目
3. 目录选择：`d:\workspaces\huanyu\atlas_demo`
4. AppID 选择「测试号」或「游客模式」
5. 点击编译预览

## 项目结构

```
atlas_demo/
├── app.js / app.json / app.wxss
├── utils/              # mock、自选、历史、搜索
├── components/         # K 线、策略卡片、搜索浮层等
└── pages/
    ├── index/          # 策略
    ├── watchlist/      # 自选
    ├── history/        # 历史复盘
    └── detail/         # 详情
```

## 视觉规范

- 暗黑底色 `#0a0e17`
- 荧光蓝 `#00d4ff` / 琥珀橙 `#ff9500` 高亮
- A 股习惯：**红涨绿跌**

## 后端对接

后端工程见同级目录 `atlas_backend/`（Mock 阶段未打通 API）。
