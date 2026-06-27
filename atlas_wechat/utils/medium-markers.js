const stockApi = require('./stock-api');
const strategyParams = require('./strategy-params');
const markerUtils = require('./marker-utils');

module.exports = markerUtils.createRefSigMarkerModule({
  strategyId: 'medium',
  periods: ['week'],
  refLabel: '基准K',
  shouldShowPeriod: function (strategyId, period) {
    return strategyParams.resolveApiStrategyId(strategyId) === 'medium'
      && period === 'week';
  },
  fetchMarkers: function (code, period, uiStrategyId) {
    return stockApi.fetchMediumMarkers(code, period, uiStrategyId);
  }
});
