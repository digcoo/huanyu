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

var CASCADE_DEFAULTS = {
  caEnableDualLowGate: false,
  caEnableMacdGate: false,
  caEnableCrossLowGate: false,
  caEnableDay: true,
  caEnableWeek: false,
  caEnableMonth: false,
  caLookbackDay: 60,
  caLookbackWeek: 52,
  caLookbackMonth: 36,
  caMinAmountWan: 3000,
  caRequireUltra: false
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
    hint: '开启后须同时满足 30m 跨日桶柱内突破；ul* 参数沿用超短 Tab 设置',
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

var CASCADE_SCHEMA = [
  {
    type: 'section',
    label: '可选三门',
    hint: '默认均关闭；开启后日/周/月须全部满足对应门控'
  },
  {
    key: 'caEnableDualLowGate',
    label: '双低支撑门',
    type: 'switch'
  },
  {
    key: 'caEnableMacdGate',
    label: '无阻力 MACD 门',
    type: 'switch'
  },
  {
    key: 'caEnableCrossLowGate',
    label: 'MACD 交叉 low 门',
    type: 'switch'
  },
  {
    key: 'caMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）；0=不启用',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    type: 'section',
    label: '级联档位',
    hint: '日K边沿突破对应基准 high；破日须周稳、破周须月稳；周/月档须日close>日基准high；多档勾选取并集'
  },
  {
    key: 'caEnableDay',
    label: '突破日基准',
    type: 'switch'
  },
  {
    key: 'caEnableWeek',
    label: '突破周基准',
    type: 'switch'
  },
  {
    key: 'caEnableMonth',
    label: '突破月基准',
    type: 'switch'
  },
  {
    type: 'section',
    label: '交叉 K 回溯',
    hint: '各周期向前搜索 MACD 金叉/死叉的最大 K 数'
  },
  {
    key: 'caLookbackDay',
    label: '日 K lookback',
    type: 'slider',
    min: 20,
    max: 120,
    step: 5,
    unit: '根'
  },
  {
    key: 'caLookbackWeek',
    label: '周 K lookback',
    type: 'slider',
    min: 20,
    max: 104,
    step: 4,
    unit: '根'
  },
  {
    key: 'caLookbackMonth',
    label: '月 K lookback',
    type: 'slider',
    min: 12,
    max: 60,
    step: 4,
    unit: '根'
  },
  {
    type: 'section',
    label: '超短叠加',
    hint: '开启后须同时满足 30m 跨日桶柱内突破；ul* 参数沿用「超短」Tab 设置'
  },
  {
    key: 'caRequireUltra',
    label: '须满足超短 Min30 突破',
    type: 'switch'
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
    hint: '开启后须同时满足 30m 跨日桶柱内突破；ul* 参数沿用超短 Tab 设置',
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
    hint: '开启后须同时满足 30m 跨日桶柱内突破；ul* 参数沿用超短 Tab 设置',
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
  nrf: [],
  retest: RETEST_SCHEMA,
  gc2: GC2_SCHEMA,
  dc2: DC2_SCHEMA,
  cascade: CASCADE_SCHEMA
};

var LADDER_TIER_SHORT = 'short';
var LADDER_TIER_MEDIUM = 'medium';
var LADDER_TIER_LONG = 'long';

var NRF_TIERS = [
  { id: LADDER_TIER_SHORT, apiId: 'trend', label: '日K', title: '日K跨周桶' },
  { id: LADDER_TIER_MEDIUM, apiId: 'medium', label: '周K', title: '周K跨月桶' },
  { id: LADDER_TIER_LONG, apiId: 'long', label: '月K', title: '月K跨年桶' }
];

function nrfTierFormDefaults(tierKey) {
  var base = tierKey === LADDER_TIER_MEDIUM ? MEDIUM_DEFAULTS
    : tierKey === LADDER_TIER_LONG ? LONG_DEFAULTS : TREND_DEFAULTS;
  var out = clone(base);
  if (tierKey === LADDER_TIER_SHORT) {
    out.trRequireUltra = false;
    out.trRequireCurrentBreakout = true;
    out.trRequireMonthMacd = false;
    out.trRequireWeekMacd = false;
    out.trRequireDayMacd = false;
    out.trRequireWeekGoldenCross = false;
  } else if (tierKey === LADDER_TIER_MEDIUM) {
    out.mdRequireUltra = false;
    out.mdRequireCurrentBreakout = true;
    out.mdRequireMonthMacd = false;
    out.mdRequireYearMacd = false;
    out.mdRequireMonthGoldenCross = false;
  } else {
    out.lgRequireUltra = false;
    out.lgRequireCurrentBreakout = true;
    out.lgRequireYearMacd = false;
    out.lgRequireMonthMacd = false;
    out.lgRequireYearGoldenCross = false;
  }
  return out;
}

function buildNrfBundleDefaults() {
  return {
    activeTier: LADDER_TIER_SHORT,
    short: nrfTierFormDefaults(LADDER_TIER_SHORT),
    medium: nrfTierFormDefaults(LADDER_TIER_MEDIUM),
    long: nrfTierFormDefaults(LADDER_TIER_LONG)
  };
}

var NRF_BUNDLE_DEFAULTS = buildNrfBundleDefaults();

function migrateLegacyNrfBundle(raw) {
  if (!raw || typeof raw !== 'object') return null;
  if (raw.activeTier && raw.short) return raw;
  var def = buildNrfBundleDefaults();
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
        wx.setStorageSync(storageKey('nrf'), normalizeLadderBundle(saved));
      }
    }
    return normalizeLadderBundle(saved || buildNrfBundleDefaults());
  } catch (e) {
    return buildNrfBundleDefaults();
  }
}

