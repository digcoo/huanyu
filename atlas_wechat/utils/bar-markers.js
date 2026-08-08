/**
 * 列表/详情 K 线标记：MA多头3M、MA交叉K突破 — 标注基准K + 突破K
 */
const adapter = require('./adapter');
const strategyNav = require('./strategy-nav');
const markerUtils = require('./marker-utils');

function normalizeId(strategyId) {
  return adapter.normalizeStrategyId(strategyId || '');
}

function isMarkedStrategy(strategyId) {
  var id = normalizeId(strategyId);
  return /^mabull3m/.test(id) || /^mabreakma/.test(id) || /^mabull4m/.test(id);
}

function chartPeriodForStrategy(strategyId) {
  return strategyNav.tierForStrategyId(normalizeId(strategyId)) || 'day';
}

function shouldShowBarMarkers(strategyId, period) {
  if (!isMarkedStrategy(strategyId)) {
    return false;
  }
  return period === chartPeriodForStrategy(strategyId);
}

function resolveBarMarkersForItem(item, strategyId, period, klines) {
  if (!shouldShowBarMarkers(strategyId, period)) {
    return [];
  }
  var parsed = markerUtils.parseRefSigFromSignal(item);
  if (markerUtils.hasRefSigParsed(parsed)) {
    var label = markerUtils.resolveRefLabelFromItem(item, '基准K');
    return markerUtils.refSigMarkersVoToBar(parsed, label);
  }
  if (klines && klines.length >= 3) {
    return markerUtils.buildMockRefSigMarkers(klines, '基准K');
  }
  return [];
}

function enrichItemsWithBarMarkers(items, strategyId, period) {
  if (!items || !items.length || !shouldShowBarMarkers(strategyId, period)) {
    return Promise.resolve(items || []);
  }
  var enriched = items.map(function (item) {
    var klines = item.chartKlines
      || (item.klines && item.klines[period] ? item.klines[period] : []);
    var markers = resolveBarMarkersForItem(item, strategyId, period, klines);
    if (!markers.length) {
      return item;
    }
    return Object.assign({}, item, {
      barMarkers: markers,
      markerEpoch: Date.now()
    });
  });
  return Promise.resolve(enriched);
}

module.exports = {
  shouldShowBarMarkers: shouldShowBarMarkers,
  resolveBarMarkersForItem: resolveBarMarkersForItem,
  enrichItemsWithBarMarkers: enrichItemsWithBarMarkers,
  isMarkedStrategy: isMarkedStrategy,
  chartPeriodForStrategy: chartPeriodForStrategy
};
