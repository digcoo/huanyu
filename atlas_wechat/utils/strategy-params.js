/**
 * 策略自定义参数 · 本地存储 + API 查询字段映射
 */

var STORAGE_PREFIX = 'strategyParams_';

var TREND_DEFAULTS = {
  trMinAmountWan: 5000,
  trPrevWeeks: 2,
  trRequireCurrentBreakout: false,
  trRequireMonthMacd: false,
  trRequireWeekMacd: false,
  trRequireDayMacd: false,
  trRequireWeekGoldenCross: false,
  trRequireUltra: true
};

var REBOUND_DEFAULTS = {
  rMinAmountWan: 3000,
  rEnableShort: true,
  rEnableMedium: true,
  rEnableLong: true,
  rTierMin: 'ALL'
};

var RESONANCE_DEFAULTS = {
  cMinAmountWan: 5000,
  cEnableShort: true,
  cEnableMedium: true,
  cEnableLong: true,
  cTierMin: 'ALL'
};

var LADDER_DEFAULTS = {
  lMinAmountWan: 3000,
  lEnableUltra: true,
  lEnableShort: true,
  lEnableMedium: true,
  lEnableLong: true,
  lTierMin: 'ALL'
};

var RETEST_DEFAULTS = {
  tMinAmountWan: 3000,
  tEnableBear: true,
  tEnableBull: true,
  tEnableUltra: true,
  tEnableShort: true,
  tEnableMedium: true,
  tEnableLong: true,
  tTierMin: 'ALL'
};

var GC2_DEFAULTS = {
  g2MinAmountWan: 5000,
  g2EnableShort: true,
  g2EnableLong: true,
  g2TierMin: 'ALL',
  g2LookbackShort: 60,
  g2LookbackLong: 52
};

var DC2_DEFAULTS = {
  d2MinAmountWan: 5000,
  d2EnableShort: true,
  d2EnableLong: true,
  d2TierMin: 'ALL',
  d2LookbackShort: 60,
  d2LookbackLong: 52
};

var BOGO_DEFAULTS = {
  boEnableDay: true,
  boEnableWeek: false,
  boEnableMonth: false,
  boLookbackDay: 60,
  boLookbackWeek: 52,
  boLookbackMonth: 36
};

var TRENDM_DEFAULTS = {
  tmLookbackDay: 60,
  tmLookbackWeek: 52,
  tmLookbackMonth: 36
};

var ULTRA_DEFAULTS = {
  ulMinAmountWan: 5000,
  ulRequireMonthMacd: false,
  ulRequireWeekMacd: false,
  ulRequireDayMacd: false,
  ulRequireDayMacdNegative: false,
  ulRequireWeekMacdNegative: false,
  ulRequireMonthMacdNegative: false,
  ulRequireCurrentBreakout: false
};

var ULTRA_SCHEMA = [
  {
    key: 'ulMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    type: 'section',
    label: 'MACD>0（可选）'
  },
  {
    key: 'ulRequireMonthMacd',
    label: '月 MACD>0',
    hint: '默认关闭，开启后需满足',
    type: 'switch'
  },
  {
    key: 'ulRequireWeekMacd',
    label: '周 MACD>0',
    hint: '默认关闭，开启后需满足',
    type: 'switch'
  },
  {
    key: 'ulRequireDayMacd',
    label: '日 MACD>0',
    hint: '默认关闭，开启后需满足',
    type: 'switch'
  },
  {
    type: 'section',
    label: 'MACD<0（可选）'
  },
  {
    key: 'ulRequireDayMacdNegative',
    label: '日 MACD<0',
    hint: '与「日MACD>0」勿同时开启',
    type: 'switch'
  },
  {
    key: 'ulRequireWeekMacdNegative',
    label: '周 MACD<0',
    hint: '大周期仍处零轴下，常与「日MACD>0」联测',
    type: 'switch'
  },
  {
    key: 'ulRequireMonthMacdNegative',
    label: '月 MACD<0',
    hint: '默认关闭，开启后需满足',
    type: 'switch'
  },
  {
    type: 'section',
    label: '突破K'
  },
  {
    key: 'ulRequireCurrentBreakout',
    label: '当前K须为突破K',
    hint: '关闭则当日任一根满足突破条件即可',
    type: 'switch'
  }
];

var LADDER_SCHEMA = [
  {
    key: 'lMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'lEnableUltra',
    label: '超短突破',
    hint: '30m · 前2日基准 + 当日首根突破',
    type: 'switch'
  },
  {
    key: 'lEnableShort',
    label: '短线突破',
    hint: '日K · 前2周基准 + 本周首根突破',
    type: 'switch'
  },
  {
    key: 'lEnableMedium',
    label: '中线突破',
    hint: '周K · 前2月基准 + 本月首根突破',
    type: 'switch'
  },
  {
    key: 'lEnableLong',
    label: '长线突破',
    hint: '月K · 前2年基准 + 本年首根突破',
    type: 'switch'
  },
  {
    key: 'lTierMin',
    label: '最低展示档位',
    type: 'picker',
    options: [
      { value: 'ALL', label: '全部档位' },
      { value: 'C', label: 'C档及以上 (长线+)' },
      { value: 'B', label: 'B档及以上 (中线+)' },
      { value: 'A', label: 'A档及以上 (短线+)' },
      { value: 'S', label: '仅超短 (S)' }
    ]
  }
];

var RETEST_SCHEMA = [
  {
    key: 'tMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'tEnableBear',
    label: '下跌反转',
    hint: '深跌背景 · 强弹回踩再升',
    type: 'switch'
  },
  {
    key: 'tEnableBull',
    label: '上涨中继',
    hint: '趋势背景 · 二次回踩再升',
    type: 'switch'
  },
  {
    key: 'tEnableUltra',
    label: '超短档',
    hint: '30m · L0/H1/L1/介入',
    type: 'switch'
  },
  {
    key: 'tEnableShort',
    label: '短线档',
    hint: '日K结构',
    type: 'switch'
  },
  {
    key: 'tEnableMedium',
    label: '中线档',
    hint: '周K结构',
    type: 'switch'
  },
  {
    key: 'tEnableLong',
    label: '长线档',
    hint: '月K结构',
    type: 'switch'
  },
  {
    key: 'tTierMin',
    label: '最低展示档位',
    type: 'picker',
    options: [
      { value: 'ALL', label: '全部档位' },
      { value: 'C', label: 'C档及以上 (长线+)' },
      { value: 'B', label: 'B档及以上 (中线+)' },
      { value: 'A', label: 'A档及以上 (短线+)' },
      { value: 'S', label: '仅超短 (S)' }
    ]
  }
];

