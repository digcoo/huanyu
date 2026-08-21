/**
 * 小程序策略导航
 */
var STRATEGY_TREND_RETEST_LOW = 'trendretestlow';
var STRATEGY_TREND_RETEST_HIGH = 'trendretesthigh';
var STRATEGY_MA_BULL_3M = 'mabull3m';
var STRATEGY_MA_BREAK_MA = 'mabreakma';
var STRATEGY_MA_GOLD_BREAK = 'magoldbreak';
var STRATEGY_MA_DEATH_BREAK = 'madeathbreak';
var STRATEGY_PREV_BAND_HIGH = 'prevbandhigh';

var LOW_TIER_SHORT = 'trendretestlowShort';
var LOW_TIER_MEDIUM = 'trendretestlowMedium';
var LOW_TIER_LONG = 'trendretestlowLong';

var HIGH_TIER_SHORT = 'trendretesthighShort';
var HIGH_TIER_MEDIUM = 'trendretesthighMedium';
var HIGH_TIER_LONG = 'trendretesthighLong';

var M3M_TIER_FLASH = 'mabull3mFlash';
var M3M_TIER_SHORT = 'mabull3mShort';
var M3M_TIER_MEDIUM = 'mabull3mMedium';
var M3M_TIER_LONG = 'mabull3mLong';

var MBM_TIER_FLASH = 'mabreakmaFlash';
var MBM_TIER_SHORT = 'mabreakmaShort';
var MBM_TIER_MEDIUM = 'mabreakmaMedium';
var MBM_TIER_LONG = 'mabreakmaLong';

var MGB_TIER_FLASH = 'magoldbreakFlash';
var MGB_TIER_SHORT = 'magoldbreakShort';
var MGB_TIER_MEDIUM = 'magoldbreakMedium';
var MGB_TIER_LONG = 'magoldbreakLong';

var MDB_TIER_FLASH = 'madeathbreakFlash';
var MDB_TIER_SHORT = 'madeathbreakShort';
var MDB_TIER_MEDIUM = 'madeathbreakMedium';
var MDB_TIER_LONG = 'madeathbreakLong';

var PBH_TIER_FLASH = 'prevbandhighFlash';
var PBH_TIER_SHORT = 'prevbandhighShort';
var PBH_TIER_MEDIUM = 'prevbandhighMedium';
var PBH_TIER_LONG = 'prevbandhighLong';

var STRATEGY_FAMILIES = [
  { id: STRATEGY_TREND_RETEST_LOW, name: '回踩破Low', icon: '↩' },
  { id: STRATEGY_TREND_RETEST_HIGH, name: '回踩破High', icon: '⤴' },
  { id: STRATEGY_MA_BULL_3M, name: 'MA多头3M', icon: '☰' },
  { id: STRATEGY_MA_BREAK_MA, name: 'MA多头破MA', icon: '↗' },
  { id: STRATEGY_MA_GOLD_BREAK, name: 'MA金叉点', icon: '✦' },
  { id: STRATEGY_MA_DEATH_BREAK, name: 'MA死叉点', icon: '✧' },
  { id: STRATEGY_PREV_BAND_HIGH, name: '前波段High', icon: '⌃' }
];

var TIER_TABS_BY_FAMILY = {
  trendretestlow: [
    { id: LOW_TIER_SHORT, name: '日线' },
    { id: LOW_TIER_MEDIUM, name: '周线' },
    { id: LOW_TIER_LONG, name: '月线' }
  ],
  trendretesthigh: [
    { id: HIGH_TIER_SHORT, name: '日线' },
    { id: HIGH_TIER_MEDIUM, name: '周线' },
    { id: HIGH_TIER_LONG, name: '月线' }
  ],
  mabull3m: [
    { id: M3M_TIER_FLASH, name: '30分' },
    { id: M3M_TIER_SHORT, name: '日线' },
    { id: M3M_TIER_MEDIUM, name: '周线' },
    { id: M3M_TIER_LONG, name: '月线' }
  ],
  mabreakma: [
    { id: MBM_TIER_FLASH, name: '30分' },
    { id: MBM_TIER_SHORT, name: '日线' },
    { id: MBM_TIER_MEDIUM, name: '周线' },
    { id: MBM_TIER_LONG, name: '月线' }
  ],
  magoldbreak: [
    { id: MGB_TIER_FLASH, name: '30分' },
    { id: MGB_TIER_SHORT, name: '日线' },
    { id: MGB_TIER_MEDIUM, name: '周线' },
    { id: MGB_TIER_LONG, name: '月线' }
  ],
  madeathbreak: [
    { id: MDB_TIER_FLASH, name: '30分' },
    { id: MDB_TIER_SHORT, name: '日线' },
    { id: MDB_TIER_MEDIUM, name: '周线' },
    { id: MDB_TIER_LONG, name: '月线' }
  ],
  prevbandhigh: [
    { id: PBH_TIER_FLASH, name: '30分' },
    { id: PBH_TIER_SHORT, name: '日线' },
    { id: PBH_TIER_MEDIUM, name: '周线' },
    { id: PBH_TIER_LONG, name: '月线' }
  ]
};

