const stockApi = require('./stock-api');
const strategyParams = require('./strategy-params');
const markerUtils = require('./marker-utils');

module.exports = markerUtils.createRefSigMarkerModule({
  strategyId: 'long',
  periods: ['month'],
  refLabel: '基准K',
  shouldShowPeriod: function (strategyId, period) {
    return strategyParams.resolveApiStrategyId(strategyId) === 'long'
      && period === 'month';
  },
  fetchMarkers: function (code, period, uiStrategyId) {
    return stockApi.fetchLongMarkers(code, period, uiStrategyId);
  }
});