var TREND_SCHEMA = [
  {
    key: 'trMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'trPrevWeeks',
    label: '基准背景周数',
    hint: '自然周，信号周之前的完整周数',
    type: 'slider',
    min: 1,
    max: 4,
    step: 1,
    unit: '周'
  },
  {
    type: 'section',
    label: '突破K'
  },
  {
    key: 'trRequireCurrentBreakout',
    label: '当前日K须为突破K',
    hint: '关闭则本周任一日K满足即可',
    type: 'switch'
  },
  {
    type: 'section',
    label: 'MACD（可选）'
  },
  {
    key: 'trRequireMonthMacd',
    label: '月 MACD>0',
    type: 'switch'
  },
  {
    key: 'trRequireWeekMacd',
    label: '周 MACD>0',
    type: 'switch'
  },
  {
    key: 'trRequireDayMacd',
    label: '日 MACD>0',
    type: 'switch'
  },
  {
    key: 'trRequireWeekGoldenCross',
    label: '周K MACD 金叉',
    type: 'switch'
  },
  {
    type: 'section',
    label: '超短叠加'
  },
  {
    key: 'trRequireUltra',
    label: '须满足超短 30m 突破',
    hint: '开启后须同时命中超短策略；30m 参数沿用超短 Tab 设置',
    type: 'switch'
  }
];

var REBOUND_SCHEMA = [
  {
    key: 'rMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'rEnableShort',
    label: '短线深跌',
    hint: '周 MACD<0 + 日 MACD>0 + 日K突破',
    type: 'switch'
  },
  {
    key: 'rEnableMedium',
    label: '中线深跌',
    hint: '月 MACD<0 + 周 MACD>0 + 周K突破',
    type: 'switch'
  },
  {
    key: 'rEnableLong',
    label: '长线深跌',
    hint: '年 MACD<0 + 月 MACD>0 + 月K突破',
    type: 'switch'
  },
  {
    key: 'rTierMin',
    label: '最低展示档位',
    type: 'picker',
    options: [
      { value: 'ALL', label: '全部档位' },
      { value: 'B', label: 'B档及以上 (长线+)' },
      { value: 'A', label: 'A档及以上 (中线+)' },
      { value: 'S', label: '仅短线 (S)' }
    ]
  }
];

var RESONANCE_SCHEMA = [
  {
    key: 'cMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'cEnableShort',
    label: '短线共振',
    hint: '周 MACD>0 + 日 MACD>0（非金叉）+ 日K突破',
    type: 'switch'
  },
  {
    key: 'cEnableMedium',
    label: '中线共振',
    hint: '月 MACD>0 + 周 MACD>0（非金叉）+ 周K突破',
    type: 'switch'
  },
  {
    key: 'cEnableLong',
    label: '长线共振',
    hint: '年 MACD>0 + 月 MACD>0（非金叉）+ 月K突破',
    type: 'switch'
  },
  {
    key: 'cTierMin',
    label: '最低展示档位',
    type: 'picker',
    options: [
      { value: 'ALL', label: '全部档位' },
      { value: 'B', label: 'B档及以上 (长线+)' },
      { value: 'A', label: 'A档及以上 (中线+)' },
      { value: 'S', label: '仅短线 (S)' }
    ]
  }
];

var GC2_SCHEMA = [
  {
    type: 'section',
    label: '硬门槛',
    hint: '不满足则不入池'
  },
  {
    key: 'g2MinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    type: 'section',
    label: '启用档位',
    hint: '短/长两档，可单独或同时开启'
  },
  {
    key: 'g2EnableShort',
    label: '短线（S）',
    hint: '月/周 MACD>0 · 日K 金叉柱 high 二次突破',
    type: 'switch'
  },
  {
    key: 'g2EnableLong',
    label: '长线（B）',
    hint: '年/月 MACD>0 · 周K 金叉柱 high 二次突破',
    type: 'switch'
  },
  {
    type: 'section',
    label: '列表筛选'
  },
  {
    key: 'g2TierMin',
    label: '最低展示档位',
    hint: '过滤扫描结果展示的最低档位',
    type: 'picker',
    options: [
      { value: 'ALL', label: '全部档位' },
      { value: 'B', label: '长线及以上（含短线）' },
      { value: 'S', label: '仅短线（S）' }
    ]
  }
];

var DC2_SCHEMA = [
  {
    type: 'section',
    label: '硬门槛',
    hint: '不满足则不入池'
  },
  {
    key: 'd2MinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    type: 'section',
    label: '启用档位',
    hint: '短/长两档，可单独或同时开启'
  },
  {
    key: 'd2EnableShort',
    label: '短线（S）',
    hint: '月/周 MACD>0 · 日K 死叉柱 high 突破',
    type: 'switch'
  },
  {
    key: 'd2EnableLong',
    label: '长线（B）',
    hint: '年/月 MACD>0 · 周K 死叉柱 high 突破',
    type: 'switch'
  },
  {
    type: 'section',
    label: '列表筛选'
  },
  {
    key: 'd2TierMin',
    label: '最低展示档位',
    hint: '过滤扫描结果展示的最低档位',
    type: 'picker',
    options: [
      { value: 'ALL', label: '全部档位' },
      { value: 'B', label: '长线及以上（含短线）' },
      { value: 'S', label: '仅短线（S）' }
    ]
  }
];

