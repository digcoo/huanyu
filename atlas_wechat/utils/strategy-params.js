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

var RELAY2Y_DEFAULTS = {
  tr2yEnableMinAmountFilter: true,
  tr2yMinAmountWan: 3000
};

var RELAY2Y_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: DAY_LAST_BAR_YANG_HINT + 'MACD>0；未回踩 bandLow；仅末二阳(倒数第三阴)；bandLow<末K close≤bandHigh'
  },
  {
    key: 'tr2yEnableMinAmountFilter',
    label: '成交额门',
    type: 'switch'
  },
  {
    key: 'tr2yMinAmountWan',
    label: '最低日均成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  }
];

var RELAYPH_DEFAULTS = {
  trphEnableMinAmountFilter: true,
  trphMinAmountWan: 3000
};

var RELAYPH_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: DAY_LAST_BAR_YANG_HINT + 'MACD>0；未回踩 bandLow；末 K 收阳；边沿突破前一根 K 的 high；末 K close≤bandHigh'
  },
  {
    key: 'trphEnableMinAmountFilter',
    label: '成交额门',
    type: 'switch'
  },
  {
    key: 'trphMinAmountWan',
    label: '最低日均成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  }
];

var RELAYBH_DEFAULTS = {
  trbhEnableMinAmountFilter: true,
  trbhMinAmountWan: 3000
};

var RELAYBH_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: DAY_LAST_BAR_YANG_HINT + 'MACD>0；未回踩 bandLow；末 K 首次边沿突破末波段 bandHigh'
  },
  {
    key: 'trbhEnableMinAmountFilter',
    label: '成交额门',
    type: 'switch'
  },
  {
    key: 'trbhMinAmountWan',
    label: '最低日均成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  }
];

var BBH_DEFAULTS = {
  bbhEnableMinAmountFilter: true,
  bbhMinAmountWan: 3000
};

var BBH_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: DAY_LAST_BAR_YANG_HINT + 'MACD<0；父级MACD>0(日→周/周→月/月→年)；末 K 首次边沿突破末波段 bandHigh'
  },
  {
    key: 'bbhEnableMinAmountFilter',
    label: '成交额门',
    type: 'switch'
  },
  {
    key: 'bbhMinAmountWan',
    label: '最低日均成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  }
];

var BP2H_DEFAULTS = {
  bp2hEnableMinAmountFilter: true,
  bp2hMinAmountWan: 3000
};

var BP2H_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: DAY_LAST_BAR_YANG_HINT + 'MACD<0；父级MACD>0(日→周/周→月/月→年)；末 K 边沿突破末前 2 根 K 的 max(high)'
  },
  {
    key: 'bp2hEnableMinAmountFilter',
    label: '成交额门',
    type: 'switch'
  },
  {
    key: 'bp2hMinAmountWan',
    label: '最低日均成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  }
];

var MAL_DEFAULTS = {
  malEnableMinAmountFilter: true,
  malMinAmountWan: 3000
};

var MAL_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: '均线MAX=max(MA5~30)；MA10>MA20>MA30；末 K close>均线MAX；边沿突破(前一根close≤该根MAX) 或 开盘突破(末K open≤该根MAX)'
  },
  {
    key: 'malEnableMinAmountFilter',
    label: '成交额门',
    type: 'switch'
  },
  {
    key: 'malMinAmountWan',
    label: '最低日均成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  }
];

var MBR_DEFAULTS = {
  mbrEnableMinAmountFilter: true,
  mbrMinAmountWan: 3000
};

var MBR_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: '均线MAX=max(MA5~30)；MA10<MA20<MA30；末 K close>均线MAX；边沿突破(前一根close≤该根MAX) 或 开盘突破(末K open≤该根MAX)'
  },
  {
    key: 'mbrEnableMinAmountFilter',
    label: '成交额门',
    type: 'switch'
  },
  {
    key: 'mbrMinAmountWan',
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
  if (/^mabearbreak/.test(id)) return 'mbr';
  if (/^maalignlift/.test(id)) return 'mal';
  if (/^bottomprev2high/.test(id)) return 'bp2h';
  if (/^bottombandhigh/.test(id)) return 'bbh';
  if (/^trendrelaybandhigh/.test(id)) return 'relaybh';
  if (/^trendrelayprevhigh/.test(id)) return 'relayph';
  if (/^trendrelay2yang/.test(id)) return 'relay2y';
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
  if (kind === 'mbr') return clone(MBR_DEFAULTS);
  if (kind === 'mal') return clone(MAL_DEFAULTS);
  if (kind === 'bp2h') return clone(BP2H_DEFAULTS);
  if (kind === 'bbh') return clone(BBH_DEFAULTS);
  if (kind === 'relaybh') return clone(RELAYBH_DEFAULTS);
  if (kind === 'relayph') return clone(RELAYPH_DEFAULTS);
  if (kind === 'relay2y') return clone(RELAY2Y_DEFAULTS);
  if (kind === 'high') return clone(HIGH_DEFAULTS);
  return clone(LOW_DEFAULTS);
}

