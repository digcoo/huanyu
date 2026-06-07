/**
 * 策略自定义参数 · 本地存储 + API 查询字段映射
 */

var STORAGE_PREFIX = 'strategyParams_';

var TREND_DEFAULTS = {
  uDayLookback: 8,
  uStrongYangPct: 3,
  uWeekContextMin: 2,
  uMinAmountWan: 5000,
  uEnableModeB: true,
  uEnableModeA: true,
  uEnableModeBWeak: true,
  uTierMin: 'ALL'
};

var REBOUND_DEFAULTS = {
  rMinAmountWan: 3000,
  rCapitulationDayPct: 6,
  rCapitulationWeekPct: 5,
  rMinDrawdownPct: 20,
  rCapitulationLookbackDays: 15,
  rCapitulationLookbackWeeks: 6,
  rEnableModeA: true,
  rEnableModeB: true,
  rEnableModeC: true,
  rTierMin: 'ALL'
};

var MULTI_DEFAULTS = {
  mMinResonancePeriods: 2,
  mMinAmountWan: 5000,
  mEnableModeA: true,
  mEnableModeB: true,
  mEnableModeC: true,
  mEnableWeakContext: false,
  mEnableWeakSingle: false,
  mTierMin: 'A'
};

var TREND_SCHEMA = [
  {
    key: 'uDayLookback',
    label: '日K平台回溯',
    hint: '突破近 N 根日K平台',
    type: 'slider',
    min: 5,
    max: 20,
    step: 1
  },
  {
    key: 'uStrongYangPct',
    label: '强阳阈值',
    hint: '前一日实体涨幅上限（%）',
    type: 'slider',
    min: 1,
    max: 8,
    step: 1,
    unit: '%'
  },
  {
    key: 'uWeekContextMin',
    label: '周K环境项数',
    hint: 'MA/低抬高等至少满足几项',
    type: 'slider',
    min: 1,
    max: 3,
    step: 1
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
    key: 'uEnableModeB',
    label: '趋势延续（模式B）',
    type: 'switch'
  },
  {
    key: 'uEnableModeBWeak',
    label: '含弱环境B档',
    hint: 'Context够但日K未确认',
    type: 'switch',
    dependsOn: 'uEnableModeB'
  },
  {
    key: 'uEnableModeA',
    label: '月K拐点（模式A）',
    type: 'switch'
  },
  {
    key: 'uTierMin',
    label: '最低展示档位',
    type: 'picker',
    options: [
      { value: 'ALL', label: '全部档位' },
      { value: 'A', label: 'A档及以上 (S+A)' },
      { value: 'S', label: '仅 S 档' }
    ]
  }
];

var REBOUND_SCHEMA = [
  {
    key: 'rCapitulationDayPct',
    label: '日K恐慌跌幅',
    hint: '情绪释放大阴线阈值（%）',
    type: 'slider',
    min: 3,
    max: 12,
    step: 1,
    unit: '%'
  },
  {
    key: 'rCapitulationWeekPct',
    label: '周K恐慌跌幅',
    hint: '大周期恐慌释放（%）',
    type: 'slider',
    min: 3,
    max: 15,
    step: 1,
    unit: '%'
  },
  {
    key: 'rMinDrawdownPct',
    label: '最小回撤',
    hint: '相对52周高回撤（%）',
    type: 'slider',
    min: 10,
    max: 50,
    step: 5,
    unit: '%'
  },
  {
    key: 'rCapitulationLookbackDays',
    label: '日K回溯',
    hint: '恐慌大跌检索根数',
    type: 'slider',
    min: 5,
    max: 30,
    step: 1
  },
  {
    key: 'rCapitulationLookbackWeeks',
    label: '周K回溯',
    hint: '恐慌大跌检索根数',
    type: 'slider',
    min: 3,
    max: 12,
    step: 1
  },
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
    key: 'rEnableModeA',
    label: '脱离（模式A）',
    hint: '反转波段突破',
    type: 'switch'
  },
  {
    key: 'rEnableModeB',
    label: '底上移（模式B）',
    type: 'switch'
  },
  {
    key: 'rEnableModeC',
    label: '底背离（模式C）',
    type: 'switch'
  },
  {
    key: 'rTierMin',
    label: '最低展示档位',
    type: 'picker',
    options: [
      { value: 'ALL', label: '全部档位' },
      { value: 'A', label: 'A档及以上 (S+A)' },
      { value: 'S', label: '仅 S 档' }
    ]
  }
];