var BOGO_SCHEMA = [
  {
    type: 'section',
    label: '启用周期',
    hint: '在对应周期找最近金叉/死叉 K 为基准，现价突破基准 K 高点'
  },
  {
    key: 'boEnableDay',
    label: '日 K',
    hint: '日 K 最近金叉或死叉柱 + 突破',
    type: 'switch'
  },
  {
    key: 'boEnableWeek',
    label: '周 K',
    hint: '周 K 最近金叉或死叉柱 + 突破',
    type: 'switch'
  },
  {
    key: 'boEnableMonth',
    label: '月 K',
    hint: '月 K 最近金叉或死叉柱 + 突破',
    type: 'switch'
  },
  {
    type: 'section',
    label: '回溯根数',
    hint: '各周期向前搜索金叉/死叉的最大 K 数'
  },
  {
    key: 'boLookbackDay',
    label: '日 K lookback',
    type: 'slider',
    min: 20,
    max: 120,
    step: 5,
    unit: '根'
  },
  {
    key: 'boLookbackWeek',
    label: '周 K lookback',
    type: 'slider',
    min: 20,
    max: 104,
    step: 4,
    unit: '根'
  },
  {
    key: 'boLookbackMonth',
    label: '月 K lookback',
    type: 'slider',
    min: 12,
    max: 60,
    step: 4,
    unit: '根'
  }
];

var TRENDM_SCHEMA = [
  {
    type: 'section',
    label: '日/周/月基准突破',
    hint: '三周期须同时满足：最近金叉/死叉 K（非当前 K）为基准，前 K 未破、当前 K 突破 ref.high'
  },
  {
    type: 'section',
    label: 'min30 梯子',
    hint: '固定联检 30m 超短梯子；参数沿用「超短」Tab'
  },
  {
    type: 'section',
    label: '回溯根数',
    hint: '各周期向前搜索金叉/死叉的最大 K 数'
  },
  {
    key: 'tmLookbackDay',
    label: '日 K lookback',
    type: 'slider',
    min: 20,
    max: 120,
    step: 5,
    unit: '根'
  },
  {
    key: 'tmLookbackWeek',
    label: '周 K lookback',
    type: 'slider',
    min: 20,
    max: 104,
    step: 4,
    unit: '根'
  },
  {
    key: 'tmLookbackMonth',
    label: '月 K lookback',
    type: 'slider',
    min: 12,
    max: 60,
    step: 4,
    unit: '根'
  }
];

var MEDIUM_DEFAULTS = {
  mdMinAmountWan: 5000,
  mdPrevMonths: 2,
  mdRequireCurrentBreakout: false,
  mdRequireMonthMacd: false,
  mdRequireYearMacd: false,
  mdRequireMonthGoldenCross: false,
  mdRequireUltra: true
};

var MEDIUM_SCHEMA = [
  {
    key: 'mdMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'mdPrevMonths',
    label: '基准背景月数',
    hint: '自然月，信号月之前的完整月数',
    type: 'slider',
    min: 1,
    max: 4,
    step: 1,
    unit: '月'
  },
  {
    type: 'section',
    label: '突破K'
  },
  {
    key: 'mdRequireCurrentBreakout',
    label: '当前周K须为突破K',
    hint: '关闭则本月任一周K满足即可',
    type: 'switch'
  },
  {
    type: 'section',
    label: 'MACD（可选）'
  },
  {
    key: 'mdRequireMonthMacd',
    label: '月 MACD>0',
    type: 'switch'
  },
  {
    key: 'mdRequireYearMacd',
    label: '年 MACD>0',
    type: 'switch'
  },
  {
    key: 'mdRequireMonthGoldenCross',
    label: '月K MACD 金叉',
    type: 'switch'
  },
  {
    type: 'section',
    label: '超短叠加'
  },
  {
    key: 'mdRequireUltra',
    label: '须满足超短 30m 突破',
    hint: '开启后须同时命中超短策略；30m 参数沿用超短 Tab 设置',
    type: 'switch'
  }
];

var LONG_DEFAULTS = {
  lgMinAmountWan: 5000,
  lgPrevYears: 2,
  lgRequireCurrentBreakout: false,
  lgRequireYearMacd: false,
  lgRequireMonthMacd: false,
  lgRequireYearGoldenCross: false,
  lgRequireUltra: true
};

var LONG_SCHEMA = [
  {
    key: 'lgMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'lgPrevYears',
    label: '基准背景年数',
    hint: '自然年，信号年之前完整年数',
    type: 'slider',
    min: 1,
    max: 4,
    step: 1,
    unit: '年'
  },
  {
    type: 'section',
    label: '突破K'
  },
  {
    key: 'lgRequireCurrentBreakout',
    label: '当前月K须为突破K',
    hint: '关闭则本年任一月K满足即可',
    type: 'switch'
  },
  {
    type: 'section',
    label: 'MACD（可选）'
  },
  {
    key: 'lgRequireYearMacd',
    label: '年 MACD>0',
    type: 'switch'
  },
  {
    key: 'lgRequireMonthMacd',
    label: '月 MACD>0',
    type: 'switch'
  },
  {
    key: 'lgRequireYearGoldenCross',
    label: '年K MACD 金叉',
    type: 'switch'
  },
  {
    type: 'section',
    label: '超短叠加'
  },
  {
    key: 'lgRequireUltra',
    label: '须满足超短 30m 突破',
    hint: '开启后须同时命中超短策略；30m 参数沿用超短 Tab 设置',
    type: 'switch'
  }
];

var SCHEMA_BY_STRATEGY = {
  ultra: ULTRA_SCHEMA,
  trend: TREND_SCHEMA,
  medium: MEDIUM_SCHEMA,
  long: LONG_SCHEMA,
  resonance: RESONANCE_SCHEMA,
  rebound: REBOUND_SCHEMA,
  ladder: [],
  nrf: [],
  retest: RETEST_SCHEMA,
  gc2: GC2_SCHEMA,
  dc2: DC2_SCHEMA,
  bogo: BOGO_SCHEMA,
  trendm: TRENDM_SCHEMA
};

var LADDER_TIER_SHORT = 'short';
var LADDER_TIER_MEDIUM = 'medium';
var LADDER_TIER_LONG = 'long';

var LADDER_TIERS = [
  { id: LADDER_TIER_SHORT, apiId: 'trend', label: '短', title: '短线' },
  { id: LADDER_TIER_MEDIUM, apiId: 'medium', label: '中', title: '中线' },
  { id: LADDER_TIER_LONG, apiId: 'long', label: '长', title: '长线' }
];

var NRF_TIERS = [
  { id: LADDER_TIER_SHORT, apiId: 'trend', label: '日+min30', title: '日+min30' },
  { id: LADDER_TIER_MEDIUM, apiId: 'medium', label: '周+min30', title: '周+min30' },
  { id: LADDER_TIER_LONG, apiId: 'long', label: '月+min30', title: '月+min30' }
];

