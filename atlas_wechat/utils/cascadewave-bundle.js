/**
 * 级联凹凸波段虚拟策略（短线/中线/长线）— 前端多 API 并集
 */
const adapter = require('./adapter');
const chartPeriods = require('./chart-periods');

var BUNDLED_IDS = {
  cascadewaveShort: true,
  cascadewaveMedium: true,
  cascadewaveLong: true
};

var TIER_BY_BUNDLE = {
  cascadewaveShort: 'day',
  cascadewaveMedium: 'week',
  cascadewaveLong: 'month'
};

var SUB_PREFIX = {
  cascadewaveconvex: 'cwcv',
  cascadewaveconcave: 'cwcav',
  cascadewaveconvexday: 'cwcvd',
  cascadewaveconcaveday: 'cwcad'
};

/** 同 code 去重时保留优先级（靠前优先） */
var SUB_PRIORITY = [
  'cascadewaveconvex',
  'cascadewaveconcave',
  'cascadewaveconvexday',
  'cascadewaveconcaveday'
];

function isBundled(strategyId) {
  return !!BUNDLED_IDS[adapter.normalizeStrategyId(strategyId)];
}

function bundleTier(strategyId) {
  return TIER_BY_BUNDLE[adapter.normalizeStrategyId(strategyId)] || 'day';
}

function getActiveSubStrategies(strategyId, params) {
  strategyId = adapter.normalizeStrategyId(strategyId);
  if (strategyId === 'cascadewaveShort') {
    return ['cascadewaveconvex', 'cascadewaveconcave'];
  }
  if (strategyId === 'cascadewaveMedium' || strategyId === 'cascadewaveLong') {
    return ['cascadewaveconvex', 'cascadewaveconcave'];
  }
  return [];
}

/** 虚拟策略下各子 API 启用的波段档位 */
function resolveSubTierFlags(virtualId, subApiId) {
  virtualId = adapter.normalizeStrategyId(virtualId);
  if (virtualId === 'cascadewaveShort') {
    return { enableDay: true, enableWeek: false, enableMonth: false };
  }
  if (virtualId === 'cascadewaveMedium') {
    return { enableDay: false, enableWeek: true, enableMonth: false };
  }
  if (virtualId === 'cascadewaveLong') {
    return { enableDay: false, enableWeek: false, enableMonth: true };
  }
  return { enableDay: false, enableWeek: false, enableMonth: false };
}

function mapBundleParamsToSub(subApiId, bundleParams, virtualId) {
  var prefix = SUB_PREFIX[subApiId];
  if (!prefix || !bundleParams) return {};
  var flags = resolveSubTierFlags(virtualId, subApiId);
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
  out[prefix + 'EnableDay'] = !!flags.enableDay;
  out[prefix + 'EnableWeek'] = !!flags.enableWeek;
  out[prefix + 'EnableMonth'] = !!flags.enableMonth;
  return out;
}

function subPriorityIndex(subApiId) {
  var idx = SUB_PRIORITY.indexOf(subApiId);
  return idx >= 0 ? idx : SUB_PRIORITY.length;
}

function remapItemForBundle(virtualId, rawItem, sourceApiId) {
  var mapped = adapter.mapRecommendation(rawItem, sourceApiId);
  return Object.assign({}, mapped, {
    id: adapter.makeStockId(virtualId, mapped.market || 'cn', mapped.code),
    strategy: virtualId,
    cascadeHitSource: sourceApiId
  });
}

function dedupeByCode(items) {
  var byCode = {};
  (items || []).forEach(function (item) {
    if (!item || !item.code) return;
    var code = adapter.normalizeCode(item.code);
    var existing = byCode[code];
    if (!existing) {
      byCode[code] = item;
      return;
    }
    var curPri = subPriorityIndex(item.cascadeHitSource);
    var oldPri = subPriorityIndex(existing.cascadeHitSource);
    if (curPri < oldPri) {
      byCode[code] = item;
    }
  });
  return Object.keys(byCode).map(function (code) { return byCode[code]; });
}

function toPageNum(v) {
  var n = Number(v);
  return n > 0 ? n : 1;
}

