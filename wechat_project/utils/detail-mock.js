/**
 * 详情页 Mock 数据 — 基于首页标的扩展，使用种子随机保证刷新一致
 */

const { buildRecommendations } = require('./mock');

const STAGE_MAP = {
  expansion: { id: 'expansion', label: '快速扩张', desc: '营收高增长，资本开支加大，现金流紧但结构健康', color: '#0ecb81' },
  stable: { id: 'stable', label: '稳定守业', desc: '营收增速回落，现金流充沛，分红回报提升', color: '#f0b90b' },
  shrink: { id: 'shrink', label: '收缩聚焦', desc: '营收下滑但利润短期回升，降本增效特征明显', color: '#ff9500' },
  decline: { id: 'decline', label: '萎缩淘汰', desc: '量价齐跌，连续承压，现金流失血风险', color: '#f6465d' }
};

const INDUSTRY_MAP = {
  cn: { '600519': '白酒', '300750': '锂电池', '601318': '保险', '688981': '半导体' },
  hk: { '00700': '互联网', '09988': '电商', '01810': '消费电子' },
  us: { NVDA: '半导体', AAPL: '消费电子', TSLA: '新能源汽车' },
  crypto: { BTC: '加密货币', SOL: '公链' },
  futures: { AU2506: '贵金属', SC2506: '能源' },
  forex: { USDCNY: '外汇' },
  bond: { CN10Y: '国债' }
};

const STAGE_INSIGHTS = {
  expansion: {
    financial: '营收保持双位数增长，资本开支同步扩大，经营现金流覆盖投资支出约 85%，扩张结构整体健康。',
    operation: '员工规模温和扩张，人均创收稳步提升；存货周转天数下降，运营效率改善。',
    chain: '毛利率稳定高于行业均值，预收账款占比提升，对上游议价能力增强。',
    capital: '资产负债率处于合理区间，有息负债率可控，货币资金对短期借款覆盖倍数 > 1.5。'
  },
  stable: {
    financial: '营收增速回落至个位数，但净利润率稳定，经营现金流持续为正，分红比例提升。',
    operation: '人员结构优化，人均创收维持高位；费用率控制良好，运营杠杆稳定。',
    chain: '行业地位稳固，渠道议价能力保持，毛利率波动收窄。',
    capital: '负债结构保守，现金储备充裕，资本回报稳定，适合长期持有。'
  },
  shrink: {
    financial: '营收短期承压，但降本增效推动利润率回升，资本开支收缩，现金流逐步修复。',
    operation: '人员精简，人均效率提升；库存去化加速，周转天数明显改善。',
    chain: '市场份额阶段性收缩，但核心产品线毛利率仍高于行业。',
    capital: '主动去杠杆，有息负债下降，财务风险可控。'
  },
  decline: {
    financial: '营收与利润双降，经营现金流转负，需关注应收账款与存货减值风险。',
    operation: '人效下滑，费用刚性偏高，运营效率弱于同业。',
    chain: '议价能力减弱，毛利率低于行业均值，渠道库存偏高。',
    capital: '负债率攀升，短期偿债压力加大，需警惕再融资风险。'
  }
};

function createSeededRandom(seed) {
  let s = seed % 2147483647;
  if (s <= 0) s += 2147483646;
  return function () {
    s = (s * 16807) % 2147483647;
    return (s - 1) / 2147483646;
  };
}

function genYearSeries(rng, startVal, growth, years, jitter) {
  jitter = jitter || 0.03;
  const res = [];
  let val = startVal;
  const y0 = new Date().getFullYear() - years + 1;
  for (let i = 0; i < years; i++) {
    const noise = 1 + (rng() - 0.5) * jitter;
    val = val * (1 + growth + (rng() - 0.5) * 0.04) * noise;
    res.push({ year: String(y0 + i), value: +val.toFixed(2) });
  }
  return res;
}