function normalize(strategyId, raw) {
  var d = getDefaults(strategyId);
  if (!raw || typeof raw !== 'object') {
    return d;
  }
  var kind = strategyKind(strategyId);
  if (kind === 'mbr') {
    if (raw.mbrEnableMinAmountFilter != null) {
      d.mbrEnableMinAmountFilter = !!raw.mbrEnableMinAmountFilter;
    }
    if (raw.mbrMinAmountWan != null && raw.mbrMinAmountWan >= 0) {
      d.mbrMinAmountWan = raw.mbrMinAmountWan;
    }
  } else if (kind === 'mal') {
    if (raw.malEnableMinAmountFilter != null) {
      d.malEnableMinAmountFilter = !!raw.malEnableMinAmountFilter;
    }
    if (raw.malMinAmountWan != null && raw.malMinAmountWan >= 0) {
      d.malMinAmountWan = raw.malMinAmountWan;
    }
  } else if (kind === 'bp2h') {
    if (raw.bp2hEnableMinAmountFilter != null) {
      d.bp2hEnableMinAmountFilter = !!raw.bp2hEnableMinAmountFilter;
    }
    if (raw.bp2hMinAmountWan != null && raw.bp2hMinAmountWan >= 0) {
      d.bp2hMinAmountWan = raw.bp2hMinAmountWan;
    }
  } else if (kind === 'bbh') {
    if (raw.bbhEnableMinAmountFilter != null) {
      d.bbhEnableMinAmountFilter = !!raw.bbhEnableMinAmountFilter;
    }
    if (raw.bbhMinAmountWan != null && raw.bbhMinAmountWan >= 0) {
      d.bbhMinAmountWan = raw.bbhMinAmountWan;
    }
  } else if (kind === 'relaybh') {
    if (raw.trbhEnableMinAmountFilter != null) {
      d.trbhEnableMinAmountFilter = !!raw.trbhEnableMinAmountFilter;
    }
    if (raw.trbhMinAmountWan != null && raw.trbhMinAmountWan >= 0) {
      d.trbhMinAmountWan = raw.trbhMinAmountWan;
    }
  } else if (kind === 'relayph') {
    if (raw.trphEnableMinAmountFilter != null) {
      d.trphEnableMinAmountFilter = !!raw.trphEnableMinAmountFilter;
    }
    if (raw.trphMinAmountWan != null && raw.trphMinAmountWan >= 0) {
      d.trphMinAmountWan = raw.trphMinAmountWan;
    }
  } else if (kind === 'relay2y') {
    if (raw.tr2yEnableMinAmountFilter != null) {
      d.tr2yEnableMinAmountFilter = !!raw.tr2yEnableMinAmountFilter;
    }
    if (raw.tr2yMinAmountWan != null && raw.tr2yMinAmountWan >= 0) {
      d.tr2yMinAmountWan = raw.tr2yMinAmountWan;
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
  if (kind === 'mbr') return MBR_SCHEMA.slice();
  if (kind === 'mal') return MAL_SCHEMA.slice();
  if (kind === 'bp2h') return BP2H_SCHEMA.slice();
  if (kind === 'bbh') return BBH_SCHEMA.slice();
  if (kind === 'relaybh') return RELAYBH_SCHEMA.slice();
  if (kind === 'relayph') return RELAYPH_SCHEMA.slice();
  if (kind === 'relay2y') return RELAY2Y_SCHEMA.slice();
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
  if (kind === 'mbr') return 'mabearbreak';
  if (kind === 'mal') return 'maalignlift';
  if (kind === 'bp2h') return 'bottomprev2high';
  if (kind === 'bbh') return 'bottombandhigh';
  if (kind === 'relaybh') return 'trendrelaybandhigh';
  if (kind === 'relayph') return 'trendrelayprevhigh';
  if (kind === 'relay2y') return 'trendrelay2yang';
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
  if (kind === 'mbr') {
    return {
      mbrTier: tier,
      mbrEnableMinAmountFilter: p.mbrEnableMinAmountFilter !== false,
      mbrMinAmountWan: p.mbrMinAmountWan != null ? p.mbrMinAmountWan : 3000
    };
  }
  if (kind === 'mal') {
    return {
      malTier: tier,
      malEnableMinAmountFilter: p.malEnableMinAmountFilter !== false,
      malMinAmountWan: p.malMinAmountWan != null ? p.malMinAmountWan : 3000
    };
  }
  if (kind === 'bp2h') {
    return {
      bp2hTier: tier,
      bp2hEnableMinAmountFilter: p.bp2hEnableMinAmountFilter !== false,
      bp2hMinAmountWan: p.bp2hMinAmountWan != null ? p.bp2hMinAmountWan : 3000
    };
  }
  if (kind === 'bbh') {
    return {
      bbhTier: tier,
      bbhEnableMinAmountFilter: p.bbhEnableMinAmountFilter !== false,
      bbhMinAmountWan: p.bbhMinAmountWan != null ? p.bbhMinAmountWan : 3000
    };
  }
  if (kind === 'relaybh') {
    return {
      trbhTier: tier,
      trbhEnableMinAmountFilter: p.trbhEnableMinAmountFilter !== false,
      trbhMinAmountWan: p.trbhMinAmountWan != null ? p.trbhMinAmountWan : 3000
    };
  }
  if (kind === 'relayph') {
    return {
      trphTier: tier,
      trphEnableMinAmountFilter: p.trphEnableMinAmountFilter !== false,
      trphMinAmountWan: p.trphMinAmountWan != null ? p.trphMinAmountWan : 3000
    };
  }
  if (kind === 'relay2y') {
    return {
      tr2yTier: tier,
      tr2yEnableMinAmountFilter: p.tr2yEnableMinAmountFilter !== false,
      tr2yMinAmountWan: p.tr2yMinAmountWan != null ? p.tr2yMinAmountWan : 3000
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
  var tierLabel = tier === 'week' ? '周' : (tier === 'month' ? '月' : '日');
  var kind = strategyKind(strategyId);
  if (kind === 'mbr') {
    var mbrParts = [tierLabel + '线', 'MA10<MA20<MA30', '末K>均线MAX', '边沿或开盘突破'];
    if (p.mbrEnableMinAmountFilter !== false) {
      mbrParts.push((p.mbrMinAmountWan != null ? p.mbrMinAmountWan : 3000) + '万');
    }
    return mbrParts.join(' · ');
  }
  if (kind === 'mal') {
    var malParts = [tierLabel + '线', 'MA10>MA20>MA30', '末K>均线MAX', '边沿或开盘突破'];
    if (p.malEnableMinAmountFilter !== false) {
      malParts.push((p.malMinAmountWan != null ? p.malMinAmountWan : 3000) + '万');
    }
    return malParts.join(' · ');
  }
  if (kind === 'bp2h') {
    var p2Parts = [tierLabel + '线', '日K末阳', 'MACD<0', '父级MACD>0', '末K破前2K max(high)'];
    if (p.bp2hEnableMinAmountFilter !== false) {
      p2Parts.push((p.bp2hMinAmountWan != null ? p.bp2hMinAmountWan : 3000) + '万');
    }
    return p2Parts.join(' · ');
  }
  if (kind === 'bbh') {
    var bbhParts = [tierLabel + '线', '日K末阳', 'MACD<0', '父级MACD>0', '末K首次破bandHigh'];
    if (p.bbhEnableMinAmountFilter !== false) {
      bbhParts.push((p.bbhMinAmountWan != null ? p.bbhMinAmountWan : 3000) + '万');
    }
    return bbhParts.join(' · ');
  }
  if (kind === 'relaybh') {
    var bhParts = [tierLabel + '线', '日K末阳', '未回踩Low', '末K首次破bandHigh'];
    if (p.trbhEnableMinAmountFilter !== false) {
      bhParts.push((p.trbhMinAmountWan != null ? p.trbhMinAmountWan : 3000) + '万');
    }
    return bhParts.join(' · ');
  }
  if (kind === 'relayph') {
    var phParts = [tierLabel + '线', '日K末阳', '未回踩Low', '末K收阳', '破前K high', 'close≤High'];
    if (p.trphEnableMinAmountFilter !== false) {
      phParts.push((p.trphMinAmountWan != null ? p.trphMinAmountWan : 3000) + '万');
    }
    return phParts.join(' · ');
  }
  if (kind === 'relay2y') {
    var rParts = [tierLabel + '线', '日K末阳', '未回踩Low', '仅末二阳', 'close≤High'];
    if (p.tr2yEnableMinAmountFilter !== false) {
      rParts.push((p.tr2yMinAmountWan != null ? p.tr2yMinAmountWan : 3000) + '万');
    }
    return rParts.join(' · ');
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