/** 按子策略优先级合并各子 API 全量结果并同 code 去重 */
function mergeAllBundledResults(virtualId, subResults) {
  var bySub = {};
  (subResults || []).forEach(function (row) {
    if (!row || !row.subId) return;
    bySub[row.subId] = row.items || [];
  });
  var flat = [];
  SUB_PRIORITY.forEach(function (subId) {
    (bySub[subId] || []).forEach(function (item) {
      flat.push(remapItemForBundle(virtualId, item, subId));
    });
  });
  (subResults || []).forEach(function (row) {
    if (!row || !row.subId || SUB_PRIORITY.indexOf(row.subId) >= 0) return;
    (row.items || []).forEach(function (item) {
      flat.push(remapItemForBundle(virtualId, item, row.subId));
    });
  });
  return dedupeByCode(flat);
}

/** 全量去重合并后做客户端分页（避免子 API totalNum 相加与去重后条数不一致） */
function sliceBundledResults(virtualId, subResults, page, size) {
  var merged = mergeAllBundledResults(virtualId, subResults);
  return sliceMergedBundledResults(merged, page, size);
}

function sliceMergedBundledResults(merged, page, size) {
  page = toPageNum(page);
  size = size > 0 ? size : 12;
  var list = merged || [];
  var start = (page - 1) * size;
  var slice = list.slice(start, start + size);
  return {
    items: slice,
    page: page,
    totalNum: list.length,
    hasMore: start + slice.length < list.length
  };
}

function mergeBundledPageResults(virtualId, results, page) {
  return sliceBundledResults(virtualId, results, page, 12);
}

function inferHitSourceFromSignal(item) {
  if (!item) return null;
  var text = [item.signalMessage, item.trendMessage].join('|');
  if (/CASCADE_WAVE_CONVEX_DAY|级联凸.*日|cwcvday/i.test(text)) {
    return 'cascadewaveconvexday';
  }
  if (/CASCADE_WAVE_CONCAVE_DAY|级联凹.*日|cwcaday/i.test(text)) {
    return 'cascadewaveconcaveday';
  }
  if (/CASCADE_WAVE_CONVEX|级联凸/i.test(text)) {
    return 'cascadewaveconvex';
  }
  if (/CASCADE_WAVE_CONCAVE|级联凹/i.test(text)) {
    return 'cascadewaveconcave';
  }
  return null;
}

function resolveHitSource(item, virtualId, params) {
  if (item && item.cascadeHitSource) return item.cascadeHitSource;
  var inferred = inferHitSourceFromSignal(item);
  if (inferred) return inferred;
  var subs = getActiveSubStrategies(virtualId, params);
  return subs.length ? subs[0] : 'cascadewaveconvex';
}

function chartPeriodForBundle(strategyId, params) {
  strategyId = adapter.normalizeStrategyId(strategyId);
  if (strategyId === 'cascadewaveShort') {
    return 'day';
  }
  var tier = bundleTier(strategyId);
  if (tier === 'week') return 'week';
  if (tier === 'month') return 'month';
  return 'day';
}

function allowedChartPeriodsForBundle(strategyId, params) {
  return chartPeriods.ALL_CHART_PERIODS;
}

module.exports = {
  BUNDLED_IDS: BUNDLED_IDS,
  SUB_PRIORITY: SUB_PRIORITY,
  isBundled: isBundled,
  bundleTier: bundleTier,
  getActiveSubStrategies: getActiveSubStrategies,
  resolveSubTierFlags: resolveSubTierFlags,
  mapBundleParamsToSub: mapBundleParamsToSub,
  remapItemForBundle: remapItemForBundle,
  dedupeByCode: dedupeByCode,
  mergeAllBundledResults: mergeAllBundledResults,
  sliceBundledResults: sliceBundledResults,
  sliceMergedBundledResults: sliceMergedBundledResults,
  mergeBundledPageResults: mergeBundledPageResults,
  inferHitSourceFromSignal: inferHitSourceFromSignal,
  resolveHitSource: resolveHitSource,
  chartPeriodForBundle: chartPeriodForBundle,
  allowedChartPeriodsForBundle: allowedChartPeriodsForBundle
};
