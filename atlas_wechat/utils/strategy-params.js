/**
 * 策略自定义参数 · 本地存储 + API 查询字段映射
 */

var STORAGE_PREFIX = 'strategyParams_';

var MACD_POSITIVE_GATE_DEFAULTS = {
  mgRequireDayMacd: false,
  mgRequireWeekMacd: false,
  mgRequireMonthMacd: false
};

var MACD_POSITIVE_GATE_SCHEMA = [
  {
    type: 'section',
    label: 'MACD>0（可选）'
  },
  {
    key: 'mgRequireDayMacd',
    label: '日 MACD>0',
    hint: '默认关闭，开启后需满足',
    type: 'switch'
  },
  {
    key: 'mgRequireWeekMacd',
    label: '周 MACD>0',
    hint: '默认关闭，开启后需满足',
    type: 'switch'
  },
  {
    key: 'mgRequireMonthMacd',
    label: '月 MACD>0',
    hint: '默认关闭，开启后需满足',
    type: 'switch'
  }
];

var TREND_DEFAULTS = {
  trMinAmountWan: 5000,
  trPrevWeeks: 2,
  trRequireCurrentBreakout: false,
  trRequireMonthMacd: false,
  trRequireWeekMacd: false,
  trRequireDayMacd: false,
  trRequireWeekGoldenCross: false,
  trRequireUltra: true
};

var REBOUND_DEFAULTS = {
  rMinAmountWan: 3000,
  rEnableShort: true,
  rEnableMedium: true,
  rEnableLong: true,
  rTierMin: 'ALL'
};

var RESONANCE_DEFAULTS = {
  cMinAmountWan: 5000,
  cEnableShort: true,
  cEnableMedium: true,
  cEnableLong: true,
  cTierMin: 'ALL'
};

var LADDER_DEFAULTS = {
  lMinAmountWan: 3000,
  lEnableUltra: true,
  lEnableShort: true,
  lEnableMedium: true,
  lEnableLong: true,
  lTierMin: 'ALL'
};

var RETEST_DEFAULTS = {
  tMinAmountWan: 3000,
  tEnableBear: true,
  tEnableBull: true,
  tEnableUltra: true,
  tEnableShort: true,
  tEnableMedium: true,
  tEnableLong: true,
  tTierMin: 'ALL'
};

var GC2_DEFAULTS = {
  g2MinAmountWan: 5000,
  g2EnableShort: true,
  g2EnableLong: true,
  g2TierMin: 'ALL',
  g2LookbackShort: 60,
  g2LookbackLong: 52
};

var DC2_DEFAULTS = {
  d2MinAmountWan: 5000,
  d2EnableShort: true,
  d2EnableLong: true,
  d2TierMin: 'ALL',
  d2LookbackShort: 60,
  d2LookbackLong: 52
};

var CASCADE_DEFAULTS = {
  caEnableDualLowGate: false,
  caEnableMacdGate: false,
  caEnableMacdDcHighGate: true,
  caEnableCrossLowGate: false,
  caEnableBarHighGate: false,
  caEnableAllYangGate: true,
  caEnableDay: true,
  caEnableWeek: false,
  caEnableMonth: false,
  caLookbackDay: 60,
  caLookbackWeek: 52,
  caLookbackMonth: 36,
  caMinAmountWan: 3000,
  caRequireUltra: false,
  caEnableAltBreakout: false
};

var MACEDGE_DEFAULTS = {
  meEnableDualLowGate: false,
  meEnableMacdGate: false,
  meEnableMacdDcHighGate: true,
  meEnableCrossLowGate: false,
  meEnableBarHighGate: false,
  meEnableAllYangGate: true,
  meEnableMin30: false,
  meEnableDay: true,
  meEnableWeek: false,
  meEnableMonth: false,
  meEnableYear: false,
  meLookbackMin30: 50,
  meLookbackDay: 60,
  meLookbackWeek: 52,
  meLookbackMonth: 36,
  meLookbackYear: 20,
  meMinAmountWan: 3000
};

var WAVECONVEX_DEFAULTS = {
  wcvEnableAllYangGate: false,
  wcvEnableMin30Gate: false,
  wcvEnableLastHighBreak: false,
  wcvEnableLastMedianBreak: false,
  wcvEnableLastLowBreak: false,
  wcvEnableDay: true,
  wcvEnableWeek: false,
  wcvEnableMonth: false,
  wcvLookbackDay: 60,
  wcvLookbackWeek: 52,
  wcvLookbackMonth: 36,
  wcvMinAmountWan: 0
};

var WAVECONCAVE_DEFAULTS = {
  wccEnableAllYangGate: false,
  wccEnableMin30Gate: false,
  wccEnableLastHighBreak: false,
  wccEnableLastMedianBreak: false,
  wccEnableLastLowBreak: false,
  wccEnableDay: true,
  wccEnableWeek: false,
  wccEnableMonth: false,
  wccLookbackDay: 60,
  wccLookbackWeek: 52,
  wccLookbackMonth: 36,
  wccMinAmountWan: 0
};

var WAVECONVEXDAY_DEFAULTS = {
  wcvdEnableAllYangGate: false,
  wcvdEnableMin30Gate: false,
  wcvdEnableLastHighBreak: false,
  wcvdEnableLastMedianBreak: false,
  wcvdEnableLastLowBreak: false,
  wcvdEnableDay: true,
  wcvdEnableWeek: false,
  wcvdEnableMonth: false,
  wcvdLookbackDay: 60,
  wcvdLookbackWeek: 52,
  wcvdLookbackMonth: 36,
  wcvdMinAmountWan: 0
};

var WAVECONCAVEDAY_DEFAULTS = {
  wccdEnableAllYangGate: false,
  wccdEnableMin30Gate: false,
  wccdEnableLastHighBreak: false,
  wccdEnableLastMedianBreak: false,
  wccdEnableLastLowBreak: false,
  wccdEnableDay: true,
  wccdEnableWeek: false,
  wccdEnableMonth: false,
  wccdLookbackDay: 60,
  wccdLookbackWeek: 52,
  wccdLookbackMonth: 36,
  wccdMinAmountWan: 0
};

var CASCADEWAVECONVEX_DEFAULTS = {
  cwcvEnableAllYangGate: false,
  cwcvEnableMin30Gate: false,
  cwcvEnableBandLastYangLowGate: true,
  cwcvEnableYangBandTrendGate: false,
  cwcvEnablePrevBandBreak: false,
  cwcvEnableDay: true,
  cwcvEnableWeek: false,
  cwcvEnableMonth: false,
  cwcvLookbackDay: 60,
  cwcvLookbackWeek: 52,
  cwcvLookbackMonth: 36,
  cwcvLookbackYear: 20,
  cwcvMinAmountWan: 0
};

var CASCADEWAVECONCAVE_DEFAULTS = {
  cwcavEnableAllYangGate: false,
  cwcavEnableMin30Gate: false,
  cwcavEnableBandLastYangLowGate: true,
  cwcavEnableYangBandTrendGate: false,
  cwcavEnablePrevBandBreak: false,
  cwcavEnableDay: true,
  cwcavEnableWeek: false,
  cwcavEnableMonth: false,
  cwcavLookbackDay: 60,
  cwcavLookbackWeek: 52,
  cwcavLookbackMonth: 36,
  cwcavLookbackYear: 20,
  cwcavMinAmountWan: 0
};

var CASCADEWAVECONVEXDAY_DEFAULTS = {
  cwcvdEnableAllYangGate: false,
  cwcvdEnableMin30Gate: false,
  cwcvdEnableBandLastYangLowGate: true,
  cwcvdEnableYangBandTrendGate: false,
  cwcvdEnablePrevBandBreak: false,
  cwcvdEnableDay: true,
  cwcvdEnableWeek: false,
  cwcvdEnableMonth: false,
  cwcvdLookbackDay: 60,
  cwcvdLookbackWeek: 52,
  cwcvdLookbackMonth: 36,
  cwcvdLookbackYear: 20,
  cwcvdMinAmountWan: 0
};

var CASCADEWAVECONCAVEDAY_DEFAULTS = {
  cwcadEnableAllYangGate: false,
  cwcadEnableMin30Gate: false,
  cwcadEnableBandLastYangLowGate: true,
  cwcadEnableYangBandTrendGate: false,
  cwcadEnablePrevBandBreak: false,
  cwcadEnableDay: true,
  cwcadEnableWeek: false,
  cwcadEnableMonth: false,
  cwcadLookbackDay: 60,
  cwcadLookbackWeek: 52,
  cwcadLookbackMonth: 36,
  cwcadLookbackYear: 20,
  cwcadMinAmountWan: 0
};

var CASCADEWAVE_BUNDLE_DEFAULTS = {
  cwbEnableAllYangGate: false,
  cwbEnableMin30Gate: false,
  cwbEnableBandLastYangLowGate: true,
  cwbEnableYangBandTrendGate: false,
  cwbEnablePrevBandBreak: false,
  cwbLookbackDay: 60,
  cwbLookbackWeek: 52,
  cwbLookbackMonth: 36,
  cwbLookbackYear: 20,
  cwbMinAmountWan: 0
};

var CASCADEWAVE_BUNDLE_COMMON_SCHEMA = [
  {
    type: 'section',
    label: '全局门控',
    hint: '末阳低门默认开启；全阳门/Min30 默认关闭'
  },
  {
    key: 'cwbEnableBandLastYangLowGate',
    label: '末阳低门',
    hint: '日、周、月收盘价须全部 > 各档末完整波段末阳 K 的 low',
    type: 'switch'
  },
  {
    key: 'cwbEnableYangBandTrendGate',
    label: '趋势门',
    hint: '按信号档：peak收盘抬升时现价>次末peak高，否则现价>末peak高',
    type: 'switch'
  },
  {
    key: 'cwbEnableAllYangGate',
    label: '全阳门',
    hint: '日、周、月最后一根K均为阳线',
    type: 'switch'
  },
  {
    key: 'cwbEnableMin30Gate',
    label: 'Min30 突破门',
    type: 'switch'
  },
  {
    key: 'cwbMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）；0=不启用',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    type: 'section',
    label: '突破线',
    hint: '凸=次波段末阳顶，凹=末波段末阳顶（按子策略形态固定）'
  },
  {
    key: 'cwbEnablePrevBandBreak',
    label: '前波段末阳顶',
    hint: '已固定按形态突破，此开关不再生效',
    type: 'switch'
  },
  {
    type: 'section',
    label: '波段回溯'
  },
  {
    key: 'cwbLookbackDay',
    label: '日 K lookback',
    type: 'slider',
    min: 20,
    max: 120,
    step: 5,
    unit: '根'
  },
  {
    key: 'cwbLookbackWeek',
    label: '周 K lookback',
    type: 'slider',
    min: 20,
    max: 104,
    step: 4,
    unit: '根'
  },
  {
    key: 'cwbLookbackMonth',
    label: '月 K lookback',
    type: 'slider',
    min: 12,
    max: 60,
    step: 4,
    unit: '根'
  },
  {
    key: 'cwbLookbackYear',
    label: '年 K lookback',
    hint: '级联MACD月→年档回溯',
    type: 'slider',
    min: 8,
    max: 40,
    step: 2,
    unit: '根'
  }
];

var CASCADEWAVE_SHORT_SCHEMA = [
  {
    type: 'section',
    label: '突破模式',
    hint: '级联凹凸同档日档突破（凸+凹并集）'
  }
].concat(CASCADEWAVE_BUNDLE_COMMON_SCHEMA);

var CASCADEWAVE_MEDIUM_SCHEMA = [
  {
    type: 'section',
    label: '突破模式',
    hint: '级联凹凸同档周档突破（凸+凹并集）'
  }
].concat(CASCADEWAVE_BUNDLE_COMMON_SCHEMA);

var CASCADEWAVE_LONG_SCHEMA = [
  {
    type: 'section',
    label: '突破模式',
    hint: '级联凹凸同档月档突破（凸+凹并集）'
  }
].concat(CASCADEWAVE_BUNDLE_COMMON_SCHEMA);

var MGCWH_DEFAULTS = Object.assign({
  mgcwhEnableMinAmountFilter: true,
  mgcwhMinAmountWan: 3000,
  mgcwhEnableSignalRiseGate: true,
  mgcwhSignalRisePct: 3
}, MACD_POSITIVE_GATE_DEFAULTS);

var MGCWH_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: '最近金叉K定基准波段，同档边沿破波段High；以下两门可选，默认开启'
  },
  {
    key: 'mgcwhEnableMinAmountFilter',
    label: '成交额门',
    hint: '近6日日均成交额（不含当日K）',
    type: 'switch'
  },
  {
    key: 'mgcwhMinAmountWan',
    label: '最低日均成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'mgcwhEnableSignalRiseGate',
    label: '末K涨幅门',
    hint: '末 K 上涨率须大于阈值',
    type: 'switch'
  },
  {
    key: 'mgcwhSignalRisePct',
    label: '末K涨幅阈值',
    hint: '上涨率 = (收盘-前收)/前收',
    type: 'slider',
    min: 1,
    max: 15,
    step: 0.5,
    unit: '%'
  }
].concat(MACD_POSITIVE_GATE_SCHEMA);

var MGCWHU_DEFAULTS = Object.assign({
  mgcwhuEnableMinAmountFilter: true,
  mgcwhuMinAmountWan: 3000,
  mgcwhuEnableSignalRiseGate: true,
  mgcwhuSignalRisePct: 1
}, {
  mgRequireDayMacd: true,
  mgRequireWeekMacd: true,
  mgRequireMonthMacd: true,
  mgRequireMin60Macd: false
});

var MGCWHU_MACD_GATE_SCHEMA = [
  {
    type: 'section',
    label: 'MACD>0',
    hint: '勾选周期须 MACD 柱>0；默认日/周/月全开，Min60 默认关'
  },
  {
    key: 'mgRequireMin60Macd',
    label: 'Min60 MACD>0',
    type: 'switch'
  },
  {
    key: 'mgRequireDayMacd',
    label: '日 MACD>0',
    type: 'switch'
  },
  {
    key: 'mgRequireWeekMacd',
    label: '周 MACD>0',
    type: 'switch'
  },
  {
    key: 'mgRequireMonthMacd',
    label: '月 MACD>0',
    type: 'switch'
  }
];

var MGCWHU_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: '日收>前日 high；前日收阳'
  }
].concat(MGCWHU_MACD_GATE_SCHEMA).concat([
  {
    key: 'mgcwhuEnableMinAmountFilter',
    label: '成交额门',
    hint: '近6日日均成交额（不含当日K）',
    type: 'switch'
  },
  {
    key: 'mgcwhuMinAmountWan',
    label: '最低日均成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'mgcwhuEnableSignalRiseGate',
    label: '末K涨幅门',
    hint: '末 K 上涨率须大于阈值',
    type: 'switch'
  },
  {
    key: 'mgcwhuSignalRisePct',
    label: '末K涨幅阈值',
    hint: '上涨率 = (收盘-前收)/前收',
    type: 'slider',
    min: 1,
    max: 15,
    step: 0.5,
    unit: '%'
  }
]);

