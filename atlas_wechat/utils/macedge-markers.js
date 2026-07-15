const stockApi = require('./stock-api');
const markerUtils = require('./marker-utils');

function refLabelFromSignal(item) {
  var text = [item && item.signalMessage, item && item.trendMessage].join('|');
  if (/crossType=GC/.test(text)) return '金叉K';
  if (/crossType=DC/.test(text)) return '死叉K';
  return '基准K';
}

module.exports = markerUtils.createRefSigMarkerModule({
  strategyId: 'macedge',
  periods: ['min30', 'day', 'week', 'month', 'year'],
  refLabel: refLabelFromSignal,
  fetchMarkers: function (code, period) {
    return stockApi.fetchMacdEdgeMarkers(code, period);
  }
});