var MULTI_SCHEMA = [
  {
    key: 'mMinResonancePeriods',
    label: '最少共振周期',
    hint: '日/周/月结构对齐至少几项',
    type: 'slider',
    min: 1,
    max: 3,
    step: 1
  },
  {
    key: 'mMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'mEnableModeA',
    label: '趋势波段突破（A）',
    type: 'switch'
  },
  {
    key: 'mEnableModeB',
    label: '反转波段突破（B）',
    type: 'switch'
  },
  {
    key: 'mEnableModeC',
    label: '梯子High突破（C）',
    type: 'switch'
  },
  {
    key: 'mEnableWeakContext',
    label: '含弱环境B档',
    hint: '有共振但无突破信号',
    type: 'switch'
  },
  {
    key: 'mEnableWeakSingle',
    label: '含单周期C档',
    type: 'switch'
  },
  {
    key: 'mTierMin',
    label: '最低展示档位',
    type: 'picker',
    options: [
      { value: 'ALL', label: '全部档位' },
      { value: 'A', label: 'A档及以上 (S+A)' },
      { value: 'S', label: '仅 S 档' }
    ]
  }
];

var SCHEMA_BY_STRATEGY = {
  trend: TREND_SCHEMA,
  rebound: REBOUND_SCHEMA,
  multi: MULTI_SCHEMA
};

var DEFAULTS_BY_STRATEGY = {
  trend: TREND_DEFAULTS,
  rebound: REBOUND_DEFAULTS,
  multi: MULTI_DEFAULTS
};

var TIER_PICKER = TREND_SCHEMA.find(function (f) { return f.key === 'uTierMin'; });
var REBOUND_TIER_PICKER = REBOUND_SCHEMA.find(function (f) { return f.key === 'rTierMin'; });
var MULTI_TIER_PICKER = MULTI_SCHEMA.find(function (f) { return f.key === 'mTierMin'; });

function storageKey(strategyId) {
  return STORAGE_PREFIX + (strategyId || 'trend');
}

function clone(obj) {
  return JSON.parse(JSON.stringify(obj));
}

function getDefaults(strategyId) {
  return clone(DEFAULTS_BY_STRATEGY[strategyId] || {});
}

function normalize(strategyId, raw) {
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
    } else if (field.key === 'uTierMin' || field.key === 'rTierMin') {
      out[field.key] = String(raw[field.key]).toUpperCase();
    } else {
      out[field.key] = raw[field.key];
    }
  });
  if (strategyId === 'trend' && !out.uEnableModeB) {
    out.uEnableModeBWeak = false;
  }
  return out;
}

function load(strategyId) {
  try {
    var saved = wx.getStorageSync(storageKey(strategyId));
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
  return SCHEMA_BY_STRATEGY[strategyId] || [];
}

function hasCustomParams(strategyId) {
  return (getSchema(strategyId) || []).length > 0;
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
  if (!hasCustomParams(strategyId)) return '';
  var p = load(strategyId);
  if (strategyId === 'rebound') {
    return '日跌' + p.rCapitulationDayPct + '% · 回撤' + p.rMinDrawdownPct + '% · '
      + tierLabelFrom(REBOUND_TIER_PICKER, p.rTierMin);
  }
  if (strategyId === 'trend') {
    return '回溯' + p.uDayLookback + ' · 周' + p.uWeekContextMin + '/3 · '
      + tierLabelFrom(TIER_PICKER, p.uTierMin);
  }
  if (strategyId === 'multi') {
    return '共振' + p.mMinResonancePeriods + '/3 · '
      + tierLabelFrom(MULTI_TIER_PICKER, p.mTierMin);
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
  normalize: normalize
};