var MDCB_DEFAULTS = Object.assign({
  mdcbEnableMinAmountFilter: true,
  mdcbMinAmountWan: 3000,
  mdcbEnableSignalRiseGate: true,
  mdcbSignalRisePct: 3
}, MACD_POSITIVE_GATE_DEFAULTS);

var MDCB_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: 'MACD<0、最近交叉为死叉、边沿突破死叉K前一根K high'
  },
  {
    key: 'mdcbEnableMinAmountFilter',
    label: '成交额门',
    hint: '近6日日均成交额（不含当日K）',
    type: 'switch'
  },
  {
    key: 'mdcbMinAmountWan',
    label: '最低日均成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'mdcbEnableSignalRiseGate',
    label: '末K涨幅门',
    hint: '末 K 上涨率须大于阈值',
    type: 'switch'
  },
  {
    key: 'mdcbSignalRisePct',
    label: '末K涨幅阈值',
    hint: '上涨率 = (收盘-前收)/前收',
    type: 'slider',
    min: 1,
    max: 15,
    step: 0.5,
    unit: '%'
  }
].concat(MACD_POSITIVE_GATE_SCHEMA);

var ULTRAGC_DEFAULTS = Object.assign({
  ulgcPrevDays: 2,
  ulgcMaxBarsPerDay: 8,
  ulgcGcLookbackBars: 24,
  ulgcSignalRisePct: 1
}, MACD_POSITIVE_GATE_DEFAULTS);

var ULTRAGC_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: '末 Min30 MACD>0、金叉 K high 边沿突破、信号日 Min30 涨幅门'
  },
  {
    key: 'ulgcPrevDays',
    label: '背景交易日',
    hint: '信号日前纳入的背景交易日数',
    type: 'slider',
    min: 0,
    max: 5,
    step: 1,
    unit: '日'
  },
  {
    key: 'ulgcMaxBarsPerDay',
    label: '每日 Min30 根数',
    hint: '每个交易日最多纳入的 Min30 根',
    type: 'slider',
    min: 4,
    max: 16,
    step: 1,
    unit: '根'
  },
  {
    key: 'ulgcGcLookbackBars',
    label: '金叉回溯',
    hint: '信号 K 前回溯查找金叉的根数',
    type: 'slider',
    min: 5,
    max: 48,
    step: 1,
    unit: '根'
  },
  {
    key: 'ulgcSignalRisePct',
    label: '信号涨幅阈值',
    hint: '上涨率 = (收盘-前收)/前收',
    type: 'slider',
    min: 0.5,
    max: 10,
    step: 0.5,
    unit: '%'
  }
].concat(MACD_POSITIVE_GATE_SCHEMA);

var M60WCCB_DEFAULTS = {
  m60wccbPrevDays: 2,
  m60wccbMaxBarsPerDay: 4,
  m60wccbLookbackBars: 120,
  m60wccbEnableMinAmountFilter: true,
  m60wccbMinAmountWan: 3000,
  m60wccbEnableSignalRiseGate: true,
  m60wccbSignalRisePct: 1
};

var M60WCCB_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: 'Min60/日/周 MACD 至少 2 个>0；Min60 凸凹边沿破波段 High；信号限末交易日'
  },
  {
    key: 'm60wccbPrevDays',
    label: '背景交易日',
    hint: '信号日前纳入的背景交易日数',
    type: 'slider',
    min: 0,
    max: 5,
    step: 1,
    unit: '日'
  },
  {
    key: 'm60wccbMaxBarsPerDay',
    label: '每日 Min60 根数',
    hint: '每个交易日最多纳入的 Min60 根',
    type: 'slider',
    min: 2,
    max: 8,
    step: 1,
    unit: '根'
  },
  {
    key: 'm60wccbLookbackBars',
    label: '波段回溯',
    hint: 'Min60 凸凹波段识别回溯根数',
    type: 'slider',
    min: 40,
    max: 240,
    step: 10,
    unit: '根'
  },
  {
    key: 'm60wccbEnableMinAmountFilter',
    label: '启用成交额门',
    type: 'switch'
  },
  {
    key: 'm60wccbMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'm60wccbEnableSignalRiseGate',
    label: '启用突破涨幅门',
    type: 'switch'
  },
  {
    key: 'm60wccbSignalRisePct',
    label: '突破涨幅阈值',
    hint: '上涨率 = (收盘-前收)/前收',
    type: 'slider',
    min: 0.5,
    max: 10,
    step: 0.5,
    unit: '%'
  }
];

var DWCCB_DEFAULTS = {
  dwccbLookbackBars: 120,
  dwccbEnableMinAmountFilter: true,
  dwccbMinAmountWan: 3000,
  dwccbEnableSignalRiseGate: true,
  dwccbSignalRisePct: 1
};

var DWCCB_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: '日/周/月 MACD 至少 2 个>0；日 K 凸凹边沿破波段 High；信号限末根日 K'
  },
  {
    key: 'dwccbLookbackBars',
    label: '波段回溯',
    hint: '日 K 凸凹波段识别回溯根数',
    type: 'slider',
    min: 40,
    max: 240,
    step: 10,
    unit: '根'
  },
  {
    key: 'dwccbEnableMinAmountFilter',
    label: '启用成交额门',
    type: 'switch'
  },
  {
    key: 'dwccbMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'dwccbEnableSignalRiseGate',
    label: '启用突破涨幅门',
    type: 'switch'
  },
  {
    key: 'dwccbSignalRisePct',
    label: '突破涨幅阈值',
    hint: '上涨率 = (收盘-前收)/前收',
    type: 'slider',
    min: 0.5,
    max: 10,
    step: 0.5,
    unit: '%'
  }
];

var WWCCB_DEFAULTS = {
  wwccbLookbackBars: 52,
  wwccbEnableMinAmountFilter: true,
  wwccbMinAmountWan: 3000,
  wwccbEnableSignalRiseGate: true,
  wwccbSignalRisePct: 1,
  wwccbMaxRetestGapPct: 1
};

var WWCCB_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: '周 MACD>0；周 K 凹/凸边沿突破或凸边沿回踩；信号限末根周 K'
  },
  {
    key: 'wwccbLookbackBars',
    label: '波段回溯',
    hint: '周 K 凸凹波段识别回溯根数',
    type: 'slider',
    min: 20,
    max: 120,
    step: 4,
    unit: '根'
  },
  {
    key: 'wwccbMaxRetestGapPct',
    label: '回踩容差',
    hint: 'low 与次波段 High 最大相对差值',
    type: 'slider',
    min: 0.5,
    max: 5,
    step: 0.5,
    unit: '%'
  },
  {
    key: 'wwccbEnableMinAmountFilter',
    label: '启用成交额门',
    type: 'switch'
  },
  {
    key: 'wwccbMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'wwccbEnableSignalRiseGate',
    label: '启用突破涨幅门',
    hint: '仅对凹/凸边沿突破生效',
    type: 'switch'
  },
  {
    key: 'wwccbSignalRisePct',
    label: '突破涨幅阈值',
    hint: '上涨率 = (收盘-前收)/前收',
    type: 'slider',
    min: 0.5,
    max: 10,
    step: 0.5,
    unit: '%'
  }
];

var MWCCB_DEFAULTS = {
  mwccbLookbackBars: 36,
  mwccbEnableMinAmountFilter: true,
  mwccbMinAmountWan: 3000,
  mwccbEnableSignalRiseGate: true,
  mwccbSignalRisePct: 1,
  mwccbMaxRetestGapPct: 1
};

var MWCCB_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: '月 MACD>0；月 K 凹/凸边沿突破或凸边沿回踩；信号限末根月 K'
  },
  {
    key: 'mwccbLookbackBars',
    label: '波段回溯',
    hint: '月 K 凸凹波段识别回溯根数',
    type: 'slider',
    min: 12,
    max: 80,
    step: 4,
    unit: '根'
  },
  {
    key: 'mwccbMaxRetestGapPct',
    label: '回踩容差',
    hint: 'low 与次波段 High 最大相对差值',
    type: 'slider',
    min: 0.5,
    max: 5,
    step: 0.5,
    unit: '%'
  },
  {
    key: 'mwccbEnableMinAmountFilter',
    label: '启用成交额门',
    type: 'switch'
  },
  {
    key: 'mwccbMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'mwccbEnableSignalRiseGate',
    label: '启用突破涨幅门',
    hint: '仅对凹/凸边沿突破生效',
    type: 'switch'
  },
  {
    key: 'mwccbSignalRisePct',
    label: '突破涨幅阈值',
    hint: '上涨率 = (收盘-前收)/前收',
    type: 'slider',
    min: 0.5,
    max: 10,
    step: 0.5,
    unit: '%'
  }
];

var ULTRA_DEFAULTS = Object.assign({
  ulMinAmountWan: 5000,
  ulRequireDayMacdNegative: false,
  ulRequireWeekMacdNegative: false,
  ulRequireMonthMacdNegative: false,
  ulRequireCurrentBreakout: false
}, MACD_POSITIVE_GATE_DEFAULTS);

