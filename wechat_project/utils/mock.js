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

function buildRecommendations() {
  if (buildRecommendations._cache) return buildRecommendations._cache;

  buildRecommendations._cache = {
    cn: [
      {
        id: 'cn-600519',
        code: '600519',
        name: '贵州茅台',
        market: 'cn',
        price: 1688.00,
        changePct: 2.35,
        tags: ['高股息', '月线MACD金叉', '外资流入'],
        summary: '消费龙头，日周共振突破，北向连续3日净流入',
        resonance: 'strong',
        klines: {
          year: (generateKlineData(50, 1400, 0.08, 'up')),
          month: (generateKlineData(50, 1600, 0.04, 'up')),
          week: (generateKlineData(50, 1650, 0.025, 'breakout')),
          day: (generateKlineData(50, 1680, 0.015, 'up'))
        }
      },
      {
        id: 'cn-300750',
        code: '300750',
        name: '宁德时代',
        market: 'cn',
        price: 198.56,
        changePct: -1.23,
        tags: ['净利润断层', '产能扩张', '锂电龙头'],
        summary: 'Q3业绩超预期，周线平台突破后回踩确认',
        resonance: 'medium',
        klines: {
          year: (generateKlineData(50, 180, 0.12, 'up')),
          month: (generateKlineData(50, 190, 0.06, 'flat')),
          week: (generateKlineData(50, 195, 0.04, 'breakout')),
          day: (generateKlineData(50, 198, 0.025, 'flat'))
        }
      },
      {
        id: 'cn-601318',
        code: '601318',
        name: '中国平安',
        market: 'cn',
        price: 52.34,
        changePct: 0.87,
        tags: ['价值回归', '低估值', '保险复苏'],
        summary: '估值处于历史10%分位，月线底背离形成',
        resonance: 'medium',
        klines: {
          year: (generateKlineData(50, 55, 0.1, 'down')),
          month: (generateKlineData(50, 50, 0.05, 'flat')),
          week: (generateKlineData(50, 51, 0.03, 'up')),
          day: (generateKlineData(50, 52, 0.02, 'up'))
        }
      },
      {
        id: 'cn-688981',
        code: '688981',
        name: '中芯国际',
        market: 'cn',
        price: 48.92,
        changePct: 3.45,
        tags: ['国产替代', '突破平台', '半导体'],
        summary: '日周月三周期共振，芯片板块资金持续流入',
        resonance: 'strong',
        klines: {
          year: (generateKlineData(50, 42, 0.15, 'up')),
          month: (generateKlineData(50, 45, 0.08, 'up')),
          week: (generateKlineData(50, 47, 0.05, 'breakout')),
          day: (generateKlineData(50, 48, 0.03, 'up'))
        }
      }
    ],
    hk: [
      {
        id: 'hk-00700',
        code: '00700',
        name: '腾讯控股',
        market: 'hk',
        price: 378.40,
        changePct: 2.12,
        tags: ['回购加码', '游戏复苏', '突破年线'],
        summary: '港股权重股，日周共振突破，南向连续5日净流入',
        resonance: 'strong',
        klines: {
          year: (generateKlineData(50, 300, 0.12, 'up')),
          month: (generateKlineData(50, 350, 0.06, 'up')),
          week: (generateKlineData(50, 370, 0.04, 'breakout')),
          day: (generateKlineData(50, 375, 0.025, 'up'))
        }
      },
      {
        id: 'hk-09988',
        code: '09988',
        name: '阿里巴巴',
        market: 'hk',
        price: 78.65,
        changePct: 1.56,
        tags: ['云计算', '估值修复', '电商回暖'],
        summary: '云业务增速回升，港股通资金持续加仓',
        resonance: 'medium',
        klines: {
          year: (generateKlineData(50, 70, 0.14, 'flat')),
          month: (generateKlineData(50, 75, 0.07, 'up')),
          week: (generateKlineData(50, 77, 0.04, 'up')),
          day: (generateKlineData(50, 78, 0.03, 'flat'))
        }
      },
      {
        id: 'hk-01810',
        code: '01810',
        name: '小米集团',
        market: 'hk',
        price: 18.92,
        changePct: 4.23,
        tags: ['IoT生态', '汽车放量', '突破平台'],
        summary: 'SU7交付超预期，周线三连阳突破前高',
        resonance: 'strong',
        klines: {
          year: (generateKlineData(50, 12, 0.18, 'up')),
          month: (generateKlineData(50, 16, 0.08, 'breakout')),
          week: (generateKlineData(50, 18, 0.05, 'up')),
          day: (generateKlineData(50, 18.5, 0.03, 'up'))
        }
      }
    ],
    us: [
      {
        id: 'us-NVDA',
        code: 'NVDA',
        name: '英伟达',
        market: 'us',
        price: 875.28,
        changePct: 2.89,
        tags: ['AI算力', '财报超预期', '机构增持'],
        summary: '数据中心营收+154%，多周期均线多头排列',
        resonance: 'strong',
        klines: {
          year: (generateKlineData(50, 450, 0.2, 'up')),
          month: (generateKlineData(50, 800, 0.1, 'up')),
          week: (generateKlineData(50, 850, 0.06, 'breakout')),
          day: (generateKlineData(50, 870, 0.04, 'up'))
        }
      },
      {
        id: 'us-AAPL',
        code: 'AAPL',
        name: '苹果',
        market: 'us',
        price: 189.45,
        changePct: 0.34,
        tags: ['服务增长', '高股息', '生态壁垒'],
        summary: 'Vision Pro 销量爬坡，服务业务占比创新高',
        resonance: 'medium',
        klines: {
          year: (generateKlineData(50, 170, 0.08, 'up')),
          month: (generateKlineData(50, 185, 0.04, 'flat')),
          week: (generateKlineData(50, 188, 0.03, 'flat')),
          day: (generateKlineData(50, 189, 0.02, 'up'))
        }
      },
      {
        id: 'us-TSLA',
        code: 'TSLA',
        name: '特斯拉',
        market: 'us',
        price: 245.67,
        changePct: -2.15,
        tags: ['FSD进展', '产能利用率', '高波动'],
        summary: 'Robotaxi 发布预期升温，但短期面临交付压力',
        resonance: 'weak',
        klines: {
          year: (generateKlineData(50, 250, 0.15, 'flat')),
          month: (generateKlineData(50, 240, 0.08, 'down')),
          week: (generateKlineData(50, 248, 0.05, 'flat')),
          day: (generateKlineData(50, 246, 0.04, 'down'))
        }
      }
    ],
    crypto: [
      {
        id: 'crypto-BTC',
        code: 'BTC',
        name: 'Bitcoin',
        market: 'crypto',
        price: 67845.23,
        changePct: 1.85,
        tags: ['减半周期', 'ETF流入', '链上活跃'],
        summary: 'ETF 连续12日净流入，链上持币地址数创新高',
        resonance: 'strong',
        klines: {
          year: (generateKlineData(50, 42000, 0.25, 'up')),
          month: (generateKlineData(50, 60000, 0.12, 'up')),
          week: (generateKlineData(50, 65000, 0.08, 'breakout')),
          day: (generateKlineData(50, 67000, 0.05, 'up'))
        }
      },
      {
        id: 'crypto-SOL',
        code: 'SOL',
        name: 'Solana',
        market: 'crypto',
        price: 156.78,
        changePct: 5.67,
        tags: ['DeFi生态', '高TPS', '生态爆发'],
        summary: 'DEX 交易量超越以太坊，开发者活跃度第一',
        resonance: 'strong',
        klines: {
          year: (generateKlineData(50, 25, 0.3, 'up')),
          month: (generateKlineData(50, 120, 0.15, 'up')),
          week: (generateKlineData(50, 145, 0.1, 'breakout')),
          day: (generateKlineData(50, 155, 0.06, 'up'))
        }
      }
    ],
    futures: [
      {
        id: 'fut-AU',
        code: 'AU2506',
        name: '沪金主力',
        market: 'futures',
        price: 568.45,
        changePct: 0.57,
        tags: ['避险需求', '央行购金', '通胀对冲'],
        summary: '地缘风险升温，黄金 ETF 持仓连续增加',
        resonance: 'medium',
        klines: {
          year: (generateKlineData(50, 480, 0.1, 'up')),
          month: (generateKlineData(50, 540, 0.05, 'up')),
          week: (generateKlineData(50, 560, 0.03, 'up')),
          day: (generateKlineData(50, 567, 0.02, 'flat'))
        }
      },
      {
        id: 'fut-SC',
        code: 'SC2506',
        name: '原油主力',
        market: 'futures',
        price: 612.34,
        changePct: -1.36,
        tags: ['OPEC+', '需求担忧', '库存上升'],
        summary: 'EIA 库存超预期，短期承压但长期供给偏紧',
        resonance: 'weak',
        klines: {
          year: (generateKlineData(50, 650, 0.12, 'down')),
          month: (generateKlineData(50, 630, 0.06, 'down')),
          week: (generateKlineData(50, 620, 0.04, 'flat')),
          day: (generateKlineData(50, 615, 0.03, 'down'))
        }
      }
    ],
    forex: [
      {
        id: 'fx-USDCNY',
        code: 'USDCNY',
        name: '美元/人民币',
        market: 'forex',
        price: 7.2345,
        changePct: 0.17,
        tags: ['利差驱动', '央行干预', '贸易顺差'],
        summary: '中美利差收窄，人民币中间价强于市场预期',
        resonance: 'medium',
        klines: {
          year: (generateKlineData(50, 7.15, 0.02, 'up')),
          month: (generateKlineData(50, 7.22, 0.01, 'flat')),
          week: (generateKlineData(50, 7.23, 0.008, 'flat')),
          day: (generateKlineData(50, 7.234, 0.005, 'up'))
        }
      }
    ],
    bond: [
      {
        id: 'bond-CN10Y',
        code: 'CN10Y',
        name: '10年期国债',
        market: 'bond',
        price: 2.345,
        changePct: -0.51,
        tags: ['宽松预期', '收益率下行', '配置窗口'],
        summary: '央行降准预期升温，长端利率下行空间打开',
        resonance: 'medium',
        klines: {
          year: (generateKlineData(50, 2.65, 0.05, 'down')),
          month: (generateKlineData(50, 2.45, 0.03, 'down')),
          week: (generateKlineData(50, 2.36, 0.02, 'down')),
          day: (generateKlineData(50, 2.35, 0.01, 'flat'))
        }
      }
    ]
  };

  return buildRecommendations._cache;
}

module.exports = {
  MARKETS,
  MARKET_INDICES,
  MARKET_SENTIMENT,
  buildRecommendations,
  generateKlineData,
  normalizeKlines
};
