/**
 * 首页策略导航
 */
var FAMILY_ULTRA = 'ultra';
var FAMILY_MACD_GC = 'macdgc';
var FAMILY_MACD_GC_WH = 'macdgcwh';
var FAMILY_MACD_GC_WHR = 'macdgcwhr';
var FAMILY_MACD_GC_WHU = 'macdgcwhu';
var FAMILY_MACD_DC_BREAKOUT = 'macddcb';

var TIER_SHORT = 'short';
var TIER_MEDIUM = 'medium';
var TIER_LONG = 'long';
var TIER_ULTRA_BUCKET = 'bucket';
var TIER_ULTRA_GC = 'gc';

var STRATEGY_FAMILIES = [
  { id: FAMILY_ULTRA, name: '超短线', icon: '⚡' },
  { id: FAMILY_MACD_GC, name: 'MACD金叉', icon: '✦' },
  { id: FAMILY_MACD_GC_WH, name: 'MACD金叉波段', icon: '⬆' },
  { id: FAMILY_MACD_GC_WHR, name: 'MACD金叉回踩', icon: '↘' },
  { id: FAMILY_MACD_GC_WHU, name: 'MACD金叉上移', icon: '⇧' },
  { id: FAMILY_MACD_DC_BREAKOUT, name: 'MACD死叉突破', icon: '✕' }
];

var WAVE_TIER_TABS = [
  { id: TIER_SHORT, name: '短线', icon: '◎' },
  { id: TIER_MEDIUM, name: '中线', icon: '◉' },
  { id: TIER_LONG, name: '长线', icon: '○' }
];

var ULTRA_SUB_TABS = [
  { id: TIER_ULTRA_BUCKET, name: '跨日桶', icon: '▣' },
  { id: TIER_ULTRA_GC, name: '金叉K', icon: '✦' }
];

var STRATEGY_TITLES = {
  ultra: '超短线·跨日桶',
  ultragc: '超短线·金叉K突破',
  macdgcShort: 'MACD金叉·短线',
  macdgcMedium: 'MACD金叉·中线',
  macdgcLong: 'MACD金叉·长线',
  macdgc: 'MACD金叉·短线',
  macdgcwhShort: 'MACD金叉波段·短线',
  macdgcwhMedium: 'MACD金叉波段·中线',
  macdgcwhLong: 'MACD金叉波段·长线',
  macdgcwh: 'MACD金叉波段·短线',
  macdgcwhrShort: 'MACD金叉回踩·短线',
  macdgcwhrMedium: 'MACD金叉回踩·中线',
  macdgcwhrLong: 'MACD金叉回踩·长线',
  macdgcwhr: 'MACD金叉回踩·短线',
  macdgcwhuShort: 'MACD金叉上移·短线',
  macdgcwhuMedium: 'MACD金叉上移·中线',
  macdgcwhuLong: 'MACD金叉上移·长线',
  macdgcwhu: 'MACD金叉上移·短线',
  macddcbShort: 'MACD死叉突破·短线',
  macddcbMedium: 'MACD死叉突破·中线',
  macddcbLong: 'MACD死叉突破·长线',
  macddcb: 'MACD死叉突破·短线'
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
  macdcrosstierLong: true
};

