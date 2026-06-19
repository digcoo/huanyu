const ultraMarkers = require('./ultra-markers');

function shouldShowBarMarkers(strategyId, period) {
  return ultraMarkers.shouldShowUltraMarkers(strategyId, period);
}

function resolveBarMarkersForItem(item, strategyId, period, klines) {
  return ultraMarkers.resolveBarMarkersForItem(item, strategyId, period, klines);
}

function enrichItemsWithBarMarkers(items, strategyId, period) {
  return ultraMarkers.enrichItemsWithUltraMarkers(items, strategyId, period);
}

module.exports = {
  shouldShowBarMarkers: shouldShowBarMarkers,
  resolveBarMarkersForItem: resolveBarMarkersForItem,
  enrichItemsWithBarMarkers: enrichItemsWithBarMarkers
};