var ULTRA_SCHEMA = [
  {
    key: 'ulMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  }
].concat(MACD_POSITIVE_GATE_SCHEMA).concat([
  {
    type: 'section',
    label: 'MACD<0（可选）'
  },
  {
    key: 'ulRequireDayMacdNegative',
    label: '日 MACD<0',
    hint: '与「日MACD>0」勿同时开启',
    type: 'switch'
  },
  {
    key: 'ulRequireWeekMacdNegative',
    label: '周 MACD<0',
    hint: '大周期仍处零轴下，常与「日MACD>0」联测',
    type: 'switch'
  },
  {
    key: 'ulRequireMonthMacdNegative',
    label: '月 MACD<0',
    hint: '默认关闭，开启后需满足',
    type: 'switch'
  },
  {
    type: 'section',
    label: '突破K'
  },
  {
    key: 'ulRequireCurrentBreakout',
    label: '当前K须为突破K',
    hint: '关闭则当日任一根满足突破条件即可',
    type: 'switch'
  }
]);

var RETEST_SCHEMA = [
  {
    key: 'tMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'tEnableBear',
    label: '下跌反转',
    hint: '深跌背景 · 强弹回踩再升',
    type: 'switch'
  },
  {
    key: 'tEnableBull',
    label: '上涨中继',
    hint: '趋势背景 · 二次回踩再升',
    type: 'switch'
  },
  {
    key: 'tEnableUltra',
    label: '超短档',
    hint: '30m · L0/H1/L1/介入',
    type: 'switch'
  },
  {
    key: 'tEnableShort',
    label: '短线档',
    hint: '日K结构',
    type: 'switch'
  },
  {
    key: 'tEnableMedium',
    label: '中线档',
    hint: '周K结构',
    type: 'switch'
  },
  {
    key: 'tEnableLong',
    label: '长线档',
    hint: '月K结构',
    type: 'switch'
  },
  {
    key: 'tTierMin',
    label: '最低展示档位',
    type: 'picker',
    options: [
      { value: 'ALL', label: '全部档位' },
      { value: 'C', label: 'C档及以上 (长线+)' },
      { value: 'B', label: 'B档及以上 (中线+)' },
      { value: 'A', label: 'A档及以上 (短线+)' },
      { value: 'S', label: '仅超短 (S)' }
    ]
  }
];

var TREND_SCHEMA = [
  {
    key: 'trMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'trPrevWeeks',
    label: '基准背景周数',
    hint: '自然周，信号周之前的完整周数',
    type: 'slider',
    min: 1,
    max: 4,
    step: 1,
    unit: '周'
  },
  {
    type: 'section',
    label: '突破K'
  },
  {
    key: 'trRequireCurrentBreakout',
    label: '当前日K须为突破K',
    hint: '关闭则本周任一日K满足即可',
    type: 'switch'
  },
  {
    type: 'section',
    label: 'MACD（可选）'
  },
  {
    key: 'trRequireMonthMacd',
    label: '月 MACD>0',
    type: 'switch'
  },
  {
    key: 'trRequireWeekMacd',
    label: '周 MACD>0',
    type: 'switch'
  },
  {
    key: 'trRequireDayMacd',
    label: '日 MACD>0',
    type: 'switch'
  },
  {
    key: 'trRequireWeekGoldenCross',
    label: '周K MACD 金叉',
    type: 'switch'
  },
  {
    type: 'section',
    label: '超短叠加'
  },
  {
    key: 'trRequireUltra',
    label: '须满足超短 30m 突破',
    hint: '开启后须同时满足 30m 跨日桶柱内突破；ul* 参数沿用超短 Tab 设置',
    type: 'switch'
  }
];

var REBOUND_SCHEMA = [
  {
    key: 'rMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'rEnableShort',
    label: '短线深跌',
    hint: '周 MACD<0 + 日 MACD>0 + 日K突破',
    type: 'switch'
  },
  {
    key: 'rEnableMedium',
    label: '中线深跌',
    hint: '月 MACD<0 + 周 MACD>0 + 周K突破',
    type: 'switch'
  },
  {
    key: 'rEnableLong',
    label: '长线深跌',
    hint: '年 MACD<0 + 月 MACD>0 + 月K突破',
    type: 'switch'
  },
  {
    key: 'rTierMin',
    label: '最低展示档位',
    type: 'picker',
    options: [
      { value: 'ALL', label: '全部档位' },
      { value: 'B', label: 'B档及以上 (长线+)' },
      { value: 'A', label: 'A档及以上 (中线+)' },
      { value: 'S', label: '仅短线 (S)' }
    ]
  }
];

var RESONANCE_SCHEMA = [
  {
    key: 'cMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'cEnableShort',
    label: '短线共振',
    hint: '周 MACD>0 + 日 MACD>0（非金叉）+ 日K突破',
    type: 'switch'
  },
  {
    key: 'cEnableMedium',
    label: '中线共振',
    hint: '月 MACD>0 + 周 MACD>0（非金叉）+ 周K突破',
    type: 'switch'
  },
  {
    key: 'cEnableLong',
    label: '长线共振',
    hint: '年 MACD>0 + 月 MACD>0（非金叉）+ 月K突破',
    type: 'switch'
  },
  {
    key: 'cTierMin',
    label: '最低展示档位',
    type: 'picker',
    options: [
      { value: 'ALL', label: '全部档位' },
      { value: 'B', label: 'B档及以上 (长线+)' },
      { value: 'A', label: 'A档及以上 (中线+)' },
      { value: 'S', label: '仅短线 (S)' }
    ]
  }
];

var GC2_SCHEMA = [
  {
    type: 'section',
    label: '硬门槛',
    hint: '不满足则不入池'
  },
  {
    key: 'g2MinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    type: 'section',
    label: '启用档位',
    hint: '短/长两档，可单独或同时开启'
  },
  {
    key: 'g2EnableShort',
    label: '短线（S）',
    hint: '月/周 MACD>0 · 日K 金叉柱 high 二次突破',
    type: 'switch'
  },
  {
    key: 'g2EnableLong',
    label: '长线（B）',
    hint: '年/月 MACD>0 · 周K 金叉柱 high 二次突破',
    type: 'switch'
  },
  {
    type: 'section',
    label: '列表筛选'
  },
  {
    key: 'g2TierMin',
    label: '最低展示档位',
    hint: '过滤扫描结果展示的最低档位',
    type: 'picker',
    options: [
      { value: 'ALL', label: '全部档位' },
      { value: 'B', label: '长线及以上（含短线）' },
      { value: 'S', label: '仅短线（S）' }
    ]
  }
];

var DC2_SCHEMA = [
  {
    type: 'section',
    label: '硬门槛',
    hint: '不满足则不入池'
  },
  {
    key: 'd2MinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    type: 'section',
    label: '启用档位',
    hint: '短/长两档，可单独或同时开启'
  },
  {
    key: 'd2EnableShort',
    label: '短线（S）',
    hint: '月/周 MACD>0 · 日K 死叉柱 high 突破',
    type: 'switch'
  },
  {
    key: 'd2EnableLong',
    label: '长线（B）',
    hint: '年/月 MACD>0 · 周K 死叉柱 high 突破',
    type: 'switch'
  },
  {
    type: 'section',
    label: '列表筛选'
  },
  {
    key: 'd2TierMin',
    label: '最低展示档位',
    hint: '过滤扫描结果展示的最低档位',
    type: 'picker',
    options: [
      { value: 'ALL', label: '全部档位' },
      { value: 'B', label: '长线及以上（含短线）' },
      { value: 'S', label: '仅短线（S）' }
    ]
  }
];

var CASCADE_SCHEMA = [
  {
    type: 'section',
    label: '可选六门',
    hint: '死叉高门、全阳门默认开；其余默认关；开启后日/周/月须全部满足对应门控'
  },
  {
    key: 'caEnableDualLowGate',
    label: '双低支撑门',
    type: 'switch'
  },
  {
    key: 'caEnableMacdGate',
    label: '无阻力 MACD 门',
    type: 'switch'
  },
  {
    key: 'caEnableMacdDcHighGate',
    label: 'MACD 死叉高门',
    hint: 'MACD>0，或 MACD≤0 且本档 close>最近 MACD 死叉 K.high；日/周/月须全部满足',
    type: 'switch'
  },
  {
    key: 'caEnableCrossLowGate',
    label: 'MACD 交叉 low 门',
    type: 'switch'
  },
  {
    key: 'caEnableBarHighGate',
    label: '高点递进门',
    type: 'switch'
  },
  {
    key: 'caEnableAllYangGate',
    label: '全阳门',
    hint: '日、周、月、年最后一根K均为阳线（close>open），须全部满足',
    type: 'switch'
  },
  {
    key: 'caMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）；0=不启用',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    type: 'section',
    label: '级联档位',
    hint: '日K边沿突破对应基准 high；破日须周稳、破周须月稳；周/月档须日close>日基准high；多档勾选取并集'
  },
  {
    key: 'caEnableDay',
    label: '突破日基准',
    type: 'switch'
  },
  {
    key: 'caEnableWeek',
    label: '突破周基准',
    type: 'switch'
  },
  {
    key: 'caEnableMonth',
    label: '突破月基准',
    type: 'switch'
  },
  {
    key: 'caEnableAltBreakout',
    label: '备选突破路径',
    hint: '开启后与基准 high 边沿取并集：日K边沿突破本档前K high，且最后一根日K close>基准low、倒数第二根日K close≤基准high',
    type: 'switch'
  },
  {
    type: 'section',
    label: '交叉 K 回溯',
    hint: '各周期向前搜索 MACD 金叉/死叉的最大 K 数'
  },
  {
    key: 'caLookbackDay',
    label: '日 K lookback',
    type: 'slider',
    min: 20,
    max: 120,
    step: 5,
    unit: '根'
  },
  {
    key: 'caLookbackWeek',
    label: '周 K lookback',
    type: 'slider',
    min: 20,
    max: 104,
    step: 4,
    unit: '根'
  },
  {
    key: 'caLookbackMonth',
    label: '月 K lookback',
    type: 'slider',
    min: 12,
    max: 60,
    step: 4,
    unit: '根'
  },
  {
    type: 'section',
    label: '超短叠加',
    hint: '开启后须同时满足 30m 跨日桶柱内突破；ul* 参数沿用「超短」Tab 设置'
  },
  {
    key: 'caRequireUltra',
    label: '须满足超短 Min30 突破',
    type: 'switch'
  }
];

var MACEDGE_SCHEMA = [
  {
    type: 'section',
    label: '可选六门',
    hint: '死叉高门、全阳门默认开；其余默认关；开启后日/周/月/年须全部满足对应门控'
  },
  {
    key: 'meEnableDualLowGate',
    label: '双低支撑门',
    type: 'switch'
  },
  {
    key: 'meEnableMacdGate',
    label: '无阻力 MACD 门',
    type: 'switch'
  },
  {
    key: 'meEnableMacdDcHighGate',
    label: 'MACD 死叉高门',
    hint: 'MACD>0，或 MACD≤0 且本档 close>最近 MACD 死叉 K.high；日/周/月须全部满足',
    type: 'switch'
  },
  {
    key: 'meEnableCrossLowGate',
    label: 'MACD 交叉 low 门',
    type: 'switch'
  },
  {
    key: 'meEnableBarHighGate',
    label: '高点递进门',
    type: 'switch'
  },
  {
    key: 'meEnableAllYangGate',
    label: '全阳门',
    hint: '日、周、月、年最后一根K均为阳线（close>open），须全部满足',
    type: 'switch'
  },
  {
    key: 'meMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）；0=不启用',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    type: 'section',
    label: '边沿突破档位',
    hint: '各档 MACD 交叉 K 为基准；同档最后一根 K 边沿破基准 high；多档并集'
  },
  {
    key: 'meEnableMin30',
    label: 'Min30 基准',
    type: 'switch'
  },
  {
    key: 'meEnableDay',
    label: '日 K 基准',
    type: 'switch'
  },
  {
    key: 'meEnableWeek',
    label: '周 K 基准',
    type: 'switch'
  },
  {
    key: 'meEnableMonth',
    label: '月 K 基准',
    type: 'switch'
  },
  {
    key: 'meEnableYear',
    label: '年 K 基准',
    type: 'switch'
  },
  {
    type: 'section',
    label: '交叉 K 回溯',
    hint: '各周期向前搜索 MACD 金叉/死叉的最大 K 数'
  },
  {
    key: 'meLookbackMin30',
    label: 'Min30 lookback',
    type: 'slider',
    min: 20,
    max: 120,
    step: 5,
    unit: '根'
  },
  {
    key: 'meLookbackDay',
    label: '日 K lookback',
    type: 'slider',
    min: 20,
    max: 120,
    step: 5,
    unit: '根'
  },
  {
    key: 'meLookbackWeek',
    label: '周 K lookback',
    type: 'slider',
    min: 20,
    max: 104,
    step: 4,
    unit: '根'
  },
  {
    key: 'meLookbackMonth',
    label: '月 K lookback',
    type: 'slider',
    min: 12,
    max: 60,
    step: 4,
    unit: '根'
  },
  {
    key: 'meLookbackYear',
    label: '年 K lookback',
    type: 'slider',
    min: 8,
    max: 40,
    step: 2,
    unit: '根'
  }
];

var MEDIUM_DEFAULTS = {
  mdMinAmountWan: 5000,
  mdPrevMonths: 2,
  mdRequireCurrentBreakout: false,
  mdRequireMonthMacd: false,
  mdRequireYearMacd: false,
  mdRequireMonthGoldenCross: false,
  mdRequireUltra: true
};

var MEDIUM_SCHEMA = [
  {
    key: 'mdMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'mdPrevMonths',
    label: '基准背景月数',
    hint: '自然月，信号月之前的完整月数',
    type: 'slider',
    min: 1,
    max: 4,
    step: 1,
    unit: '月'
  },
  {
    type: 'section',
    label: '突破K'
  },
  {
    key: 'mdRequireCurrentBreakout',
    label: '当前周K须为突破K',
    hint: '关闭则本月任一周K满足即可',
    type: 'switch'
  },
  {
    type: 'section',
    label: 'MACD（可选）'
  },
  {
    key: 'mdRequireMonthMacd',
    label: '月 MACD>0',
    type: 'switch'
  },
  {
    key: 'mdRequireYearMacd',
    label: '年 MACD>0',
    type: 'switch'
  },
  {
    key: 'mdRequireMonthGoldenCross',
    label: '月K MACD 金叉',
    type: 'switch'
  },
  {
    type: 'section',
    label: '超短叠加'
  },
  {
    key: 'mdRequireUltra',
    label: '须满足超短 30m 突破',
    hint: '开启后须同时满足 30m 跨日桶柱内突破；ul* 参数沿用超短 Tab 设置',
    type: 'switch'
  }
];

var LONG_DEFAULTS = {
  lgMinAmountWan: 5000,
  lgPrevYears: 2,
  lgRequireCurrentBreakout: false,
  lgRequireYearMacd: false,
  lgRequireMonthMacd: false,
  lgRequireYearGoldenCross: false,
  lgRequireUltra: true
};

var LONG_SCHEMA = [
  {
    key: 'lgMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    key: 'lgPrevYears',
    label: '基准背景年数',
    hint: '自然年，信号年之前完整年数',
    type: 'slider',
    min: 1,
    max: 4,
    step: 1,
    unit: '年'
  },
  {
    type: 'section',
    label: '突破K'
  },
  {
    key: 'lgRequireCurrentBreakout',
    label: '当前月K须为突破K',
    hint: '关闭则本年任一月K满足即可',
    type: 'switch'
  },
  {
    type: 'section',
    label: 'MACD（可选）'
  },
  {
    key: 'lgRequireYearMacd',
    label: '年 MACD>0',
    type: 'switch'
  },
  {
    key: 'lgRequireMonthMacd',
    label: '月 MACD>0',
    type: 'switch'
  },
  {
    key: 'lgRequireYearGoldenCross',
    label: '年K MACD 金叉',
    type: 'switch'
  },
  {
    type: 'section',
    label: '超短叠加'
  },
  {
    key: 'lgRequireUltra',
    label: '须满足超短 30m 突破',
    hint: '开启后须同时满足 30m 跨日桶柱内突破；ul* 参数沿用超短 Tab 设置',
    type: 'switch'
  }
];

var WAVECONVEX_SCHEMA = [
  {
    type: 'section',
    label: '全局门控',
    hint: '默认均关闭；开启后须全部满足'
  },
  {
    key: 'wcvEnableAllYangGate',
    label: '全阳门',
    hint: '日、周、月最后一根K均为阳线（close>open），须全部满足',
    type: 'switch'
  },
  {
    key: 'wcvEnableMin30Gate',
    label: 'Min30 突破门',
    hint: '须满足 30m 跨日桶柱内突破（超短引擎默认参数）',
    type: 'switch'
  },
  {
    key: 'wcvMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）；0=不启用',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    type: 'section',
    label: '突破线',
    hint: '均未开启时用形态默认（凸=末阳中位，凹=末阳顶）；多开时优先 底>中位>顶'
  },
  {
    key: 'wcvEnableLastHighBreak',
    label: '末阳顶突破',
    type: 'switch'
  },
  {
    key: 'wcvEnableLastMedianBreak',
    label: '末阳中位突破',
    type: 'switch'
  },
  {
    key: 'wcvEnableLastLowBreak',
    label: '末阳底突破',
    type: 'switch'
  },
  {
    type: 'section',
    label: '凸波段档位',
    hint: '末两波段：末阳high>前波段末阳high；同档K边沿突破'
  },
  {
    key: 'wcvEnableDay',
    label: '日 K 波段',
    type: 'switch'
  },
  {
    key: 'wcvEnableWeek',
    label: '周 K 波段',
    type: 'switch'
  },
  {
    key: 'wcvEnableMonth',
    label: '月 K 波段',
    type: 'switch'
  },
  {
    type: 'section',
    label: '波段回溯',
    hint: '各周期向前扫描完整阳波段的最大 K 数'
  },
  {
    key: 'wcvLookbackDay',
    label: '日 K lookback',
    type: 'slider',
    min: 20,
    max: 120,
    step: 5,
    unit: '根'
  },
  {
    key: 'wcvLookbackWeek',
    label: '周 K lookback',
    type: 'slider',
    min: 20,
    max: 104,
    step: 4,
    unit: '根'
  },
  {
    key: 'wcvLookbackMonth',
    label: '月 K lookback',
    type: 'slider',
    min: 12,
    max: 60,
    step: 4,
    unit: '根'
  }
];

var WAVECONCAVE_SCHEMA = [
  {
    type: 'section',
    label: '全局门控',
    hint: '默认均关闭；开启后须全部满足'
  },
  {
    key: 'wccEnableAllYangGate',
    label: '全阳门',
    hint: '日、周、月最后一根K均为阳线（close>open），须全部满足',
    type: 'switch'
  },
  {
    key: 'wccEnableMin30Gate',
    label: 'Min30 突破门',
    hint: '须满足 30m 跨日桶柱内突破（超短引擎默认参数）',
    type: 'switch'
  },
  {
    key: 'wccMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）；0=不启用',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    type: 'section',
    label: '突破线',
    hint: '均未开启时用形态默认（凸=末阳中位，凹=末阳顶）；多开时优先 底>中位>顶'
  },
  {
    key: 'wccEnableLastHighBreak',
    label: '末阳顶突破',
    type: 'switch'
  },
  {
    key: 'wccEnableLastMedianBreak',
    label: '末阳中位突破',
    type: 'switch'
  },
  {
    key: 'wccEnableLastLowBreak',
    label: '末阳底突破',
    type: 'switch'
  },
  {
    type: 'section',
    label: '凹波段档位',
    hint: '末两波段：末阳high≤前波段末阳high；同档K边沿突破'
  },
  {
    key: 'wccEnableDay',
    label: '日 K 波段',
    type: 'switch'
  },
  {
    key: 'wccEnableWeek',
    label: '周 K 波段',
    type: 'switch'
  },
  {
    key: 'wccEnableMonth',
    label: '月 K 波段',
    type: 'switch'
  },
  {
    type: 'section',
    label: '波段回溯',
    hint: '各周期向前扫描完整阳波段的最大 K 数'
  },
  {
    key: 'wccLookbackDay',
    label: '日 K lookback',
    type: 'slider',
    min: 20,
    max: 120,
    step: 5,
    unit: '根'
  },
  {
    key: 'wccLookbackWeek',
    label: '周 K lookback',
    type: 'slider',
    min: 20,
    max: 104,
    step: 4,
    unit: '根'
  },
  {
    key: 'wccLookbackMonth',
    label: '月 K lookback',
    type: 'slider',
    min: 12,
    max: 60,
    step: 4,
    unit: '根'
  }
];

var WAVECONVEXDAY_SCHEMA = [
  {
    type: 'section',
    label: '全局门控',
    hint: '默认均关闭；开启后须全部满足'
  },
  {
    key: 'wcvdEnableAllYangGate',
    label: '全阳门',
    hint: '日、周、月最后一根K均为阳线（close>open），须全部满足',
    type: 'switch'
  },
  {
    key: 'wcvdEnableMin30Gate',
    label: 'Min30 突破门',
    hint: '须满足 30m 跨日桶柱内突破（超短引擎默认参数）',
    type: 'switch'
  },
  {
    key: 'wcvdMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）；0=不启用',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    type: 'section',
    label: '突破线',
    hint: '均未开启时用形态默认（凸=末阳中位，凹=末阳顶）；多开时优先 底>中位>顶'
  },
  {
    key: 'wcvdEnableLastHighBreak',
    label: '末阳顶突破',
    type: 'switch'
  },
  {
    key: 'wcvdEnableLastMedianBreak',
    label: '末阳中位突破',
    type: 'switch'
  },
  {
    key: 'wcvdEnableLastLowBreak',
    label: '末阳底突破',
    type: 'switch'
  },
  {
    type: 'section',
    label: '凸波段档位',
    hint: '末两波段：末阳high>前波段末阳high；统一日K边沿突破'
  },
  {
    key: 'wcvdEnableDay',
    label: '日 K 波段',
    type: 'switch'
  },
  {
    key: 'wcvdEnableWeek',
    label: '周 K 波段',
    type: 'switch'
  },
  {
    key: 'wcvdEnableMonth',
    label: '月 K 波段',
    type: 'switch'
  },
  {
    type: 'section',
    label: '波段回溯',
    hint: '各周期向前扫描完整阳波段的最大 K 数'
  },
  {
    key: 'wcvdLookbackDay',
    label: '日 K lookback',
    type: 'slider',
    min: 20,
    max: 120,
    step: 5,
    unit: '根'
  },
  {
    key: 'wcvdLookbackWeek',
    label: '周 K lookback',
    type: 'slider',
    min: 20,
    max: 104,
    step: 4,
    unit: '根'
  },
  {
    key: 'wcvdLookbackMonth',
    label: '月 K lookback',
    type: 'slider',
    min: 12,
    max: 60,
    step: 4,
    unit: '根'
  }
];

var WAVECONCAVEDAY_SCHEMA = [
  {
    type: 'section',
    label: '全局门控',
    hint: '默认均关闭；开启后须全部满足'
  },
  {
    key: 'wccdEnableAllYangGate',
    label: '全阳门',
    hint: '日、周、月最后一根K均为阳线（close>open），须全部满足',
    type: 'switch'
  },
  {
    key: 'wccdEnableMin30Gate',
    label: 'Min30 突破门',
    hint: '须满足 30m 跨日桶柱内突破（超短引擎默认参数）',
    type: 'switch'
  },
  {
    key: 'wccdMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）；0=不启用',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    type: 'section',
    label: '突破线',
    hint: '均未开启时用形态默认（凸=末阳中位，凹=末阳顶）；多开时优先 底>中位>顶'
  },
  {
    key: 'wccdEnableLastHighBreak',
    label: '末阳顶突破',
    type: 'switch'
  },
  {
    key: 'wccdEnableLastMedianBreak',
    label: '末阳中位突破',
    type: 'switch'
  },
  {
    key: 'wccdEnableLastLowBreak',
    label: '末阳底突破',
    type: 'switch'
  },
  {
    type: 'section',
    label: '凹波段档位',
    hint: '末两波段：末阳high≤前波段末阳high；统一日K边沿突破'
  },
  {
    key: 'wccdEnableDay',
    label: '日 K 波段',
    type: 'switch'
  },
  {
    key: 'wccdEnableWeek',
    label: '周 K 波段',
    type: 'switch'
  },
  {
    key: 'wccdEnableMonth',
    label: '月 K 波段',
    type: 'switch'
  },
  {
    type: 'section',
    label: '波段回溯',
    hint: '各周期向前扫描完整阳波段的最大 K 数'
  },
  {
    key: 'wccdLookbackDay',
    label: '日 K lookback',
    type: 'slider',
    min: 20,
    max: 120,
    step: 5,
    unit: '根'
  },
  {
    key: 'wccdLookbackWeek',
    label: '周 K lookback',
    type: 'slider',
    min: 20,
    max: 104,
    step: 4,
    unit: '根'
  },
  {
    key: 'wccdLookbackMonth',
    label: '月 K lookback',
    type: 'slider',
    min: 12,
    max: 60,
    step: 4,
    unit: '根'
  }
];

var CASCADEWAVECONVEX_SCHEMA = [
  {
    type: 'section',
    label: '全局门控',
    hint: '末阳低门默认开启；全阳门/Min30 默认关闭'
  },
  {
    key: 'cwcvEnableBandLastYangLowGate',
    label: '末阳低门',
    hint: '日、周、月收盘价须全部 > 各档末完整波段末阳 K 的 low',
    type: 'switch'
  },
  {
    key: 'cwcvEnableYangBandTrendGate',
    label: '趋势门',
    hint: '按信号档：peak收盘抬升时现价>次末peak高，否则现价>末peak高',
    type: 'switch'
  },
  {
    key: 'cwcvEnableAllYangGate',
    label: '全阳门',
    hint: '日、周、月最后一根K均为阳线（close>open），须全部满足',
    type: 'switch'
  },
  {
    key: 'cwcvEnableMin30Gate',
    label: 'Min30 突破门',
    hint: '须满足 30m 跨日桶柱内突破（超短引擎默认参数）',
    type: 'switch'
  },
  {
    key: 'cwcvMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）；0=不启用',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    type: 'section',
    label: '突破线',
    hint: '凸=次波段末阳顶，凹=末波段末阳顶（按子策略形态固定）'
  },
  {
    key: 'cwcvEnablePrevBandBreak',
    label: '前波段末阳顶',
    hint: '开启后突破线可取倒数第2波段末阳high（已固定按形态，不再生效）',
    type: 'switch'
  },
  {
    type: 'section',
    label: '级联凸波段档位',
    hint: '末两波段凸形态 + 级联MACD柱>0；同档K边沿突破'
  },
  {
    key: 'cwcvEnableDay',
    label: '日 K 波段',
    type: 'switch'
  },
  {
    key: 'cwcvEnableWeek',
    label: '周 K 波段',
    type: 'switch'
  },
  {
    key: 'cwcvEnableMonth',
    label: '月 K 波段',
    type: 'switch'
  },
  {
    type: 'section',
    label: '波段回溯',
    hint: '各周期向前扫描完整阳波段的最大 K 数'
  },
  {
    key: 'cwcvLookbackDay',
    label: '日 K lookback',
    type: 'slider',
    min: 20,
    max: 120,
    step: 5,
    unit: '根'
  },
  {
    key: 'cwcvLookbackWeek',
    label: '周 K lookback',
    type: 'slider',
    min: 20,
    max: 104,
    step: 4,
    unit: '根'
  },
  {
    key: 'cwcvLookbackMonth',
    label: '月 K lookback',
    type: 'slider',
    min: 12,
    max: 60,
    step: 4,
    unit: '根'
  },
  {
    key: 'cwcvLookbackYear',
    label: '年 K lookback',
    hint: '级联MACD月→年档回溯',
    type: 'slider',
    min: 8,
    max: 40,
    step: 2,
    unit: '根'
  }
];

var CASCADEWAVECONCAVE_SCHEMA = [
  {
    type: 'section',
    label: '全局门控',
    hint: '末阳低门默认开启；全阳门/Min30 默认关闭'
  },
  {
    key: 'cwcavEnableBandLastYangLowGate',
    label: '末阳低门',
    hint: '日、周、月收盘价须全部 > 各档末完整波段末阳 K 的 low',
    type: 'switch'
  },
  {
    key: 'cwcavEnableYangBandTrendGate',
    label: '趋势门',
    hint: '按信号档：peak收盘抬升时现价>次末peak高，否则现价>末peak高',
    type: 'switch'
  },
  {
    key: 'cwcavEnableAllYangGate',
    label: '全阳门',
    hint: '日、周、月最后一根K均为阳线（close>open），须全部满足',
    type: 'switch'
  },
  {
    key: 'cwcavEnableMin30Gate',
    label: 'Min30 突破门',
    hint: '须满足 30m 跨日桶柱内突破（超短引擎默认参数）',
    type: 'switch'
  },
  {
    key: 'cwcavMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）；0=不启用',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  },
  {
    type: 'section',
    label: '突破线',
    hint: '凸=次波段末阳顶，凹=末波段末阳顶（按子策略形态固定）'
  },
  {
    key: 'cwcavEnablePrevBandBreak',
    label: '前波段末阳顶',
    hint: '开启后突破线可取倒数第2波段末阳high（已固定按形态，不再生效）',
    type: 'switch'
  },
  {
    type: 'section',
    label: '级联凹波段档位',
    hint: '末两波段凹形态 + 级联MACD柱>0；同档K边沿突破'
  },
  {
    key: 'cwcavEnableDay',
    label: '日 K 波段',
    type: 'switch'
  },
  {
    key: 'cwcavEnableWeek',
    label: '周 K 波段',
    type: 'switch'
  },
  {
    key: 'cwcavEnableMonth',
    label: '月 K 波段',
    type: 'switch'
  },
  {
    type: 'section',
    label: '波段回溯',
    hint: '各周期向前扫描完整阳波段的最大 K 数'
  },
  {
    key: 'cwcavLookbackDay',
    label: '日 K lookback',
    type: 'slider',
    min: 20,
    max: 120,
    step: 5,
    unit: '根'
  },
  {
    key: 'cwcavLookbackWeek',
    label: '周 K lookback',
    type: 'slider',
    min: 20,
    max: 104,
    step: 4,
    unit: '根'
  },
  {
    key: 'cwcavLookbackMonth',
    label: '月 K lookback',
    type: 'slider',
    min: 12,
    max: 60,
    step: 4,
    unit: '根'
  },
  {
    key: 'cwcavLookbackYear',
    label: '年 K lookback',
    hint: '级联MACD月→年档回溯',
    type: 'slider',
    min: 8,
    max: 40,
    step: 2,
    unit: '根'
  }
];

var CASCADEWAVECONVEXDAY_SCHEMA = [
  { type: 'section', label: '全局门控', hint: '末阳低门默认开启；全阳门/Min30 默认关闭' },
  { key: 'cwcvdEnableBandLastYangLowGate', label: '末阳低门', hint: '日、周、月收盘价须全部 > 各档末完整波段末阳 K 的 low', type: 'switch' },
  { key: 'cwcvdEnableYangBandTrendGate', label: '趋势门', hint: '按信号档：peak收盘抬升时现价>次末peak高，否则现价>末peak高', type: 'switch' },
  { key: 'cwcvdEnableAllYangGate', label: '全阳门', hint: '日、周、月最后一根K均为阳线', type: 'switch' },
  { key: 'cwcvdEnableMin30Gate', label: 'Min30 突破门', type: 'switch' },
  { key: 'cwcvdMinAmountWan', label: '最低成交额', hint: '近6日日均成交额（万）；0=不启用', type: 'slider', min: 0, max: 10000, step: 500, unit: '万' },
  { type: 'section', label: '突破线', hint: '固定末波段末阳顶；可选 OR 前波段末阳顶' },
  { key: 'cwcvdEnablePrevBandBreak', label: '前波段末阳顶', type: 'switch' },
  { type: 'section', label: '级联凸波段档位', hint: '末两波段凸形态 + 级联MACD柱>0；统一日K边沿突破' },
  { key: 'cwcvdEnableDay', label: '日 K 波段', type: 'switch' },
  { key: 'cwcvdEnableWeek', label: '周 K 波段', type: 'switch' },
  { key: 'cwcvdEnableMonth', label: '月 K 波段', type: 'switch' },
  { type: 'section', label: '波段回溯' },
  { key: 'cwcvdLookbackDay', label: '日 K lookback', type: 'slider', min: 20, max: 120, step: 5, unit: '根' },
  { key: 'cwcvdLookbackWeek', label: '周 K lookback', type: 'slider', min: 20, max: 104, step: 4, unit: '根' },
  { key: 'cwcvdLookbackMonth', label: '月 K lookback', type: 'slider', min: 12, max: 60, step: 4, unit: '根' },
  { key: 'cwcvdLookbackYear', label: '年 K lookback', hint: '级联MACD月→年档回溯', type: 'slider', min: 8, max: 40, step: 2, unit: '根' }
];

var CASCADEWAVECONCAVEDAY_SCHEMA = [
  { type: 'section', label: '全局门控', hint: '末阳低门默认开启；全阳门/Min30 默认关闭' },
  { key: 'cwcadEnableBandLastYangLowGate', label: '末阳低门', hint: '日、周、月收盘价须全部 > 各档末完整波段末阳 K 的 low', type: 'switch' },
  { key: 'cwcadEnableYangBandTrendGate', label: '趋势门', hint: '按信号档：peak收盘抬升时现价>次末peak高，否则现价>末peak高', type: 'switch' },
  { key: 'cwcadEnableAllYangGate', label: '全阳门', hint: '日、周、月最后一根K均为阳线', type: 'switch' },
  { key: 'cwcadEnableMin30Gate', label: 'Min30 突破门', type: 'switch' },
  { key: 'cwcadMinAmountWan', label: '最低成交额', hint: '近6日日均成交额（万）；0=不启用', type: 'slider', min: 0, max: 10000, step: 500, unit: '万' },
  { type: 'section', label: '突破线', hint: '固定末波段末阳顶；可选 OR 前波段末阳顶' },
  { key: 'cwcadEnablePrevBandBreak', label: '前波段末阳顶', type: 'switch' },
  { type: 'section', label: '级联凹波段档位', hint: '末两波段凹形态 + 级联MACD柱>0；统一日K边沿突破' },
  { key: 'cwcadEnableDay', label: '日 K 波段', type: 'switch' },
  { key: 'cwcadEnableWeek', label: '周 K 波段', type: 'switch' },
  { key: 'cwcadEnableMonth', label: '月 K 波段', type: 'switch' },
  { type: 'section', label: '波段回溯' },
  { key: 'cwcadLookbackDay', label: '日 K lookback', type: 'slider', min: 20, max: 120, step: 5, unit: '根' },
  { key: 'cwcadLookbackWeek', label: '周 K lookback', type: 'slider', min: 20, max: 104, step: 4, unit: '根' },
  { key: 'cwcadLookbackMonth', label: '月 K lookback', type: 'slider', min: 12, max: 60, step: 4, unit: '根' },
  { key: 'cwcadLookbackYear', label: '年 K lookback', hint: '级联MACD月→年档回溯', type: 'slider', min: 8, max: 40, step: 2, unit: '根' }
];

var SCHEMA_BY_STRATEGY = {
  ultra: ULTRA_SCHEMA,
  ultragc: ULTRAGC_SCHEMA,
  min60wavecc: M60WCCB_SCHEMA,
  daywavecc: DWCCB_SCHEMA,
  weekwavecc: WWCCB_SCHEMA,
  monthwavecc: MWCCB_SCHEMA,
  trend: TREND_SCHEMA,
  medium: MEDIUM_SCHEMA,
  long: LONG_SCHEMA,
  resonance: RESONANCE_SCHEMA,
  rebound: REBOUND_SCHEMA,
  nrf: [],
  retest: RETEST_SCHEMA,
  gc2: GC2_SCHEMA,
  dc2: DC2_SCHEMA,
  cascade: CASCADE_SCHEMA,
  macedge: MACEDGE_SCHEMA,
  cascadewaveconvex: CASCADEWAVECONVEX_SCHEMA,
  cascadewaveconcave: CASCADEWAVECONCAVE_SCHEMA,
  cascadewaveconvexday: CASCADEWAVECONVEXDAY_SCHEMA,
  cascadewaveconcaveday: CASCADEWAVECONCAVEDAY_SCHEMA,
  cascadewaveShort: CASCADEWAVE_SHORT_SCHEMA,
  cascadewaveMedium: CASCADEWAVE_MEDIUM_SCHEMA,
  cascadewaveLong: CASCADEWAVE_LONG_SCHEMA,
  macdgcwh: MGCWH_SCHEMA,
  macdgcwhShort: MGCWH_SCHEMA,
  macdgcwhMedium: MGCWH_SCHEMA,
  macdgcwhLong: MGCWH_SCHEMA,
  macdgcwhu: MGCWHU_SCHEMA
};

var LADDER_TIER_SHORT = 'short';
var LADDER_TIER_MEDIUM = 'medium';
var LADDER_TIER_LONG = 'long';

var NRF_TIERS = [
  { id: LADDER_TIER_SHORT, apiId: 'trend', label: '日K', title: '日K跨周桶' },
  { id: LADDER_TIER_MEDIUM, apiId: 'medium', label: '周K', title: '周K跨月桶' },
  { id: LADDER_TIER_LONG, apiId: 'long', label: '月K', title: '月K跨年桶' }
];

function nrfTierFormDefaults(tierKey) {
  var base = tierKey === LADDER_TIER_MEDIUM ? MEDIUM_DEFAULTS
    : tierKey === LADDER_TIER_LONG ? LONG_DEFAULTS : TREND_DEFAULTS;
  var out = clone(base);
  if (tierKey === LADDER_TIER_SHORT) {
    out.trRequireUltra = false;
    out.trRequireCurrentBreakout = true;
    out.trRequireMonthMacd = false;
    out.trRequireWeekMacd = false;
    out.trRequireDayMacd = false;
    out.trRequireWeekGoldenCross = false;
  } else if (tierKey === LADDER_TIER_MEDIUM) {
    out.mdRequireUltra = false;
    out.mdRequireCurrentBreakout = true;
    out.mdRequireMonthMacd = false;
    out.mdRequireYearMacd = false;
    out.mdRequireMonthGoldenCross = false;
  } else {
    out.lgRequireUltra = false;
    out.lgRequireCurrentBreakout = true;
    out.lgRequireYearMacd = false;
    out.lgRequireMonthMacd = false;
    out.lgRequireYearGoldenCross = false;
  }
  return out;
}

function buildNrfBundleDefaults() {
  return {
    activeTier: LADDER_TIER_SHORT,
    short: nrfTierFormDefaults(LADDER_TIER_SHORT),
    medium: nrfTierFormDefaults(LADDER_TIER_MEDIUM),
    long: nrfTierFormDefaults(LADDER_TIER_LONG)
  };
}

var NRF_BUNDLE_DEFAULTS = buildNrfBundleDefaults();

function migrateLegacyNrfBundle(raw) {
  if (!raw || typeof raw !== 'object') return null;
  if (raw.activeTier && raw.short) return raw;
  var def = buildNrfBundleDefaults();
  var tier = LADDER_TIER_SHORT;
  if (raw.nrfLadderMode === 'week') tier = LADDER_TIER_MEDIUM;
  else if (raw.nrfLadderMode === 'month') tier = LADDER_TIER_LONG;
  return {
    activeTier: tier,
    short: normalizeTierParams(LADDER_TIER_SHORT, raw.short || raw.day || def.short),
    medium: normalizeTierParams(LADDER_TIER_MEDIUM, raw.medium || raw.week || def.medium),
    long: normalizeTierParams(LADDER_TIER_LONG, raw.long || raw.month || def.long)
  };
}

function loadNrfBundleRaw() {
  try {
    var saved = wx.getStorageSync(storageKey('nrf'));
    if (!saved || !saved.short) {
      var legacy = wx.getStorageSync(STORAGE_PREFIX + 'ladder');
      if (legacy && legacy.short) {
        saved = legacy;
      } else {
        saved = migrateLegacyNrfBundle(saved) || migrateLadderFromLegacyStorages();
      }
      if (saved && saved.short) {
        wx.setStorageSync(storageKey('nrf'), normalizeLadderBundle(saved));
      }
    }
    return normalizeLadderBundle(saved || buildNrfBundleDefaults());
  } catch (e) {
    return buildNrfBundleDefaults();
  }
}

function sanitizeNrfTierParams(tierKey, params) {
  var out = clone(params);
  if (tierKey === LADDER_TIER_SHORT) {
    out.trRequireMonthMacd = false;
    out.trRequireWeekMacd = false;
    out.trRequireDayMacd = false;
    out.trRequireWeekGoldenCross = false;
  } else if (tierKey === LADDER_TIER_MEDIUM) {
    out.mdRequireMonthMacd = false;
    out.mdRequireYearMacd = false;
    out.mdRequireMonthGoldenCross = false;
  } else {
    out.lgRequireYearMacd = false;
    out.lgRequireMonthMacd = false;
    out.lgRequireYearGoldenCross = false;
  }
  return out;
}

function loadNrfTierForm(tier) {
  var bundle = loadNrfBundleRaw();
  var key = tier || bundle.activeTier || LADDER_TIER_SHORT;
  return sanitizeNrfTierParams(key, clone(bundle[key] || bundle.short));
}

function saveNrfTierForm(tier, form, setActive) {
  var bundle = loadNrfBundleRaw();
  var tierKey = (form && form.nrfActiveTier) || tier || bundle.activeTier || LADDER_TIER_SHORT;
  if (setActive) {
    bundle.activeTier = tierKey;
  }
  var tierForm = clone(form || {});
  delete tierForm.nrfActiveTier;
  var normalized = sanitizeNrfTierParams(tierKey, normalizeTierParams(tierKey, tierForm));
  bundle[tierKey] = normalized;
  wx.setStorageSync(storageKey('nrf'), bundle);
  return bundle;
}

function resetNrfTier(tier) {
  var bundle = loadNrfBundleRaw();
  var tierKey = tier || bundle.activeTier || LADDER_TIER_SHORT;
  var def = buildNrfBundleDefaults();
  bundle[tierKey] = clone(def[tierKey]);
  wx.setStorageSync(storageKey('nrf'), bundle);
  return bundle;
}

function getFrictionlessGateHeadSchema() {
  return [{
    type: 'section',
    label: '双低支撑门',
    hint: '固定：现价 > max(前K.low, 前K2.low)，日、周、月须全部满足'
  }, {
    type: 'section',
    label: '无阻力 MACD 门',
    hint: '固定：日、周、月须同时满足 MACD>0 或 (MACD≤0 且 当前K.close>前K.high)'
  }, {
    type: 'section',
    label: 'MACD 死叉高门',
    hint: '固定：日、周、月须同时满足 MACD>0 或 (MACD≤0 且 本档 close>最近 MACD 死叉 K.high)'
  }, {
    type: 'section',
    label: 'MACD 交叉 low 门',
    hint: '固定：现价 > 最近一根 MACD 交叉 K 的 low（金叉或死叉），日、周、月须全部满足'
  }, {
    type: 'section',
    label: '高点递进门',
    hint: '固定：最后一根 high>倒数第二根 high，或倒数第二根 high>倒数第三根 high，日、周、月须全部满足'
  }, {
    type: 'section',
    label: '全阳门',
    hint: '固定：日、周、月、年最后一根K均为阳线（close>open），须全部满足'
  }];
}

var NRF_TIER_PICKER = [
  { value: LADDER_TIER_SHORT, label: '日 K · 跨周桶' },
  { value: LADDER_TIER_MEDIUM, label: '周 K · 跨月桶' },
  { value: LADDER_TIER_LONG, label: '月 K · 跨年桶' }
];

var NRF_IN_BAR_HINT = '背景桶内任一根强K可为基准；中间K收盘<=ref.high；'
  + '信号K须 close>前K.high、close>=ref.low 且 min(low,前收)<=ref.high';

function nrfTierBodySchema(tierId) {
  if (tierId === LADDER_TIER_MEDIUM || tierApiId(tierId) === 'medium') {
    return [{
      type: 'section',
      label: '档位参数',
      hint: '近6日日均成交额；信号月之前的完整自然月数'
    }, {
      key: 'mdMinAmountWan',
      label: '最低成交额',
      hint: '近6日日均成交额（万）',
      type: 'slider',
      min: 1000,
      max: 10000,
      step: 500,
      unit: '万'
    }, {
      key: 'mdPrevMonths',
      label: '基准背景月数',
      hint: '自然月，信号月之前的完整月数',
      type: 'slider',
      min: 1,
      max: 4,
      step: 1,
      unit: '月'
    }, {
      type: 'section',
      label: '突破 K',
      hint: '开启则须最新一根操作周期 K 满足信号条件；关闭则本周期任一根满足即可'
    }, {
      key: 'mdRequireCurrentBreakout',
      label: '当前 K 须为突破 K',
      type: 'switch'
    }, {
      type: 'section',
      label: '超短叠加',
      hint: '开启后须同时满足 30m 跨日桶柱内突破；ul* 参数沿用超短 Tab 设置'
    }, {
      key: 'mdRequireUltra',
      label: '须满足超短 30m 突破',
      type: 'switch'
    }];
  }
  if (tierId === LADDER_TIER_LONG || tierApiId(tierId) === 'long') {
    return [{
      type: 'section',
      label: '档位参数',
      hint: '近6日日均成交额；信号年之前的完整自然年数'
    }, {
      key: 'lgMinAmountWan',
      label: '最低成交额',
      hint: '近6日日均成交额（万）',
      type: 'slider',
      min: 1000,
      max: 10000,
      step: 500,
      unit: '万'
    }, {
      key: 'lgPrevYears',
      label: '基准背景年数',
      hint: '自然年，信号年之前的完整年数',
      type: 'slider',
      min: 1,
      max: 4,
      step: 1,
      unit: '年'
    }, {
      type: 'section',
      label: '突破 K',
      hint: '开启则须最新一根操作周期 K 满足信号条件；关闭则本周期任一根满足即可'
    }, {
      key: 'lgRequireCurrentBreakout',
      label: '当前 K 须为突破 K',
      type: 'switch'
    }, {
      type: 'section',
      label: '超短叠加',
      hint: '开启后须同时满足 30m 跨日桶柱内突破；ul* 参数沿用超短 Tab 设置'
    }, {
      key: 'lgRequireUltra',
      label: '须满足超短 30m 突破',
      type: 'switch'
    }];
  }
  return [{
    type: 'section',
    label: '档位参数',
    hint: '近6日日均成交额；信号周之前的完整自然周数'
  }, {
    key: 'trMinAmountWan',
    label: '最低成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 1000,
    max: 10000,
    step: 500,
    unit: '万'
  }, {
    key: 'trPrevWeeks',
    label: '基准背景周数',
    hint: '自然周，信号周之前的完整周数',
    type: 'slider',
    min: 1,
    max: 4,
      step: 1,
      unit: '周'
    }, {
      type: 'section',
      label: '突破 K',
      hint: '开启则须最新一根操作周期 K 满足信号条件；关闭则本周期任一根满足即可'
    }, {
      key: 'trRequireCurrentBreakout',
      label: '当前 K 须为突破 K',
      type: 'switch'
    }, {
      type: 'section',
      label: '超短叠加',
      hint: '开启后须同时满足 30m 跨日桶柱内突破；ul* 参数沿用超短 Tab 设置'
    }, {
      key: 'trRequireUltra',
    label: '须满足超短 30m 突破',
    type: 'switch'
  }];
}

function getNrfPanelSchema(tierId) {
  var tier = tierId || LADDER_TIER_SHORT;
  return getFrictionlessGateHeadSchema().concat([{
    type: 'section',
    label: '运行档位',
    hint: '一次只扫描一档；在更大周期背景桶内找强K基准，信号K须柱内突破'
  }, {
    key: 'nrfActiveTier',
    label: '当前档位',
    type: 'picker',
    options: NRF_TIER_PICKER
  }, {
    type: 'section',
    label: '跨周期柱内突破',
    hint: NRF_IN_BAR_HINT
  }]).concat(nrfTierBodySchema(tier));
}

var FRICTIONLESS_GATE_STRATEGIES = { ultra: true, nrf: true };

function schemaWithFrictionlessGate(strategyId, schema) {
  if (!FRICTIONLESS_GATE_STRATEGIES[strategyId]) {
    return schema || [];
  }
  if (strategyId === 'nrf') {
    return getNrfPanelSchema(LADDER_TIER_SHORT);
  }
  var body = schema || [];
  if (body[0] && body[0].label === '双低支撑门') {
    while (body[0] && (body[0].label === '双低支撑门' || body[0].label === '无阻力 MACD 门'
        || body[0].label === 'MACD 死叉高门' || body[0].label === 'MACD 交叉 low 门'
        || body[0].label === '高点递进门'
        || body[0].label === '全阳门')) {
      body = body.slice(1);
    }
  } else if (body[0] && body[0].label === '无阻力 MACD 门') {
    body = body.slice(1);
  }
  return getFrictionlessGateHeadSchema().concat(body);
}

function nrfBreakoutModeLabel(tier, p) {
  if (tier === LADDER_TIER_MEDIUM || tierApiId(tier) === 'medium') {
    return p.mdRequireCurrentBreakout !== false ? '当前K突破' : '周期内突破';
  }
  if (tier === LADDER_TIER_LONG || tierApiId(tier) === 'long') {
    return p.lgRequireCurrentBreakout !== false ? '当前K突破' : '周期内突破';
  }
  return p.trRequireCurrentBreakout !== false ? '当前K突破' : '周期内突破';
}

function formatNrfTierSummary(tier, p) {
  var breakoutPart = ' · ' + nrfBreakoutModeLabel(tier, p);
  if (tier === LADDER_TIER_MEDIUM || tierApiId(tier) === 'medium') {
    return (p.mdMinAmountWan != null ? p.mdMinAmountWan : 5000) + '万 · '
      + (p.mdPrevMonths != null ? p.mdPrevMonths : 2) + '月基准'
      + breakoutPart
      + (p.mdRequireUltra ? ' · +min30' : '');
  }
  if (tier === LADDER_TIER_LONG || tierApiId(tier) === 'long') {
    return (p.lgMinAmountWan != null ? p.lgMinAmountWan : 5000) + '万 · '
      + (p.lgPrevYears != null ? p.lgPrevYears : 2) + '年基准'
      + breakoutPart
      + (p.lgRequireUltra ? ' · +min30' : '');
  }
  return (p.trMinAmountWan != null ? p.trMinAmountWan : 5000) + '万 · '
    + (p.trPrevWeeks != null ? p.trPrevWeeks : 2) + '周基准'
    + breakoutPart
    + (p.trRequireUltra ? ' · +min30' : '');
}

function getTierListFor(strategyId) {
  return normalizeStrategyId(strategyId) === 'nrf' ? NRF_TIERS : [];
}

function loadTierFormFor(strategyId, tier) {
  return loadNrfTierForm(tier);
}

function saveTierFormFor(strategyId, tier, form, setActive) {
  return saveNrfTierForm(tier, form, setActive);
}

function resetTierFor(strategyId, tier) {
  return resetNrfTier(tier);
}

function getPanelSchema(strategyId, tier) {
  if (normalizeStrategyId(strategyId) === 'nrf') return getNrfPanelSchema(tier);
  return getSchema(strategyId);
}

var DEFAULTS_BY_STRATEGY = {
  ultra: ULTRA_DEFAULTS,
  ultragc: ULTRAGC_DEFAULTS,
  min60wavecc: M60WCCB_DEFAULTS,
  daywavecc: DWCCB_DEFAULTS,
  weekwavecc: WWCCB_DEFAULTS,
  monthwavecc: MWCCB_DEFAULTS,
  trend: TREND_DEFAULTS,
  medium: MEDIUM_DEFAULTS,
  long: LONG_DEFAULTS,
  resonance: RESONANCE_DEFAULTS,
  rebound: REBOUND_DEFAULTS,
  nrf: NRF_BUNDLE_DEFAULTS,
  retest: RETEST_DEFAULTS,
  gc2: GC2_DEFAULTS,
  dc2: DC2_DEFAULTS,
  cascade: CASCADE_DEFAULTS,
  macedge: MACEDGE_DEFAULTS,
  cascadewaveconvex: CASCADEWAVECONVEX_DEFAULTS,
  cascadewaveconcave: CASCADEWAVECONCAVE_DEFAULTS,
  cascadewaveconvexday: CASCADEWAVECONVEXDAY_DEFAULTS,
  cascadewaveconcaveday: CASCADEWAVECONCAVEDAY_DEFAULTS,
  cascadewaveShort: CASCADEWAVE_BUNDLE_DEFAULTS,
  cascadewaveMedium: CASCADEWAVE_BUNDLE_DEFAULTS,
  cascadewaveLong: CASCADEWAVE_BUNDLE_DEFAULTS,
  macdgcwh: MGCWH_DEFAULTS,
  macdgcwhShort: MGCWH_DEFAULTS,
  macdgcwhMedium: MGCWH_DEFAULTS,
  macdgcwhLong: MGCWH_DEFAULTS,
  macdgcwhu: MGCWHU_DEFAULTS
};

var TIER_PICKER = null;
var RESONANCE_TIER_PICKER = RESONANCE_SCHEMA.find(function (f) { return f.key === 'cTierMin'; });
var REBOUND_TIER_PICKER = REBOUND_SCHEMA.find(function (f) { return f.key === 'rTierMin'; });
var RETEST_TIER_PICKER = RETEST_SCHEMA.find(function (f) { return f.key === 'tTierMin'; });
var GC2_TIER_PICKER = GC2_SCHEMA.find(function (f) { return f.key === 'g2TierMin'; });
var DC2_TIER_PICKER = DC2_SCHEMA.find(function (f) { return f.key === 'd2TierMin'; });

function tierApiId(tier) {
  if (tier === LADDER_TIER_MEDIUM) return 'medium';
  if (tier === LADDER_TIER_LONG) return 'long';
  return 'trend';
}

function resolveApiStrategyId(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId === 'nrf') {
    var nrfBundle = loadNrfBundleRaw();
    return tierApiId(nrfBundle.activeTier || LADDER_TIER_SHORT);
  }
  if (strategyId === 'trend' || strategyId === 'medium' || strategyId === 'long') {
    return strategyId;
  }
  if (isMacdGcWaveHighStrategy(strategyId)) {
    return 'macdgcwh';
  }
  if (isMacdGcWaveHighLiftStrategy(strategyId)) {
    return 'macdgcwhu';
  }
  return strategyId;
}