var STRATEGY_TITLES = {
  trendretestlow: '趋势回踩破Low',
  trendretestlowShort: '趋势回踩破Low · 日',
  trendretestlowMedium: '趋势回踩破Low · 周',
  trendretestlowLong: '趋势回踩破Low · 月',
  trendretesthigh: '趋势回踩破High',
  trendretesthighShort: '趋势回踩破High · 日',
  trendretesthighMedium: '趋势回踩破High · 周',
  trendretesthighLong: '趋势回踩破High · 月',
  mabull3m: 'MA多头3M突破',
  mabull3mFlash: 'MA多头3M突破 · 30分',
  mabull3mShort: 'MA多头3M突破 · 日',
  mabull3mMedium: 'MA多头3M突破 · 周',
  mabull3mLong: 'MA多头3M突破 · 月',
  mabreakma: 'MA多头破MA',
  mabreakmaFlash: 'MA多头破MA · 30分',
  mabreakmaShort: 'MA多头破MA · 日',
  mabreakmaMedium: 'MA多头破MA · 周',
  mabreakmaLong: 'MA多头破MA · 月',
  magoldbreak: 'MA金叉点突破',
  magoldbreakFlash: 'MA金叉点突破 · 30分',
  magoldbreakShort: 'MA金叉点突破 · 日',
  magoldbreakMedium: 'MA金叉点突破 · 周',
  magoldbreakLong: 'MA金叉点突破 · 月',
  madeathbreak: 'MA死叉点突破',
  madeathbreakFlash: 'MA死叉点突破 · 30分',
  madeathbreakShort: 'MA死叉点突破 · 日',
  madeathbreakMedium: 'MA死叉点突破 · 周',
  madeathbreakLong: 'MA死叉点突破 · 月',
  prevbandhigh: '突破前波段High',
  prevbandhighFlash: '突破前波段High · 30分',
  prevbandhighShort: '突破前波段High · 日',
  prevbandhighMedium: '突破前波段High · 周',
  prevbandhighLong: '突破前波段High · 月'
};

var ACTIVE_STRATEGY_IDS = {
  trendretestlowShort: true,
  trendretestlowMedium: true,
  trendretestlowLong: true,
  trendretesthighShort: true,
  trendretesthighMedium: true,
  trendretesthighLong: true,
  mabull3mFlash: true,
  mabull3mShort: true,
  mabull3mMedium: true,
  mabull3mLong: true,
  mabreakmaFlash: true,
  mabreakmaShort: true,
  mabreakmaMedium: true,
  mabreakmaLong: true,
  magoldbreakFlash: true,
  magoldbreakShort: true,
  magoldbreakMedium: true,
  magoldbreakLong: true,
  madeathbreakFlash: true,
  madeathbreakShort: true,
  madeathbreakMedium: true,
  madeathbreakLong: true,
  prevbandhighFlash: true,
  prevbandhighShort: true,
  prevbandhighMedium: true,
  prevbandhighLong: true
};

var TIER_BY_STRATEGY = {
  trendretestlowShort: 'day',
  trendretestlowMedium: 'week',
  trendretestlowLong: 'month',
  trendretesthighShort: 'day',
  trendretesthighMedium: 'week',
  trendretesthighLong: 'month',
  mabull3mFlash: 'min30',
  mabull3mShort: 'day',
  mabull3mMedium: 'week',
  mabull3mLong: 'month',
  mabreakmaFlash: 'min30',
  mabreakmaShort: 'day',
  mabreakmaMedium: 'week',
  mabreakmaLong: 'month',
  magoldbreakFlash: 'min30',
  magoldbreakShort: 'day',
  magoldbreakMedium: 'week',
  magoldbreakLong: 'month',
  madeathbreakFlash: 'min30',
  madeathbreakShort: 'day',
  madeathbreakMedium: 'week',
  madeathbreakLong: 'month',
  prevbandhighFlash: 'min30',
  prevbandhighShort: 'day',
  prevbandhighMedium: 'week',
  prevbandhighLong: 'month'
};

var M32_MIGRATION = {
  mabull3m2: M3M_TIER_FLASH,
  mabull3m2Ultra: M3M_TIER_FLASH,
  mabull3m2Short: M3M_TIER_SHORT,
  mabull3m2Medium: M3M_TIER_MEDIUM,
  mabull3m2Long: M3M_TIER_LONG,
  mabull3mUltra: M3M_TIER_FLASH
};

