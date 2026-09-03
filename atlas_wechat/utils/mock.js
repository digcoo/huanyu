/**
 * 生成模拟 K 线数据
 * @param {number} count 数据点数量
 * @param {number} basePrice 基准价格
 * @param {number} volatility 波动率
 * @param {string} trend up | down | flat | breakout
 */
function generateKlineData(count, basePrice, volatility, trend = 'flat') {
  const data = [];
  let price = basePrice;
  const trendBias = { up: 0.003, down: -0.003, flat: 0, breakout: 0.006 };

  for (let i = 0; i < count; i++) {
    const bias = trendBias[trend] || 0;
    const change = (Math.random() - 0.48 + bias) * volatility;
    const open = price;
    const close = price * (1 + change);
    const high = Math.max(open, close) * (1 + Math.random() * volatility * 0.3);
    const low = Math.min(open, close) * (1 - Math.random() * volatility * 0.3);
    data.push({
      open: +open.toFixed(2),
      high: +high.toFixed(2),
      low: +low.toFixed(2),
      close: +close.toFixed(2)
    });
    price = close;
  }
  return data;
}

/** 归一化 K 线到 0-1 区间，便于多周期对比 */
function normalizeKlines(klines) {
  if (!klines.length) return [];
  let min = Infinity;
  let max = -Infinity;
  klines.forEach(k => {
    min = Math.min(min, k.low);
    max = Math.max(max, k.high);
  });
  const range = max - min || 1;
  return klines.map(k => ({
    open: (k.open - min) / range,
    high: (k.high - min) / range,
    low: (k.low - min) / range,
    close: (k.close - min) / range
  }));
}

/** 大盘指数多周期 K 线 */
function buildIndexKlines(basePrice, volatility, trend) {
  return {
    year: generateKlineData(50, basePrice * 0.82, volatility * 1.8, trend),
    month: generateKlineData(50, basePrice * 0.92, volatility * 1.3, trend),
    week: generateKlineData(50, basePrice, volatility, trend),
    day: generateKlineData(50, basePrice, volatility * 0.85, trend)
  };
}

const MARKETS = [
  { id: 'cn', name: 'A股', category: 'equity', icon: '🇨🇳' },
  { id: 'hk', name: '港股', category: 'equity', icon: '🇭🇰' },
  { id: 'us', name: '美股', category: 'equity', icon: '🇺🇸' },
  { id: 'crypto', name: '数字货币', category: 'alt', icon: '₿' },
  { id: 'futures', name: '期货', category: 'alt', icon: '📊' },
  { id: 'forex', name: '汇率', category: 'alt', icon: '💱' },
  { id: 'bond', name: '债券', category: 'alt', icon: '📈' }
];

const MARKET_INDICES = {
  cn: [
    { name: '上证指数', code: '000001', price: 3245.67, change: 1.23, changePct: 0.038, klines: buildIndexKlines(3200, 0.012, 'up') },
    { name: '深证成指', code: '399001', price: 10582.34, change: -15.67, changePct: -0.148, klines: buildIndexKlines(10600, 0.015, 'flat') },
    { name: '创业板指', code: '399006', price: 2134.56, change: 28.90, changePct: 1.37, klines: buildIndexKlines(2100, 0.02, 'up') }
  ],
  hk: [
    { name: '恒生指数', code: 'HSI', price: 17892.45, change: 234.56, changePct: 1.33, klines: buildIndexKlines(17600, 0.018, 'up') },
    { name: '恒生科技', code: 'HSTECH', price: 3845.23, change: 89.12, changePct: 2.37, klines: buildIndexKlines(3750, 0.025, 'breakout') },
    { name: '国企指数', code: 'HSCEI', price: 6234.78, change: -12.34, changePct: -0.20, klines: buildIndexKlines(6250, 0.014, 'flat') }
  ],
  us: [
    { name: '标普500', code: 'SPX', price: 5234.18, change: 45.67, changePct: 0.88, klines: buildIndexKlines(5180, 0.008, 'up') },
    { name: '纳斯达克', code: 'IXIC', price: 16456.23, change: 123.45, changePct: 0.76, klines: buildIndexKlines(16300, 0.012, 'up') },
    { name: '道琼斯', code: 'DJI', price: 39123.45, change: -89.12, changePct: -0.23, klines: buildIndexKlines(39200, 0.006, 'flat') }
  ],
  crypto: [
    { name: 'BTC/USDT', code: 'BTC', price: 67845.23, change: 1234.56, changePct: 1.85, klines: buildIndexKlines(66500, 0.025, 'breakout') },
    { name: 'ETH/USDT', code: 'ETH', price: 3456.78, change: 89.12, changePct: 2.65, klines: buildIndexKlines(3360, 0.03, 'up') },
    { name: '恐惧贪婪', code: 'FGI', price: 72, change: 5, changePct: 7.46, klines: buildIndexKlines(65, 0.05, 'up') }
  ],
  futures: [
    { name: '沪金主力', code: 'AU', price: 568.45, change: 3.21, changePct: 0.57, klines: buildIndexKlines(562, 0.008, 'up') },
    { name: '原油主力', code: 'SC', price: 612.34, change: -8.45, changePct: -1.36, klines: buildIndexKlines(620, 0.015, 'down') },
    { name: '沪深300期指', code: 'IF', price: 3845.6, change: 12.3, changePct: 0.32, klines: buildIndexKlines(3820, 0.01, 'flat') }
  ],
  forex: [
    { name: 'USD/CNY', code: 'USDCNY', price: 7.2345, change: 0.0123, changePct: 0.17, klines: buildIndexKlines(7.22, 0.002, 'flat') },
    { name: 'EUR/USD', code: 'EURUSD', price: 1.0876, change: -0.0023, changePct: -0.21, klines: buildIndexKlines(1.09, 0.003, 'down') },
    { name: 'DXY', code: 'DXY', price: 104.56, change: 0.34, changePct: 0.33, klines: buildIndexKlines(104.2, 0.004, 'up') }
  ],
  bond: [
    { name: '10Y国债', code: 'CN10Y', price: 2.345, change: -0.012, changePct: -0.51, klines: buildIndexKlines(2.36, 0.008, 'down') },
    { name: '10Y美债', code: 'US10Y', price: 4.456, change: 0.023, changePct: 0.52, klines: buildIndexKlines(4.42, 0.006, 'up') },
    { name: '10Y德债', code: 'DE10Y', price: 2.678, change: 0.015, changePct: 0.56, klines: buildIndexKlines(2.65, 0.005, 'up') }
  ]
};

