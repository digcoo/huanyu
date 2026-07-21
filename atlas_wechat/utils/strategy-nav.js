/**
 * 首页策略导航
 */
var FAMILY_ULTRA = 'ultra';
var FAMILY_MACD_GC_WH = 'macdgcwh';
var FAMILY_MACD_GC_WHU = 'macdgcwhu';

var TIER_SHORT = 'short';
var TIER_MEDIUM = 'medium';
var TIER_LONG = 'long';
var TIER_ULTRA_BUCKET = 'bucket';
var TIER_MIN60_WAVE_CC = 'min60wavecc';
var TIER_DAY_WAVE_CC = 'daywavecc';
var TIER_WEEK_WAVE_CC = 'weekwavecc';
var TIER_MONTH_WAVE_CC = 'monthwavecc';

var ULTRA_SUB_TABS = [
  { id: TIER_MIN60_WAVE_CC, name: '小时凹凸', icon: '⬡' },
  { id: TIER_DAY_WAVE_CC, name: '日凹凸', icon: '◆' },
  { id: TIER_WEEK_WAVE_CC, name: '周凹凸', icon: '◎' },
  { id: TIER_MONTH_WAVE_CC, name: '月凹凸', icon: '○' }
];

var STRATEGY_FAMILIES = [
  { id: FAMILY_ULTRA, name: '超短线', icon: '⚡' },
  { id: FAMILY_MACD_GC_WH, name: 'MACD金叉波段', icon: '⬆' },
  { id: FAMILY_MACD_GC_WHU, name: 'MACD金叉上移', icon: '⇧' }
];

var WAVE_TIER_TABS = [
  { id: TIER_SHORT, name: '短线', icon: '◎' },
  { id: TIER_MEDIUM, name: '中线', icon: '◉' },
  { id: TIER_LONG, name: '长线', icon: '○' }
];

var STRATEGY_TITLES = {
  ultra: '超短线',
  min60wavecc: '小时凹凸突破',
  daywavecc: '日凹凸突破',
  weekwavecc: '周凹凸突破',
  monthwavecc: '月凹凸突破',
  macdgcwhShort: 'MACD金叉波段·短线',
  macdgcwhMedium: 'MACD金叉波段·中线',
  macdgcwhLong: 'MACD金叉波段·长线',
  macdgcwh: 'MACD金叉波段·短线',
  macdgcwhu: 'MACD金叉上移'
};

var ARCHIVED_STRATEGIES = {
  nrf: true,
  cascade: true,
  macedge: true,
  wavebreak: true,
  wavetier: true,
  wavelow: true,
  wavetierlow: true,
  waveconvex: true,
  waveconcave: true,
  waveconvexday: true,
  waveconcaveday: true,
  cascadewaveconvex: true,
  cascadewaveconcave: true,
  cascadewaveconvexday: true,
  cascadewaveconcaveday: true,
  cascadewaveShort: true,
  cascadewaveMedium: true,
  cascadewaveLong: true,
  wavebandconvex: true,
  wavebandconcave: true,
  wavebandLong: true,
  waveband: true,
  wavebandShort: true,
  wavebandMedium: true,
  waveconcavetierShort: true,
  waveconcavetierMedium: true,
  waveconcavetierLong: true,
  waveconvextierShort: true,
  waveconvextierMedium: true,
  waveconvextierLong: true,
  cascadedipShort: true,
  cascadedipMedium: true,
  cascadedipLong: true,
  ladder: true,
  ultraLow: true,
  pillar: true,
  trend: true,
  medium: true,
  long: true,
  waveperiodgate: true,
  waveperiodgateShort: true,
  waveperiodgateMedium: true,
  waveperiodgateLong: true,
  convexlifttier: true,
  convexlifttierShort: true,
  convexlifttierMedium: true,
  convexlifttierLong: true,
  bodybar: true,
  bodybarShort: true,
  bodybarMedium: true,
  bodybarLong: true,
  macdcrosstier: true,
  macdcrosstierShort: true,
  macdcrosstierMedium: true,
  macdcrosstierLong: true,
  macddcb: true,
  macddcbShort: true,
  macddcbMedium: true,
  macddcbLong: true,
  ultragc: true,
  ultra: true,
  macdgc: true,
  macdgcShort: true,
  macdgcMedium: true,
  macdgcLong: true,
  macdgcwhr: true,
  macdgcwhrShort: true,
  macdgcwhrMedium: true,
  macdgcwhrLong: true,
  waveccbreak: true,
  waveccbreakShort: true,
  waveccbreakMedium: true,
  waveccbreakLong: true,
  daymin60: true,
  weekmin60: true,
  dayweek: true,
  daymonth: true,
  macdgcwhuShort: true,
  macdgcwhuMedium: true,
  macdgcwhuLong: true
};