function familyForStrategyId(strategyId) {
  var id = String(strategyId || '');
  if (id.indexOf('prevbandhigh') === 0) {
    return STRATEGY_PREV_BAND_HIGH;
  }
  if (id.indexOf('madeathbreak') === 0) {
    return STRATEGY_MA_DEATH_BREAK;
  }
  if (id.indexOf('magoldbreak') === 0) {
    return STRATEGY_MA_GOLD_BREAK;
  }
  if (id.indexOf('mabreakma') === 0) {
    return STRATEGY_MA_BREAK_MA;
  }
  if (id.indexOf('mabull3m') === 0) {
    return STRATEGY_MA_BULL_3M;
  }
  if (id.indexOf('trendretesthigh') === 0) {
    return STRATEGY_TREND_RETEST_HIGH;
  }
  return STRATEGY_TREND_RETEST_LOW;
}

function strategyIdFor(family, tier) {
  if (tier && ACTIVE_STRATEGY_IDS[tier]) {
    return tier;
  }
  if (family === STRATEGY_PREV_BAND_HIGH) {
    return PBH_TIER_FLASH;
  }
  if (family === STRATEGY_MA_DEATH_BREAK) {
    return MDB_TIER_FLASH;
  }
  if (family === STRATEGY_MA_GOLD_BREAK) {
    return MGB_TIER_FLASH;
  }
  if (family === STRATEGY_MA_BREAK_MA) {
    return MBM_TIER_FLASH;
  }
  if (family === STRATEGY_MA_BULL_3M) {
    return M3M_TIER_FLASH;
  }
  if (family === STRATEGY_TREND_RETEST_HIGH) {
    return HIGH_TIER_SHORT;
  }
  return LOW_TIER_SHORT;
}

function parseStrategyId(strategyId) {
  var migrated = migrateSavedStrategy(strategyId);
  var id = migrated.strategy;
  return {
    family: familyForStrategyId(id),
    tier: id,
    strategyId: id
  };
}

function showTierRow(family) {
  return !!TIER_TABS_BY_FAMILY[family];
}

function tierTabsForFamily(family) {
  return TIER_TABS_BY_FAMILY[family] || TIER_TABS_BY_FAMILY.trendretestlow;
}

function strategyTitleFor(strategyId) {
  return STRATEGY_TITLES[strategyId] || STRATEGY_TITLES.trendretestlow;
}

function migrateSavedStrategy(strategyId) {
  var id = strategyId || LOW_TIER_SHORT;
  if (M32_MIGRATION[id]) {
    return { strategy: M32_MIGRATION[id] };
  }
  if (id.indexOf('bottombandhigh') === 0
      || id.indexOf('bottomprev2high') === 0
      || id.indexOf('maalignlift') === 0
      || id.indexOf('macrossbreak') === 0
      || id.indexOf('mabearbreakma') === 0
      || id.indexOf('mabull4m') === 0
      || id.indexOf('concavebreak') === 0) {
    return { strategy: LOW_TIER_SHORT };
  }
  if (ACTIVE_STRATEGY_IDS[id]) {
    return { strategy: id };
  }
  return { strategy: LOW_TIER_SHORT };
}

function isActiveStrategy(strategyId) {
  return !!ACTIVE_STRATEGY_IDS[strategyId];
}

function tierForStrategyId(strategyId) {
  return TIER_BY_STRATEGY[strategyId] || 'day';
}

module.exports = {
  STRATEGY_TREND_RETEST_LOW: STRATEGY_TREND_RETEST_LOW,
  STRATEGY_TREND_RETEST_HIGH: STRATEGY_TREND_RETEST_HIGH,
  STRATEGY_MA_BULL_3M: STRATEGY_MA_BULL_3M,
  STRATEGY_MA_BREAK_MA: STRATEGY_MA_BREAK_MA,
  STRATEGY_MA_GOLD_BREAK: STRATEGY_MA_GOLD_BREAK,
  STRATEGY_MA_DEATH_BREAK: STRATEGY_MA_DEATH_BREAK,
  STRATEGY_PREV_BAND_HIGH: STRATEGY_PREV_BAND_HIGH,
  TIER_SHORT: LOW_TIER_SHORT,
  TIER_MEDIUM: LOW_TIER_MEDIUM,
  TIER_LONG: LOW_TIER_LONG,
  STRATEGY_FAMILIES: STRATEGY_FAMILIES,
  strategyIdFor: strategyIdFor,
  parseStrategyId: parseStrategyId,
  showTierRow: showTierRow,
  tierTabsForFamily: tierTabsForFamily,
  strategyTitleFor: strategyTitleFor,
  migrateSavedStrategy: migrateSavedStrategy,
  isActiveStrategy: isActiveStrategy,
  tierForStrategyId: tierForStrategyId,
  familyForStrategyId: familyForStrategyId
};
