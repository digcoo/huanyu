/**
 * 小程序策略导航（my4：死叉交叉点 / 金叉交叉点 / 金叉波段顶 / 一阳穿多线）
 */
var STRATEGY_MA_DC_BREAK = 'madcbreak';
var STRATEGY_MA_GC_BREAK = 'magcbreak';
var STRATEGY_MA_GH_BREAK = 'maghbreak';
var STRATEGY_MA_YANG_PIERCE = 'mayangpierce';

var MDX_TIER_FLASH = 'madcbreakFlash';
var MDX_TIER_SHORT = 'madcbreakShort';
var MDX_TIER_MEDIUM = 'madcbreakMedium';
var MDX_TIER_LONG = 'madcbreakLong';
var MDX_TIER_QUARTER = 'madcbreakQuarter';

var MGX_TIER_FLASH = 'magcbreakFlash';
var MGX_TIER_SHORT = 'magcbreakShort';
var MGX_TIER_MEDIUM = 'magcbreakMedium';
var MGX_TIER_LONG = 'magcbreakLong';
var MGX_TIER_QUARTER = 'magcbreakQuarter';

var MGH_TIER_FLASH = 'maghbreakFlash';
var MGH_TIER_SHORT = 'maghbreakShort';
var MGH_TIER_MEDIUM = 'maghbreakMedium';
var MGH_TIER_LONG = 'maghbreakLong';
var MGH_TIER_QUARTER = 'maghbreakQuarter';

var MYP_TIER_FLASH = 'mayangpierceFlash';
var MYP_TIER_SHORT = 'mayangpierceShort';
var MYP_TIER_MEDIUM = 'mayangpierceMedium';
var MYP_TIER_LONG = 'mayangpierceLong';
var MYP_TIER_QUARTER = 'mayangpierceQuarter';

var DEFAULT_STRATEGY = MDX_TIER_FLASH;

var STRATEGY_FAMILIES = [
  { id: STRATEGY_MA_DC_BREAK, name: '死叉交叉点', icon: '✕' },
  { id: STRATEGY_MA_GC_BREAK, name: '金叉交叉点', icon: '✚' },
  { id: STRATEGY_MA_GH_BREAK, name: '金叉波段顶', icon: '▲' },
  { id: STRATEGY_MA_YANG_PIERCE, name: '一阳穿多线', icon: '│' }
];

var TIER_TABS_BY_FAMILY = {
  madcbreak: [
    { id: MDX_TIER_FLASH, name: '30分' },
    { id: MDX_TIER_SHORT, name: '日线' },
    { id: MDX_TIER_MEDIUM, name: '周线' },
    { id: MDX_TIER_LONG, name: '月线' },
    { id: MDX_TIER_QUARTER, name: '季线' }
  ],
  magcbreak: [
    { id: MGX_TIER_FLASH, name: '30分' },
    { id: MGX_TIER_SHORT, name: '日线' },
    { id: MGX_TIER_MEDIUM, name: '周线' },
    { id: MGX_TIER_LONG, name: '月线' },
    { id: MGX_TIER_QUARTER, name: '季线' }
  ],
  maghbreak: [
    { id: MGH_TIER_FLASH, name: '30分' },
    { id: MGH_TIER_SHORT, name: '日线' },
    { id: MGH_TIER_MEDIUM, name: '周线' },
    { id: MGH_TIER_LONG, name: '月线' },
    { id: MGH_TIER_QUARTER, name: '季线' }
  ],
  mayangpierce: [
    { id: MYP_TIER_FLASH, name: '30分' },
    { id: MYP_TIER_SHORT, name: '日线' },
    { id: MYP_TIER_MEDIUM, name: '周线' },
    { id: MYP_TIER_LONG, name: '月线' },
    { id: MYP_TIER_QUARTER, name: '季线' }
  ]
};

var STRATEGY_TITLES = {
  madcbreak: '死叉交叉点突破',
  madcbreakFlash: '死叉交叉点突破 · 30分',
  madcbreakShort: '死叉交叉点突破 · 日',
  madcbreakMedium: '死叉交叉点突破 · 周',
  madcbreakLong: '死叉交叉点突破 · 月',
  madcbreakQuarter: '死叉交叉点突破 · 季',
  magcbreak: '金叉交叉点突破',
  magcbreakFlash: '金叉交叉点突破 · 30分',
  magcbreakShort: '金叉交叉点突破 · 日',
  magcbreakMedium: '金叉交叉点突破 · 周',
  magcbreakLong: '金叉交叉点突破 · 月',
  magcbreakQuarter: '金叉交叉点突破 · 季',
  maghbreak: '金叉波段顶突破',
  maghbreakFlash: '金叉波段顶突破 · 30分',
  maghbreakShort: '金叉波段顶突破 · 日',
  maghbreakMedium: '金叉波段顶突破 · 周',
  maghbreakLong: '金叉波段顶突破 · 月',
  maghbreakQuarter: '金叉波段顶突破 · 季',
  mayangpierce: '一阳穿多线',
  mayangpierceFlash: '一阳穿多线 · 30分',
  mayangpierceShort: '一阳穿多线 · 日',
  mayangpierceMedium: '一阳穿多线 · 周',
  mayangpierceLong: '一阳穿多线 · 月',
  mayangpierceQuarter: '一阳穿多线 · 季'
};

