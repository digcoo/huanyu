/**
 * 策略参数：趋势回踩破 Low / 破 High
 */
var strategyNav = require('./strategy-nav');

var STORAGE_PREFIX = 'strategyParams_';

var DAY_LAST_BAR_YANG_HINT = '日K末阳(close≥open)；';

var LOW_DEFAULTS = {
  trlEnableMinAmountFilter: true,
  trlMinAmountWan: 3000
};

var HIGH_DEFAULTS = {
  trhEnableMinAmountFilter: true,
  trhMinAmountWan: 3000
};

var LOW_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: DAY_LAST_BAR_YANG_HINT + 'MACD>0；回踩 bandLow；末 K 收阳；bandLow<close≤bandHigh'
  },
  {
    key: 'trlEnableMinAmountFilter',
    label: '成交额门',
    type: 'switch'
  },
  {
    key: 'trlMinAmountWan',
    label: '最低日均成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  }
];

var HIGH_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: DAY_LAST_BAR_YANG_HINT + 'MACD>0；回踩 bandLow；末 K 首次边沿突破末波段 bandHigh'
  },
  {
    key: 'trhEnableMinAmountFilter',
    label: '成交额门',
    type: 'switch'
  },
  {
    key: 'trhMinAmountWan',
    label: '最低日均成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  }
];

var M3M_DEFAULTS = {
  m3mEnableMinAmountFilter: true,
  m3mMinAmountWan: 3000
};

var M3M_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: '3M1(MA5/MA10≥MA20) 或 3M2(MA5/MA10≥MA30)；边沿突破金叉/死叉(须在该路径关键K之后)或关键K任一High'
  },
  {
    key: 'm3mEnableMinAmountFilter',
    label: '成交额门',
    type: 'switch'
  },
  {
    key: 'm3mMinAmountWan',
    label: '最低日均成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  }
];

var MBM_DEFAULTS = {
  mbmEnableMinAmountFilter: true,
  mbmMinAmountWan: 3000,
  mbmEnableMin30BreakFilter: false,
  mbmRequireDayAlign: false,
  mbmRequireWeekAlign: false,
  mbmRequireMonthAlign: false
};

var MBM_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: '3M1/3M2多头；边沿或开盘突破均线MAX=max(MA5,10,20,30)'
  },
  {
    key: 'mbmEnableMinAmountFilter',
    label: '成交额门',
    type: 'switch'
  },
  {
    key: 'mbmMinAmountWan',
    label: '最低日均成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'mbmEnableMin30BreakFilter',
    label: '30分钟破MAX门',
    hint: '额外要求30分也满足3M多头+破MAX',
    type: 'switch'
  },
  {
    key: 'mbmRequireDayAlign',
    label: '日线3M多头',
    type: 'switch'
  },
  {
    key: 'mbmRequireWeekAlign',
    label: '周线3M多头',
    type: 'switch'
  },
  {
    key: 'mbmRequireMonthAlign',
    label: '月线3M多头',
    type: 'switch'
  }
];

var MBBM_DEFAULTS = {
  mbbmEnableMinAmountFilter: true,
  mbbmMinAmountWan: 3000
};

var MBBM_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: '日线3M空头且close>max(MA5,MA10)；30分3M多头+边沿/开盘破MAX'
  },
  {
    key: 'mbbmEnableMinAmountFilter',
    label: '成交额门',
    type: 'switch'
  },
  {
    key: 'mbbmMinAmountWan',
    label: '最低日均成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  }
];

var M4M_DEFAULTS = {
  m4mEnableMinAmountFilter: true,
  m4mMinAmountWan: 3000
};

var M4M_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: 'MA5≥MA10≥MA20≥MA30；收阳；末K close>前一根Low'
  },
  {
    key: 'm4mEnableMinAmountFilter',
    label: '成交额门',
    type: 'switch'
  },
  {
    key: 'm4mMinAmountWan',
    label: '最低日均成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  }
];

var NRF_TIERS = [];

function strategyKind(strategyId) {
  var id = String(strategyId || '');
  if (/^mabearbreakma/.test(id)) return 'mbbm';
  if (/^mabreakma/.test(id)) return 'mbm';
  if (/^mabull4m/.test(id)) return 'm4m';
  if (/^mabull3m/.test(id)) return 'm3m';
  if (/^trendretesthigh/.test(id)) return 'high';
  return 'low';
}

