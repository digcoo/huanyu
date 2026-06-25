const ultraMarkers = require('./ultra-markers');
const trendMarkers = require('./trend-markers');
const mediumMarkers = require('./medium-markers');
const longMarkers = require('./long-markers');
const bogoMarkers = require('./bogo-markers');
const trendmMarkers = require('./trendm-markers');
const strategyParams = require('./strategy-params');

function markerStrategyId(strategyId) {
  return strategyParams.resolveApiStrategyId(strategyId);
}

function shouldShowBarMarkers(strategyId, period) {
  return ultraMarkers.shouldShowUltraMarkers(strategyId, period)
    || trendMarkers.shouldShowTrendMarkers(strategyId, period)
    || mediumMarkers.shouldShowMediumMarkers(strategyId, period)
    || longMarkers.shouldShowLongMarkers(strategyId, period)
    || bogoMarkers.shouldShowBogoMarkers(strategyId, period)
    || trendmMarkers.shouldShowTrendmMarkers(strategyId, period);
}

function resolveBarMarkersForItem(item, strategyId, period, klines) {
  if (ultraMarkers.shouldShowUltraMarkers(strategyId, period)) {
    return ultraMarkers.resolveBarMarkersForItem(item, strategyId, period, klines);
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
  if (bogoMarkers.shouldShowBogoMarkers(strategyId, period)) {
    return bogoMarkers.resolveBarMarkersForItem(item, strategyId, period, klines);
  }
  if (trendmMarkers.shouldShowTrendmMarkers(strategyId, period)) {
    return trendmMarkers.resolveBarMarkersForItem(item, strategyId, period, klines);
  }
  return [];
}

function enrichItemsWithBarMarkers(items, strategyId, period) {
  if (ultraMarkers.shouldShowUltraMarkers(strategyId, period)) {
    return ultraMarkers.enrichItemsWithUltraMarkers(items, strategyId, period);
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
  if (bogoMarkers.shouldShowBogoMarkers(strategyId, period)) {
    return bogoMarkers.enrichItemsWithBogoMarkers(items, strategyId, period);
  }
  if (trendmMarkers.shouldShowTrendmMarkers(strategyId, period)) {
    return trendmMarkers.enrichItemsWithTrendmMarkers(items, strategyId, period);
  }
  return Promise.resolve(items || []);
}

module.exports = {
  shouldShowBarMarkers: shouldShowBarMarkers,
  resolveBarMarkersForItem: resolveBarMarkersForItem,
  enrichItemsWithBarMarkers: enrichItemsWithBarMarkers
};
