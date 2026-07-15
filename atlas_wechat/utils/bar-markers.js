const ultraMarkers = require('./ultra-markers');

function shouldShowBarMarkers(strategyId, period) {
  return ultraMarkers.shouldShowUltraMarkers(strategyId, period);
}

function resolveBarMarkersForItem(item, strategyId, period, klines) {
  if (ultraMarkers.shouldShowUltraMarkers(strategyId, period)) {
    return ultraMarkers.resolveBarMarkersForItem(item, strategyId, period, klines);
  }
  return [];
}

function enrichItemsWithBarMarkers(items, strategyId, period) {
  if (ultraMarkers.shouldShowUltraMarkers(strategyId, period)) {
    return ultraMarkers.enrichItemsWithUltraMarkers(items, strategyId, period);
  }
  return Promise.resolve(items || []);
}

module.exports = {
  shouldShowBarMarkers: shouldShowBarMarkers,
  resolveBarMarkersForItem: resolveBarMarkersForItem,
  enrichItemsWithBarMarkers: enrichItemsWithBarMarkers
};