function genPercentSeries(rng, start, trend, years) {
  const res = [];
  const y0 = new Date().getFullYear() - years + 1;
  for (let i = 0; i < years; i++) {
    const v = start + trend * i + (rng() - 0.5) * 2;
    res.push({ year: String(y0 + i), value: +Math.max(0, v).toFixed(1) });
  }
  return res;
}

function findBaseItem(id) {
  const all = buildRecommendations();
  for (const market in all) {
    const found = all[market].find(item => item.id === id);
    if (found) return found;
  }
  return null;
}

function pickStage(resonance, changePct) {
  if (resonance === 'strong' && changePct > 0) return STAGE_MAP.expansion;
  if (resonance === 'weak' || (changePct < -1 && changePct > -2)) return STAGE_MAP.shrink;
  if (changePct <= -2) return STAGE_MAP.decline;
  if (resonance === 'medium') return STAGE_MAP.stable;
  return STAGE_MAP.expansion;
}

function buildRadar(seed, stageId) {
  const rng = createSeededRandom(seed + 100);
  const stageBoost = { expansion: 8, stable: 3, shrink: -2, decline: -10 };
  const boost = stageBoost[stageId] || 0;

  const company = [
    +(16 + boost + rng() * 12).toFixed(1),
    +(38 + boost * 0.8 + rng() * 10).toFixed(1),
    +(6 + boost * 0.5 + rng() * 8).toFixed(1),
    +(30 + rng() * 18).toFixed(1),
    +(1.8 + rng() * 4).toFixed(1)
  ];

  const industry = [18.2, 41.5, 6.3, 47.8, 2.4];

  return {
    dimensions: ['ROE', '毛利率', '营收增速', '资产负债率', '研发占比'],
    unit: ['%', '%', '%', '%', '%'],
    company,
    industry,
    industryAvg: industry
  };
}

function radarToPoints(values, maxValues) {
  const count = values.length;
  const points = [];
  for (let i = 0; i < count; i++) {
    const angle = (Math.PI * 2 * i / count) - Math.PI / 2;
    const ratio = values[i] / (maxValues[i] || 1);
    const r = Math.min(ratio, 1.15) * 42;
    points.push({
      x: (50 + r * Math.cos(angle)).toFixed(2),
      y: (50 + r * Math.sin(angle)).toFixed(2)
    });
  }
  return points;
}

