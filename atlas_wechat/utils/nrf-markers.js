const stockApi = require('./stock-api');
const strategyParams = require('./strategy-params');
const markerUtils = require('./marker-utils');

function nrfPrimaryPeriod() {
  var bundle = strategyParams.load('nrf');
  var tier = bundle.activeTier || 'short';
  if (tier === 'medium') return 'week';
  if (tier === 'long') return 'month';
  return 'day';
}

module.exports = markerUtils.createRefSigMarkerModule({
  strategyId: 'nrf',
  refLabel: '基准K',
  shouldShowPeriod: function (strategyId, period) {
    return require('./adapter').normalizeStrategyId(strategyId) === 'nrf'
      && period === nrfPrimaryPeriod();
  },
  fetchMarkers: function (code, period) {
    return stockApi.fetchNrfMarkers(code, period);
  }
});
