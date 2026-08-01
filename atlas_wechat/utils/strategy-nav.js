/**
 * 小程序策略导航
 */
var STRATEGY_TREND_RETEST_LOW = 'trendretestlow';
var STRATEGY_TREND_RETEST_HIGH = 'trendretesthigh';
var STRATEGY_TREND_RELAY_2YANG = 'trendrelay2yang';
var STRATEGY_TREND_RELAY_PREV_HIGH = 'trendrelayprevhigh';
var STRATEGY_TREND_RELAY_BAND_HIGH = 'trendrelaybandhigh';
var STRATEGY_BOTTOM_BAND_HIGH = 'bottombandhigh';
var STRATEGY_BOTTOM_PREV2_HIGH = 'bottomprev2high';
var STRATEGY_MA_ALIGN_LIFT = 'maalignlift';
var STRATEGY_MA_BEAR_BREAK = 'mabearbreak';

var LOW_TIER_SHORT = 'trendretestlowShort';
var LOW_TIER_MEDIUM = 'trendretestlowMedium';
var LOW_TIER_LONG = 'trendretestlowLong';

var HIGH_TIER_SHORT = 'trendretesthighShort';
var HIGH_TIER_MEDIUM = 'trendretesthighMedium';
var HIGH_TIER_LONG = 'trendretesthighLong';

var RELAY2Y_TIER_SHORT = 'trendrelay2yangShort';
var RELAY2Y_TIER_MEDIUM = 'trendrelay2yangMedium';
var RELAY2Y_TIER_LONG = 'trendrelay2yangLong';

var RELAYPH_TIER_SHORT = 'trendrelayprevhighShort';
var RELAYPH_TIER_MEDIUM = 'trendrelayprevhighMedium';
var RELAYPH_TIER_LONG = 'trendrelayprevhighLong';

var RELAYBH_TIER_SHORT = 'trendrelaybandhighShort';
var RELAYBH_TIER_MEDIUM = 'trendrelaybandhighMedium';
var RELAYBH_TIER_LONG = 'trendrelaybandhighLong';

var BBH_TIER_SHORT = 'bottombandhighShort';
var BBH_TIER_MEDIUM = 'bottombandhighMedium';
var BBH_TIER_LONG = 'bottombandhighLong';

var BP2H_TIER_SHORT = 'bottomprev2highShort';
var BP2H_TIER_MEDIUM = 'bottomprev2highMedium';
var BP2H_TIER_LONG = 'bottomprev2highLong';

var MAL_TIER_SHORT = 'maalignliftShort';
var MAL_TIER_MEDIUM = 'maalignliftMedium';
var MAL_TIER_LONG = 'maalignliftLong';

var MBR_TIER_SHORT = 'mabearbreakShort';
var MBR_TIER_MEDIUM = 'mabearbreakMedium';
var MBR_TIER_LONG = 'mabearbreakLong';

