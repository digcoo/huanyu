/**
 * 策略参数面板（MA金叉点 / MA死叉点 / 多头趋势突破 / 空头趋势启动）
 */
var strategyNav = require('./strategy-nav');

var STORAGE_PREFIX = 'strategyParams_';

var MCP_DEFAULTS = {
  mcpEnableMinAmountFilter: true,
  mcpMinAmountWan: 3000,
  mcpRequireDayMaBull: false,
  mcpRequireWeekMaBull: false,
  mcpRequireMonthMaBull: false,
  mcpRequireQuarterMaBull: false,
  mcpRequireYearMaBull: false
};

var GATE_BOOL_KEYS = [
  'mcpRequireDayMaBull',
  'mcpRequireWeekMaBull',
  'mcpRequireMonthMaBull',
  'mcpRequireQuarterMaBull',
  'mcpRequireYearMaBull'
];

var GATE_FIELDS = [
  { type: 'section', label: '均线多头', hint: '该周期末K MA5>MA60，可多选取交集' },
  { key: 'mcpRequireDayMaBull', label: '日', type: 'switch' },
  { key: 'mcpRequireWeekMaBull', label: '周', type: 'switch' },
  { key: 'mcpRequireMonthMaBull', label: '月', type: 'switch' },
  { key: 'mcpRequireQuarterMaBull', label: '季', type: 'switch' },
  { key: 'mcpRequireYearMaBull', label: '年', type: 'switch' }
];

var AMOUNT_FIELDS = [
  {
    key: 'mcpEnableMinAmountFilter',
    label: '成交额门',
    type: 'switch'
  },
  {
    key: 'mcpMinAmountWan',
    label: '最低日均成交额',
    hint: '近6日日均成交额（万）',
    type: 'slider',
    min: 0,
    max: 10000,
    step: 500,
    unit: '万'
  }
];

var MGB_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: '边沿突破金叉波段顶：完结阳线取所在波段High；未完结阳线或阴线取前一完整波段High。本档MA5>MA10。边沿含跳空开盘（末K开盘价≤基准且收盘价>基准）。父级均价之上：父周期收盘价>max(MA5,MA10)（30分→日，日→周，周→月，月→季，季→年）'
  }
].concat(AMOUNT_FIELDS, GATE_FIELDS);

var MDB_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: '边沿突破最近死叉交叉点（MA5下穿MA10插值价）。边沿含跳空开盘（末K开盘价≤基准且收盘价>基准）。父级均价之上：父周期收盘价>max(MA5,MA10)（30分→日，日→周，周→月，月→季，季→年）'
  }
].concat(AMOUNT_FIELDS, GATE_FIELDS);

var MTB_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: '（边沿破金叉波段顶且MA5≥MA10）或边沿破死叉交叉点或（边沿破金叉交叉点且MA5≥MA10）。硬条件：本档MA10≥MA60。边沿含跳空开盘：末K开盘价≤基准且收盘价>基准。'
  }
].concat(AMOUNT_FIELDS, GATE_FIELDS);

var MBS_SCHEMA = [
  {
    type: 'section',
    label: '命中条件',
    hint: '（边沿破金叉波段顶且MA5≥MA10）或边沿破死叉交叉点或（边沿破金叉交叉点且MA5≥MA10）。硬条件：本档MA10<MA60。边沿含跳空开盘：末K开盘价≤基准且收盘价>基准。'
  }
].concat(AMOUNT_FIELDS, GATE_FIELDS);

var NRF_TIERS = [];

function strategyKind(strategyId) {
  var id = String(strategyId || '');
  if (/^mabearstart/.test(id)) return 'mbs';
  if (/^mabullbreak/.test(id)) return 'mtb';
  if (/^madeathbreak/.test(id)) return 'mdb';
  return 'mgb';
}