var ACTIVE_STRATEGY_IDS = {
  madcbreakFlash: true,
  madcbreakShort: true,
  madcbreakMedium: true,
  madcbreakLong: true,
  madcbreakQuarter: true,
  magcbreakFlash: true,
  magcbreakShort: true,
  magcbreakMedium: true,
  magcbreakLong: true,
  magcbreakQuarter: true,
  maghbreakFlash: true,
  maghbreakShort: true,
  maghbreakMedium: true,
  maghbreakLong: true,
  maghbreakQuarter: true,
  mayangpierceFlash: true,
  mayangpierceShort: true,
  mayangpierceMedium: true,
  mayangpierceLong: true,
  mayangpierceQuarter: true
};

var TIER_BY_STRATEGY = {
  madcbreakFlash: 'min30',
  madcbreakShort: 'day',
  madcbreakMedium: 'week',
  madcbreakLong: 'month',
  madcbreakQuarter: 'quarter',
  magcbreakFlash: 'min30',
  magcbreakShort: 'day',
  magcbreakMedium: 'week',
  magcbreakLong: 'month',
  magcbreakQuarter: 'quarter',
  maghbreakFlash: 'min30',
  maghbreakShort: 'day',
  maghbreakMedium: 'week',
  maghbreakLong: 'month',
  maghbreakQuarter: 'quarter',
  mayangpierceFlash: 'min30',
  mayangpierceShort: 'day',
  mayangpierceMedium: 'week',
  mayangpierceLong: 'month',
  mayangpierceQuarter: 'quarter'
};

function familyForStrategyId(strategyId) {
  var id = String(strategyId || '');
  if (id.indexOf('mayangpierce') === 0) {
    return STRATEGY_MA_YANG_PIERCE;
  }
  if (id.indexOf('magcbreak') === 0) {
    return STRATEGY_MA_GC_BREAK;
  }
  if (id.indexOf('maghbreak') === 0) {
    return STRATEGY_MA_GH_BREAK;
  }
  return STRATEGY_MA_DC_BREAK;
}

function strategyIdFor(family, tier) {
  if (tier && ACTIVE_STRATEGY_IDS[tier]) {
    return tier;
  }
  if (family === STRATEGY_MA_GC_BREAK) {
    return MGX_TIER_FLASH;
  }
  if (family === STRATEGY_MA_GH_BREAK) {
    return MGH_TIER_FLASH;
  }
  if (family === STRATEGY_MA_YANG_PIERCE) {
    return MYP_TIER_FLASH;
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
  return TIER_TABS_BY_FAMILY[family] || TIER_TABS_BY_FAMILY.madcbreak;
}

function strategyTitleFor(strategyId) {
  return STRATEGY_TITLES[strategyId] || STRATEGY_TITLES.madcbreakFlash;
}

function migrateLegacyMaId(id) {
  if (id.indexOf('magoldbreak') === 0) {
    return id.replace('magoldbreak', 'maghbreak');
  }
  if (id.indexOf('madeathbreak') === 0) {
    return id.replace('madeathbreak', 'madcbreak');
  }
  return id;
}

function migrateSavedStrategy(strategyId) {
  var id = migrateLegacyMaId(strategyId || DEFAULT_STRATEGY);
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
  STRATEGY_MA_DC_BREAK: STRATEGY_MA_DC_BREAK,
  STRATEGY_MA_GC_BREAK: STRATEGY_MA_GC_BREAK,
  STRATEGY_MA_GH_BREAK: STRATEGY_MA_GH_BREAK,
  STRATEGY_MA_YANG_PIERCE: STRATEGY_MA_YANG_PIERCE,
  DEFAULT_STRATEGY: DEFAULT_STRATEGY,
  TIER_SHORT: DEFAULT_STRATEGY,
  TIER_MEDIUM: MDX_TIER_MEDIUM,
  TIER_LONG: MDX_TIER_LONG,
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