var ACTIVE_STRATEGY_IDS = {
  min60wavecc: true,
  daywavecc: true,
  weekwavecc: true,
  monthwavecc: true,
  macdgcwhShort: true,
  macdgcwhMedium: true,
  macdgcwhLong: true,
  macdgcwhu: true
};

function isLegacyWavebandStrategy(strategyId) {
  return strategyId === 'wavebandShort' || strategyId === 'wavebandMedium'
    || strategyId === 'waveband' || strategyId === 'wavebandconvex'
    || strategyId === 'wavebandconcave' || strategyId === 'waveconcavetierShort'
    || strategyId === 'waveconcavetierMedium' || strategyId === 'waveconcavetierLong'
    || strategyId === 'waveconvextierShort' || strategyId === 'waveconvextierMedium'
    || strategyId === 'waveconvextierLong' || strategyId === 'waveperiodgate';
}

function normalizeTier(tier) {
  if (tier === TIER_MEDIUM || tier === TIER_LONG) return tier;
  return TIER_SHORT;
}

function macdGcWhTierStrategyId(tier) {
  tier = normalizeTier(tier);
  if (tier === TIER_MEDIUM) return 'macdgcwhMedium';
  if (tier === TIER_LONG) return 'macdgcwhLong';
  return 'macdgcwhShort';
}

function strategyIdFor(family, tier) {
  family = family || FAMILY_ULTRA;
  if (family === FAMILY_ULTRA) {
    if (tier === TIER_DAY_WAVE_CC) return 'daywavecc';
    if (tier === TIER_WEEK_WAVE_CC) return 'weekwavecc';
    if (tier === TIER_MONTH_WAVE_CC) return 'monthwavecc';
    if (tier === TIER_MIN60_WAVE_CC) return 'min60wavecc';
    return 'min60wavecc';
  }
  if (family === FAMILY_MACD_GC_WHU) {
    return 'macdgcwhu';
  }
  tier = normalizeTier(tier);
  if (family === FAMILY_MACD_GC_WH) {
    return macdGcWhTierStrategyId(tier);
  }
  return 'ultra';
}

function parseStrategyId(strategyId) {
  strategyId = strategyId || 'ultra';
  if (strategyId === 'macdgcwhShort' || strategyId === 'macdgcwh') {
    return { family: FAMILY_MACD_GC_WH, tier: TIER_SHORT, strategyId: 'macdgcwhShort' };
  }
  if (strategyId === 'macdgcwhMedium') {
    return { family: FAMILY_MACD_GC_WH, tier: TIER_MEDIUM, strategyId: 'macdgcwhMedium' };
  }
  if (strategyId === 'macdgcwhLong') {
    return { family: FAMILY_MACD_GC_WH, tier: TIER_LONG, strategyId: 'macdgcwhLong' };
  }
  if (strategyId === 'macdgcwhu' || strategyId === 'macdgcwhuShort'
      || strategyId === 'macdgcwhuMedium' || strategyId === 'macdgcwhuLong') {
    return { family: FAMILY_MACD_GC_WHU, tier: TIER_SHORT, strategyId: 'macdgcwhu' };
  }
  if (strategyId === 'min60wavecc') {
    return { family: FAMILY_ULTRA, tier: TIER_MIN60_WAVE_CC, strategyId: 'min60wavecc' };
  }
  if (strategyId === 'daywavecc') {
    return { family: FAMILY_ULTRA, tier: TIER_DAY_WAVE_CC, strategyId: 'daywavecc' };
  }
  if (strategyId === 'weekwavecc') {
    return { family: FAMILY_ULTRA, tier: TIER_WEEK_WAVE_CC, strategyId: 'weekwavecc' };
  }
  if (strategyId === 'monthwavecc') {
    return { family: FAMILY_ULTRA, tier: TIER_MONTH_WAVE_CC, strategyId: 'monthwavecc' };
  }
  return { family: FAMILY_ULTRA, tier: TIER_MIN60_WAVE_CC, strategyId: 'min60wavecc' };
}

