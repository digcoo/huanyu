const stockApi = require('./stock-api');
const markerUtils = require('./marker-utils');

module.exports = markerUtils.createRefSigMarkerModule({
  strategyId: 'ultra',
  periods: ['min30'],
  refLabel: '基准K',
  shouldShowPeriod: function (strategyId, period) {
    strategyId = require('./adapter').normalizeStrategyId(strategyId);
    if (period !== 'min30') return false;
    return strategyId === 'ultra' || strategyId === 'nrf' || strategyId === 'cascade';
  },
  fetchMarkers: function (code, period, uiStrategyId) {
    return stockApi.fetchUltraMarkers(code, period, uiStrategyId);
  }
});