function getTierSchema(tier) {
  return SCHEMA_BY_STRATEGY[tierApiId(tier)] || [];
}

function normalizeTierParams(tier, raw) {
  return normalize(tierApiId(tier), raw);
}

function normalizeLadderBundle(raw) {
  var def = buildNrfBundleDefaults();
  if (!raw || typeof raw !== 'object') {
    return def;
  }
  return {
    activeTier: raw.activeTier === LADDER_TIER_MEDIUM || raw.activeTier === LADDER_TIER_LONG
      ? raw.activeTier
      : LADDER_TIER_SHORT,
    short: normalizeTierParams(LADDER_TIER_SHORT, raw.short || def.short),
    medium: normalizeTierParams(LADDER_TIER_MEDIUM, raw.medium || def.medium),
    long: normalizeTierParams(LADDER_TIER_LONG, raw.long || def.long)
  };
}

function migrateLadderFromLegacyStorages() {
  var bundle = buildNrfBundleDefaults();
  try {
    var tr = wx.getStorageSync(STORAGE_PREFIX + 'trend');
    if (tr) bundle.short = normalize('trend', tr);
    var md = wx.getStorageSync(STORAGE_PREFIX + 'medium');
    if (md) bundle.medium = normalize('medium', md);
    var lg = wx.getStorageSync(STORAGE_PREFIX + 'long');
    if (lg) bundle.long = normalize('long', lg);
    var oldLadder = wx.getStorageSync(STORAGE_PREFIX + 'ladder');
    if (oldLadder && oldLadder.lLadderTier) {
      var tierCode = String(oldLadder.lLadderTier).toUpperCase();
      if (tierCode === 'B') bundle.activeTier = LADDER_TIER_MEDIUM;
      else if (tierCode === 'C') bundle.activeTier = LADDER_TIER_LONG;
      else bundle.activeTier = LADDER_TIER_SHORT;
    }
  } catch (e) {}
  return bundle;
}