var STRATEGY_FAMILIES = [
  { id: STRATEGY_TREND_RETEST_LOW, name: '回踩破Low', icon: '↩' },
  { id: STRATEGY_TREND_RETEST_HIGH, name: '回踩破High', icon: '⤴' },
  { id: STRATEGY_TREND_RELAY_2YANG, name: '中转二阳', icon: '☀' },
  { id: STRATEGY_TREND_RELAY_PREV_HIGH, name: '中转破前High', icon: '⬆' },
  { id: STRATEGY_TREND_RELAY_BAND_HIGH, name: '中转破末High', icon: '🚀' },
  { id: STRATEGY_BOTTOM_BAND_HIGH, name: '底部波段突破', icon: '📍' },
  { id: STRATEGY_BOTTOM_PREV2_HIGH, name: '底部High突破', icon: '📈' },
  { id: STRATEGY_MA_ALIGN_LIFT, name: '均线多头突破', icon: '📊' },
  { id: STRATEGY_MA_BEAR_BREAK, name: '均线空头突破', icon: '📉' }
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
  trendrelay2yang: [
    { id: RELAY2Y_TIER_SHORT, name: '日线' },
    { id: RELAY2Y_TIER_MEDIUM, name: '周线' },
    { id: RELAY2Y_TIER_LONG, name: '月线' }
  ],
  trendrelayprevhigh: [
    { id: RELAYPH_TIER_SHORT, name: '日线' },
    { id: RELAYPH_TIER_MEDIUM, name: '周线' },
    { id: RELAYPH_TIER_LONG, name: '月线' }
  ],
  trendrelaybandhigh: [
    { id: RELAYBH_TIER_SHORT, name: '日线' },
    { id: RELAYBH_TIER_MEDIUM, name: '周线' },
    { id: RELAYBH_TIER_LONG, name: '月线' }
  ],
  bottombandhigh: [
    { id: BBH_TIER_SHORT, name: '日线' },
    { id: BBH_TIER_MEDIUM, name: '周线' },
    { id: BBH_TIER_LONG, name: '月线' }
  ],
  bottomprev2high: [
    { id: BP2H_TIER_SHORT, name: '日线' },
    { id: BP2H_TIER_MEDIUM, name: '周线' },
    { id: BP2H_TIER_LONG, name: '月线' }
  ],
  maalignlift: [
    { id: MAL_TIER_SHORT, name: '日线' },
    { id: MAL_TIER_MEDIUM, name: '周线' },
    { id: MAL_TIER_LONG, name: '月线' }
  ],
  mabearbreak: [
    { id: MBR_TIER_SHORT, name: '日线' },
    { id: MBR_TIER_MEDIUM, name: '周线' },
    { id: MBR_TIER_LONG, name: '月线' }
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
  trendrelay2yang: '趋势中转二阳',
  trendrelay2yangShort: '趋势中转二阳 · 日',
  trendrelay2yangMedium: '趋势中转二阳 · 周',
  trendrelay2yangLong: '趋势中转二阳 · 月',
  trendrelayprevhigh: '趋势中转破前High',
  trendrelayprevhighShort: '趋势中转破前High · 日',
  trendrelayprevhighMedium: '趋势中转破前High · 周',
  trendrelayprevhighLong: '趋势中转破前High · 月',
  trendrelaybandhigh: '趋势中转破末High',
  trendrelaybandhighShort: '趋势中转破末High · 日',
  trendrelaybandhighMedium: '趋势中转破末High · 周',
  trendrelaybandhighLong: '趋势中转破末High · 月',
  bottombandhigh: '底部波段突破',
  bottombandhighShort: '底部波段突破 · 日',
  bottombandhighMedium: '底部波段突破 · 周',
  bottombandhighLong: '底部波段突破 · 月',
  bottomprev2high: '底部High突破',
  bottomprev2highShort: '底部High突破 · 日',
  bottomprev2highMedium: '底部High突破 · 周',
  bottomprev2highLong: '底部High突破 · 月',
  maalignlift: '均线多头突破',
  maalignliftShort: '均线多头突破 · 日',
  maalignliftMedium: '均线多头突破 · 周',
  maalignliftLong: '均线多头突破 · 月',
  mabearbreak: '均线空头突破',
  mabearbreakShort: '均线空头突破 · 日',
  mabearbreakMedium: '均线空头突破 · 周',
  mabearbreakLong: '均线空头突破 · 月'
};

var ACTIVE_STRATEGY_IDS = {
  trendretestlowShort: true,
  trendretestlowMedium: true,
  trendretestlowLong: true,
  trendretesthighShort: true,
  trendretesthighMedium: true,
  trendretesthighLong: true,
  trendrelay2yangShort: true,
  trendrelay2yangMedium: true,
  trendrelay2yangLong: true,
  trendrelayprevhighShort: true,
  trendrelayprevhighMedium: true,
  trendrelayprevhighLong: true,
  trendrelaybandhighShort: true,
  trendrelaybandhighMedium: true,
  trendrelaybandhighLong: true,
  bottombandhighShort: true,
  bottombandhighMedium: true,
  bottombandhighLong: true,
  bottomprev2highShort: true,
  bottomprev2highMedium: true,
  bottomprev2highLong: true,
  maalignliftShort: true,
  maalignliftMedium: true,
  maalignliftLong: true,
  mabearbreakShort: true,
  mabearbreakMedium: true,
  mabearbreakLong: true
};

var TIER_BY_STRATEGY = {
  trendretestlowShort: 'day',
  trendretestlowMedium: 'week',
  trendretestlowLong: 'month',
  trendretesthighShort: 'day',
  trendretesthighMedium: 'week',
  trendretesthighLong: 'month',
  trendrelay2yangShort: 'day',
  trendrelay2yangMedium: 'week',
  trendrelay2yangLong: 'month',
  trendrelayprevhighShort: 'day',
  trendrelayprevhighMedium: 'week',
  trendrelayprevhighLong: 'month',
  trendrelaybandhighShort: 'day',
  trendrelaybandhighMedium: 'week',
  trendrelaybandhighLong: 'month',
  bottombandhighShort: 'day',
  bottombandhighMedium: 'week',
  bottombandhighLong: 'month',
  bottomprev2highShort: 'day',
  bottomprev2highMedium: 'week',
  bottomprev2highLong: 'month',
  maalignliftShort: 'day',
  maalignliftMedium: 'week',
  maalignliftLong: 'month',
  mabearbreakShort: 'day',
  mabearbreakMedium: 'week',
  mabearbreakLong: 'month'
};

function familyForStrategyId(strategyId) {
  var id = String(strategyId || '');
  if (id.indexOf('mabearbreak') === 0) {
    return STRATEGY_MA_BEAR_BREAK;
  }
  if (id.indexOf('maalignlift') === 0) {
    return STRATEGY_MA_ALIGN_LIFT;
  }
  if (id.indexOf('bottomprev2high') === 0) {
    return STRATEGY_BOTTOM_PREV2_HIGH;
  }
  if (id.indexOf('bottombandhigh') === 0) {
    return STRATEGY_BOTTOM_BAND_HIGH;
  }
  if (id.indexOf('trendrelaybandhigh') === 0) {
    return STRATEGY_TREND_RELAY_BAND_HIGH;
  }
  if (id.indexOf('trendrelayprevhigh') === 0) {
    return STRATEGY_TREND_RELAY_PREV_HIGH;
  }
  if (id.indexOf('trendrelay2yang') === 0) {
    return STRATEGY_TREND_RELAY_2YANG;
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
  if (family === STRATEGY_BOTTOM_PREV2_HIGH) {
    return BP2H_TIER_SHORT;
  }
  if (family === STRATEGY_MA_ALIGN_LIFT) {
    return MAL_TIER_SHORT;
  }
  if (family === STRATEGY_MA_BEAR_BREAK) {
    return MBR_TIER_SHORT;
  }
  if (family === STRATEGY_BOTTOM_BAND_HIGH) {
    return BBH_TIER_SHORT;
  }
  if (family === STRATEGY_TREND_RELAY_BAND_HIGH) {
    return RELAYBH_TIER_SHORT;
  }
  if (family === STRATEGY_TREND_RELAY_PREV_HIGH) {
    return RELAYPH_TIER_SHORT;
  }
  if (family === STRATEGY_TREND_RELAY_2YANG) {
    return RELAY2Y_TIER_SHORT;
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
  STRATEGY_TREND_RELAY_2YANG: STRATEGY_TREND_RELAY_2YANG,
  STRATEGY_TREND_RELAY_PREV_HIGH: STRATEGY_TREND_RELAY_PREV_HIGH,
  STRATEGY_TREND_RELAY_BAND_HIGH: STRATEGY_TREND_RELAY_BAND_HIGH,
  STRATEGY_BOTTOM_BAND_HIGH: STRATEGY_BOTTOM_BAND_HIGH,
  STRATEGY_BOTTOM_PREV2_HIGH: STRATEGY_BOTTOM_PREV2_HIGH,
  STRATEGY_MA_ALIGN_LIFT: STRATEGY_MA_ALIGN_LIFT,
  STRATEGY_MA_BEAR_BREAK: STRATEGY_MA_BEAR_BREAK,
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