function buildCompass(rng, base, stageId) {
  const insights = STAGE_INSIGHTS[stageId] || STAGE_INSIGHTS.expansion;
  const rev = genYearSeries(rng, base * 0.4, 0.12, 8, 0.05);
  const profit = genYearSeries(rng, base * 0.08, 0.15, 8, 0.08);
  const ocf = genYearSeries(rng, base * 0.06, 0.1, 8, 0.1);
  const capex = genYearSeries(rng, base * 0.05, 0.08, 8, 0.12);
  const employees = genYearSeries(rng, 8000, 0.06, 8, 0.02).map(d => ({
    year: d.year,
    value: Math.round(d.value)
  }));
  const revPerEmp = rev.map((r, i) => ({
    year: r.year,
    value: +(r.value * 1000 / employees[i].value).toFixed(1)
  }));

  return {
    financial: {
      title: '财务动能',
      color: '#0ecb81',
      insight: insights.financial,
      charts: [
        { name: '营收', unit: '亿', color: '#0ecb81', data: rev },
        { name: '净利润', unit: '亿', color: '#f0b90b', data: profit },
        { name: '经营现金流', unit: '亿', color: '#848e9c', data: ocf },
        { name: '资本开支', unit: '亿', color: '#f6465d', data: capex }
      ]
    },
    operation: {
      title: '运营人效',
      color: '#f0b90b',
      insight: insights.operation,
      charts: [
        { name: '员工数', unit: '人', color: '#848e9c', data: employees },
        { name: '人均创收', unit: '万', color: '#0ecb81', data: revPerEmp },
        { name: '存货周转天数', unit: '天', color: '#ff9500', data: genPercentSeries(rng, 85, -3, 8) },
        { name: '应收周转天数', unit: '天', color: '#a78bfa', data: genPercentSeries(rng, 42, -1.5, 8) }
      ]
    },
    chain: {
      title: '产业链地位',
      color: '#a78bfa',
      insight: insights.chain,
      charts: [
        { name: '毛利率', unit: '%', color: '#0ecb81', data: genPercentSeries(rng, 38, 1.2, 8) },
        { name: '净利率', unit: '%', color: '#f0b90b', data: genPercentSeries(rng, 22, 0.8, 8) },
        { name: '预收占比', unit: '%', color: '#848e9c', data: genPercentSeries(rng, 8, 0.6, 8) },
        { name: '应付/应收比', unit: 'x', color: '#ff9500', data: genYearSeries(rng, 1.2, 0.02, 8).map(d => ({ year: d.year, value: +d.value.toFixed(2) })) }
      ]
    },
    capital: {
      title: '资本结构',
      color: '#f6465d',
      insight: insights.capital,
      charts: [
        { name: '资产负债率', unit: '%', color: '#f6465d', data: genPercentSeries(rng, 32, 0.5, 8) },
        { name: '有息负债率', unit: '%', color: '#ff9500', data: genPercentSeries(rng, 12, 0.2, 8) },
        { name: '货币资金', unit: '亿', color: '#0ecb81', data: genYearSeries(rng, base * 0.15, 0.08, 8) },
        { name: '短期借款', unit: '亿', color: '#848e9c', data: genYearSeries(rng, base * 0.06, 0.03, 8) }
      ]
    }
  };
}

function getDetailById(id) {
  const base = findBaseItem(id);
  if (!base) return null;

  const seed = id.split('').reduce((s, c) => s + c.charCodeAt(0), 0);
  const rng = createSeededRandom(seed);
  const industry = (INDUSTRY_MAP[base.market] && INDUSTRY_MAP[base.market][base.code]) || '综合';
  const stage = pickStage(base.resonance, base.changePct);
  const healthScore = Math.min(95, 58 + (seed % 38));
  const radar = buildRadar(seed, stage.id);
  const maxRadar = radar.dimensions.map((_, i) =>
    Math.max(radar.company[i], radar.industry[i]) * 1.2
  );

  const priceBase = typeof base.price === 'number' ? base.price : 100;
  const compass = buildCompass(createSeededRandom(seed + 200), priceBase, stage.id);

  const compassList = [
    compass.financial,
    compass.operation,
    compass.chain,
    compass.capital
  ];

  return Object.assign({}, base, {
    industry,
    stage,
    healthScore,
    healthRank: '超越' + industry + '行业 ' + healthScore + '% 的公司',
    healthBreakdown: {
      profit: Math.min(99, healthScore + 5),
      growth: Math.min(99, healthScore - 3),
      debt: Math.min(99, healthScore + 8),
      operation: Math.min(99, healthScore - 1)
    },
    radar: Object.assign({}, radar, {
      companyPoints: radarToPoints(radar.company, maxRadar),
      industryPoints: radarToPoints(radar.industry, maxRadar),
      gridLevels: [20, 35, 50]
    }),
    compass,
    compassList,
    keyMetrics: [
      { label: '市盈率 TTM', value: (15 + seed % 25) + 'x' },
      { label: '市净率', value: (2 + (seed % 10) * 0.3).toFixed(1) + 'x' },
      { label: 'ROE', value: radar.company[0] + '%' },
      { label: '毛利率', value: radar.company[1] + '%' },
      { label: '股息率', value: (1 + (seed % 5) * 0.4).toFixed(1) + '%' },
      { label: '52周高', value: (priceBase * 1.18).toFixed(2) }
    ]
  });
}

module.exports = {
  getDetailById,
  STAGE_MAP
};
