const ladderMarkers = require('./ladder-markers');
const retestMarkers = require('./retest-markers');
const gc2Markers = require('./gc2-markers');

function shouldShowBarMarkers(strategyId, period) {
  return ladderMarkers.shouldShowLadderMarkers(strategyId, period)
    || retestMarkers.shouldShowRetestMarkers(strategyId, period)
    || gc2Markers.shouldShowGc2Markers(strategyId, period);
}

function resolveBarMarkersForItem(item, strategyId, period, klines) {
  if (ladderMarkers.shouldShowLadderMarkers(strategyId, period)) {
    return ladderMarkers.resolveBarMarkersForItem(item, strategyId, period, klines);
  }
  if (gc2Markers.shouldShowGc2Markers(strategyId, period)) {
    return gc2Markers.resolveBarMarkersForItem(item, strategyId, period, klines);
  }
  if (retestMarkers.shouldShowRetestMarkers(strategyId, period)) {
    return retestMarkers.resolveBarMarkersForItem(item, strategyId, period, klines);
  }
  return [];
}

function enrichItemsWithBarMarkers(items, strategyId, period) {
  if (ladderMarkers.shouldShowLadderMarkers(strategyId, period)) {
    return ladderMarkers.enrichItemsWithLadderMarkers(items, strategyId, period);
  }
  if (gc2Markers.shouldShowGc2Markers(strategyId, period)) {
    return gc2Markers.enrichItemsWithGc2Markers(items, strategyId, period);
  }
  if (retestMarkers.shouldShowRetestMarkers(strategyId, period)) {
    return retestMarkers.enrichItemsWithRetestMarkers(items, strategyId, period);
  }
  return Promise.resolve(items || []);
}

module.exports = {
  shouldShowBarMarkers: shouldShowBarMarkers,
  resolveBarMarkersForItem: resolveBarMarkersForItem,
  enrichItemsWithBarMarkers: enrichItemsWithBarMarkers
};
