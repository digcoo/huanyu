/**
 * 策略自定义参数 · 本地存储 + API 查询字段映射
 */

var STORAGE_PREFIX = 'strategyParams_';

var TREND_DEFAULTS = {
  uMinAmountWan: 5000,
  uEnableShort: true,
  uEnableMedium: true,
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

var PRE_GOLDEN_DEFAULTS = {
  pMinAmountWan: 5000,
  pEnableShort: true,
  pEnableMedium: true,
  pEnableLong: true,
  pTierMin: 'ALL'
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

var TREND_SCHEMA = [
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
    key: 'uEnableShort',
    label: '短线金叉',
    hint: '周 MACD>0 + 日 MACD 金叉',
    type: 'switch'
  },
  {
    key: 'uEnableMedium',
    label: '中线金叉',
    hint: '月 MACD>0 + 周 MACD 金叉',
    type: 'switch'
  },
  {
    key: 'uEnableLong',
    label: '长线金叉',
    hint: '年 MACD>0 + 月 MACD 金叉',
    type: 'switch'
  },
  {
    key: 'uTierMin',
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

var PRE_GOLDEN_SCHEMA = [
  {
    key: 'pMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'pEnableShort',
    label: '短线预判',
    hint: '周 MACD>0 + 日 MACD<0 + 日K突破',
    type: 'switch'
  },
  {
    key: 'pEnableMedium',
    label: '中线预判',
    hint: '月 MACD>0 + 周 MACD<0 + 周K突破',
    type: 'switch'
  },
  {
    key: 'pEnableLong',
    label: '长线预判',
    hint: '年 MACD>0 + 月 MACD<0 + 月K突破',
    type: 'switch'
  },
  {
    key: 'pTierMin',
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

var SCHEMA_BY_STRATEGY = {
  trend: TREND_SCHEMA,
  preGolden: PRE_GOLDEN_SCHEMA,
  resonance: RESONANCE_SCHEMA,
  rebound: REBOUND_SCHEMA,
  ladder: LADDER_SCHEMA
};

var DEFAULTS_BY_STRATEGY = {
  trend: TREND_DEFAULTS,
  preGolden: PRE_GOLDEN_DEFAULTS,
  resonance: RESONANCE_DEFAULTS,
  rebound: REBOUND_DEFAULTS,
  ladder: LADDER_DEFAULTS
};

var TIER_PICKER = TREND_SCHEMA.find(function (f) { return f.key === 'uTierMin'; });
var PRE_GOLDEN_TIER_PICKER = PRE_GOLDEN_SCHEMA.find(function (f) { return f.key === 'pTierMin'; });
var RESONANCE_TIER_PICKER = RESONANCE_SCHEMA.find(function (f) { return f.key === 'cTierMin'; });
var REBOUND_TIER_PICKER = REBOUND_SCHEMA.find(function (f) { return f.key === 'rTierMin'; });
var LADDER_TIER_PICKER = LADDER_SCHEMA.find(function (f) { return f.key === 'lTierMin'; });

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
    } else if (field.key === 'uTierMin' || field.key === 'rTierMin' || field.key === 'pTierMin' || field.key === 'cTierMin' || field.key === 'lTierMin') {
      out[field.key] = String(raw[field.key]).toUpperCase();
    } else {
      out[field.key] = raw[field.key];
    }
  });
  if (strategyId === 'trend' && !out.uEnableShort && !out.uEnableMedium && !out.uEnableLong) {
    out.uEnableShort = true;
  }
  if (strategyId === 'preGolden' && !out.pEnableShort && !out.pEnableMedium && !out.pEnableLong) {
    out.pEnableShort = true;
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
    if (p.uEnableMedium) modes.push('中线');
    if (p.uEnableLong) modes.push('长线');
    return (modes.length ? modes.join('+') : '未启用') + ' · '
      + tierLabelFrom(TIER_PICKER, p.uTierMin);
  }
  if (strategyId === 'preGolden') {
    var preModes = [];
    if (p.pEnableShort) preModes.push('短线');
    if (p.pEnableMedium) preModes.push('中线');
    if (p.pEnableLong) preModes.push('长线');
    return (preModes.length ? preModes.join('+') : '未启用') + ' · '
      + tierLabelFrom(PRE_GOLDEN_TIER_PICKER, p.pTierMin);
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
  ladderPrimaryPeriod: ladderPrimaryPeriod
};