function getActiveTierParams(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId === 'nrf') {
    var nrfBundle = loadNrfBundleRaw();
    var nrfTier = nrfBundle.activeTier || LADDER_TIER_SHORT;
    return nrfBundle[nrfTier];
  }
  return load(strategyId);
}

function nrfTierTitle(tier) {
  for (var i = 0; i < NRF_TIERS.length; i++) {
    if (NRF_TIERS[i].id === tier) return NRF_TIERS[i].title;
  }
  return '日+min30';
}

function formatTierSummary(tier, p) {
  if (tier === LADDER_TIER_SHORT || tierApiId(tier) === 'trend') {
    var macdParts = [];
    if (p.trRequireMonthMacd) macdParts.push('月MACD');
    if (p.trRequireWeekMacd) macdParts.push('周MACD');
    if (p.trRequireDayMacd) macdParts.push('日MACD');
    if (p.trRequireWeekGoldenCross) macdParts.push('周金叉');
    return (p.trMinAmountWan != null ? p.trMinAmountWan : 5000) + '万 · '
      + (p.trPrevWeeks != null ? p.trPrevWeeks : 2) + '周基准 · '
      + (p.trRequireCurrentBreakout ? '当日突破' : '本周突破')
      + (macdParts.length ? ' · ' + macdParts.join('+') : '')
      + (p.trRequireUltra !== false ? ' · +超短' : '');
  }
  if (tier === LADDER_TIER_MEDIUM || tierApiId(tier) === 'medium') {
    var mdMacdParts = [];
    if (p.mdRequireMonthMacd) mdMacdParts.push('月MACD');
    if (p.mdRequireYearMacd) mdMacdParts.push('年MACD');
    if (p.mdRequireMonthGoldenCross) mdMacdParts.push('月金叉');
    return (p.mdMinAmountWan != null ? p.mdMinAmountWan : 5000) + '万 · '
      + (p.mdPrevMonths != null ? p.mdPrevMonths : 2) + '月基准 · '
      + (p.mdRequireCurrentBreakout ? '当周突破' : '本月突破')
      + (mdMacdParts.length ? ' · ' + mdMacdParts.join('+') : '')
      + (p.mdRequireUltra !== false ? ' · +超短' : '');
  }
  var lgMacdParts = [];
  if (p.lgRequireYearMacd) lgMacdParts.push('年MACD');
  if (p.lgRequireMonthMacd) lgMacdParts.push('月MACD');
  if (p.lgRequireYearGoldenCross) lgMacdParts.push('年金叉');
  return (p.lgMinAmountWan != null ? p.lgMinAmountWan : 5000) + '万 · '
    + (p.lgPrevYears != null ? p.lgPrevYears : 2) + '年基准 · '
    + (p.lgRequireCurrentBreakout ? '当月突破' : '本年突破')
    + (lgMacdParts.length ? ' · ' + lgMacdParts.join('+') : '')
    + (p.lgRequireUltra !== false ? ' · +超短' : '');
}

