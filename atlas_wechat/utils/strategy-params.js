/**
 * 策略自定义参数 · 本地存储 + API 查询字段映射
 */

var STORAGE_PREFIX = 'strategyParams_';

var TREND_DEFAULTS = {
  uMinAmountWan: 5000,
  uEnableShort: true,
  uEnableLong: true,
  uTierMin: 'ALL'
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

var ULTRA_DEFAULTS = {
  ulMinAmountWan: 5000,
  ulRequireMonthMacd: false,
  ulRequireWeekMacd: false,
  ulRequireDayMacd: false,
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
    type: 'section',
    label: '硬门槛',
    hint: '不满足则不入池'
  },
  {
    key: 'uMinAmountWan',
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
    key: 'uEnableShort',
    label: '短线（S）',
    hint: '月K 或 周K MACD > 0 · 日K 金叉',
    type: 'switch'
  },
  {
    key: 'uEnableLong',
    label: '长线（B）',
    hint: '年K 或 月K MACD > 0 · 周K 金叉',
    type: 'switch'
  },
  {
    type: 'section',
    label: '列表筛选'
  },
  {
    key: 'uTierMin',
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

var SCHEMA_BY_STRATEGY = {
  ultra: ULTRA_SCHEMA,
  trend: TREND_SCHEMA,
  resonance: RESONANCE_SCHEMA,
  rebound: REBOUND_SCHEMA,
  ladder: LADDER_SCHEMA,
  retest: RETEST_SCHEMA,
  gc2: GC2_SCHEMA,
  dc2: DC2_SCHEMA
};

var DEFAULTS_BY_STRATEGY = {
  ultra: ULTRA_DEFAULTS,
  trend: TREND_DEFAULTS,
  resonance: RESONANCE_DEFAULTS,
  rebound: REBOUND_DEFAULTS,
  ladder: LADDER_DEFAULTS,
  retest: RETEST_DEFAULTS,
  gc2: GC2_DEFAULTS,
  dc2: DC2_DEFAULTS
};

var TIER_PICKER = TREND_SCHEMA.find(function (f) { return f.key === 'uTierMin'; });
var RESONANCE_TIER_PICKER = RESONANCE_SCHEMA.find(function (f) { return f.key === 'cTierMin'; });
var REBOUND_TIER_PICKER = REBOUND_SCHEMA.find(function (f) { return f.key === 'rTierMin'; });
var LADDER_TIER_PICKER = LADDER_SCHEMA.find(function (f) { return f.key === 'lTierMin'; });
var RETEST_TIER_PICKER = RETEST_SCHEMA.find(function (f) { return f.key === 'tTierMin'; });
var GC2_TIER_PICKER = GC2_SCHEMA.find(function (f) { return f.key === 'g2TierMin'; });
var DC2_TIER_PICKER = DC2_SCHEMA.find(function (f) { return f.key === 'd2TierMin'; });

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

function chartPrimaryPeriod(strategyId, params) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId === 'ultra') return 'min30';
  if (strategyId === 'ladder') return ladderPrimaryPeriod(params);
  if (strategyId === 'retest') return retestPrimaryPeriod(params);
  if (strategyId === 'gc2') return gc2PrimaryPeriod(params);
  if (strategyId === 'dc2') return dc2PrimaryPeriod(params);
  return null;
}

function normalizeStrategyId(strategyId) {
  if (strategyId === 'ultraLow') return 'ladder';
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
  if (strategyId === 'trend' && !out.uEnableShort && !out.uEnableLong) {
    out.uEnableShort = true;
  }
  if (strategyId === 'trend' && out.uTierMin === 'A') {
    out.uTierMin = 'B';
  }
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
  return out;
}

function load(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  try {
    var saved = wx.getStorageSync(storageKey(strategyId));
    if (!saved && strategyId === 'ladder') {
      saved = wx.getStorageSync(STORAGE_PREFIX + 'ultraLow');
    }
    return normalize(strategyId, saved);
  } catch (e) {
    return getDefaults(strategyId);
  }
}

function save(strategyId, params) {
  var normalized = normalize(strategyId, params);
  wx.setStorageSync(storageKey(strategyId), normalized);
  return normalized;
}

function reset(strategyId) {
  var defaults = getDefaults(strategyId);
  wx.setStorageSync(storageKey(strategyId), clone(defaults));
  return clone(defaults);
}

function getSchema(strategyId) {
  return SCHEMA_BY_STRATEGY[normalizeStrategyId(strategyId)] || [];
}

function hasCustomParams(strategyId) {
  return (getSchema(normalizeStrategyId(strategyId)) || []).length > 0;
}

/** 转为 findMy / rescan 查询参数 */
function toApiParams(strategyId) {
  var params = load(strategyId);
  var schema = getSchema(strategyId);
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

function isCustomized(strategyId) {
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
    var modes = [];
    if (p.uEnableShort) modes.push('短线');
    if (p.uEnableLong) modes.push('长线');
    return (modes.length ? modes.join('+') : '未启用') + ' · '
      + tierLabelFrom(TIER_PICKER, p.uTierMin);
  }
  if (strategyId === 'resonance') {
    var resModes = [];
    if (p.cEnableShort) resModes.push('短线');
    if (p.cEnableMedium) resModes.push('中线');
    if (p.cEnableLong) resModes.push('长线');
    return (resModes.length ? resModes.join('+') : '未启用') + ' · '
      + tierLabelFrom(RESONANCE_TIER_PICKER, p.cTierMin);
  }
  if (strategyId === 'ladder') {
    var lModes = [];
    if (p.lEnableUltra) lModes.push('超短');
    if (p.lEnableShort) lModes.push('短');
    if (p.lEnableMedium) lModes.push('中');
    if (p.lEnableLong) lModes.push('长');
    return (lModes.length ? lModes.join('+') : '未启用') + ' · '
      + tierLabelFrom(LADDER_TIER_PICKER, p.lTierMin);
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
  if (strategyId === 'ultra') {
    var macdParts = [];
    if (p.ulRequireMonthMacd) macdParts.push('月MACD');
    if (p.ulRequireWeekMacd) macdParts.push('周MACD');
    if (p.ulRequireDayMacd) macdParts.push('日MACD');
    var amountPart = (p.ulMinAmountWan != null ? p.ulMinAmountWan : 5000) + '万';
    var sigPart = p.ulRequireCurrentBreakout ? '当前K突破' : '当日有突破';
    return amountPart + ' · ' + sigPart + (macdParts.length ? ' · ' + macdParts.join('+') : '');
  }
  return '';
}

module.exports = {
  getDefaults: getDefaults,
  getSchema: getSchema,
  hasCustomParams: hasCustomParams,
  load: load,
  save: save,
  reset: reset,
  toApiParams: toApiParams,
  isCustomized: isCustomized,
  formatSummary: formatSummary,
  normalize: normalize,
  ladderPrimaryPeriod: ladderPrimaryPeriod,
  retestPrimaryPeriod: retestPrimaryPeriod,
  gc2PrimaryPeriod: gc2PrimaryPeriod,
  dc2PrimaryPeriod: dc2PrimaryPeriod,
  chartPrimaryPeriod: chartPrimaryPeriod
};