function sanitizeNrfTierParams(tierKey, params) {
  var out = clone(params);
  if (tierKey === LADDER_TIER_SHORT) {
    out.trRequireMonthMacd = false;
    out.trRequireWeekMacd = false;
    out.trRequireDayMacd = false;
    out.trRequireWeekGoldenCross = false;
  } else if (tierKey === LADDER_TIER_MEDIUM) {
    out.mdRequireMonthMacd = false;
    out.mdRequireYearMacd = false;
    out.mdRequireMonthGoldenCross = false;
  } else {
    out.lgRequireYearMacd = false;
    out.lgRequireMonthMacd = false;
    out.lgRequireYearGoldenCross = false;
  }
  return out;
}

function loadNrfTierForm(tier) {
  var bundle = loadNrfBundleRaw();
  var key = tier || bundle.activeTier || LADDER_TIER_SHORT;
  return sanitizeNrfTierParams(key, clone(bundle[key] || bundle.short));
}

function saveNrfTierForm(tier, form, setActive) {
  var bundle = loadNrfBundleRaw();
  var tierKey = (form && form.nrfActiveTier) || tier || bundle.activeTier || LADDER_TIER_SHORT;
  if (setActive) {
    bundle.activeTier = tierKey;
  }
  var tierForm = clone(form || {});
  delete tierForm.nrfActiveTier;
  var normalized = sanitizeNrfTierParams(tierKey, normalizeTierParams(tierKey, tierForm));
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
  }, {
    type: 'section',
    label: 'MACD 交叉 low 门',
    hint: '固定：现价 > 最近一根 MACD 交叉 K 的 low（金叉或死叉），日、周、月须全部满足'
  }];
}

var NRF_TIER_PICKER = [
  { value: LADDER_TIER_SHORT, label: '日 K · 跨周桶' },
  { value: LADDER_TIER_MEDIUM, label: '周 K · 跨月桶' },
  { value: LADDER_TIER_LONG, label: '月 K · 跨年桶' }
];

var NRF_IN_BAR_HINT = '背景桶内任一根强K可为基准；中间K收盘<=ref.high；'
  + '信号K须 close>前K.high、close>=ref.low 且 min(low,前收)<=ref.high';

