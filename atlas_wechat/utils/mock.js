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
  {
    id: 'trend',
    name: '单边趋势',
    icon: '📈',
    iconImage: '/assets/strategy/trend-icon.png',
    badge: 'TREND',
    desc: '梯子试盘 · 均线多头 · 突破延续'
  },
  {
    id: 'rebound',
    name: '深坑反弹',
    icon: '🕳️',
    iconImage: '/assets/strategy/rebound-icon.png',
    badge: 'REBOUND',
    desc: '长期深跌 · 恐慌释放 · 反弹接入'
  },
  {
    id: 'multi',
    name: '多周期强势',
    icon: '⚡',
    badge: 'MULTI',
    desc: '日周月共振 · 多周期对齐 · 强势延续'
  }
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
      day: generateKlineData(50, price * (trends.dayBase || 0.99), vol * 0.85, trends.day)
    }
  };
}

function buildStrategyRecommendations() {
  if (buildStrategyRecommendations._cache) return buildStrategyRecommendations._cache;

  buildStrategyRecommendations._cache = {
    trend: {
      cn: [
        stock('trend', 'cn', '600519', '贵州茅台', 1688, 2.35,
          ['均线多头', '趋势延续', '北向流入'], '消费龙头，日周均线多头排列，趋势单边上行', 'strong',
          { year: 'up', month: 'up', week: 'breakout', day: 'up', vol: 0.015 }),
        stock('trend', 'cn', '688981', '中芯国际', 48.92, 3.45,
          ['突破平台', '半导体', '量价齐升'], '突破前高后缩量回踩，趋势结构完好', 'strong',
          { year: 'up', month: 'up', week: 'breakout', day: 'up', vol: 0.03 }),
        stock('trend', 'cn', '300750', '宁德时代', 198.56, 1.86,
          ['锂电龙头', '趋势加速', '机构增持'], '周线三连阳，均线呈单边发散', 'medium',
          { year: 'up', month: 'up', week: 'up', day: 'up', vol: 0.025 })
      ],
      hk: [
        stock('trend', 'hk', '00700', '腾讯控股', 378.4, 2.12,
          ['回购加码', '突破年线', '趋势强化'], '港股权重龙头，均线多头排列', 'strong',
          { year: 'up', month: 'up', week: 'breakout', day: 'up', vol: 0.025 }),
        stock('trend', 'hk', '01810', '小米集团', 18.92, 4.23,
          ['汽车放量', '趋势突破', 'IoT生态'], 'SU7 催化下形成单边上升趋势', 'strong',
          { year: 'up', month: 'breakout', week: 'up', day: 'up', vol: 0.03 }),
        stock('trend', 'hk', '09992', '泡泡玛特', 68.5, 3.18,
          ['IP龙头', '出海加速', 'MEGA放量'], 'Labubu 全球破圈，海外营收高增，趋势结构完好', 'strong',
          { year: 'up', month: 'breakout', week: 'up', day: 'up', vol: 0.035 })
      ],
      us: [
        stock('trend', 'us', 'NVDA', '英伟达', 875.28, 2.89,
          ['AI算力', '趋势龙头', '财报超预期'], '数据中心营收高增，多均线单边向上', 'strong',
          { year: 'up', month: 'up', week: 'breakout', day: 'up', vol: 0.04 }),
        stock('trend', 'us', 'AAPL', '苹果', 189.45, 0.84,
          ['服务增长', '稳健趋势', '生态壁垒'], '慢牛结构，趋势斜率稳定', 'medium',
          { year: 'up', month: 'flat', week: 'up', day: 'up', vol: 0.02 })
      ],
      crypto: [
        stock('trend', 'crypto', 'BTC', 'Bitcoin', 67845.23, 1.85,
          ['减半周期', 'ETF流入', '趋势延续'], 'ETF 连续净流入，中期趋势未破坏', 'strong',
          { year: 'up', month: 'up', week: 'breakout', day: 'up', vol: 0.05 })
      ],
      futures: [
        stock('trend', 'futures', 'AU2506', '沪金主力', 568.45, 0.57,
          ['避险需求', '趋势上行', '央行购金'], '地缘风险推升，黄金维持单边强势', 'medium',
          { year: 'up', month: 'up', week: 'up', day: 'flat', vol: 0.02 })
      ],
      forex: [
        stock('trend', 'forex', 'USDCNY', '美元/人民币', 7.2345, 0.17,
          ['利差驱动', '趋势偏强', '贸易顺差'], '美元阶段性偏强，汇率趋势延续', 'medium',
          { year: 'up', month: 'flat', week: 'flat', day: 'up', vol: 0.005 })
      ],
      bond: [
        stock('trend', 'bond', 'US10Y', '10Y美债', 4.456, 0.52,
          ['收益率上行', '通胀预期', '趋势偏空债券'], '美债收益率单边上行，价格趋势下行', 'medium',
          { yearBase: 1.02, monthBase: 1.01, weekBase: 1.005, dayBase: 1.002, year: 'up', month: 'up', week: 'up', day: 'up', vol: 0.006 })
      ]
    },
    rebound: {
      cn: [
        stock('rebound', 'cn', '601318', '中国平安', 52.34, 2.15,
          ['深坑探底', '底背离', '价值回归'], '估值历史低位，月线 MACD 底背离，反弹初现', 'medium',
          { year: 'down', month: 'flat', week: 'up', day: 'up', vol: 0.02 }),
        stock('rebound', 'cn', '000858', '五粮液', 128.6, 1.92,
          ['超跌反弹', '白酒复苏', '深坑修复'], '深度回调后放量阳线，反弹结构确认', 'medium',
          { year: 'down', month: 'down', week: 'flat', day: 'up', vol: 0.022 }),
        stock('rebound', 'cn', '601012', '隆基绿能', 18.45, 3.68,
          ['深坑反弹', '光伏龙头', '产能出清'], '行业低谷后首次周线阳包阴，反弹信号', 'medium',
          { year: 'down', month: 'down', week: 'up', day: 'breakout', vol: 0.035 })
      ],
      hk: [
        stock('rebound', 'hk', '09988', '阿里巴巴', 78.65, 2.36,
          ['深坑修复', '估值修复', '云计算'], '港股深坑后放量反弹，云业务边际改善', 'medium',
          { year: 'down', month: 'flat', week: 'up', day: 'up', vol: 0.03 }),
        stock('rebound', 'hk', '03690', '美团', 112.3, 1.78,
          ['超跌反弹', '本地生活', '盈利修复'], '深坑区企稳，反弹波段开启', 'medium',
          { year: 'down', month: 'flat', week: 'up', day: 'flat', vol: 0.028 })
      ],
      us: [
        stock('rebound', 'us', 'TSLA', '特斯拉', 245.67, 3.25,
          ['深坑反弹', 'FSD预期', '高弹性'], '深度回调后 V 型反弹，短线动能恢复', 'medium',
          { year: 'flat', month: 'down', week: 'up', day: 'breakout', vol: 0.04 }),
        stock('rebound', 'us', 'BABA', '阿里巴巴 ADR', 78.2, 2.88,
          ['中概反弹', '深坑修复', '回购支撑'], '中概深坑后集体反弹，估值极端压缩', 'medium',
          { year: 'down', month: 'flat', week: 'up', day: 'up', vol: 0.035 })
      ],
      crypto: [
        stock('rebound', 'crypto', 'ETH', 'Ethereum', 3456.78, 4.12,
          ['深坑反弹', 'Layer2', '生态修复'], 'BTC 带动下的深坑反弹，ETH 弹性更大', 'medium',
          { year: 'down', month: 'flat', week: 'up', day: 'up', vol: 0.03 })
      ],
      futures: [
        stock('rebound', 'futures', 'SC2506', '原油主力', 612.34, 2.45,
          ['深坑反弹', 'OPEC支撑', '库存回落'], '需求担忧后的深坑反弹，短线技术修复', 'medium',
          { year: 'down', month: 'down', week: 'up', day: 'up', vol: 0.03 })
      ],
      forex: [],
      bond: [
        stock('rebound', 'bond', 'CN10Y', '10年期国债', 2.345, -0.51,
          ['收益率下行', '宽松预期', '深坑后反弹'], '降准预期下债券深坑反弹，收益率下行', 'medium',
          { year: 'down', month: 'down', week: 'down', day: 'flat', vol: 0.01 })
      ]
    },
    multi: {
      cn: [
        stock('multi', 'cn', '600519', '贵州茅台', 1688, 2.35,
          ['多周期共振', '日周月对齐', '外资流入'], '日/周/月三周期同步走强，共振信号强', 'strong',
          { year: 'up', month: 'up', week: 'breakout', day: 'up', vol: 0.015 }),
        stock('multi', 'cn', '688981', '中芯国际', 48.92, 3.45,
          ['三周期共振', '国产替代', '半导体'], '芯片板块资金流入，多周期均线同步上穿', 'strong',
          { year: 'up', month: 'up', week: 'breakout', day: 'up', vol: 0.03 }),
        stock('multi', 'cn', '300750', '宁德时代', 198.56, -1.23,
          ['多周期强势', '产能扩张', '净利断层'], 'Q3 业绩超预期，多周期结构仍偏强', 'strong',
          { year: 'up', month: 'flat', week: 'breakout', day: 'flat', vol: 0.025 })
      ],
      hk: [
        stock('multi', 'hk', '00700', '腾讯控股', 378.4, 2.12,
          ['多周期共振', '游戏复苏', '南向流入'], '日周共振突破，南向连续 5 日净流入', 'strong',
          { year: 'up', month: 'up', week: 'breakout', day: 'up', vol: 0.025 }),
        stock('multi', 'hk', '01810', '小米集团', 18.92, 4.23,
          ['四周期对齐', '汽车放量', '突破平台'], 'SU7 交付超预期，多周期强势共振', 'strong',
          { year: 'up', month: 'breakout', week: 'up', day: 'up', vol: 0.03 })
      ],
      us: [
        stock('multi', 'us', 'NVDA', '英伟达', 875.28, 2.89,
          ['多周期强势', 'AI算力', '机构增持'], '数据中心营收 +154%，多周期均线多头排列', 'strong',
          { year: 'up', month: 'up', week: 'breakout', day: 'up', vol: 0.04 }),
        stock('multi', 'us', 'MSFT', '微软', 425.6, 1.56,
          ['云+AI共振', '多周期强势', '盈利稳定'], 'Azure 与 Copilot 双轮驱动，多周期结构健康', 'strong',
          { year: 'up', month: 'up', week: 'up', day: 'up', vol: 0.018 })
      ],
      crypto: [
        stock('multi', 'crypto', 'BTC', 'Bitcoin', 67845.23, 1.85,
          ['多周期共振', 'ETF流入', '链上活跃'], 'ETF 连续净流入，链上地址数创新高', 'strong',
          { year: 'up', month: 'up', week: 'breakout', day: 'up', vol: 0.05 }),
        stock('multi', 'crypto', 'SOL', 'Solana', 156.78, 5.67,
          ['生态爆发', '多周期强势', '高TPS'], 'DEX 交易量超越以太坊，开发者活跃度第一', 'strong',
          { year: 'up', month: 'up', week: 'breakout', day: 'up', vol: 0.06 })
      ],
      futures: [
        stock('multi', 'futures', 'AU2506', '沪金主力', 568.45, 0.57,
          ['多周期偏强', '避险需求', '央行购金'], '地缘风险升温，多周期黄金结构偏强', 'medium',
          { year: 'up', month: 'up', week: 'up', day: 'flat', vol: 0.02 })
      ],
      forex: [
        stock('multi', 'forex', 'USDCNY', '美元/人民币', 7.2345, 0.17,
          ['多周期对齐', '利差驱动', '贸易顺差'], '中美利差收窄，人民币中间价强于预期', 'medium',
          { year: 'up', month: 'flat', week: 'flat', day: 'up', vol: 0.005 })
      ],
      bond: [
        stock('multi', 'bond', 'CN10Y', '10年期国债', 2.345, -0.51,
          ['多周期下行', '宽松预期', '配置窗口'], '央行降准预期升温，长端利率多周期下行', 'medium',
          { year: 'down', month: 'down', week: 'down', day: 'flat', vol: 0.01 })
      ]
    }
  };

  return buildStrategyRecommendations._cache;
}

/** @deprecated 兼容旧调用，返回当前默认策略（多周期强势）数据 */
function buildRecommendations() {
  const all = buildStrategyRecommendations();
  return all.multi;
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