function retestPrimaryPeriod(params) {
  var p = params || {};
  if (p.tEnableUltra) return 'min30';
  if (p.tEnableShort) return 'day';
  if (p.tEnableMedium) return 'week';
  if (p.tEnableLong) return 'month';
  return 'day';
}

function gc2PrimaryPeriod(params) {
  var p = params || {};
  if (p.g2EnableShort) return 'day';
  if (p.g2EnableLong) return 'week';
  return 'day';
}

function dc2PrimaryPeriod(params) {
  var p = params || {};
  if (p.d2EnableShort) return 'day';
  if (p.d2EnableLong) return 'week';
  return 'day';
}

function cascadePrimaryPeriod(params) {
  var p = params || {};
  if (p.caEnableMonth) return 'month';
  if (p.caEnableWeek) return 'week';
  if (p.caEnableDay) return 'day';
  return 'day';
}

function cascadewaveconvexPrimaryPeriod(params) {
  var p = params || {};
  if (p.cwcvEnableMonth) return 'month';
  if (p.cwcvEnableWeek) return 'week';
  if (p.cwcvEnableDay) return 'day';
  return 'day';
}

function cascadewaveconcavePrimaryPeriod(params) {
  var p = params || {};
  if (p.cwcavEnableMonth) return 'month';
  if (p.cwcavEnableWeek) return 'week';
  if (p.cwcavEnableDay) return 'day';
  return 'day';
}

function cascadewaveconvexdayPrimaryPeriod(params) {
  var p = params || {};
  if (p.cwcvdEnableMonth) return 'month';
  if (p.cwcvdEnableWeek) return 'week';
  if (p.cwcvdEnableDay) return 'day';
  return 'day';
}

function cascadewaveconcavedayPrimaryPeriod(params) {
  var p = params || {};
  if (p.cwcadEnableMonth) return 'month';
  if (p.cwcadEnableWeek) return 'week';
  if (p.cwcadEnableDay) return 'day';
  return 'day';
}

function macedgePrimaryPeriod(params) {
  var p = params || {};
  if (p.meEnableYear) return 'year';
  if (p.meEnableMonth) return 'month';
  if (p.meEnableWeek) return 'week';
  if (p.meEnableDay) return 'day';
  if (p.meEnableMin30) return 'min30';
  return 'day';
}

function shortChartPrimaryPeriod() {
  return 'day';
}

var CASCADE_TIER_STRATEGIES = {
  cascadewaveShort: true,
  cascadewaveMedium: true,
  cascadewaveLong: true
};

var MACD_GC_WAVE_HIGH_STRATEGIES = {
  macdgcwh: true,
  macdgcwhShort: true,
  macdgcwhMedium: true,
  macdgcwhLong: true
};

var MACD_GC_WAVE_HIGH_LIFT_STRATEGIES = {
  macdgcwhu: true
};

function isMacdGcWaveHighStrategy(strategyId) {
  return !!MACD_GC_WAVE_HIGH_STRATEGIES[normalizeStrategyId(strategyId)];
}

function isMacdGcWaveHighLiftStrategy(strategyId) {
  return !!MACD_GC_WAVE_HIGH_LIFT_STRATEGIES[normalizeStrategyId(strategyId)];
}

function mgcwhTierForStrategy(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId === 'macdgcwhMedium') return 'week';
  if (strategyId === 'macdgcwhLong') return 'month';
  return 'day';
}

function mgcwhPrimaryPeriod(strategyId) {
  var tier = mgcwhTierForStrategy(strategyId);
  if (tier === 'week') return 'week';
  if (tier === 'month') return 'month';
  return 'day';
}

function mgcwhuPrimaryPeriod(strategyId) {
  return 'day';
}

function isCascadeTierStrategy(strategyId) {
  return !!CASCADE_TIER_STRATEGIES[normalizeStrategyId(strategyId)];
}

function defaultChartPeriod(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  return chartPrimaryPeriod(strategyId, load(strategyId)) || 'week';
}

function chartPrimaryPeriod(strategyId, params) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId === 'ultra') return 'min30';
  if (strategyId === 'ultragc') return 'min30';
  if (strategyId === 'min60wavecc') return 'min60';
  if (strategyId === 'daywavecc') return 'day';
  if (strategyId === 'weekwavecc') return 'week';
  if (strategyId === 'monthwavecc') return 'month';
  if (strategyId === 'trend') return 'day';
  if (strategyId === 'medium') return 'week';
  if (strategyId === 'long') return 'month';
  if (strategyId === 'nrf') {
    var nrfBundle = params && params.activeTier ? params : loadNrfBundleRaw();
    return chartPrimaryPeriod(tierApiId(nrfBundle.activeTier || LADDER_TIER_SHORT));
  }
  if (strategyId === 'retest') return retestPrimaryPeriod(params);
  if (strategyId === 'gc2') return gc2PrimaryPeriod(params);
  if (strategyId === 'dc2') return dc2PrimaryPeriod(params);
  if (strategyId === 'cascade') return cascadePrimaryPeriod(params);
  if (strategyId === 'macedge') return macedgePrimaryPeriod(params);
  if (strategyId === 'cascadewaveconvex') return cascadewaveconvexPrimaryPeriod(params);
  if (strategyId === 'cascadewaveconcave') return cascadewaveconcavePrimaryPeriod(params);
  if (strategyId === 'cascadewaveconvexday') return cascadewaveconvexdayPrimaryPeriod(params);
  if (strategyId === 'cascadewaveconcaveday') return cascadewaveconcavedayPrimaryPeriod(params);
  if (strategyId === 'cascadewaveShort') return shortChartPrimaryPeriod(params);
  if (strategyId === 'cascadewaveMedium') return 'week';
  if (strategyId === 'cascadewaveLong') return 'month';
  if (isMacdGcWaveHighStrategy(strategyId)) {
    return mgcwhPrimaryPeriod(strategyId);
  }
  if (isMacdGcWaveHighLiftStrategy(strategyId)) {
    return mgcwhuPrimaryPeriod(strategyId);
  }
  return null;
}