function showTierRow(family) {
  return family === FAMILY_ULTRA || family === FAMILY_MACD_GC_WH;
}

function tierTabsForFamily(family) {
  if (family === FAMILY_ULTRA) {
    return ULTRA_SUB_TABS;
  }
  if (family === FAMILY_MACD_GC_WH) {
    return WAVE_TIER_TABS;
  }
  return [];
}

function strategyTitleFor(strategyId) {
  return STRATEGY_TITLES[strategyId] || '策略';
}

function migrateSavedStrategy(strategyId) {
  var id = strategyId || 'min60wavecc';
  if (id === 'cascadewaveconvex' || id === 'cascadewaveconcave'
      || id === 'cascadewaveconvexday' || id === 'cascadewaveconcaveday'
      || id === 'cascadewaveShort' || id === 'cascadewaveMedium' || id === 'cascadewaveLong') {
    return { strategy: 'min60wavecc' };
  }
  if (id === 'macdgcwh') {
    return { strategy: 'macdgcwhShort' };
  }
  if (id === 'macdgcwhu' || id === 'macdgcwhuShort'
      || id === 'macdgcwhuMedium' || id === 'macdgcwhuLong') {
    return { strategy: 'macdgcwhu' };
  }
  if (id === 'ultragc' || id === 'ultra') {
    return { strategy: 'min60wavecc' };
  }
  if (isLegacyWavebandStrategy(id)
      || id === 'waveperiodgateShort' || id === 'waveperiodgateMedium' || id === 'waveperiodgateLong'
      || id === 'convexlifttier' || id === 'convexlifttierShort'
      || id === 'convexlifttierMedium' || id === 'convexlifttierLong'
      || id === 'bodybar' || id === 'bodybarShort'
      || id === 'bodybarMedium' || id === 'bodybarLong') {
    return { strategy: 'min60wavecc' };
  }
  if (ARCHIVED_STRATEGIES[id]) {
    return { strategy: 'min60wavecc' };
  }
  if (ACTIVE_STRATEGY_IDS[id]) {
    return { strategy: id };
  }
  return { strategy: 'min60wavecc' };
}

function isActiveStrategy(strategyId) {
  return !!ACTIVE_STRATEGY_IDS[strategyId];
}

module.exports = {
  FAMILY_ULTRA: FAMILY_ULTRA,
  FAMILY_MACD_GC_WH: FAMILY_MACD_GC_WH,
  FAMILY_MACD_GC_WHU: FAMILY_MACD_GC_WHU,
  STRATEGY_FAMILIES: STRATEGY_FAMILIES,
  WAVE_TIER_TABS: WAVE_TIER_TABS,
  TIER_ULTRA_BUCKET: TIER_ULTRA_BUCKET,
  TIER_MIN60_WAVE_CC: TIER_MIN60_WAVE_CC,
  TIER_DAY_WAVE_CC: TIER_DAY_WAVE_CC,
  TIER_WEEK_WAVE_CC: TIER_WEEK_WAVE_CC,
  TIER_MONTH_WAVE_CC: TIER_MONTH_WAVE_CC,
  ULTRA_SUB_TABS: ULTRA_SUB_TABS,
  strategyIdFor: strategyIdFor,
  parseStrategyId: parseStrategyId,
  showTierRow: showTierRow,
  tierTabsForFamily: tierTabsForFamily,
  strategyTitleFor: strategyTitleFor,
  migrateSavedStrategy: migrateSavedStrategy,
  isActiveStrategy: isActiveStrategy
};