function isHighStrategy(strategyId) {
  return strategyKind(strategyId) === 'high';
}

function normalizeStrategyId(strategyId) {
  if (!strategyId) return strategyNav.TIER_SHORT;
  if (strategyNav.isActiveStrategy(strategyId)) {
    return strategyId;
  }
  return strategyNav.TIER_SHORT;
}

function storageKey(strategyId) {
  return STORAGE_PREFIX + normalizeStrategyId(strategyId);
}

function clone(obj) {
  return JSON.parse(JSON.stringify(obj));
}

function getDefaults(strategyId) {
  var kind = strategyKind(strategyId);
  if (kind === 'mbbm') return clone(MBBM_DEFAULTS);
  if (kind === 'mbm') return clone(MBM_DEFAULTS);
  if (kind === 'm4m') return clone(M4M_DEFAULTS);
  if (kind === 'm3m') return clone(M3M_DEFAULTS);
  if (kind === 'high') return clone(HIGH_DEFAULTS);
  return clone(LOW_DEFAULTS);
}

function normalize(strategyId, raw) {
  var d = getDefaults(strategyId);
  if (!raw || typeof raw !== 'object') {
    return d;
  }
  var kind = strategyKind(strategyId);
  if (kind === 'mbbm') {
    if (raw.mbbmEnableMinAmountFilter != null) {
      d.mbbmEnableMinAmountFilter = !!raw.mbbmEnableMinAmountFilter;
    }
    if (raw.mbbmMinAmountWan != null && raw.mbbmMinAmountWan >= 0) {
      d.mbbmMinAmountWan = raw.mbbmMinAmountWan;
    }
  } else if (kind === 'mbm') {
    if (raw.mbmEnableMinAmountFilter != null) {
      d.mbmEnableMinAmountFilter = !!raw.mbmEnableMinAmountFilter;
    }
    if (raw.mbmMinAmountWan != null && raw.mbmMinAmountWan >= 0) {
      d.mbmMinAmountWan = raw.mbmMinAmountWan;
    }
    if (raw.mbmEnableMin30BreakFilter != null) {
      d.mbmEnableMin30BreakFilter = !!raw.mbmEnableMin30BreakFilter;
    }
    if (raw.mbmRequireDayAlign != null) {
      d.mbmRequireDayAlign = !!raw.mbmRequireDayAlign;
    }
    if (raw.mbmRequireWeekAlign != null) {
      d.mbmRequireWeekAlign = !!raw.mbmRequireWeekAlign;
    }
    if (raw.mbmRequireMonthAlign != null) {
      d.mbmRequireMonthAlign = !!raw.mbmRequireMonthAlign;
    }
  } else if (kind === 'm4m') {
    if (raw.m4mEnableMinAmountFilter != null) {
      d.m4mEnableMinAmountFilter = !!raw.m4mEnableMinAmountFilter;
    }
    if (raw.m4mMinAmountWan != null && raw.m4mMinAmountWan >= 0) {
      d.m4mMinAmountWan = raw.m4mMinAmountWan;
    }
  } else if (kind === 'm3m') {
    if (raw.m3mEnableMinAmountFilter != null) {
      d.m3mEnableMinAmountFilter = !!raw.m3mEnableMinAmountFilter;
    }
    if (raw.m3mMinAmountWan != null && raw.m3mMinAmountWan >= 0) {
      d.m3mMinAmountWan = raw.m3mMinAmountWan;
    }
  } else if (kind === 'high') {
    if (raw.trhEnableMinAmountFilter != null) {
      d.trhEnableMinAmountFilter = !!raw.trhEnableMinAmountFilter;
    }
    if (raw.trhMinAmountWan != null && raw.trhMinAmountWan >= 0) {
      d.trhMinAmountWan = raw.trhMinAmountWan;
    }
  } else {
    if (raw.trlEnableMinAmountFilter != null) {
      d.trlEnableMinAmountFilter = !!raw.trlEnableMinAmountFilter;
    }
    if (raw.trlMinAmountWan != null && raw.trlMinAmountWan >= 0) {
      d.trlMinAmountWan = raw.trlMinAmountWan;
    }
  }
  return d;
}

function load(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  try {
    var raw = wx.getStorageSync(storageKey(strategyId));
    return normalize(strategyId, raw);
  } catch (e) {
    return getDefaults(strategyId);
  }
}

