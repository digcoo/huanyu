const config = require('./config');
const stockApi = require('./stock-api');
const adapter = require('./adapter');

var DC2_MARKER_PERIODS = ['day', 'week'];

function parseMarkersFromSignal(item) {
  if (!item) return null;
  var text = [item.signalMessage, item.trendMessage, item.summary]
    .filter(function (s) { return s && String(s).trim(); })
    .join('|');
  if (!text) return null;
  var refM = text.match(/refDay=([^,|]+(?:\s[^,|]+)*)/);
  var sigM = text.match(/sigDay=([^,|]+(?:\s[^,|]+)*)/);
  if (!refM && !sigM) return null;
  return {
    referenceDay: refM ? refM[1].trim() : '',
    signalDay: sigM ? sigM[1].trim() : ''
  };
}

function markersVoToBarMarkers(m) {
  var markers = [];
  if (m && m.referenceDay) {
    markers.push({ day: m.referenceDay, label: '死叉K', type: 'ref' });
  }
  if (m && m.signalDay) {
    markers.push({ day: m.signalDay, label: '突破K', type: 'signal' });
  }
  return markers;
}

function barMarkersFromItem(item) {
  return markersVoToBarMarkers(parseMarkersFromSignal(item));
}

function buildMockDc2Markers(klines) {
  if (!klines || klines.length < 10) return [];
  return [
    { day: klines[klines.length - 8].day, label: '死叉K', type: 'ref' },
    { day: klines[klines.length - 1].day, label: '突破K', type: 'signal' }
  ];
}

function isDc2Strategy(strategyId) {
  return adapter.normalizeStrategyId(strategyId) === 'dc2';
}

function shouldShowDc2Markers(strategyId, period) {
  return isDc2Strategy(strategyId) && DC2_MARKER_PERIODS.indexOf(period) >= 0;
}

function resolveBarMarkersForItem(item, strategyId, period, klines) {
  if (!shouldShowDc2Markers(strategyId, period)) return [];
  if (config.useMock) {
    return buildMockDc2Markers(klines);
  }
  return barMarkersFromItem(item);
}

function fetchBarMarkersForItem(item, period) {
  var cached = barMarkersFromItem(item);
  if (cached.length) return Promise.resolve(cached);
  return stockApi.fetchDc2Markers(item.code, period).then(function (m) {
    if (m && (m.referenceDay || m.signalDay)) {
      return markersVoToBarMarkers(m);
    }
    return stockApi.fetchSummary(item.code).then(function (summary) {
      var merged = Object.assign({}, item, {
        signalMessage: (summary && summary.signalMessage) || item.signalMessage || '',
        trendMessage: (summary && summary.trendMessage) || item.trendMessage || ''
      });
      return barMarkersFromItem(merged);
    });
  }).catch(function () {
    return barMarkersFromItem(item);
  });
}

function enrichItemsWithDc2Markers(items, strategyId, period) {
  if (!items || !items.length || !shouldShowDc2Markers(strategyId, period)) {
    return Promise.resolve(items || []);
  }
  if (config.useMock) {
    return Promise.resolve(items.map(function (item) {
      var klines = item.chartKlines
        || (item.klines && item.klines[period] ? item.klines[period] : []);
      return Object.assign({}, item, {
        barMarkers: buildMockDc2Markers(klines),
        markerEpoch: Date.now()
      });
    }));
  }
  return Promise.all(items.map(function (item) {
    var cached = barMarkersFromItem(item);
    if (cached.length) {
      return Promise.resolve(Object.assign({}, item, {
        barMarkers: cached,
        markerEpoch: Date.now()
      }));
    }
    return fetchBarMarkersForItem(item, period).then(function (markers) {
      return Object.assign({}, item, {
        barMarkers: markers,
        markerEpoch: Date.now()
      });
    });
  }));
}

module.exports = {
  parseMarkersFromSignal: parseMarkersFromSignal,
  markersVoToBarMarkers: markersVoToBarMarkers,
  barMarkersFromItem: barMarkersFromItem,
  buildMockDc2Markers: buildMockDc2Markers,
  isDc2Strategy: isDc2Strategy,
  shouldShowDc2Markers: shouldShowDc2Markers,
  resolveBarMarkersForItem: resolveBarMarkersForItem,
  fetchBarMarkersForItem: fetchBarMarkersForItem,
  enrichItemsWithDc2Markers: enrichItemsWithDc2Markers
};