function nrfTierBodySchema(tierId) {
  if (tierId === LADDER_TIER_MEDIUM || tierApiId(tierId) === 'medium') {
    return [{
      type: 'section',
      label: '档位参数',
      hint: '近6日日均成交额；信号月之前的完整自然月数'
    }, {
      key: 'mdMinAmountWan',
      label: '最低成交额',
      hint: '近6日日均成交额（万）',
      type: 'slider',
      min: 1000,
      max: 10000,
      step: 500,
      unit: '万'
    }, {
      key: 'mdPrevMonths',
      label: '基准背景月数',
      hint: '自然月，信号月之前的完整月数',
      type: 'slider',
      min: 1,
      max: 4,
      step: 1,
      unit: '月'
    }, {
      type: 'section',
      label: '突破 K',
      hint: '开启则须最新一根操作周期 K 满足信号条件；关闭则本周期任一根满足即可'
    }, {
      key: 'mdRequireCurrentBreakout',
      label: '当前 K 须为突破 K',
      type: 'switch'
    }, {
      type: 'section',
      label: '超短叠加',
      hint: '开启后须同时满足 30m 跨日桶柱内突破；ul* 参数沿用超短 Tab 设置'
    }, {
      key: 'mdRequireUltra',
      label: '须满足超短 30m 突破',
      type: 'switch'
    }];
  }
  if (tierId === LADDER_TIER_LONG || tierApiId(tierId) === 'long') {
    return [{
      type: 'section',
      label: '档位参数',
      hint: '近6日日均成交额；信号年之前的完整自然年数'
    }, {
      key: 'lgMinAmountWan',
      label: '最低成交额',
      hint: '近6日日均成交额（万）',
      type: 'slider',
      min: 1000,
      max: 10000,
      step: 500,
      unit: '万'
    }, {
      key: 'lgPrevYears',
      label: '基准背景年数',
      hint: '自然年，信号年之前的完整年数',
      type: 'slider',
      min: 1,
      max: 4,
      step: 1,
      unit: '年'
    }, {
      type: 'section',
      label: '突破 K',
      hint: '开启则须最新一根操作周期 K 满足信号条件；关闭则本周期任一根满足即可'
    }, {
      key: 'lgRequireCurrentBreakout',
      label: '当前 K 须为突破 K',
      type: 'switch'
    }, {
      type: 'section',
      label: '超短叠加',
      hint: '开启后须同时满足 30m 跨日桶柱内突破；ul* 参数沿用超短 Tab 设置'
    }, {
      key: 'lgRequireUltra',
      label: '须满足超短 30m 突破',
      type: 'switch'
    }];
  }
  return [{
    type: 'section',
    label: '档位参数',
    hint: '近6日日均成交额；信号周之前的完整自然周数'
  }, {
    key: 'trMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  }, {
    key: 'trPrevWeeks',
    label: '基准背景周数',
    hint: '自然周，信号周之前的完整周数',
    type: 'slider',
    min: 1,
    max: 4,
      step: 1,
      unit: '周'
    }, {
      type: 'section',
      label: '突破 K',
      hint: '开启则须最新一根操作周期 K 满足信号条件；关闭则本周期任一根满足即可'
    }, {
      key: 'trRequireCurrentBreakout',
      label: '当前 K 须为突破 K',
      type: 'switch'
    }, {
      type: 'section',
      label: '超短叠加',
      hint: '开启后须同时满足 30m 跨日桶柱内突破；ul* 参数沿用超短 Tab 设置'
    }, {
      key: 'trRequireUltra',
    label: '须满足超短 30m 突破',
    type: 'switch'
  }];
}

function getNrfPanelSchema(tierId) {
  var tier = tierId || LADDER_TIER_SHORT;
  return getFrictionlessGateHeadSchema().concat([{
    type: 'section',
    label: '运行档位',
    hint: '一次只扫描一档；在更大周期背景桶内找强K基准，信号K须柱内突破'
  }, {
    key: 'nrfActiveTier',
    label: '当前档位',
    type: 'picker',
    options: NRF_TIER_PICKER
  }, {
    type: 'section',
    label: '跨周期柱内突破',
    hint: NRF_IN_BAR_HINT
  }]).concat(nrfTierBodySchema(tier));
}

