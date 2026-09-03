/**
 * 小程序策略导航
 */
var STRATEGY_MA_GOLD_BREAK = 'magoldbreak';
var STRATEGY_MA_DEATH_BREAK = 'madeathbreak';
var STRATEGY_MA_DC_BREAK = 'madcbreak';

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

var MDX_TIER_FLASH = 'madcbreakFlash';
var MDX_TIER_SHORT = 'madcbreakShort';
var MDX_TIER_MEDIUM = 'madcbreakMedium';
var MDX_TIER_LONG = 'madcbreakLong';
var MDX_TIER_QUARTER = 'madcbreakQuarter';

var DEFAULT_STRATEGY = MGB_TIER_FLASH;

var STRATEGY_FAMILIES = [
  { id: STRATEGY_MA_GOLD_BREAK, name: 'MA金叉点', icon: '✦' },
  { id: STRATEGY_MA_DEATH_BREAK, name: 'MA死叉点', icon: '✧' },
  { id: STRATEGY_MA_DC_BREAK, name: '死叉交叉点', icon: '✕' }
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
  madcbreak: [
    { id: MDX_TIER_FLASH, name: '30分' },
    { id: MDX_TIER_SHORT, name: '日线' },
    { id: MDX_TIER_MEDIUM, name: '周线' },
    { id: MDX_TIER_LONG, name: '月线' },
    { id: MDX_TIER_QUARTER, name: '季线' }
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
  madcbreak: '死叉交叉点突破',
  madcbreakFlash: '死叉交叉点突破 · 30分',
  madcbreakShort: '死叉交叉点突破 · 日',
  madcbreakMedium: '死叉交叉点突破 · 周',
  madcbreakLong: '死叉交叉点突破 · 月',
  madcbreakQuarter: '死叉交叉点突破 · 季'
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
  madcbreakFlash: true,
  madcbreakShort: true,
  madcbreakMedium: true,
  madcbreakLong: true,
  madcbreakQuarter: true
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
  madcbreakFlash: 'min30',
  madcbreakShort: 'day',
  madcbreakMedium: 'week',
  madcbreakLong: 'month',
  madcbreakQuarter: 'quarter'
};

function familyForStrategyId(strategyId) {
  var id = String(strategyId || '');
  if (id.indexOf('madcbreak') === 0) {
    return STRATEGY_MA_DC_BREAK;
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
  if (family === STRATEGY_MA_DC_BREAK) {
    return MDX_TIER_FLASH;
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
      || id.indexOf('mabullbreak') === 0
      || id.indexOf('mabearstart') === 0
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
  STRATEGY_MA_DC_BREAK: STRATEGY_MA_DC_BREAK,
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
