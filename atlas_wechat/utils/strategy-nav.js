/**
 * 小程序策略导航
 */
var STRATEGY_MA_GOLD_BREAK = 'magoldbreak';
var STRATEGY_MA_DEATH_BREAK = 'madeathbreak';
var STRATEGY_MA_BULL_BREAK = 'mabullbreak';
var STRATEGY_MA_BEAR_START = 'mabearstart';

var MGB_TIER_FLASH = 'magoldbreakFlash';
var MGB_TIER_SHORT = 'magoldbreakShort';
var MGB_TIER_MEDIUM = 'magoldbreakMedium';
var MGB_TIER_LONG = 'magoldbreakLong';
var MGB_TIER_QUARTER = 'magoldbreakQuarter';

var MDB_TIER_FLASH = 'madeathbreakFlash';
var MDB_TIER_SHORT = 'madeathbreakShort';
var MDB_TIER_MEDIUM = 'madeathbreakMedium';
var MDB_TIER_LONG = 'madeathbreakLong';
var MDB_TIER_QUARTER = 'madeathbreakQuarter';

var MTB_TIER_FLASH = 'mabullbreakFlash';
var MTB_TIER_SHORT = 'mabullbreakShort';
var MTB_TIER_MEDIUM = 'mabullbreakMedium';
var MTB_TIER_LONG = 'mabullbreakLong';
var MTB_TIER_QUARTER = 'mabullbreakQuarter';

var MBS_TIER_FLASH = 'mabearstartFlash';
var MBS_TIER_SHORT = 'mabearstartShort';
var MBS_TIER_MEDIUM = 'mabearstartMedium';
var MBS_TIER_LONG = 'mabearstartLong';
var MBS_TIER_QUARTER = 'mabearstartQuarter';

var DEFAULT_STRATEGY = MGB_TIER_FLASH;

var STRATEGY_FAMILIES = [
  { id: STRATEGY_MA_GOLD_BREAK, name: 'MA金叉点', icon: '✦' },
  { id: STRATEGY_MA_DEATH_BREAK, name: 'MA死叉点', icon: '✧' },
  { id: STRATEGY_MA_BULL_BREAK, name: '多头趋势', icon: '▲' },
  { id: STRATEGY_MA_BEAR_START, name: '空头启动', icon: '▼' }
];

var TIER_TABS_BY_FAMILY = {
  magoldbreak: [
    { id: MGB_TIER_FLASH, name: '30分' },
    { id: MGB_TIER_SHORT, name: '日线' },
    { id: MGB_TIER_MEDIUM, name: '周线' },
    { id: MGB_TIER_LONG, name: '月线' },
    { id: MGB_TIER_QUARTER, name: '季线' }
  ],
  madeathbreak: [
    { id: MDB_TIER_FLASH, name: '30分' },
    { id: MDB_TIER_SHORT, name: '日线' },
    { id: MDB_TIER_MEDIUM, name: '周线' },
    { id: MDB_TIER_LONG, name: '月线' },
    { id: MDB_TIER_QUARTER, name: '季线' }
  ],
  mabullbreak: [
    { id: MTB_TIER_FLASH, name: '30分' },
    { id: MTB_TIER_SHORT, name: '日线' },
    { id: MTB_TIER_MEDIUM, name: '周线' },
    { id: MTB_TIER_LONG, name: '月线' },
    { id: MTB_TIER_QUARTER, name: '季线' }
  ],
  mabearstart: [
    { id: MBS_TIER_FLASH, name: '30分' },
    { id: MBS_TIER_SHORT, name: '日线' },
    { id: MBS_TIER_MEDIUM, name: '周线' },
    { id: MBS_TIER_LONG, name: '月线' },
    { id: MBS_TIER_QUARTER, name: '季线' }
  ]
};

var STRATEGY_TITLES = {
  magoldbreak: 'MA金叉点突破',
  magoldbreakFlash: 'MA金叉点突破 · 30分',
  magoldbreakShort: 'MA金叉点突破 · 日',
  magoldbreakMedium: 'MA金叉点突破 · 周',
  magoldbreakLong: 'MA金叉点突破 · 月',
  magoldbreakQuarter: 'MA金叉点突破 · 季',
  madeathbreak: 'MA死叉点突破',
  madeathbreakFlash: 'MA死叉点突破 · 30分',
  madeathbreakShort: 'MA死叉点突破 · 日',
  madeathbreakMedium: 'MA死叉点突破 · 周',
  madeathbreakLong: 'MA死叉点突破 · 月',
  madeathbreakQuarter: 'MA死叉点突破 · 季',
  mabullbreak: '多头趋势突破',
  mabullbreakFlash: '多头趋势突破 · 30分',
  mabullbreakShort: '多头趋势突破 · 日',
  mabullbreakMedium: '多头趋势突破 · 周',
  mabullbreakLong: '多头趋势突破 · 月',
  mabullbreakQuarter: '多头趋势突破 · 季',
  mabearstart: '空头趋势启动',
  mabearstartFlash: '空头趋势启动 · 30分',
  mabearstartShort: '空头趋势启动 · 日',
  mabearstartMedium: '空头趋势启动 · 周',
  mabearstartLong: '空头趋势启动 · 月',
  mabearstartQuarter: '空头趋势启动 · 季'
};