var NRF_ULTRA_SWITCH_KEYS = {
  trRequireUltra: true,
  mdRequireUltra: true,
  lgRequireUltra: true
};

function buildLadderBundleDefaults() {
  return {
    activeTier: LADDER_TIER_SHORT,
    short: clone(TREND_DEFAULTS),
    medium: clone(MEDIUM_DEFAULTS),
    long: clone(LONG_DEFAULTS)
  };
}

var LADDER_BUNDLE_DEFAULTS = buildLadderBundleDefaults();

var NRF_BUNDLE_DEFAULTS = buildLadderBundleDefaults();

function migrateLegacyNrfBundle(raw) {
  if (!raw || typeof raw !== 'object') return null;
  if (raw.activeTier && raw.short) return raw;
  var def = buildLadderBundleDefaults();
  var tier = LADDER_TIER_SHORT;
  if (raw.nrfLadderMode === 'week') tier = LADDER_TIER_MEDIUM;
  else if (raw.nrfLadderMode === 'month') tier = LADDER_TIER_LONG;
  return {
    activeTier: tier,
    short: normalizeTierParams(LADDER_TIER_SHORT, raw.short || raw.day || def.short),
    medium: normalizeTierParams(LADDER_TIER_MEDIUM, raw.medium || raw.week || def.medium),
    long: normalizeTierParams(LADDER_TIER_LONG, raw.long || raw.month || def.long)
  };
}

function buildNrfBundleDefaults() {
  return buildLadderBundleDefaults();
}

function applyNrfUltraDefaults(bundle) {
  if (bundle.short) bundle.short.trRequireUltra = true;
  if (bundle.medium) bundle.medium.mdRequireUltra = true;
  if (bundle.long) bundle.long.lgRequireUltra = true;
  return bundle;
}

function loadNrfBundleRaw() {
  try {
    var saved = wx.getStorageSync(storageKey('nrf'));
    if (!saved || !saved.short) {
      var legacy = wx.getStorageSync(STORAGE_PREFIX + 'ladder');
      if (legacy && legacy.short) {
        saved = legacy;
      } else {
        saved = migrateLegacyNrfBundle(saved) || migrateLadderFromLegacyStorages();
      }
      if (saved && saved.short) {
        wx.setStorageSync(storageKey('nrf'), applyNrfUltraDefaults(normalizeLadderBundle(saved)));
      }
    }
    return applyNrfUltraDefaults(normalizeLadderBundle(saved || buildNrfBundleDefaults()));
  } catch (e) {
    return applyNrfUltraDefaults(buildNrfBundleDefaults());
  }
}

function loadNrfTierForm(tier) {
  var bundle = loadNrfBundleRaw();
  var key = tier || bundle.activeTier || LADDER_TIER_SHORT;
  return clone(bundle[key] || bundle.short);
}

function saveNrfTierForm(tier, form, setActive) {
  var bundle = loadNrfBundleRaw();
  var tierKey = tier || bundle.activeTier || LADDER_TIER_SHORT;
  if (setActive) {
    bundle.activeTier = tierKey;
  }
  var normalized = normalizeTierParams(tierKey, form);
  if (tierKey === LADDER_TIER_SHORT) normalized.trRequireUltra = true;
  else if (tierKey === LADDER_TIER_MEDIUM) normalized.mdRequireUltra = true;
  else normalized.lgRequireUltra = true;
  bundle[tierKey] = normalized;
  wx.setStorageSync(storageKey('nrf'), bundle);
  return bundle;
}

function resetNrfTier(tier) {
  var bundle = loadNrfBundleRaw();
  var tierKey = tier || bundle.activeTier || LADDER_TIER_SHORT;
  var def = buildNrfBundleDefaults();
  bundle[tierKey] = clone(def[tierKey]);
  wx.setStorageSync(storageKey('nrf'), bundle);
  return bundle;
}

function getFrictionlessGateHeadSchema() {
  return [{
    type: 'section',
    label: '双低支撑门',
    hint: '固定：现价 > max(前K.low, 前K2.low)，日、周、月须全部满足'
  }, {
    type: 'section',
    label: '无阻力 MACD 门',
    hint: '固定：日、周、月须同时满足 MACD>0 或 (MACD≤0 且 当前K.close>前K.high)'
  }];
}

var FRICTIONLESS_GATE_STRATEGIES = { ultra: true, bogo: true, trendm: true };

function schemaWithFrictionlessGate(strategyId, schema) {
  if (!FRICTIONLESS_GATE_STRATEGIES[strategyId]) {
    return schema || [];
  }
  var body = schema || [];
  if (body[0] && body[0].label === '双低支撑门') {
    while (body[0] && (body[0].label === '双低支撑门' || body[0].label === '无阻力 MACD 门')) {
      body = body.slice(1);
    }
  } else if (body[0] && body[0].label === '无阻力 MACD 门') {
    body = body.slice(1);
  }
  return getFrictionlessGateHeadSchema().concat(body);
}

function getNrfHeadSchema() {
  return getFrictionlessGateHeadSchema().concat([{
    type: 'section',
    label: 'min30 梯子',
    hint: '三档均固定联检 30m 超短梯子 + 对应日/周/月梯子；30m 参数沿用「超短」Tab'
  }]);
}

function getNrfPanelSchema(tierId) {
  return getNrfHeadSchema().concat(getTierSchema(tierId).filter(function (f) {
    if (NRF_ULTRA_SWITCH_KEYS[f.key]) return false;
    if (f.type === 'section' && f.label === '超短叠加') return false;
    return true;
  }));
}

function getTierListFor(strategyId) {
  return normalizeStrategyId(strategyId) === 'nrf' ? NRF_TIERS : [];
}

function loadTierFormFor(strategyId, tier) {
  return loadNrfTierForm(tier);
}

function saveTierFormFor(strategyId, tier, form, setActive) {
  return saveNrfTierForm(tier, form, setActive);
}

function resetTierFor(strategyId, tier) {
  return resetNrfTier(tier);
}

function getPanelSchema(strategyId, tier) {
  if (normalizeStrategyId(strategyId) === 'nrf') return getNrfPanelSchema(tier);
  return getSchema(strategyId);
}

