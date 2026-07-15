const config = require('./config');
const adapter = require('./adapter');
const cascadewaveBundle = require('./cascadewave-bundle');
const chartPeriods = require('./chart-periods');
const strategyParams = require('./strategy-params');
const cascadewaveconvexMarkers = require('./cascadewaveconvex-markers');
const cascadewaveconcaveMarkers = require('./cascadewaveconcave-markers');
const cascadewaveconvexdayMarkers = require('./cascadewaveconvexday-markers');
const cascadewaveconcavedayMarkers = require('./cascadewaveconcaveday-markers');

var MARKER_MODULES = {
  cascadewaveconvex: cascadewaveconvexMarkers,
  cascadewaveconcave: cascadewaveconcaveMarkers,
  cascadewaveconvexday: cascadewaveconvexdayMarkers,
  cascadewaveconcaveday: cascadewaveconcavedayMarkers
};

function markerModuleForSource(sourceId) {
  return MARKER_MODULES[sourceId] || null;
}

function shouldShow(strategyId, period) {
  if (!cascadewaveBundle.isBundled(strategyId)) return false;
  return chartPeriods.isChartPeriod(period);
}

function resolveHitSource(item, strategyId) {
  var params = strategyParams.load(strategyId);
  return cascadewaveBundle.resolveHitSource(item, strategyId, params);
}

function resolveOverlayForItem(item, strategyId, period, klines) {
  if (!shouldShow(strategyId, period)) {
    return { barMarkers: [], priceLines: [] };
  }
  var sourceId = resolveHitSource(item, strategyId);
  var mod = markerModuleForSource(sourceId);
  if (!mod) return { barMarkers: [], priceLines: [] };
  return mod.resolveOverlayForItem(item, sourceId, period, klines);
}

function fetchOverlayForItem(item, strategyId, period) {
  if (!item || !item.code) return Promise.resolve({ barMarkers: [], priceLines: [] });
  var sourceId = resolveHitSource(item, strategyId);
  var mod = markerModuleForSource(sourceId);
  if (!mod || !mod.fetchOverlayForItem) {
    return Promise.resolve(resolveOverlayForItem(item, strategyId, period, []));
  }
  return mod.fetchOverlayForItem(item, sourceId, period);
}

function enrichItemsWithBundleMarkers(items, strategyId, period) {
  if (!items || !items.length || !shouldShow(strategyId, period)) {
    return Promise.resolve(items);
  }
  if (config.useMock) return Promise.resolve(items);
  return Promise.all(items.map(function (item) {
    return fetchOverlayForItem(item, strategyId, period).then(function (overlay) {
      return Object.assign({}, item, overlay);
    });
  }));
}

function syncDetailMarkers(page, detail, period, klines, fetchBySource) {
  var strategyId = adapter.extractStrategy(detail && detail.id);
  if (!shouldShow(strategyId, period)) {
    return Promise.resolve([]);
  }
  var params = strategyParams.load(strategyId);
  var subs = cascadewaveBundle.getActiveSubStrategies(strategyId, params);
  var sourceId = cascadewaveBundle.resolveHitSource(detail, strategyId, params);

  function trySource(idx) {
    if (idx >= subs.length) return Promise.resolve(null);
    var sid = idx === 0 && subs.indexOf(sourceId) >= 0 ? sourceId : subs[idx];
    var mod = markerModuleForSource(sid);
    var fetchFn = fetchBySource[sid];
    if (!mod || !fetchFn) return trySource(idx + 1);
    return fetchFn(detail.code, period, sid).then(function (vo) {
      if (vo && (vo.referenceDay || vo.signalDay)) {
        return { mod: mod, vo: vo, sourceId: sid };
      }
      return trySource(idx + 1);
    }).catch(function () {
      return trySource(idx + 1);
    });
  }

  var startIdx = Math.max(0, subs.indexOf(sourceId));
  return trySource(startIdx >= 0 ? startIdx : 0);
}

module.exports = {
  shouldShow: shouldShow,
  resolveOverlayForItem: resolveOverlayForItem,
  enrichItemsWithBundleMarkers: enrichItemsWithBundleMarkers,
  syncDetailMarkers: syncDetailMarkers,
  resolveHitSource: resolveHitSource
};