var ACTIVE_STRATEGY_IDS = {
  magoldbreakFlash: true,
  magoldbreakShort: true,
  magoldbreakMedium: true,
  magoldbreakLong: true,
  magoldbreakQuarter: true,
  madeathbreakFlash: true,
  madeathbreakShort: true,
  madeathbreakMedium: true,
  madeathbreakLong: true,
  madeathbreakQuarter: true,
  mabullbreakFlash: true,
  mabullbreakShort: true,
  mabullbreakMedium: true,
  mabullbreakLong: true,
  mabullbreakQuarter: true,
  mabearstartFlash: true,
  mabearstartShort: true,
  mabearstartMedium: true,
  mabearstartLong: true,
  mabearstartQuarter: true
};

var TIER_BY_STRATEGY = {
  magoldbreakFlash: 'min30',
  magoldbreakShort: 'day',
  magoldbreakMedium: 'week',
  magoldbreakLong: 'month',
  magoldbreakQuarter: 'quarter',
  madeathbreakFlash: 'min30',
  madeathbreakShort: 'day',
  madeathbreakMedium: 'week',
  madeathbreakLong: 'month',
  madeathbreakQuarter: 'quarter',
  mabullbreakFlash: 'min30',
  mabullbreakShort: 'day',
  mabullbreakMedium: 'week',
  mabullbreakLong: 'month',
  mabullbreakQuarter: 'quarter',
  mabearstartFlash: 'min30',
  mabearstartShort: 'day',
  mabearstartMedium: 'week',
  mabearstartLong: 'month',
  mabearstartQuarter: 'quarter'
};

function familyForStrategyId(strategyId) {
  var id = String(strategyId || '');
  if (id.indexOf('mabearstart') === 0) {
    return STRATEGY_MA_BEAR_START;
  }
  if (id.indexOf('mabullbreak') === 0) {
    return STRATEGY_MA_BULL_BREAK;
  }
  if (id.indexOf('madeathbreak') === 0) {
    return STRATEGY_MA_DEATH_BREAK;
  }
  return STRATEGY_MA_GOLD_BREAK;
}

function strategyIdFor(family, tier) {
  if (tier && ACTIVE_STRATEGY_IDS[tier]) {
    return tier;
  }
  if (family === STRATEGY_MA_DEATH_BREAK) {
    return MDB_TIER_FLASH;
  }
  if (family === STRATEGY_MA_BULL_BREAK) {
    return MTB_TIER_FLASH;
  }
  if (family === STRATEGY_MA_BEAR_START) {
    return MBS_TIER_FLASH;
  }
  return DEFAULT_STRATEGY;
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
  return TIER_TABS_BY_FAMILY[family] || TIER_TABS_BY_FAMILY.magoldbreak;
}

function strategyTitleFor(strategyId) {
  return STRATEGY_TITLES[strategyId] || STRATEGY_TITLES.magoldbreakFlash;
}

function migrateSavedStrategy(strategyId) {
  var id = strategyId || DEFAULT_STRATEGY;
  if (id.indexOf('bottombandhigh') === 0
      || id.indexOf('bottomprev2high') === 0
      || id.indexOf('maalignlift') === 0
      || id.indexOf('macrossbreak') === 0
      || id.indexOf('mabearbreakma') === 0
      || id.indexOf('mabull4m') === 0
      || id.indexOf('concavebreak') === 0
      || id.indexOf('trendretestlow') === 0
      || id.indexOf('trendretesthigh') === 0
      || id.indexOf('prevbandhigh') === 0
      || id.indexOf('mabreakma') === 0
      || id.indexOf('mabull3m') === 0) {
    return { strategy: DEFAULT_STRATEGY };
  }
  if (ACTIVE_STRATEGY_IDS[id]) {
    return { strategy: id };
  }
  return { strategy: DEFAULT_STRATEGY };
}

function isActiveStrategy(strategyId) {
  return !!ACTIVE_STRATEGY_IDS[strategyId];
}

function tierForStrategyId(strategyId) {
  return TIER_BY_STRATEGY[strategyId] || 'day';
}

module.exports = {
  STRATEGY_MA_GOLD_BREAK: STRATEGY_MA_GOLD_BREAK,
  STRATEGY_MA_DEATH_BREAK: STRATEGY_MA_DEATH_BREAK,
  STRATEGY_MA_BULL_BREAK: STRATEGY_MA_BULL_BREAK,
  STRATEGY_MA_BEAR_START: STRATEGY_MA_BEAR_START,
  DEFAULT_STRATEGY: DEFAULT_STRATEGY,
  TIER_SHORT: DEFAULT_STRATEGY,
  TIER_MEDIUM: MGB_TIER_MEDIUM,
  TIER_LONG: MGB_TIER_LONG,
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