var DEFAULTS_BY_STRATEGY = {
  ultra: ULTRA_DEFAULTS,
  trend: TREND_DEFAULTS,
  medium: MEDIUM_DEFAULTS,
  long: LONG_DEFAULTS,
  resonance: RESONANCE_DEFAULTS,
  rebound: REBOUND_DEFAULTS,
  ladder: LADDER_BUNDLE_DEFAULTS,
  nrf: NRF_BUNDLE_DEFAULTS,
  retest: RETEST_DEFAULTS,
  gc2: GC2_DEFAULTS,
  dc2: DC2_DEFAULTS,
  bogo: BOGO_DEFAULTS,
  trendm: TRENDM_DEFAULTS
};

var TIER_PICKER = null;
var RESONANCE_TIER_PICKER = RESONANCE_SCHEMA.find(function (f) { return f.key === 'cTierMin'; });
var REBOUND_TIER_PICKER = REBOUND_SCHEMA.find(function (f) { return f.key === 'rTierMin'; });
var LADDER_TIER_PICKER = LADDER_SCHEMA.find(function (f) { return f.key === 'lTierMin'; });
var RETEST_TIER_PICKER = RETEST_SCHEMA.find(function (f) { return f.key === 'tTierMin'; });
var GC2_TIER_PICKER = GC2_SCHEMA.find(function (f) { return f.key === 'g2TierMin'; });
var DC2_TIER_PICKER = DC2_SCHEMA.find(function (f) { return f.key === 'd2TierMin'; });

function tierApiId(tier) {
  if (tier === LADDER_TIER_MEDIUM) return 'medium';
  if (tier === LADDER_TIER_LONG) return 'long';
  return 'trend';
}

function resolveApiStrategyId(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId === 'nrf') {
    var nrfBundle = loadNrfBundleRaw();
    return tierApiId(nrfBundle.activeTier || LADDER_TIER_SHORT);
  }
  if (strategyId === 'trend' || strategyId === 'medium' || strategyId === 'long') {
    return strategyId;
  }
  return strategyId;
}

function getTierSchema(tier) {
  return SCHEMA_BY_STRATEGY[tierApiId(tier)] || [];
}

function normalizeTierParams(tier, raw) {
  return normalize(tierApiId(tier), raw);
}

function normalizeLadderBundle(raw) {
  var def = buildLadderBundleDefaults();
  if (!raw || typeof raw !== 'object') {
    return def;
  }
  return {
    activeTier: raw.activeTier === LADDER_TIER_MEDIUM || raw.activeTier === LADDER_TIER_LONG
      ? raw.activeTier
      : LADDER_TIER_SHORT,
    short: normalizeTierParams(LADDER_TIER_SHORT, raw.short || def.short),
    medium: normalizeTierParams(LADDER_TIER_MEDIUM, raw.medium || def.medium),
    long: normalizeTierParams(LADDER_TIER_LONG, raw.long || def.long)
  };
}

function migrateLadderFromLegacyStorages() {
  var bundle = buildLadderBundleDefaults();
  try {
    var tr = wx.getStorageSync(STORAGE_PREFIX + 'trend');
    if (tr) bundle.short = normalize('trend', tr);
    var md = wx.getStorageSync(STORAGE_PREFIX + 'medium');
    if (md) bundle.medium = normalize('medium', md);
    var lg = wx.getStorageSync(STORAGE_PREFIX + 'long');
    if (lg) bundle.long = normalize('long', lg);
    if (oldLadder && oldLadder.lLadderTier) {
      var tierCode = String(oldLadder.lLadderTier).toUpperCase();
      if (tierCode === 'B') bundle.activeTier = LADDER_TIER_MEDIUM;
      else if (tierCode === 'C') bundle.activeTier = LADDER_TIER_LONG;
      else bundle.activeTier = LADDER_TIER_SHORT;
    }
  } catch (e) {}
  return bundle;
}

function loadLadderBundleRaw() {
  try {
    var saved = wx.getStorageSync(storageKey('ladder'));
    if (!saved || !saved.short) {
      saved = migrateLadderFromLegacyStorages();
      wx.setStorageSync(storageKey('ladder'), saved);
    }
    return normalizeLadderBundle(saved);
  } catch (e) {
    return buildLadderBundleDefaults();
  }
}

function loadTierForm(tier) {
  var bundle = loadLadderBundleRaw();
  var key = tier || bundle.activeTier || LADDER_TIER_SHORT;
  return clone(bundle[key] || bundle.short);
}

function saveTierForm(tier, form, setActive) {
  var bundle = loadLadderBundleRaw();
  var tierKey = tier || bundle.activeTier || LADDER_TIER_SHORT;
  if (setActive) {
    bundle.activeTier = tierKey;
  }
  bundle[tierKey] = normalizeTierParams(tierKey, form);
  wx.setStorageSync(storageKey('ladder'), bundle);
  return bundle;
}

function resetLadderTier(tier) {
  var bundle = loadLadderBundleRaw();
  var tierKey = tier || bundle.activeTier || LADDER_TIER_SHORT;
  var def = buildLadderBundleDefaults();
  bundle[tierKey] = clone(def[tierKey]);
  wx.setStorageSync(storageKey('ladder'), bundle);
  return bundle;
}

function getActiveTierParams(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId === 'nrf') {
    var nrfBundle = loadNrfBundleRaw();
    var nrfTier = nrfBundle.activeTier || LADDER_TIER_SHORT;
    return nrfBundle[nrfTier];
  }
  return load(strategyId);
}

function ladderTierTitle(tier) {
  for (var i = 0; i < LADDER_TIERS.length; i++) {
    if (LADDER_TIERS[i].id === tier) return LADDER_TIERS[i].title;
  }
  return '短线';
}

function nrfTierTitle(tier) {
  for (var i = 0; i < NRF_TIERS.length; i++) {
    if (NRF_TIERS[i].id === tier) return NRF_TIERS[i].title;
  }
  return '日+min30';
}