const MARKET_SENTIMENT = {
  cn: { label: '市场宽度', value: '62%', sub: '上涨家数占比', extra: '成交额 8,234亿' },
  hk: { label: '南向资金', value: '+42.3亿', sub: '今日净流入', extra: '恒指PE 9.8x' },
  us: { label: 'VIX恐慌', value: '14.2', sub: '低波动区间', extra: '纳指宽度 68%' },
  crypto: { label: '全网爆仓', value: '$1.2亿', sub: '24h 多单', extra: 'FGI 72 贪婪' },
  futures: { label: '商品指数', value: '+0.8%', sub: '文华商品', extra: '持仓量 +3.2%' },
  forex: { label: '美元强弱', value: '104.56', sub: 'DXY 指数', extra: '人民币中间价 7.1023' },
  bond: { label: '利差', value: '211bp', sub: '中美10Y利差', extra: '收益率曲线 正常' }
};

const STRATEGIES = [
  { id: 'magoldbreakFlash', name: 'MA金叉点突破·30分' },
  { id: 'magoldbreakShort', name: 'MA金叉点突破·日' },
  { id: 'magoldbreakMedium', name: 'MA金叉点突破·周' },
  { id: 'magoldbreakLong', name: 'MA金叉点突破·月' },
  { id: 'magoldbreakQuarter', name: 'MA金叉点突破·季' },
  { id: 'madeathbreakFlash', name: 'MA死叉点突破·30分' },
  { id: 'madeathbreakShort', name: 'MA死叉点突破·日' },
  { id: 'madeathbreakMedium', name: 'MA死叉点突破·周' },
  { id: 'madeathbreakLong', name: 'MA死叉点突破·月' },
  { id: 'madeathbreakQuarter', name: 'MA死叉点突破·季' },
  { id: 'madcbreakFlash', name: '死叉交叉点突破·30分' },
  { id: 'madcbreakShort', name: '死叉交叉点突破·日' },
  { id: 'madcbreakMedium', name: '死叉交叉点突破·周' },
  { id: 'madcbreakLong', name: '死叉交叉点突破·月' },
  { id: 'madcbreakQuarter', name: '死叉交叉点突破·季' },
  { id: 'magcbreakFlash', name: '金叉交叉点突破·30分' },
  { id: 'magcbreakShort', name: '金叉交叉点突破·日' },
  { id: 'magcbreakMedium', name: '金叉交叉点突破·周' },
  { id: 'magcbreakLong', name: '金叉交叉点突破·月' },
  { id: 'magcbreakQuarter', name: '金叉交叉点突破·季' },
  { id: 'maghbreakFlash', name: '金叉波段顶突破·30分' },
  { id: 'maghbreakShort', name: '金叉波段顶突破·日' },
  { id: 'maghbreakMedium', name: '金叉波段顶突破·周' },
  { id: 'maghbreakLong', name: '金叉波段顶突破·月' },
  { id: 'maghbreakQuarter', name: '金叉波段顶突破·季' }
];

const { formatDataUpdatedLabel, todayDayStr } = require('./time');

function stock(strategy, market, code, name, price, changePct, tags, summary, resonance, trends, dataDay) {
  const vol = trends.vol || 0.03;
  const day = dataDay || todayDayStr();
  return {
    id: strategy + '-' + market + '-' + code,
    strategy,
    code,
    name,
    market,
    price,
    changePct,
    tags,
    summary,
    resonance,
    dataDay: day,
    dataUpdatedLabel: formatDataUpdatedLabel(day),
    klines: {
      year: generateKlineData(50, price * (trends.yearBase || 0.82), vol * 1.8, trends.year),
      month: generateKlineData(50, price * (trends.monthBase || 0.92), vol * 1.3, trends.month),
      week: generateKlineData(50, price * (trends.weekBase || 0.98), vol, trends.week),
      day: generateKlineData(50, price * (trends.dayBase || 0.99), vol * 0.85, trends.day),
      min30: generateKlineData(50, price * (trends.min30Base || 0.998), vol * 0.45, trends.min30 || trends.day)
    }
  };
}

function buildStrategyRecommendations() {
  return {};
}

/** @deprecated 兼容旧调用 */
function buildRecommendations() {
  return { cn: [], hk: [], us: [], crypto: [], futures: [], forex: [], bond: [] };
}

module.exports = {
  MARKETS,
  MARKET_INDICES,
  MARKET_SENTIMENT,
  STRATEGIES,
  buildStrategyRecommendations,
  buildRecommendations,
  generateKlineData,
  normalizeKlines
};