function normalizeStrategyId(strategyId) {
  if (!strategyId) return strategyId;
  if (strategyId === 'ultraLow' || strategyId === 'ladder') return 'nrf';
  if (strategyId === 'macdgcwhuShort' || strategyId === 'macdgcwhuMedium'
      || strategyId === 'macdgcwhuLong') {
    return 'macdgcwhu';
  }
  return strategyId;
}

function storageKey(strategyId) {
  return STORAGE_PREFIX + normalizeStrategyId(strategyId || 'trend');
}

function clone(obj) {
  return JSON.parse(JSON.stringify(obj));
}

function getDefaults(strategyId) {
  return clone(DEFAULTS_BY_STRATEGY[normalizeStrategyId(strategyId)] || {});
}

function migrateMacdPositiveGateFields(raw) {
  if (!raw || typeof raw !== 'object') {
    return raw;
  }
  if (raw.mgRequireDayMacd === undefined && raw.ulRequireDayMacd !== undefined) {
    raw.mgRequireDayMacd = raw.ulRequireDayMacd;
  }
  if (raw.mgRequireWeekMacd === undefined && raw.ulRequireWeekMacd !== undefined) {
    raw.mgRequireWeekMacd = raw.ulRequireWeekMacd;
  }
  if (raw.mgRequireMonthMacd === undefined && raw.ulRequireMonthMacd !== undefined) {
    raw.mgRequireMonthMacd = raw.ulRequireMonthMacd;
  }
  return raw;
}

function formatMacdPositiveGateSummary(p) {
  var parts = [];
  if (p.mgRequireDayMacd) parts.push('日MACD>0');
  if (p.mgRequireMin60Macd) parts.push('Min60MACD>0');
  if (p.mgRequireWeekMacd) parts.push('周MACD>0');
  if (p.mgRequireMonthMacd) parts.push('月MACD>0');
  return parts.length ? parts.join('+') : '';
}

function normalize(strategyId, raw) {
  raw = migrateMacdPositiveGateFields(raw);
  strategyId = normalizeStrategyId(strategyId);
  var defaults = getDefaults(strategyId);
  var schema = SCHEMA_BY_STRATEGY[strategyId] || [];
  var out = clone(defaults);
  if (!raw || typeof raw !== 'object') {
    return out;
  }
  schema.forEach(function (field) {
    if (raw[field.key] === undefined || raw[field.key] === null) return;
    if (field.type === 'switch') {
      out[field.key] = !!raw[field.key];
    } else if (field.key === 'uTierMin' || field.key === 'rTierMin' || field.key === 'pTierMin' || field.key === 'cTierMin' || field.key === 'lTierMin' || field.key === 'tTierMin' || field.key === 'g2TierMin') {
      out[field.key] = String(raw[field.key]).toUpperCase();
    } else {
      out[field.key] = raw[field.key];
    }
  });
  if (strategyId === 'resonance' && !out.cEnableShort && !out.cEnableMedium && !out.cEnableLong) {
    out.cEnableShort = true;
  }
  if (strategyId === 'rebound') {
    if (!out.rEnableShort && !out.rEnableMedium && !out.rEnableLong) {
      out.rEnableShort = true;
    }
  }
  if (strategyId === 'retest') {
    if (!out.tEnableBear && !out.tEnableBull) {
      out.tEnableBear = true;
    }
    if (!out.tEnableUltra && !out.tEnableShort && !out.tEnableMedium && !out.tEnableLong) {
      out.tEnableUltra = true;
    }
  }
  if (strategyId === 'gc2') {
    if (!out.g2EnableShort && !out.g2EnableLong) {
      out.g2EnableShort = true;
    }
    if (out.g2TierMin === 'A') {
      out.g2TierMin = 'B';
    }
  }
  if (strategyId === 'dc2') {
    if (!out.d2EnableShort && !out.d2EnableLong) {
      out.d2EnableShort = true;
    }
    if (out.d2TierMin === 'A') {
      out.d2TierMin = 'B';
    }
  }
  if (strategyId === 'cascade') {
    if (!out.caEnableDay && !out.caEnableWeek && !out.caEnableMonth) {
      out.caEnableDay = true;
    }
  }
  if (strategyId === 'macedge') {
    if (!out.meEnableMin30 && !out.meEnableDay && !out.meEnableWeek
        && !out.meEnableMonth && !out.meEnableYear) {
      out.meEnableDay = true;
    }
  }
  if (strategyId === 'cascadewaveconvex') {
    if (!out.cwcvEnableDay && !out.cwcvEnableWeek && !out.cwcvEnableMonth) {
      out.cwcvEnableDay = true;
    }
  }
  if (strategyId === 'cascadewaveconcave') {
    if (!out.cwcavEnableDay && !out.cwcavEnableWeek && !out.cwcavEnableMonth) {
      out.cwcavEnableDay = true;
    }
  }
  if (strategyId === 'cascadewaveconvexday') {
    if (!out.cwcvdEnableDay && !out.cwcvdEnableWeek && !out.cwcvdEnableMonth) {
      out.cwcvdEnableDay = true;
    }
  }
  if (strategyId === 'cascadewaveconcaveday') {
    if (!out.cwcadEnableDay && !out.cwcadEnableWeek && !out.cwcadEnableMonth) {
      out.cwcadEnableDay = true;
    }
  }
  if (strategyId === 'ultra') {
    if (out.mgRequireDayMacd && out.ulRequireDayMacdNegative) {
      out.ulRequireDayMacdNegative = false;
    }
  }
  return out;
}

/** 当前参数组合可能导致零结果时的提示（供首页空态） */
function emptyResultHint(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId === 'cascadewaveShort') {
    if (isCustomized('cascadewaveShort')) {
      return '当前参数无匹配，可点 ⚙ 恢复默认';
    }
    return '';
  }
  if (strategyId === 'cascadewaveMedium' || strategyId === 'cascadewaveLong') {
    return isCustomized(strategyId) ? '当前参数无匹配，可点 ⚙ 恢复默认' : '';
  }
  if (strategyId !== 'ultra') {
    return isCustomized(strategyId) ? '当前参数无匹配，可点 ⚙ 恢复默认' : '';
  }
  var p = load('ultra');
  if (p.ulRequireDayMacdNegative && p.ulRequireWeekMacdNegative && p.ulRequireMonthMacdNegative) {
    return '日/周/月 MACD<0 全开时无匹配标的，请点 ⚙ 恢复默认';
  }
  if (isCustomized('ultra')) {
    return '当前参数无匹配，可点 ⚙ 恢复默认或放宽 MACD/突破 条件';
  }
  return '';
}

function load(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId === 'nrf') {
    return loadNrfBundleRaw();
  }
  try {
    var saved = wx.getStorageSync(storageKey(strategyId));
    return normalize(strategyId, saved);
  } catch (e) {
    return getDefaults(strategyId);
  }
}

function save(strategyId, params) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId === 'nrf') {
    if (params && params.short && params.medium && params.long) {
      var nrfBundle = normalizeLadderBundle(params);
      wx.setStorageSync(storageKey('nrf'), nrfBundle);
      return nrfBundle;
    }
    var nrfTier = (params && params.activeTier) || loadNrfBundleRaw().activeTier || LADDER_TIER_SHORT;
    return saveNrfTierForm(nrfTier, params, true);
  }
  var normalized = normalize(strategyId, params);
  wx.setStorageSync(storageKey(strategyId), normalized);
  return normalized;
}

function reset(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId === 'nrf') {
    var nrfDefaults = buildNrfBundleDefaults();
    wx.setStorageSync(storageKey('nrf'), clone(nrfDefaults));
    return clone(nrfDefaults);
  }
  var defaults = getDefaults(strategyId);
  wx.setStorageSync(storageKey(strategyId), clone(defaults));
  return clone(defaults);
}

function getSchema(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  return schemaWithFrictionlessGate(strategyId, SCHEMA_BY_STRATEGY[strategyId] || []);
}

function hasCustomParams(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId === 'nrf') return true;
  return (getSchema(strategyId) || []).length > 0;
}

/** 转为 findMy / rescan 查询参数 */
function toApiParamsFromForm(apiStrategyId, params) {
  var schema = SCHEMA_BY_STRATEGY[apiStrategyId] || [];
  if (!schema.length) return {};
  var api = {};
  schema.forEach(function (field) {
    var val = params[field.key];
    if (val === undefined || val === null) return;
    if (field.type === 'switch') {
      api[field.key] = val ? 1 : 0;
    } else {
      api[field.key] = val;
    }
  });
  return api;
}

var SUB_API_PREFIX = {
  cascadewaveconvex: 'cwcv',
  cascadewaveconcave: 'cwcav',
  cascadewaveconvexday: 'cwcvd',
  cascadewaveconcaveday: 'cwcad'
};

function mapBundleParamsToSubForm(subApiId, bundleParams, tierFlags) {
  var prefix = SUB_API_PREFIX[subApiId];
  if (!prefix || !bundleParams) return {};
  tierFlags = tierFlags || { enableDay: false, enableWeek: false, enableMonth: false };
  var out = {};
  out[prefix + 'EnableAllYangGate'] = !!bundleParams.cwbEnableAllYangGate;
  out[prefix + 'EnableMin30Gate'] = !!bundleParams.cwbEnableMin30Gate;
  out[prefix + 'EnableBandLastYangLowGate'] = bundleParams.cwbEnableBandLastYangLowGate !== false;
  out[prefix + 'EnableYangBandTrendGate'] = !!bundleParams.cwbEnableYangBandTrendGate;
  out[prefix + 'EnablePrevBandBreak'] = !!bundleParams.cwbEnablePrevBandBreak;
  out[prefix + 'MinAmountWan'] = bundleParams.cwbMinAmountWan != null
    ? bundleParams.cwbMinAmountWan : 0;
  out[prefix + 'LookbackDay'] = bundleParams.cwbLookbackDay != null
    ? bundleParams.cwbLookbackDay : 60;
  out[prefix + 'LookbackWeek'] = bundleParams.cwbLookbackWeek != null
    ? bundleParams.cwbLookbackWeek : 52;
  out[prefix + 'LookbackMonth'] = bundleParams.cwbLookbackMonth != null
    ? bundleParams.cwbLookbackMonth : 36;
  out[prefix + 'LookbackYear'] = bundleParams.cwbLookbackYear != null
    ? bundleParams.cwbLookbackYear : 20;
  out[prefix + 'EnableDay'] = !!tierFlags.enableDay;
  out[prefix + 'EnableWeek'] = !!tierFlags.enableWeek;
  out[prefix + 'EnableMonth'] = !!tierFlags.enableMonth;
  return out;
}

function getBundleSubStrategies(virtualId, params) {
  virtualId = normalizeStrategyId(virtualId);
  if (virtualId === 'cascadewaveShort') {
    return ['cascadewaveconvex', 'cascadewaveconcave'];
  }
  if (virtualId === 'cascadewaveMedium' || virtualId === 'cascadewaveLong') {
    return ['cascadewaveconvex', 'cascadewaveconcave'];
  }
  return [];
}

/** 虚拟策略 → 子策略 API 查询参数 */
function toSubApiParams(virtualId, subApiId) {
  virtualId = normalizeStrategyId(virtualId);
  subApiId = normalizeStrategyId(subApiId);
  var bundleParams = load(virtualId);
  var tierFlagsCw = require('./cascadewave-bundle').resolveSubTierFlags(virtualId, subApiId);
  var subForm = normalize(subApiId, mapBundleParamsToSubForm(subApiId, bundleParams, tierFlagsCw));
  return toApiParamsFromForm(subApiId, subForm);
}

function toApiParams(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId === 'nrf') {
    var nrfBundle = loadNrfBundleRaw();
    var nrfActive = nrfBundle.activeTier || LADDER_TIER_SHORT;
    return Object.assign(
      { nrfActiveTier: nrfActive },
      toApiParamsFromForm(tierApiId(nrfActive), nrfBundle[nrfActive])
    );
  }
  if (isMacdGcWaveHighStrategy(strategyId)) {
    return Object.assign(
      { mgcwhTier: mgcwhTierForStrategy(strategyId) },
      toApiParamsFromForm('macdgcwh', load(strategyId))
    );
  }
  if (isMacdGcWaveHighLiftStrategy(strategyId)) {
    return toApiParamsFromForm('macdgcwhu', load(strategyId));
  }
  if (strategyId === 'ultragc') {
    return toApiParamsFromForm('ultragc', load(strategyId));
  }
  return toApiParamsFromForm(strategyId, load(strategyId));
}

function isCustomized(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  if (strategyId === 'nrf') {
    return JSON.stringify(loadNrfBundleRaw()) !== JSON.stringify(buildNrfBundleDefaults());
  }
  var current = load(strategyId);
  var defaults = getDefaults(strategyId);
  return JSON.stringify(current) !== JSON.stringify(defaults);
}

function tierLabelFrom(picker, tierVal) {
  var label = '全部';
  (picker || { options: [] }).options.forEach(function (o) {
    if (o.value === tierVal) label = o.label.replace(/\(.*\)/, '').trim();
  });
  return label;
}

function waveBreakLineLabel(p, prefix, convexDefault) {
  if (p[prefix + 'EnableLastLowBreak']) return '末阳底';
  if (p[prefix + 'EnableLastMedianBreak']) return '末阳中位';
  if (p[prefix + 'EnableLastHighBreak']) return '末阳顶';
  return convexDefault ? '末阳中位' : '末阳顶';
}