function formatTierSummary(tier, p) {
  if (tier === LADDER_TIER_SHORT || tierApiId(tier) === 'trend') {
    var macdParts = [];
    if (p.trRequireMonthMacd) macdParts.push('月MACD');
    if (p.trRequireWeekMacd) macdParts.push('周MACD');
    if (p.trRequireDayMacd) macdParts.push('日MACD');
    if (p.trRequireWeekGoldenCross) macdParts.push('周金叉');
    return (p.trMinAmountWan != null ? p.trMinAmountWan : 5000) + '万 · '
      + (p.trPrevWeeks != null ? p.trPrevWeeks : 2) + '周基准 · '
      + (p.trRequireCurrentBreakout ? '当日突破' : '本周突破')
      + (macdParts.length ? ' · ' + macdParts.join('+') : '')
      + (p.trRequireUltra !== false ? ' · +超短' : '');
  }
  if (tier === LADDER_TIER_MEDIUM || tierApiId(tier) === 'medium') {
    var mdMacdParts = [];
    if (p.mdRequireMonthMacd) mdMacdParts.push('月MACD');
    if (p.mdRequireYearMacd) mdMacdParts.push('年MACD');
    if (p.mdRequireMonthGoldenCross) mdMacdParts.push('月金叉');
    return (p.mdMinAmountWan != null ? p.mdMinAmountWan : 5000) + '万 · '
      + (p.mdPrevMonths != null ? p.mdPrevMonths : 2) + '月基准 · '
      + (p.mdRequireCurrentBreakout ? '当周突破' : '本月突破')
      + (mdMacdParts.length ? ' · ' + mdMacdParts.join('+') : '')
      + (p.mdRequireUltra !== false ? ' · +超短' : '');
  }
  var lgMacdParts = [];
  if (p.lgRequireYearMacd) lgMacdParts.push('年MACD');
  if (p.lgRequireMonthMacd) lgMacdParts.push('月MACD');
  if (p.lgRequireYearGoldenCross) lgMacdParts.push('年金叉');
  return (p.lgMinAmountWan != null ? p.lgMinAmountWan : 5000) + '万 · '
    + (p.lgPrevYears != null ? p.lgPrevYears : 2) + '年基准 · '
    + (p.lgRequireCurrentBreakout ? '当月突破' : '本年突破')
    + (lgMacdParts.length ? ' · ' + lgMacdParts.join('+') : '')
    + (p.lgRequireUltra !== false ? ' · +超短' : '');
}

function migrateLadderTier(raw, out) {
  if (!raw || !raw.lLadderTier) return;
  var t = String(raw.lLadderTier).toUpperCase();
  out.lEnableUltra = t === 'S';
  out.lEnableShort = t === 'A';
  out.lEnableMedium = t === 'B';
  out.lEnableLong = t === 'C';
}

/** 列表主图默认周期：取已启用档位中最高频（超短优先） */
function ladderPrimaryPeriod(params) {
  var p = params || {};
  if (p.lEnableUltra) return 'min30';
  if (p.lEnableShort) return 'day';
  if (p.lEnableMedium) return 'week';
  if (p.lEnableLong) return 'month';
  return 'min30';
}

function retestPrimaryPeriod(params) {
  var p = params || {};
  if (p.tEnableUltra) return 'min30';
  if (p.tEnableShort) return 'day';
  if (p.tEnableMedium) return 'week';
  if (p.tEnableLong) return 'month';
  return 'day';
}

function gc2PrimaryPeriod(params) {
  var p = params || {};
  if (p.g2EnableShort) return 'day';
  if (p.g2EnableLong) return 'week';
  return 'day';
}

function dc2PrimaryPeriod(params) {
  var p = params || {};
  if (p.d2EnableShort) return 'day';
  if (p.d2EnableLong) return 'week';
  return 'day';
}

function bogoPrimaryPeriod(params) {
  var p = params || {};
  if (p.boEnableDay) return 'day';
  if (p.boEnableWeek) return 'week';
  if (p.boEnableMonth) return 'month';
  return 'day';
}

function trendmPrimaryPeriod(params) {
  return 'min30';
}

function chartPrimaryPeriod(strategyId, params) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId === 'ultra') return 'min30';
  if (strategyId === 'trend') return 'day';
  if (strategyId === 'medium') return 'week';
  if (strategyId === 'long') return 'month';
  if (strategyId === 'nrf') {
    var nrfBundle = params && params.activeTier ? params : loadNrfBundleRaw();
    return chartPrimaryPeriod(tierApiId(nrfBundle.activeTier || LADDER_TIER_SHORT));
  }
  if (strategyId === 'retest') return retestPrimaryPeriod(params);
  if (strategyId === 'gc2') return gc2PrimaryPeriod(params);
  if (strategyId === 'dc2') return dc2PrimaryPeriod(params);
  if (strategyId === 'bogo') return bogoPrimaryPeriod(params);
  if (strategyId === 'trendm') return trendmPrimaryPeriod(params);
  return null;
}

function normalizeStrategyId(strategyId) {
  if (!strategyId) return strategyId;
  if (strategyId === 'ultraLow' || strategyId === 'ladder') return 'nrf';
  return strategyId;
}

function storageKey(strategyId) {
  return STORAGE_PREFIX + normalizeStrategyId(strategyId || 'trend');
}

function clone(obj) {
  return JSON.parse(JSON.stringify(obj));
}

function getDefaults(strategyId) {
  return clone(DEFAULTS_BY_STRATEGY[normalizeStrategyId(strategyId)] || {});
}