function isHighStrategy() {
  return false;
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

function getDefaults() {
  return clone(MCP_DEFAULTS);
}

function normalize(strategyId, raw) {
  var d = getDefaults(strategyId);
  if (!raw || typeof raw !== 'object') {
    return d;
  }
  if (raw.mcpEnableMinAmountFilter != null) {
    d.mcpEnableMinAmountFilter = !!raw.mcpEnableMinAmountFilter;
  }
  if (raw.mcpMinAmountWan != null && raw.mcpMinAmountWan >= 0) {
    d.mcpMinAmountWan = raw.mcpMinAmountWan;
  }
  GATE_BOOL_KEYS.forEach(function (key) {
    if (raw[key] != null) {
      d[key] = !!raw[key];
    }
  });
  if (raw.mcpEnableRightTrend && raw.mcpRequireDayMaBull == null) {
    d.mcpRequireDayMaBull = true;
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
  if (kind === 'mdb') return MDB_SCHEMA.slice();
  if (kind === 'mtb') return MTB_SCHEMA.slice();
  if (kind === 'mbs') return MBS_SCHEMA.slice();
  return MGB_SCHEMA.slice();
}

function getPanelSchema(strategyId) {
  return getSchema(strategyId);
}

function hasCustomParams() {
  return true;
}

function resolveApiStrategyId(strategyId) {
  var kind = strategyKind(strategyId);
  if (kind === 'mdb') return 'madeathbreak';
  if (kind === 'mtb') return 'mabullbreak';
  if (kind === 'mbs') return 'mabearstart';
  return 'magoldbreak';
}

function tierToApi(frontendId) {
  return strategyNav.tierForStrategyId(normalizeStrategyId(frontendId));
}

function gateApiParams(p, prefix) {
  var out = {};
  out[prefix + 'RequireDayMaBull'] = !!p.mcpRequireDayMaBull;
  out[prefix + 'RequireWeekMaBull'] = !!p.mcpRequireWeekMaBull;
  out[prefix + 'RequireMonthMaBull'] = !!p.mcpRequireMonthMaBull;
  out[prefix + 'RequireQuarterMaBull'] = !!p.mcpRequireQuarterMaBull;
  out[prefix + 'RequireYearMaBull'] = !!p.mcpRequireYearMaBull;
  return out;
}

function toApiParams(strategyId) {
  var p = load(strategyId);
  var tier = tierToApi(strategyId);
  var kind = strategyKind(strategyId);
  var prefix = kind === 'mdb' ? 'mdb' : (kind === 'mtb' ? 'mtb' : (kind === 'mbs' ? 'mbs' : 'mgb'));
  var params = {
    EnableMinAmountFilter: p.mcpEnableMinAmountFilter !== false,
    MinAmountWan: p.mcpMinAmountWan != null ? p.mcpMinAmountWan : 3000
  };
  var out = {};
  out[prefix + 'Tier'] = tier;
  out[prefix + 'EnableMinAmountFilter'] = params.EnableMinAmountFilter;
  out[prefix + 'MinAmountWan'] = params.MinAmountWan;
  return Object.assign(out, gateApiParams(p, prefix));
}

function toApiParamsFromForm(apiStrategyId) {
  return toApiParams(apiStrategyId);
}

function isCustomized(strategyId) {
  var p = load(strategyId);
  var d = getDefaults(strategyId);
  return JSON.stringify(p) !== JSON.stringify(d);
}

function appendGateSummary(p, parts) {
  var bull = [];
  if (p.mcpRequireDayMaBull) bull.push('日');
  if (p.mcpRequireWeekMaBull) bull.push('周');
  if (p.mcpRequireMonthMaBull) bull.push('月');
  if (p.mcpRequireQuarterMaBull) bull.push('季');
  if (p.mcpRequireYearMaBull) bull.push('年');
  if (bull.length) parts.push('多头' + bull.join('/'));
}

function formatSummary(strategyId) {
  var p = load(strategyId);
  var tier = tierToApi(strategyId);
  var tierLabel = tier === 'min30' ? '30分'
    : (tier === 'min60' ? '60分'
      : (tier === 'week' ? '周'
        : (tier === 'month' ? '月'
          : (tier === 'quarter' ? '季' : '日'))));
  var parts;
  var kind = strategyKind(strategyId);
  if (kind === 'mdb') {
    parts = [tierLabel + '线', '边沿破死叉点', '父级均价上'];
  } else if (kind === 'mtb') {
    parts = [tierLabel + '线', '破金叉顶/死叉点/金叉点', '金叉需MA5≥MA10', 'MA10≥MA60'];
  } else if (kind === 'mbs') {
    parts = [tierLabel + '线', '破金叉顶/死叉点/金叉点', '金叉需MA5≥MA10', 'MA10<MA60'];
  } else {
    parts = [tierLabel + '线', '边沿破金叉波段顶', 'MA5>MA10', '父级均价上'];
  }
  appendGateSummary(p, parts);
  if (p.mcpEnableMinAmountFilter !== false) {
    parts.push((p.mcpMinAmountWan != null ? p.mcpMinAmountWan : 3000) + '万');
  }
  return parts.join(' · ');
}

function emptyResultHint() {
  return '暂无命中：可放宽成交额或切换日/周/月/季档后重跑';
}

function defaultChartPeriod(strategyId) {
  var tier = tierToApi(strategyId);
  if (tier === 'min30') return 'min30';
  if (tier === 'min60') return 'min60';
  if (tier === 'week') return 'week';
  if (tier === 'month') return 'month';
  if (tier === 'quarter') return 'quarter';
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
  toApiParamsFromForm: toApiParamsFromForm,
  strategyKind: strategyKind,
  isHighStrategy: isHighStrategy
};