function save(strategyId, params) {
  strategyId = normalizeStrategyId(strategyId);
  var saved = normalize(strategyId, params);
  wx.setStorageSync(storageKey(strategyId), saved);
  return saved;
}

function reset(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  var d = getDefaults(strategyId);
  wx.setStorageSync(storageKey(strategyId), d);
  return d;
}

function getSchema(strategyId) {
  var kind = strategyKind(strategyId);
  if (kind === 'mbbm') return MBBM_SCHEMA.slice();
  if (kind === 'mbm') return MBM_SCHEMA.slice();
  if (kind === 'm4m') return M4M_SCHEMA.slice();
  if (kind === 'm3m') return M3M_SCHEMA.slice();
  if (kind === 'high') return HIGH_SCHEMA.slice();
  return LOW_SCHEMA.slice();
}

function getPanelSchema(strategyId) {
  return getSchema(strategyId);
}

function hasCustomParams() {
  return true;
}

function resolveApiStrategyId(strategyId) {
  var kind = strategyKind(strategyId);
  if (kind === 'mbbm') return 'mabearbreakma';
  if (kind === 'mbm') return 'mabreakma';
  if (kind === 'm4m') return 'mabull4m';
  if (kind === 'm3m') return 'mabull3m';
  if (kind === 'high') return 'trendretesthigh';
  return 'trendretestlow';
}

function tierToApi(frontendId) {
  return strategyNav.tierForStrategyId(normalizeStrategyId(frontendId));
}

function toApiParams(strategyId) {
  var p = load(strategyId);
  var tier = tierToApi(strategyId);
  var kind = strategyKind(strategyId);
  if (kind === 'mbbm') {
    return {
      mbbmEnableMinAmountFilter: p.mbbmEnableMinAmountFilter !== false,
      mbbmMinAmountWan: p.mbbmMinAmountWan != null ? p.mbbmMinAmountWan : 3000
    };
  }
  if (kind === 'mbm') {
    return {
      mbmTier: tier,
      mbmEnableMinAmountFilter: p.mbmEnableMinAmountFilter !== false,
      mbmMinAmountWan: p.mbmMinAmountWan != null ? p.mbmMinAmountWan : 3000,
      mbmEnableMin30BreakFilter: !!p.mbmEnableMin30BreakFilter,
      mbmRequireDayAlign: !!p.mbmRequireDayAlign,
      mbmRequireWeekAlign: !!p.mbmRequireWeekAlign,
      mbmRequireMonthAlign: !!p.mbmRequireMonthAlign
    };
  }
  if (kind === 'm4m') {
    return {
      m4mTier: tier,
      m4mEnableMinAmountFilter: p.m4mEnableMinAmountFilter !== false,
      m4mMinAmountWan: p.m4mMinAmountWan != null ? p.m4mMinAmountWan : 3000
    };
  }
  if (kind === 'm3m') {
    return {
      m3mTier: tier,
      m3mEnableMinAmountFilter: p.m3mEnableMinAmountFilter !== false,
      m3mMinAmountWan: p.m3mMinAmountWan != null ? p.m3mMinAmountWan : 3000
    };
  }
  if (kind === 'high') {
    return {
      trhTier: tier,
      trhEnableMinAmountFilter: p.trhEnableMinAmountFilter !== false,
      trhMinAmountWan: p.trhMinAmountWan != null ? p.trhMinAmountWan : 3000
    };
  }
  return {
    trlTier: tier,
    trlEnableMinAmountFilter: p.trlEnableMinAmountFilter !== false,
    trlMinAmountWan: p.trlMinAmountWan != null ? p.trlMinAmountWan : 3000
  };
}

function toApiParamsFromForm(apiStrategyId, params) {
  return toApiParams(apiStrategyId);
}

function isCustomized(strategyId) {
  var p = load(strategyId);
  var d = getDefaults(strategyId);
  return JSON.stringify(p) !== JSON.stringify(d);
}