function formatSummary(strategyId) {
  strategyId = normalizeStrategyId(strategyId);
  if (!hasCustomParams(strategyId)) return '';
  var p = load(strategyId);
  if (strategyId === 'rebound') {
    var rModes = [];
    if (p.rEnableShort) rModes.push('短线');
    if (p.rEnableMedium) rModes.push('中线');
    if (p.rEnableLong) rModes.push('长线');
    return (rModes.length ? rModes.join('+') : '未启用') + ' · '
      + tierLabelFrom(REBOUND_TIER_PICKER, p.rTierMin);
  }
  if (strategyId === 'trend') {
    var macdParts = [];
    if (p.trRequireMonthMacd) macdParts.push('月MACD');
    if (p.trRequireWeekMacd) macdParts.push('周MACD');
    if (p.trRequireDayMacd) macdParts.push('日MACD');
    if (p.trRequireWeekGoldenCross) macdParts.push('周金叉');
    var amountPart = (p.trMinAmountWan != null ? p.trMinAmountWan : 5000) + '万';
    var weekPart = (p.trPrevWeeks != null ? p.trPrevWeeks : 2) + '周基准';
    var sigPart = p.trRequireCurrentBreakout ? '当日突破' : '本周突破';
    return amountPart + ' · ' + weekPart + ' · ' + sigPart
      + (macdParts.length ? ' · ' + macdParts.join('+') : '')
      + (p.trRequireUltra !== false ? ' · +超短' : '');
  }
  if (strategyId === 'medium') {
    var mdMacdParts = [];
    if (p.mdRequireMonthMacd) mdMacdParts.push('月MACD');
    if (p.mdRequireYearMacd) mdMacdParts.push('年MACD');
    if (p.mdRequireMonthGoldenCross) mdMacdParts.push('月金叉');
    var mdAmountPart = (p.mdMinAmountWan != null ? p.mdMinAmountWan : 5000) + '万';
    var monthPart = (p.mdPrevMonths != null ? p.mdPrevMonths : 2) + '月基准';
    var mdSigPart = p.mdRequireCurrentBreakout ? '当周突破' : '本月突破';
    return mdAmountPart + ' · ' + monthPart + ' · ' + mdSigPart
      + (mdMacdParts.length ? ' · ' + mdMacdParts.join('+') : '')
      + (p.mdRequireUltra !== false ? ' · +超短' : '');
  }
  if (strategyId === 'long') {
    var lgMacdParts = [];
    if (p.lgRequireYearMacd) lgMacdParts.push('年MACD');
    if (p.lgRequireMonthMacd) lgMacdParts.push('月MACD');
    if (p.lgRequireYearGoldenCross) lgMacdParts.push('年金叉');
    var lgAmountPart = (p.lgMinAmountWan != null ? p.lgMinAmountWan : 5000) + '万';
    var yearPart = (p.lgPrevYears != null ? p.lgPrevYears : 2) + '年基准';
    var lgSigPart = p.lgRequireCurrentBreakout ? '当月突破' : '本年突破';
    return lgAmountPart + ' · ' + yearPart + ' · ' + lgSigPart
      + (lgMacdParts.length ? ' · ' + lgMacdParts.join('+') : '')
      + (p.lgRequireUltra !== false ? ' · +超短' : '');
  }
  if (strategyId === 'resonance') {
    var resModes = [];
    if (p.cEnableShort) resModes.push('短线');
    if (p.cEnableMedium) resModes.push('中线');
    if (p.cEnableLong) resModes.push('长线');
    return (resModes.length ? resModes.join('+') : '未启用') + ' · '
      + tierLabelFrom(RESONANCE_TIER_PICKER, p.cTierMin);
  }
  if (strategyId === 'nrf') {
    var nrfBundle2 = loadNrfBundleRaw();
    var nrfTier = nrfBundle2.activeTier || LADDER_TIER_SHORT;
    return nrfTierTitle(nrfTier) + ' · ' + formatNrfTierSummary(nrfTier, nrfBundle2[nrfTier]) + ' · 四门全局';
  }
  if (strategyId === 'retest') {
    var tModes = [];
    if (p.tEnableBear) tModes.push('下跌反转');
    if (p.tEnableBull) tModes.push('上涨中继');
    var tTiers = [];
    if (p.tEnableUltra) tTiers.push('超短');
    if (p.tEnableShort) tTiers.push('短');
    if (p.tEnableMedium) tTiers.push('中');
    if (p.tEnableLong) tTiers.push('长');
    return (tModes.length ? tModes.join('+') : '未启用模式') + ' · '
      + (tTiers.length ? tTiers.join('+') : '未启用档位') + ' · '
      + tierLabelFrom(RETEST_TIER_PICKER, p.tTierMin);
  }
  if (strategyId === 'gc2') {
    var g2Modes = [];
    if (p.g2EnableShort) g2Modes.push('短线');
    if (p.g2EnableLong) g2Modes.push('长线');
    return (g2Modes.length ? g2Modes.join('+') : '未启用') + ' · '
      + tierLabelFrom(GC2_TIER_PICKER, p.g2TierMin);
  }
  if (strategyId === 'dc2') {
    var d2Modes = [];
    if (p.d2EnableShort) d2Modes.push('短线');
    if (p.d2EnableLong) d2Modes.push('长线');
    return (d2Modes.length ? d2Modes.join('+') : '未启用') + ' · '
      + tierLabelFrom(DC2_TIER_PICKER, p.d2TierMin);
  }
  if (strategyId === 'cascade') {
    var caModes = [];
    if (p.caEnableDay) caModes.push('日基准');
    if (p.caEnableWeek) caModes.push('周基准');
    if (p.caEnableMonth) caModes.push('月基准');
    var gateParts = [];
    if (p.caEnableDualLowGate) gateParts.push('双低');
    if (p.caEnableMacdGate) gateParts.push('无阻力');
    if (p.caEnableMacdDcHighGate) gateParts.push('死叉高');
    if (p.caEnableCrossLowGate) gateParts.push('交叉low');
    if (p.caEnableBarHighGate) gateParts.push('高点递');
    if (p.caEnableAllYangGate) gateParts.push('全阳');
    return (caModes.length ? caModes.join('+') : '未启用') + ' · 级联交叉突破'
      + (gateParts.length ? ' · ' + gateParts.join('+') : '')
      + (p.caMinAmountWan != null && p.caMinAmountWan > 0 ? ' · ' + p.caMinAmountWan + '万' : '')
      + (p.caRequireUltra ? ' · +min30' : '')
      + (p.caEnableAltBreakout ? ' · +前K路径' : '');
  }
  if (strategyId === 'macedge') {
    var meModes = [];
    if (p.meEnableMin30) meModes.push('Min30');
    if (p.meEnableDay) meModes.push('日');
    if (p.meEnableWeek) meModes.push('周');
    if (p.meEnableMonth) meModes.push('月');
    if (p.meEnableYear) meModes.push('年');
    var meGateParts = [];
    if (p.meEnableDualLowGate) meGateParts.push('双低');
    if (p.meEnableMacdGate) meGateParts.push('无阻力');
    if (p.meEnableMacdDcHighGate) meGateParts.push('死叉高');
    if (p.meEnableCrossLowGate) meGateParts.push('交叉low');
    if (p.meEnableBarHighGate) meGateParts.push('高点递');
    if (p.meEnableAllYangGate) meGateParts.push('全阳');
    return (meModes.length ? meModes.join('+') : '未启用') + ' · MACD交叉边沿突破'
      + (meGateParts.length ? ' · ' + meGateParts.join('+') : '')
      + (p.meMinAmountWan != null && p.meMinAmountWan > 0 ? ' · ' + p.meMinAmountWan + '万' : '');
  }
  if (strategyId === 'cascadewaveconvex') {
    var cwcvModes = [];
    if (p.cwcvEnableDay) cwcvModes.push('日');
    if (p.cwcvEnableWeek) cwcvModes.push('周');
    if (p.cwcvEnableMonth) cwcvModes.push('月');
    var cwcvGateParts = [];
    if (p.cwcvEnableAllYangGate) cwcvGateParts.push('全阳');
    if (p.cwcvEnableMin30Gate) cwcvGateParts.push('Min30');
    if (p.cwcvEnableBandLastYangLowGate !== false) cwcvGateParts.push('末阳低');
    if (p.cwcvEnableYangBandTrendGate) cwcvGateParts.push('趋势');
    var cwcvBreak = '次波段末阳顶';
    return (cwcvModes.length ? cwcvModes.join('+') : '未启用') + ' · 级联凸' + cwcvBreak
      + (cwcvGateParts.length ? ' · ' + cwcvGateParts.join('+') : '')
      + (p.cwcvMinAmountWan != null && p.cwcvMinAmountWan > 0 ? ' · ' + p.cwcvMinAmountWan + '万' : '');
  }
  if (strategyId === 'cascadewaveconcave') {
    var cwcavModes = [];
    if (p.cwcavEnableDay) cwcavModes.push('日');
    if (p.cwcavEnableWeek) cwcavModes.push('周');
    if (p.cwcavEnableMonth) cwcavModes.push('月');
    var cwcavGateParts = [];
    if (p.cwcavEnableAllYangGate) cwcavGateParts.push('全阳');
    if (p.cwcavEnableMin30Gate) cwcavGateParts.push('Min30');
    if (p.cwcavEnableBandLastYangLowGate !== false) cwcavGateParts.push('末阳低');
    if (p.cwcavEnableYangBandTrendGate) cwcavGateParts.push('趋势');
    var cwcavBreak = '末波段末阳顶';
    return (cwcavModes.length ? cwcavModes.join('+') : '未启用') + ' · 级联凹' + cwcavBreak
      + (cwcavGateParts.length ? ' · ' + cwcavGateParts.join('+') : '')
      + (p.cwcavMinAmountWan != null && p.cwcavMinAmountWan > 0 ? ' · ' + p.cwcavMinAmountWan + '万' : '');
  }
  if (strategyId === 'cascadewaveconvexday') {
    var cwcvdModes = [];
    if (p.cwcvdEnableDay) cwcvdModes.push('日');
    if (p.cwcvdEnableWeek) cwcvdModes.push('周');
    if (p.cwcvdEnableMonth) cwcvdModes.push('月');
    var cwcvdGateParts = [];
    if (p.cwcvdEnableAllYangGate) cwcvdGateParts.push('全阳');
    if (p.cwcvdEnableMin30Gate) cwcvdGateParts.push('Min30');
    if (p.cwcvdEnableBandLastYangLowGate !== false) cwcvdGateParts.push('末阳低');
    if (p.cwcvdEnableYangBandTrendGate) cwcvdGateParts.push('趋势');
    var cwcvdBreak = '次波段末阳顶';
    return (cwcvdModes.length ? cwcvdModes.join('+') : '未启用') + ' · 级联凸日' + cwcvdBreak
      + (cwcvdGateParts.length ? ' · ' + cwcvdGateParts.join('+') : '')
      + (p.cwcvdMinAmountWan != null && p.cwcvdMinAmountWan > 0 ? ' · ' + p.cwcvdMinAmountWan + '万' : '');
  }
  if (strategyId === 'cascadewaveconcaveday') {
    var cwcadModes = [];
    if (p.cwcadEnableDay) cwcadModes.push('日');
    if (p.cwcadEnableWeek) cwcadModes.push('周');
    if (p.cwcadEnableMonth) cwcadModes.push('月');
    var cwcadGateParts = [];
    if (p.cwcadEnableAllYangGate) cwcadGateParts.push('全阳');
    if (p.cwcadEnableMin30Gate) cwcadGateParts.push('Min30');
    if (p.cwcadEnableBandLastYangLowGate !== false) cwcadGateParts.push('末阳低');
    if (p.cwcadEnableYangBandTrendGate) cwcadGateParts.push('趋势');
    var cwcadBreak = '末波段末阳顶';
    return (cwcadModes.length ? cwcadModes.join('+') : '未启用') + ' · 级联凹日' + cwcadBreak
      + (cwcadGateParts.length ? ' · ' + cwcadGateParts.join('+') : '')
      + (p.cwcadMinAmountWan != null && p.cwcadMinAmountWan > 0 ? ' · ' + p.cwcadMinAmountWan + '万' : '');
  }
  if (strategyId === 'cascadewaveShort' || strategyId === 'cascadewaveMedium'
      || strategyId === 'cascadewaveLong') {
    var cwbModes = [];
    if (strategyId === 'cascadewaveShort') {
      cwbModes.push('同档日');
    } else if (strategyId === 'cascadewaveMedium') {
      cwbModes.push('同档周');
    } else {
      cwbModes.push('同档月');
    }
    var cwbGateParts = [];
    if (p.cwbEnableAllYangGate) cwbGateParts.push('全阳');
    if (p.cwbEnableMin30Gate) cwbGateParts.push('Min30');
    if (p.cwbEnableBandLastYangLowGate !== false) cwbGateParts.push('末阳低');
    if (p.cwbEnableYangBandTrendGate) cwbGateParts.push('趋势');
    var cwbBreak = '凸次波段/凹末波段';
    var cwbTitle = strategyId === 'cascadewaveShort' ? '级联短线'
      : strategyId === 'cascadewaveMedium' ? '级联中线' : '级联长线';
    return (cwbModes.length ? cwbModes.join('+') : '未启用') + ' · ' + cwbTitle + cwbBreak
      + (cwbGateParts.length ? ' · ' + cwbGateParts.join('+') : '')
      + (p.cwbMinAmountWan != null && p.cwbMinAmountWan > 0 ? ' · ' + p.cwbMinAmountWan + '万' : '');
  }
  if (isMacdGcWaveHighStrategy(strategyId)) {
    var mgcwhLabel = mgcwhTierForStrategy(strategyId) === 'week' ? '周档'
      : mgcwhTierForStrategy(strategyId) === 'month' ? '月档' : '日档';
    var mgcwhParts = [mgcwhLabel + 'MACD金叉波段High突破'];
    if (p.mgcwhEnableMinAmountFilter !== false) {
      mgcwhParts.push((p.mgcwhMinAmountWan != null ? p.mgcwhMinAmountWan : 3000) + '万');
    }
    if (p.mgcwhEnableSignalRiseGate !== false) {
      mgcwhParts.push('末K>' + (p.mgcwhSignalRisePct != null ? p.mgcwhSignalRisePct : 3) + '%');
    }
    var mgcwhMacd = formatMacdPositiveGateSummary(p);
    if (mgcwhMacd) mgcwhParts.push(mgcwhMacd);
    return mgcwhParts.join(' · ');
  }
  if (isMacdGcWaveHighLiftStrategy(strategyId)) {
    var mgcwhuParts = ['日收>前日high', '前日收阳'];
    var mgcwhuMacd = formatMacdPositiveGateSummary(p);
    if (mgcwhuMacd) mgcwhuParts.unshift(mgcwhuMacd);
    if (p.mgcwhuEnableMinAmountFilter !== false) {
      mgcwhuParts.push((p.mgcwhuMinAmountWan != null ? p.mgcwhuMinAmountWan : 3000) + '万');
    }
    if (p.mgcwhuEnableSignalRiseGate !== false) {
      mgcwhuParts.push('末K>' + (p.mgcwhuSignalRisePct != null ? p.mgcwhuSignalRisePct : 1) + '%');
    }
    return mgcwhuParts.join(' · ');
  }
  if (strategyId === 'ultra') {
    var macdParts = [];
    var mgMacd = formatMacdPositiveGateSummary(p);
    if (mgMacd) macdParts.push(mgMacd);
    if (p.ulRequireDayMacdNegative) macdParts.push('日MACD<0');
    if (p.ulRequireWeekMacdNegative) macdParts.push('周MACD<0');
    if (p.ulRequireMonthMacdNegative) macdParts.push('月MACD<0');
    var amountPart = (p.ulMinAmountWan != null ? p.ulMinAmountWan : 5000) + '万';
    var sigPart = p.ulRequireCurrentBreakout ? '当前K突破' : '当日有突破';
    return '六门全局 · ' + amountPart + ' · ' + sigPart
      + (macdParts.length ? ' · ' + macdParts.join('+') : '');
  }
  if (strategyId === 'ultragc') {
    var ulgcParts = ['Min30金叉K high突破'];
    ulgcParts.push('背景' + (p.ulgcPrevDays != null ? p.ulgcPrevDays : 2) + '日');
    ulgcParts.push('涨幅>' + (p.ulgcSignalRisePct != null ? p.ulgcSignalRisePct : 1) + '%');
    var ulgcMacd = formatMacdPositiveGateSummary(p);
    if (ulgcMacd) ulgcParts.push(ulgcMacd);
    return ulgcParts.join(' · ');
  }
  return '';
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
  defaultChartPeriod: defaultChartPeriod
};