var FRICTIONLESS_GATE_STRATEGIES = { ultra: true, nrf: true };

function schemaWithFrictionlessGate(strategyId, schema) {
  if (!FRICTIONLESS_GATE_STRATEGIES[strategyId]) {
    return schema || [];
  }
  if (strategyId === 'nrf') {
    return getNrfPanelSchema(LADDER_TIER_SHORT);
  }
  var body = schema || [];
  if (body[0] && body[0].label === '双低支撑门') {
    while (body[0] && (body[0].label === '双低支撑门' || body[0].label === '无阻力 MACD 门'
        || body[0].label === 'MACD 交叉 low 门')) {
      body = body.slice(1);
    }
  } else if (body[0] && body[0].label === '无阻力 MACD 门') {
    body = body.slice(1);
  }
  return getFrictionlessGateHeadSchema().concat(body);
}

function nrfBreakoutModeLabel(tier, p) {
  if (tier === LADDER_TIER_MEDIUM || tierApiId(tier) === 'medium') {
    return p.mdRequireCurrentBreakout !== false ? '当前K突破' : '周期内突破';
  }
  if (tier === LADDER_TIER_LONG || tierApiId(tier) === 'long') {
    return p.lgRequireCurrentBreakout !== false ? '当前K突破' : '周期内突破';
  }
  return p.trRequireCurrentBreakout !== false ? '当前K突破' : '周期内突破';
}

function formatNrfTierSummary(tier, p) {
  var breakoutPart = ' · ' + nrfBreakoutModeLabel(tier, p);
  if (tier === LADDER_TIER_MEDIUM || tierApiId(tier) === 'medium') {
    return (p.mdMinAmountWan != null ? p.mdMinAmountWan : 5000) + '万 · '
      + (p.mdPrevMonths != null ? p.mdPrevMonths : 2) + '月基准'
      + breakoutPart
      + (p.mdRequireUltra ? ' · +min30' : '');
  }
  if (tier === LADDER_TIER_LONG || tierApiId(tier) === 'long') {
    return (p.lgMinAmountWan != null ? p.lgMinAmountWan : 5000) + '万 · '
      + (p.lgPrevYears != null ? p.lgPrevYears : 2) + '年基准'
      + breakoutPart
      + (p.lgRequireUltra ? ' · +min30' : '');
  }
  return (p.trMinAmountWan != null ? p.trMinAmountWan : 5000) + '万 · '
    + (p.trPrevWeeks != null ? p.trPrevWeeks : 2) + '周基准'
    + breakoutPart
    + (p.trRequireUltra ? ' · +min30' : '');
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
  nrf: NRF_BUNDLE_DEFAULTS,
  retest: RETEST_DEFAULTS,
  gc2: GC2_DEFAULTS,
  dc2: DC2_DEFAULTS,
  cascade: CASCADE_DEFAULTS
};

var TIER_PICKER = null;
var RESONANCE_TIER_PICKER = RESONANCE_SCHEMA.find(function (f) { return f.key === 'cTierMin'; });
var REBOUND_TIER_PICKER = REBOUND_SCHEMA.find(function (f) { return f.key === 'rTierMin'; });
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
  var def = buildNrfBundleDefaults();
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
  var bundle = buildNrfBundleDefaults();
  try {
    var tr = wx.getStorageSync(STORAGE_PREFIX + 'trend');
    if (tr) bundle.short = normalize('trend', tr);
    var md = wx.getStorageSync(STORAGE_PREFIX + 'medium');
    if (md) bundle.medium = normalize('medium', md);
    var lg = wx.getStorageSync(STORAGE_PREFIX + 'long');
    if (lg) bundle.long = normalize('long', lg);
    var oldLadder = wx.getStorageSync(STORAGE_PREFIX + 'ladder');
    if (oldLadder && oldLadder.lLadderTier) {
      var tierCode = String(oldLadder.lLadderTier).toUpperCase();
      if (tierCode === 'B') bundle.activeTier = LADDER_TIER_MEDIUM;
      else if (tierCode === 'C') bundle.activeTier = LADDER_TIER_LONG;
      else bundle.activeTier = LADDER_TIER_SHORT;
    }
  } catch (e) {}
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

