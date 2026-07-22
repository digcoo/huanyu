/**
 * 首页策略导航
 */
var FAMILY_ULTRA = 'ultra';

var TIER_MIN60_WAVE_CC = 'min60wavecc';
var TIER_DAY_WAVE_CC = 'daywavecc';

var ULTRA_SUB_TABS = [
  { id: TIER_MIN60_WAVE_CC, name: '分时凹凸', icon: '⬡' },
  { id: TIER_DAY_WAVE_CC, name: '日凹凸', icon: '◆' }
];

var STRATEGY_FAMILIES = [
  { id: FAMILY_ULTRA, name: '超短线', icon: '⚡' }
];

var STRATEGY_TITLES = {
  ultra: '超短线',
  min60wavecc: '分时凹凸突破',
  daywavecc: '日凹凸突破'
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
  macdgcwh: true,
  macdgcwhShort: true,
  macdgcwhMedium: true,
  macdgcwhLong: true,
  macdgcwhr: true,
  macdgcwhrShort: true,
  macdgcwhrMedium: true,
  macdgcwhrLong: true,
  macdgcwhu: true,
  macdgcwhuShort: true,
  macdgcwhuMedium: true,
  macdgcwhuLong: true,
  waveccbreak: true,
  waveccbreakShort: true,
  waveccbreakMedium: true,
  waveccbreakLong: true,
  daymin60: true,
  weekmin60: true,
  dayweek: true,
  daymonth: true,
  weekwavecc: true,
  monthwavecc: true
};

var ACTIVE_STRATEGY_IDS = {
  min60wavecc: true,
  daywavecc: true
};

function isLegacyWavebandStrategy(strategyId) {
  return strategyId === 'wavebandShort' || strategyId === 'wavebandMedium'
    || strategyId === 'waveband' || strategyId === 'wavebandconvex'
    || strategyId === 'wavebandconcave' || strategyId === 'waveconcavetierShort'
    || strategyId === 'waveconcavetierMedium' || strategyId === 'waveconcavetierLong'
    || strategyId === 'waveconvextierShort' || strategyId === 'waveconvextierMedium'
    || strategyId === 'waveconvextierLong' || strategyId === 'waveperiodgate';
}

function strategyIdFor(family, tier) {
  family = family || FAMILY_ULTRA;
  if (tier === TIER_DAY_WAVE_CC) {
    return 'daywavecc';
  }
  return 'min60wavecc';
}

function parseStrategyId(strategyId) {
  strategyId = strategyId || 'min60wavecc';
  if (strategyId === 'daywavecc') {
    return { family: FAMILY_ULTRA, tier: TIER_DAY_WAVE_CC, strategyId: 'daywavecc' };
  }
  return { family: FAMILY_ULTRA, tier: TIER_MIN60_WAVE_CC, strategyId: 'min60wavecc' };
}

function showTierRow(family) {
  return family === FAMILY_ULTRA;
}

function tierTabsForFamily(family) {
  if (family === FAMILY_ULTRA) {
    return ULTRA_SUB_TABS;
  }
  return [];
}

function strategyTitleFor(strategyId) {
  return STRATEGY_TITLES[strategyId] || '策略';
}

function migrateSavedStrategy(strategyId) {
  var id = strategyId || 'min60wavecc';
  if (id === 'daywavecc') {
    return { strategy: 'daywavecc' };
  }
  if (ARCHIVED_STRATEGIES[id] || isLegacyWavebandStrategy(id)
      || id === 'waveperiodgateShort' || id === 'waveperiodgateMedium' || id === 'waveperiodgateLong'
      || id === 'convexlifttier' || id === 'convexlifttierShort'
      || id === 'convexlifttierMedium' || id === 'convexlifttierLong'
      || id === 'bodybar' || id === 'bodybarShort'
      || id === 'bodybarMedium' || id === 'bodybarLong') {
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
  STRATEGY_FAMILIES: STRATEGY_FAMILIES,
  TIER_MIN60_WAVE_CC: TIER_MIN60_WAVE_CC,
  TIER_DAY_WAVE_CC: TIER_DAY_WAVE_CC,
  ULTRA_SUB_TABS: ULTRA_SUB_TABS,
  strategyIdFor: strategyIdFor,
  parseStrategyId: parseStrategyId,
  showTierRow: showTierRow,
  tierTabsForFamily: tierTabsForFamily,
  strategyTitleFor: strategyTitleFor,
  migrateSavedStrategy: migrateSavedStrategy,
  isActiveStrategy: isActiveStrategy
};
