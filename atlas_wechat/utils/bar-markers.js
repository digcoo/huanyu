const ultraMarkers = require('./ultra-markers');
const trendMarkers = require('./trend-markers');
const mediumMarkers = require('./medium-markers');
const longMarkers = require('./long-markers');
const cascadeMarkers = require('./cascade-markers');
const cladderMarkers = require('./cladder-markers');
const nrfMarkers = require('./nrf-markers');
const strategyParams = require('./strategy-params');

function markerStrategyId(strategyId) {
  return strategyParams.resolveApiStrategyId(strategyId);
}

function shouldShowBarMarkers(strategyId, period) {
  return ultraMarkers.shouldShowUltraMarkers(strategyId, period)
    || nrfMarkers.shouldShowNrfMarkers(strategyId, period)
    || trendMarkers.shouldShowTrendMarkers(strategyId, period)
    || mediumMarkers.shouldShowMediumMarkers(strategyId, period)
    || longMarkers.shouldShowLongMarkers(strategyId, period)
    || cascadeMarkers.shouldShowCascadeMarkers(strategyId, period)
    || cladderMarkers.shouldShowCladderMarkers(strategyId, period);
}

function resolveBarMarkersForItem(item, strategyId, period, klines) {
  if (ultraMarkers.shouldShowUltraMarkers(strategyId, period)) {
    return ultraMarkers.resolveBarMarkersForItem(item, strategyId, period, klines);
  }
  if (nrfMarkers.shouldShowNrfMarkers(strategyId, period)) {
    return nrfMarkers.resolveBarMarkersForItem(item, strategyId, period, klines);
  }
  var sid = markerStrategyId(strategyId);
  if (trendMarkers.shouldShowTrendMarkers(strategyId, period)) {
    return trendMarkers.resolveBarMarkersForItem(item, sid, period, klines);
  }
  if (mediumMarkers.shouldShowMediumMarkers(strategyId, period)) {
    return mediumMarkers.resolveBarMarkersForItem(item, sid, period, klines);
  }
  if (longMarkers.shouldShowLongMarkers(strategyId, period)) {
    return longMarkers.resolveBarMarkersForItem(item, sid, period, klines);
  }
  if (cascadeMarkers.shouldShowCascadeMarkers(strategyId, period)) {
    return cascadeMarkers.resolveBarMarkersForItem(item, strategyId, period, klines);
  }
  if (cladderMarkers.shouldShowCladderMarkers(strategyId, period)) {
    return cladderMarkers.resolveBarMarkersForItem(item, strategyId, period, klines);
  }
  return [];
}

function enrichItemsWithBarMarkers(items, strategyId, period) {
  if (ultraMarkers.shouldShowUltraMarkers(strategyId, period)) {
    return ultraMarkers.enrichItemsWithUltraMarkers(items, strategyId, period);
  }
  if (nrfMarkers.shouldShowNrfMarkers(strategyId, period)) {
    return nrfMarkers.enrichItemsWithNrfMarkers(items, strategyId, period);
  }
  var sid = markerStrategyId(strategyId);
  if (trendMarkers.shouldShowTrendMarkers(strategyId, period)) {
    return trendMarkers.enrichItemsWithTrendMarkers(items, sid, period);
  }
  if (mediumMarkers.shouldShowMediumMarkers(strategyId, period)) {
    return mediumMarkers.enrichItemsWithMediumMarkers(items, sid, period);
  }
  if (longMarkers.shouldShowLongMarkers(strategyId, period)) {
    return longMarkers.enrichItemsWithLongMarkers(items, sid, period);
  }
  if (cascadeMarkers.shouldShowCascadeMarkers(strategyId, period)) {
    return cascadeMarkers.enrichItemsWithCascadeMarkers(items, strategyId, period);
  }
  if (cladderMarkers.shouldShowCladderMarkers(strategyId, period)) {
    return cladderMarkers.enrichItemsWithCladderMarkers(items, strategyId, period);
  }
  return Promise.resolve(items || []);
}

module.exports = {
  shouldShowBarMarkers: shouldShowBarMarkers,
  resolveBarMarkersForItem: resolveBarMarkersForItem,
  enrichItemsWithBarMarkers: enrichItemsWithBarMarkers
};