function cascadePrimaryPeriod(params) {
  var p = params || {};
  if (p.caEnableMonth) return 'month';
  if (p.caEnableWeek) return 'week';
  if (p.caEnableDay) return 'day';
  return 'day';
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
  if (strategyId === 'cascade') return cascadePrimaryPeriod(params);
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
  if (strategyId === 'cascade') {
    if (!out.caEnableDay && !out.caEnableWeek && !out.caEnableMonth) {
      out.caEnableDay = true;
    }
  }
  if (strategyId === 'ultra') {
    if (out.ulRequireDayMacd && out.ulRequireDayMacdNegative) {
      out.ulRequireDayMacdNegative = false;
    }
  }
  return out;
}

/** 当前参数组合可能导致零结果时的提示（供首页空态） */
function emptyResultHint(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId !== 'ultra') {
    return isCustomized(strategyId) ? '当前参数无匹配，可点 ⚙ 恢复默认' : '';
  }
  var p = load('ultra');
  if (p.ulRequireDayMacdNegative && p.ulRequireWeekMacdNegative && p.ulRequireMonthMacdNegative) {
    return '日/周/月 MACD<0 全开时无匹配标的，请点 ⚙ 恢复默认';
  }
  if (isCustomized('ultra')) {
    return '当前参数无匹配，可点 ⚙ 恢复默认或放宽 MACD/突破 条件';
  }
  return '';
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
    return nrfTierTitle(nrfTier) + ' · ' + formatNrfTierSummary(nrfTier, nrfBundle2[nrfTier]) + ' · 三门全局';
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
  if (strategyId === 'cascade') {
    var caModes = [];
    if (p.caEnableDay) caModes.push('日基准');
    if (p.caEnableWeek) caModes.push('周基准');
    if (p.caEnableMonth) caModes.push('月基准');
    var gateParts = [];
    if (p.caEnableDualLowGate) gateParts.push('双低');
    if (p.caEnableMacdGate) gateParts.push('无阻力');
    if (p.caEnableCrossLowGate) gateParts.push('交叉low');
    return (caModes.length ? caModes.join('+') : '未启用') + ' · 级联交叉突破'
      + (gateParts.length ? ' · ' + gateParts.join('+') : '')
      + (p.caMinAmountWan != null && p.caMinAmountWan > 0 ? ' · ' + p.caMinAmountWan + '万' : '')
      + (p.caRequireUltra ? ' · +min30' : '');
  }
  if (strategyId === 'ultra') {
    var macdParts = [];
    if (p.ulRequireDayMacd) macdParts.push('日MACD>0');
    if (p.ulRequireDayMacdNegative) macdParts.push('日MACD<0');
    if (p.ulRequireWeekMacdNegative) macdParts.push('周MACD<0');
    if (p.ulRequireMonthMacdNegative) macdParts.push('月MACD<0');
    var amountPart = (p.ulMinAmountWan != null ? p.ulMinAmountWan : 5000) + '万';
    var sigPart = p.ulRequireCurrentBreakout ? '当前K突破' : '当日有突破';
    return '三门全局 · ' + amountPart + ' · ' + sigPart
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
  save: save,
  reset: reset,
  toApiParams: toApiParams,
  isCustomized: isCustomized,
  formatSummary: formatSummary,
  emptyResultHint: emptyResultHint,
  normalize: normalize,
  resolveApiStrategyId: resolveApiStrategyId,
  getActiveTierParams: getActiveTierParams,
  nrfTierTitle: nrfTierTitle,
  NRF_TIERS: NRF_TIERS,
  getTierListFor: getTierListFor,
  retestPrimaryPeriod: retestPrimaryPeriod,
  gc2PrimaryPeriod: gc2PrimaryPeriod,
  dc2PrimaryPeriod: dc2PrimaryPeriod,
  cascadePrimaryPeriod: cascadePrimaryPeriod,
  chartPrimaryPeriod: chartPrimaryPeriod
};