function normalize(strategyId, raw) {
  strategyId = normalizeStrategyId(strategyId);
  var defaults = getDefaults(strategyId);
  var schema = SCHEMA_BY_STRATEGY[strategyId] || [];
  var out = clone(defaults);
  if (!raw || typeof raw !== 'object') {
    return out;
  }
  schema.forEach(function (field) {
    if (raw[field.key] === undefined || raw[field.key] === null) return;
    if (field.type === 'switch') {
      out[field.key] = !!raw[field.key];
    } else if (field.key === 'uTierMin' || field.key === 'rTierMin' || field.key === 'pTierMin' || field.key === 'cTierMin' || field.key === 'lTierMin' || field.key === 'tTierMin' || field.key === 'g2TierMin') {
      out[field.key] = String(raw[field.key]).toUpperCase();
    } else {
      out[field.key] = raw[field.key];
    }
  });
  if (strategyId === 'resonance' && !out.cEnableShort && !out.cEnableMedium && !out.cEnableLong) {
    out.cEnableShort = true;
  }
  if (strategyId === 'rebound') {
    if (!out.rEnableShort && !out.rEnableMedium && !out.rEnableLong) {
      out.rEnableShort = true;
    }
  }
  if (strategyId === 'ladder') {
    migrateLadderTier(raw, out);
    if (!out.lEnableUltra && !out.lEnableShort && !out.lEnableMedium && !out.lEnableLong) {
      out.lEnableUltra = true;
    }
  }
  if (strategyId === 'retest') {
    if (!out.tEnableBear && !out.tEnableBull) {
      out.tEnableBear = true;
    }
    if (!out.tEnableUltra && !out.tEnableShort && !out.tEnableMedium && !out.tEnableLong) {
      out.tEnableUltra = true;
    }
  }
  if (strategyId === 'gc2') {
    if (!out.g2EnableShort && !out.g2EnableLong) {
      out.g2EnableShort = true;
    }
    if (out.g2TierMin === 'A') {
      out.g2TierMin = 'B';
    }
  }
  if (strategyId === 'dc2') {
    if (!out.d2EnableShort && !out.d2EnableLong) {
      out.d2EnableShort = true;
    }
    if (out.d2TierMin === 'A') {
      out.d2TierMin = 'B';
    }
  }
  if (strategyId === 'bogo') {
    if (!out.boEnableDay && !out.boEnableWeek && !out.boEnableMonth) {
      out.boEnableDay = true;
    }
  }
  return out;
}

function load(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId === 'nrf') {
    return loadNrfBundleRaw();
  }
  try {
    var saved = wx.getStorageSync(storageKey(strategyId));
    return normalize(strategyId, saved);
  } catch (e) {
    return getDefaults(strategyId);
  }
}

function save(strategyId, params) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId === 'nrf') {
    if (params && params.short && params.medium && params.long) {
      var nrfBundle = normalizeLadderBundle(params);
      wx.setStorageSync(storageKey('nrf'), nrfBundle);
      return nrfBundle;
    }
    var nrfTier = (params && params.activeTier) || loadNrfBundleRaw().activeTier || LADDER_TIER_SHORT;
    return saveNrfTierForm(nrfTier, params, true);
  }
  var normalized = normalize(strategyId, params);
  wx.setStorageSync(storageKey(strategyId), normalized);
  return normalized;
}

function reset(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId === 'nrf') {
    var nrfDefaults = buildNrfBundleDefaults();
    wx.setStorageSync(storageKey('nrf'), clone(nrfDefaults));
    return clone(nrfDefaults);
  }
  var defaults = getDefaults(strategyId);
  wx.setStorageSync(storageKey(strategyId), clone(defaults));
  return clone(defaults);
}

function getSchema(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  return schemaWithFrictionlessGate(strategyId, SCHEMA_BY_STRATEGY[strategyId] || []);
}

function hasCustomParams(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId === 'nrf') return true;
  return (getSchema(strategyId) || []).length > 0;
}

/** 转为 findMy / rescan 查询参数 */
function toApiParamsFromForm(apiStrategyId, params) {
  var schema = SCHEMA_BY_STRATEGY[apiStrategyId] || [];
  if (!schema.length) return {};
  var api = {};
  schema.forEach(function (field) {
    var val = params[field.key];
    if (val === undefined || val === null) return;
    if (field.type === 'switch') {
      api[field.key] = val ? 1 : 0;
    } else {
      api[field.key] = val;
    }
  });
  return api;
}

function toApiParams(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId === 'nrf') {
    var nrfBundle = loadNrfBundleRaw();
    var nrfActive = nrfBundle.activeTier || LADDER_TIER_SHORT;
    return Object.assign(
      { nrfActiveTier: nrfActive },
      toApiParamsFromForm(tierApiId(nrfActive), nrfBundle[nrfActive])
    );
  }
  return toApiParamsFromForm(strategyId, load(strategyId));
}

function isCustomized(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId === 'nrf') {
    return JSON.stringify(loadNrfBundleRaw()) !== JSON.stringify(buildNrfBundleDefaults());
  }
  var current = load(strategyId);
  var defaults = getDefaults(strategyId);
  return JSON.stringify(current) !== JSON.stringify(defaults);
}

function tierLabelFrom(picker, tierVal) {
  var label = '全部';
  (picker || { options: [] }).options.forEach(function (o) {
    if (o.value === tierVal) label = o.label.replace(/\(.*\)/, '').trim();
  });
  return label;
}

