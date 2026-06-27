const stockApi = require('./stock-api');
const strategyParams = require('./strategy-params');
const markerUtils = require('./marker-utils');

module.exports = markerUtils.createRefSigMarkerModule({
  strategyId: 'trend',
  periods: ['day'],
  refLabel: '基准K',
  shouldShowPeriod: function (strategyId, period) {
    return strategyParams.resolveApiStrategyId(strategyId) === 'trend'
      && period === 'day';
  },
  fetchMarkers: function (code, period, uiStrategyId) {
    return stockApi.fetchTrendMarkers(code, period, uiStrategyId);
  }
});
