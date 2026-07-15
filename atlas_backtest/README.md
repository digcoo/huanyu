# Atlas 回测工作站

专业 Web 端策略回测界面（Vue 3 + ECharts），对接 `atlas_backend` 回测 API，在历史 K 线上逐日复用与实盘相同的策略引擎，统计持有期收益与量化指标。

## 功能模块

| 模块 | 说明 |
|------|------|
| **配置面板** | 策略选择、回测窗口、持有期、扫描上限、盈利阈值、信号冷却、指定股票 |
| **概览 KPI** | 胜率、信号数、平均/期望收益、盈亏比、最大回撤、极值盈亏 |
| **收益曲线** | 累计收益走势、月度均收益柱状图 |
| **分布分析** | 收益率直方图、档位（tier）分组统计 |
| **交易明细** | 可搜索/排序的完整信号列表，支持 CSV 导出 |

## 前置条件

1. MySQL 中已有 K 线数据
2. 后端 `tts-spider` 已启动（默认 `http://localhost:9010/tts`）
3. `RealtimeStockCache` 已加载（启动日志中可见 filterStockMap 数量）

## 启动

```bash
cd atlas_backtest
npm install
npm run dev
```

浏览器打开 http://localhost:5174

开发模式下 Vite 将 `/tts` 代理到 `http://localhost:9010`。

## API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/tts/backtest/health` | 缓存是否就绪 |
| GET | `/tts/backtest/strategies` | 可回测策略列表（全部活跃策略） |
| POST | `/tts/backtest/run` | 执行回测 |

### 回测请求示例

```json
{
  "strategy": "wavebandShort",
  "days": 365,
  "holdDays": 10,
  "maxStocks": 200,
  "winThreshold": 0,
  "signalCooldown": 0,
  "codes": "600519,600961"
}
```

## 回测逻辑

- 对每只股票逐日截断 K 线至该日，复用与线上一致的策略 `check()` 逻辑
- 信号日收盘价作为买入价，持有 N 个交易日后收盘价作为卖出价
- 同一股票两次信号间隔默认等于持有期（可通过 `signalCooldown` 调整），避免重叠样本
- 胜率 = 收益率高于 `winThreshold` 的交易笔数 / 总信号数

## 生产部署

```bash
npm run build
```

将 `dist/` 部署到任意静态服务器；需配置反向代理将 `/tts` 转发至后端 API。

或通过环境变量指定 API 地址：

```bash
VITE_API_BASE=http://your-host:9010/tts npm run build
```
