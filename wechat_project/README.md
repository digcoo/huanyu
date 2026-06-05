# Atlas · 寰宇全息投研助手

个人选股助手微信小程序 Demo（Mock 数据）

## 功能概览

### 首页（已完成）
- 多市场滑动切换：A股 / 港股 / 美股 / 数字货币 / 期货 / 汇率 / 债券
- 各市场大盘指数 K 线对比图 + 市场情绪指标
- 策略推荐卡片流：年/月/周/日 四宫格迷你 K 线矩阵
- 核心标签云 + 逻辑摘要
- 左滑忽略 / 右滑加入自选 / 点击进入详情

### 详情页 / 自选页（占位）
- 暂留白，后续迭代

## 运行方式

1. 下载并安装 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 打开微信开发者工具 → 导入项目
3. 目录选择：`d:\workspaces\huanyu_wealth`
4. AppID 选择「测试号」或「游客模式」
5. 点击编译预览

## 项目结构

```
huanyu_wealth/
├── app.js / app.json / app.wxss    # 全局配置
├── utils/
│   ├── mock.js                     # Mock 数据
│   └── kline.js                    # Canvas K线绘制
├── components/
│   ├── market-tabs/                # 市场切换器
│   ├── index-chart/                # 大盘指数图
│   ├── mini-kline/                 # 单周期迷你K线
│   ├── mini-kline-matrix/          # 四宫格K线矩阵
│   ├── strategy-card/              # 策略推荐卡片（含滑动手势）
│   └── bottom-nav/                 # 底部导航
└── pages/
    ├── index/                      # 首页 ★
    ├── detail/                     # 详情页（占位）
    └── watchlist/                  # 自选页（占位）
```

## 视觉规范

- 暗黑底色 `#0a0e17`
- 荧光蓝 `#00d4ff` / 琥珀橙 `#ff9500` 高亮
- K 线红绿低饱和度（A 股习惯：红涨绿跌）

## 下一步

1. 详情页：四联屏 K 线 + 企业四维罗盘
2. 自选页：分市场列表 + 自选时间戳标记
3. 接入真实行情 / 财务数据源
