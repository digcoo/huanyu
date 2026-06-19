# Atlas 策略回测 Web 客户端

基于 `atlas_backend` 回测 API 的独立 Web 界面，用于在历史 K 线上检验自定义策略的胜率与持有期收益。

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

开发模式下 Vite 会将 `/tts` 代理到 `http://localhost:9010`。

## API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/tts/backtest/health` | 缓存是否就绪 |
| GET | `/tts/backtest/strategies` | 可回测策略列表 |
| POST | `/tts/backtest/run` | 执行回测 |

### 回测请求示例

```json
{
  "strategy": "gc2",
  "days": 365,
  "holdDays": 10,
  "maxStocks": 200,
  "winThreshold": 0,
  "codes": "600519,600961"
}
```

## 回测逻辑说明

- 对每只股票逐日截断 K 线至该日，复用与线上一致的策略 `check()` 逻辑
- 信号日收盘价作为买入价，持有 N 个交易日后收盘价作为卖出价
- 同一股票两次信号间隔默认等于持有期，避免重叠样本
- 胜率 = 收益率高于阈值的交易笔数 / 总信号数

## 生产部署

```bash
npm run build
```

将 `dist/` 目录部署到任意静态服务器；需配置反向代理将 `/tts` 转发至后端 API。

或通过环境变量指定 API 地址：

```bash
VITE_API_BASE=http://your-host:9010/tts npm run build
```