var ACTIVE_STRATEGY_IDS = {
  ultra: true,
  ultragc: true,
  macdgcShort: true,
  macdgcMedium: true,
  macdgcLong: true,
  macdgcwhShort: true,
  macdgcwhMedium: true,
  macdgcwhLong: true,
  macdgcwhrShort: true,
  macdgcwhrMedium: true,
  macdgcwhrLong: true,
  macdgcwhuShort: true,
  macdgcwhuMedium: true,
  macdgcwhuLong: true,
  macddcbShort: true,
  macddcbMedium: true,
  macddcbLong: true
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

function macdGcTierStrategyId(tier) {
  tier = normalizeTier(tier);
  if (tier === TIER_MEDIUM) return 'macdgcMedium';
  if (tier === TIER_LONG) return 'macdgcLong';
  return 'macdgcShort';
}

function macdGcWhTierStrategyId(tier) {
  tier = normalizeTier(tier);
  if (tier === TIER_MEDIUM) return 'macdgcwhMedium';
  if (tier === TIER_LONG) return 'macdgcwhLong';
  return 'macdgcwhShort';
}

function macdGcWhrTierStrategyId(tier) {
  tier = normalizeTier(tier);
  if (tier === TIER_MEDIUM) return 'macdgcwhrMedium';
  if (tier === TIER_LONG) return 'macdgcwhrLong';
  return 'macdgcwhrShort';
}

function macdGcWhuTierStrategyId(tier) {
  tier = normalizeTier(tier);
  if (tier === TIER_MEDIUM) return 'macdgcwhuMedium';
  if (tier === TIER_LONG) return 'macdgcwhuLong';
  return 'macdgcwhuShort';
}

function normalizeUltraTier(tier) {
  if (tier === TIER_ULTRA_GC) return TIER_ULTRA_GC;
  return TIER_ULTRA_BUCKET;
}

function macdDcBreakoutTierStrategyId(tier) {
  tier = normalizeTier(tier);
  if (tier === TIER_MEDIUM) return 'macddcbMedium';
  if (tier === TIER_LONG) return 'macddcbLong';
  return 'macddcbShort';
}

function strategyIdFor(family, tier) {
  family = family || FAMILY_ULTRA;
  if (family === FAMILY_ULTRA) {
    return normalizeUltraTier(tier) === TIER_ULTRA_GC ? 'ultragc' : 'ultra';
  }
  tier = normalizeTier(tier);
  if (family === FAMILY_MACD_GC) {
    return macdGcTierStrategyId(tier);
  }
  if (family === FAMILY_MACD_GC_WH) {
    return macdGcWhTierStrategyId(tier);
  }
  if (family === FAMILY_MACD_GC_WHR) {
    return macdGcWhrTierStrategyId(tier);
  }
  if (family === FAMILY_MACD_GC_WHU) {
    return macdGcWhuTierStrategyId(tier);
  }
  if (family === FAMILY_MACD_DC_BREAKOUT) {
    return macdDcBreakoutTierStrategyId(tier);
  }
  return 'ultra';
}

function parseStrategyId(strategyId) {
  strategyId = strategyId || 'ultra';
  if (strategyId === 'macdgcShort' || strategyId === 'macdgc') {
    return { family: FAMILY_MACD_GC, tier: TIER_SHORT, strategyId: 'macdgcShort' };
  }
  if (strategyId === 'macdgcMedium') {
    return { family: FAMILY_MACD_GC, tier: TIER_MEDIUM, strategyId: 'macdgcMedium' };
  }
  if (strategyId === 'macdgcLong') {
    return { family: FAMILY_MACD_GC, tier: TIER_LONG, strategyId: 'macdgcLong' };
  }
  if (strategyId === 'macdgcwhShort' || strategyId === 'macdgcwh') {
    return { family: FAMILY_MACD_GC_WH, tier: TIER_SHORT, strategyId: 'macdgcwhShort' };
  }
  if (strategyId === 'macdgcwhMedium') {
    return { family: FAMILY_MACD_GC_WH, tier: TIER_MEDIUM, strategyId: 'macdgcwhMedium' };
  }
  if (strategyId === 'macdgcwhLong') {
    return { family: FAMILY_MACD_GC_WH, tier: TIER_LONG, strategyId: 'macdgcwhLong' };
  }
  if (strategyId === 'macdgcwhrShort' || strategyId === 'macdgcwhr') {
    return { family: FAMILY_MACD_GC_WHR, tier: TIER_SHORT, strategyId: 'macdgcwhrShort' };
  }
  if (strategyId === 'macdgcwhrMedium') {
    return { family: FAMILY_MACD_GC_WHR, tier: TIER_MEDIUM, strategyId: 'macdgcwhrMedium' };
  }
  if (strategyId === 'macdgcwhrLong') {
    return { family: FAMILY_MACD_GC_WHR, tier: TIER_LONG, strategyId: 'macdgcwhrLong' };
  }
  if (strategyId === 'macdgcwhuShort' || strategyId === 'macdgcwhu') {
    return { family: FAMILY_MACD_GC_WHU, tier: TIER_SHORT, strategyId: 'macdgcwhuShort' };
  }
  if (strategyId === 'macdgcwhuMedium') {
    return { family: FAMILY_MACD_GC_WHU, tier: TIER_MEDIUM, strategyId: 'macdgcwhuMedium' };
  }
  if (strategyId === 'macdgcwhuLong') {
    return { family: FAMILY_MACD_GC_WHU, tier: TIER_LONG, strategyId: 'macdgcwhuLong' };
  }
  if (strategyId === 'macddcbShort' || strategyId === 'macddcb') {
    return { family: FAMILY_MACD_DC_BREAKOUT, tier: TIER_SHORT, strategyId: 'macddcbShort' };
  }
  if (strategyId === 'macddcbMedium') {
    return { family: FAMILY_MACD_DC_BREAKOUT, tier: TIER_MEDIUM, strategyId: 'macddcbMedium' };
  }
  if (strategyId === 'macddcbLong') {
    return { family: FAMILY_MACD_DC_BREAKOUT, tier: TIER_LONG, strategyId: 'macddcbLong' };
  }
  if (strategyId === 'ultragc') {
    return { family: FAMILY_ULTRA, tier: TIER_ULTRA_GC, strategyId: 'ultragc' };
  }
  return { family: FAMILY_ULTRA, tier: TIER_ULTRA_BUCKET, strategyId: 'ultra' };
}

function showTierRow(family) {
  return family === FAMILY_ULTRA || family === FAMILY_MACD_GC || family === FAMILY_MACD_GC_WH
    || family === FAMILY_MACD_GC_WHR || family === FAMILY_MACD_GC_WHU
    || family === FAMILY_MACD_DC_BREAKOUT;
}

function tierTabsForFamily(family) {
  if (family === FAMILY_ULTRA) {
    return ULTRA_SUB_TABS;
  }
  if (family === FAMILY_MACD_GC || family === FAMILY_MACD_GC_WH
      || family === FAMILY_MACD_GC_WHR || family === FAMILY_MACD_GC_WHU
      || family === FAMILY_MACD_DC_BREAKOUT) {
    return WAVE_TIER_TABS;
  }
  return [];
}

function strategyTitleFor(strategyId) {
  return STRATEGY_TITLES[strategyId] || '策略';
}

function migrateSavedStrategy(strategyId) {
  var id = strategyId || 'ultra';
  if (id === 'cascadewaveconvex' || id === 'cascadewaveconcave'
      || id === 'cascadewaveconvexday' || id === 'cascadewaveconcaveday'
      || id === 'cascadewaveShort' || id === 'cascadewaveMedium' || id === 'cascadewaveLong') {
    return { strategy: 'ultra' };
  }
  if (id === 'macdcrosstier' || id === 'macdcrosstierShort'
      || id === 'macdcrosstierMedium' || id === 'macdcrosstierLong') {
    return { strategy: 'macdgcShort' };
  }
  if (id === 'macdgc') {
    return { strategy: 'macdgcShort' };
  }
  if (id === 'macdgcwh') {
    return { strategy: 'macdgcwhShort' };
  }
  if (id === 'macdgcwhr') {
    return { strategy: 'macdgcwhrShort' };
  }
  if (id === 'macdgcwhu') {
    return { strategy: 'macdgcwhuShort' };
  }
  if (id === 'macddcb') {
    return { strategy: 'macddcbShort' };
  }
  if (isLegacyWavebandStrategy(id)
      || id === 'waveperiodgateShort' || id === 'waveperiodgateMedium' || id === 'waveperiodgateLong'
      || id === 'convexlifttier' || id === 'convexlifttierShort'
      || id === 'convexlifttierMedium' || id === 'convexlifttierLong'
      || id === 'bodybar' || id === 'bodybarShort'
      || id === 'bodybarMedium' || id === 'bodybarLong') {
    return { strategy: 'ultra' };
  }
  if (ARCHIVED_STRATEGIES[id]) {
    return { strategy: 'ultra' };
  }
  if (ACTIVE_STRATEGY_IDS[id]) {
    return { strategy: id };
  }
  return { strategy: 'ultra' };
}

function isActiveStrategy(strategyId) {
  return !!ACTIVE_STRATEGY_IDS[strategyId];
}

module.exports = {
  FAMILY_ULTRA: FAMILY_ULTRA,
  FAMILY_MACD_GC: FAMILY_MACD_GC,
  FAMILY_MACD_GC_WH: FAMILY_MACD_GC_WH,
  FAMILY_MACD_GC_WHR: FAMILY_MACD_GC_WHR,
  FAMILY_MACD_GC_WHU: FAMILY_MACD_GC_WHU,
  FAMILY_MACD_DC_BREAKOUT: FAMILY_MACD_DC_BREAKOUT,
  STRATEGY_FAMILIES: STRATEGY_FAMILIES,
  WAVE_TIER_TABS: WAVE_TIER_TABS,
  ULTRA_SUB_TABS: ULTRA_SUB_TABS,
  TIER_ULTRA_BUCKET: TIER_ULTRA_BUCKET,
  TIER_ULTRA_GC: TIER_ULTRA_GC,
  strategyIdFor: strategyIdFor,
  parseStrategyId: parseStrategyId,
  showTierRow: showTierRow,
  tierTabsForFamily: tierTabsForFamily,
  strategyTitleFor: strategyTitleFor,
  migrateSavedStrategy: migrateSavedStrategy,
  isActiveStrategy: isActiveStrategy
};