function formatSummary(strategyId) {
  var p = load(strategyId);
  var tier = tierToApi(strategyId);
  var tierLabel = tier === 'min30' ? '30分' : (tier === 'min60' ? '60分' : (tier === 'week' ? '周' : (tier === 'month' ? '月' : '日')));
  var kind = strategyKind(strategyId);
  if (kind === 'mbbm') {
    var mbbmParts = ['日3M空头', '30分破MAX'];
    if (p.mbbmEnableMinAmountFilter !== false) {
      mbbmParts.push((p.mbbmMinAmountWan != null ? p.mbbmMinAmountWan : 3000) + '万');
    }
    return mbbmParts.join(' · ');
  }
  if (kind === 'mbm') {
    var mbmParts = [tierLabel + '线', '3M多头', '破MAX'];
    if (p.mbmEnableMin30BreakFilter) mbmParts.push('30分门');
    if (p.mbmRequireDayAlign) mbmParts.push('日多头');
    if (p.mbmRequireWeekAlign) mbmParts.push('周多头');
    if (p.mbmRequireMonthAlign) mbmParts.push('月多头');
    if (p.mbmEnableMinAmountFilter !== false) {
      mbmParts.push((p.mbmMinAmountWan != null ? p.mbmMinAmountWan : 3000) + '万');
    }
    return mbmParts.join(' · ');
  }
  if (kind === 'm4m') {
    var m4mParts = [tierLabel + '线', '4M多头', '收阳', 'close>前Low'];
    if (p.m4mEnableMinAmountFilter !== false) {
      m4mParts.push((p.m4mMinAmountWan != null ? p.m4mMinAmountWan : 3000) + '万');
    }
    return m4mParts.join(' · ');
  }
  if (kind === 'm3m') {
    var m3mParts = [tierLabel + '线', '3M1≥MA20或3M2≥MA30', '边沿破金叉/死叉/关键K'];
    if (p.m3mEnableMinAmountFilter !== false) {
      m3mParts.push((p.m3mMinAmountWan != null ? p.m3mMinAmountWan : 3000) + '万');
    }
    return m3mParts.join(' · ');
  }
  if (kind === 'high') {
    var hParts = [tierLabel + '线', '日K末阳', '回踩bandLow', '末K破High'];
    if (p.trhEnableMinAmountFilter !== false) {
      hParts.push((p.trhMinAmountWan != null ? p.trhMinAmountWan : 3000) + '万');
    }
    return hParts.join(' · ');
  }
  var parts = [tierLabel + '线', '日K末阳', '回踩bandLow', 'close≤High', '末K收阳'];
  if (p.trlEnableMinAmountFilter !== false) {
    parts.push((p.trlMinAmountWan != null ? p.trlMinAmountWan : 3000) + '万');
  }
  return parts.join(' · ');
}

function emptyResultHint() {
  return '暂无命中：可放宽成交额或切换日/周/月档后重跑';
}

function defaultChartPeriod(strategyId) {
  var tier = tierToApi(strategyId);
  if (tier === 'min30') return 'min30';
  if (tier === 'min60') return 'min60';
  if (tier === 'week') return 'week';
  if (tier === 'month') return 'month';
  return 'day';
}

function chartPrimaryPeriod(strategyId) {
  return defaultChartPeriod(strategyId);
}

function getTierListFor() {
  return [];
}

function loadTierFormFor(strategyId) {
  return load(strategyId);
}

function saveTierFormFor(strategyId, tier, form) {
  return save(strategyId, form);
}

function resetTierFor(strategyId) {
  return reset(strategyId);
}

function getTierSchema() {
  return [];
}

function getActiveTierParams(strategyId) {
  return load(strategyId);
}

function twccbApiPeriodOverrides() {
  return {};
}

function toSubApiParams() {
  return {};
}

function getBundleSubStrategies() {
  return [];
}

function isCascadeTierStrategy() {
  return false;
}

function isMacdGcWaveHighStrategy() {
  return false;
}

function isMacdGcWaveHighLiftStrategy() {
  return false;
}

function nrfTierTitle() {
  return '';
}

function retestPrimaryPeriod() {
  return 'day';
}

function gc2PrimaryPeriod() {
  return 'day';
}

function dc2PrimaryPeriod() {
  return 'day';
}

function cascadePrimaryPeriod() {
  return 'day';
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
  twccbApiPeriodOverrides: twccbApiPeriodOverrides,
  toSubApiParams: toSubApiParams,
  getBundleSubStrategies: getBundleSubStrategies,
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
  chartPrimaryPeriod: chartPrimaryPeriod,
  isCascadeTierStrategy: isCascadeTierStrategy,
  isMacdGcWaveHighStrategy: isMacdGcWaveHighStrategy,
  isMacdGcWaveHighLiftStrategy: isMacdGcWaveHighLiftStrategy,
  defaultChartPeriod: defaultChartPeriod,
  toApiParamsFromForm: toApiParamsFromForm
};