function formatSummary(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  if (!hasCustomParams(strategyId)) return '';
  var p = load(strategyId);
  if (strategyId === 'rebound') {
    var rModes = [];
    if (p.rEnableShort) rModes.push('短线');
    if (p.rEnableMedium) rModes.push('中线');
    if (p.rEnableLong) rModes.push('长线');
    return (rModes.length ? rModes.join('+') : '未启用') + ' · '
      + tierLabelFrom(REBOUND_TIER_PICKER, p.rTierMin);
  }
  if (strategyId === 'trend') {
    var macdParts = [];
    if (p.trRequireMonthMacd) macdParts.push('月MACD');
    if (p.trRequireWeekMacd) macdParts.push('周MACD');
    if (p.trRequireDayMacd) macdParts.push('日MACD');
    if (p.trRequireWeekGoldenCross) macdParts.push('周金叉');
    var amountPart = (p.trMinAmountWan != null ? p.trMinAmountWan : 5000) + '万';
    var weekPart = (p.trPrevWeeks != null ? p.trPrevWeeks : 2) + '周基准';
    var sigPart = p.trRequireCurrentBreakout ? '当日突破' : '本周突破';
    return amountPart + ' · ' + weekPart + ' · ' + sigPart
      + (macdParts.length ? ' · ' + macdParts.join('+') : '')
      + (p.trRequireUltra !== false ? ' · +超短' : '');
  }
  if (strategyId === 'medium') {
    var mdMacdParts = [];
    if (p.mdRequireMonthMacd) mdMacdParts.push('月MACD');
    if (p.mdRequireYearMacd) mdMacdParts.push('年MACD');
    if (p.mdRequireMonthGoldenCross) mdMacdParts.push('月金叉');
    var mdAmountPart = (p.mdMinAmountWan != null ? p.mdMinAmountWan : 5000) + '万';
    var monthPart = (p.mdPrevMonths != null ? p.mdPrevMonths : 2) + '月基准';
    var mdSigPart = p.mdRequireCurrentBreakout ? '当周突破' : '本月突破';
    return mdAmountPart + ' · ' + monthPart + ' · ' + mdSigPart
      + (mdMacdParts.length ? ' · ' + mdMacdParts.join('+') : '')
      + (p.mdRequireUltra !== false ? ' · +超短' : '');
  }
  if (strategyId === 'long') {
    var lgMacdParts = [];
    if (p.lgRequireYearMacd) lgMacdParts.push('年MACD');
    if (p.lgRequireMonthMacd) lgMacdParts.push('月MACD');
    if (p.lgRequireYearGoldenCross) lgMacdParts.push('年金叉');
    var lgAmountPart = (p.lgMinAmountWan != null ? p.lgMinAmountWan : 5000) + '万';
    var yearPart = (p.lgPrevYears != null ? p.lgPrevYears : 2) + '年基准';
    var lgSigPart = p.lgRequireCurrentBreakout ? '当月突破' : '本年突破';
    return lgAmountPart + ' · ' + yearPart + ' · ' + lgSigPart
      + (lgMacdParts.length ? ' · ' + lgMacdParts.join('+') : '')
      + (p.lgRequireUltra !== false ? ' · +超短' : '');
  }
  if (strategyId === 'resonance') {
    var resModes = [];
    if (p.cEnableShort) resModes.push('短线');
    if (p.cEnableMedium) resModes.push('中线');
    if (p.cEnableLong) resModes.push('长线');
    return (resModes.length ? resModes.join('+') : '未启用') + ' · '
      + tierLabelFrom(RESONANCE_TIER_PICKER, p.cTierMin);
  }
  if (strategyId === 'nrf') {
    var nrfBundle2 = loadNrfBundleRaw();
    var nrfTier = nrfBundle2.activeTier || LADDER_TIER_SHORT;
    return nrfTierTitle(nrfTier) + ' · ' + formatTierSummary(nrfTier, nrfBundle2[nrfTier]) + ' · 双低门 · 无阻力门';
  }
  if (strategyId === 'retest') {
    var tModes = [];
    if (p.tEnableBear) tModes.push('下跌反转');
    if (p.tEnableBull) tModes.push('上涨中继');
    var tTiers = [];
    if (p.tEnableUltra) tTiers.push('超短');
    if (p.tEnableShort) tTiers.push('短');
    if (p.tEnableMedium) tTiers.push('中');
    if (p.tEnableLong) tTiers.push('长');
    return (tModes.length ? tModes.join('+') : '未启用模式') + ' · '
      + (tTiers.length ? tTiers.join('+') : '未启用档位') + ' · '
      + tierLabelFrom(RETEST_TIER_PICKER, p.tTierMin);
  }
  if (strategyId === 'gc2') {
    var g2Modes = [];
    if (p.g2EnableShort) g2Modes.push('短线');
    if (p.g2EnableLong) g2Modes.push('长线');
    return (g2Modes.length ? g2Modes.join('+') : '未启用') + ' · '
      + tierLabelFrom(GC2_TIER_PICKER, p.g2TierMin);
  }
  if (strategyId === 'dc2') {
    var d2Modes = [];
    if (p.d2EnableShort) d2Modes.push('短线');
    if (p.d2EnableLong) d2Modes.push('长线');
    return (d2Modes.length ? d2Modes.join('+') : '未启用') + ' · '
      + tierLabelFrom(DC2_TIER_PICKER, p.d2TierMin);
  }
  if (strategyId === 'bogo') {
    var boModes = [];
    if (p.boEnableDay) boModes.push('日K');
    if (p.boEnableWeek) boModes.push('周K');
    if (p.boEnableMonth) boModes.push('月K');
    return (boModes.length ? boModes.join('+') : '未启用') + ' · 双低门 · 无阻力门 · 金叉/死叉突破';
  }
  if (strategyId === 'trendm') {
    return '日+周+月基准突破 · 双低门 · 无阻力门 · min30梯子';
  }
  if (strategyId === 'ultra') {
    var macdParts = [];
    if (p.ulRequireDayMacd) macdParts.push('日MACD>0');
    if (p.ulRequireDayMacdNegative) macdParts.push('日MACD<0');
    if (p.ulRequireWeekMacdNegative) macdParts.push('周MACD<0');
    if (p.ulRequireMonthMacdNegative) macdParts.push('月MACD<0');
    var amountPart = (p.ulMinAmountWan != null ? p.ulMinAmountWan : 5000) + '万';
    var sigPart = p.ulRequireCurrentBreakout ? '当前K突破' : '当日有突破';
    return '双低门 · 无阻力门 · ' + amountPart + ' · ' + sigPart
      + (macdParts.length ? ' · ' + macdParts.join('+') : '');
  }
  return '';
}

module.exports = {
  getDefaults: getDefaults,
  getSchema: getSchema,
  getTierSchema: getTierSchema,
  getPanelSchema: getPanelSchema,
  loadTierFormFor: loadTierFormFor,
  saveTierFormFor: saveTierFormFor,
  resetTierFor: resetTierFor,
  hasCustomParams: hasCustomParams,
  load: load,
  loadTierForm: loadTierForm,
  save: save,
  saveTierForm: saveTierForm,
  reset: reset,
  resetLadderTier: resetLadderTier,
  toApiParams: toApiParams,
  isCustomized: isCustomized,
  formatSummary: formatSummary,
  normalize: normalize,
  resolveApiStrategyId: resolveApiStrategyId,
  getActiveTierParams: getActiveTierParams,
  ladderTierTitle: ladderTierTitle,
  nrfTierTitle: nrfTierTitle,
  LADDER_TIERS: LADDER_TIERS,
  NRF_TIERS: NRF_TIERS,
  getTierListFor: getTierListFor,
  ladderPrimaryPeriod: ladderPrimaryPeriod,
  retestPrimaryPeriod: retestPrimaryPeriod,
  gc2PrimaryPeriod: gc2PrimaryPeriod,
  dc2PrimaryPeriod: dc2PrimaryPeriod,
  bogoPrimaryPeriod: bogoPrimaryPeriod,
  trendmPrimaryPeriod: trendmPrimaryPeriod,
  chartPrimaryPeriod: chartPrimaryPeriod
};

