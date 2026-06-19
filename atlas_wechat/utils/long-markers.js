const config = require('./config');
const stockApi = require('./stock-api');
const adapter = require('./adapter');

var LONG_MARKER_PERIODS = ['month'];

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
    markers.push({ day: m.referenceDay, label: '基准K', type: 'ref' });
  }
  if (m && m.signalDay) {
    markers.push({ day: m.signalDay, label: '突破K', type: 'signal' });
  }
  return markers;
}

function barMarkersFromItem(item) {
  return markersVoToBarMarkers(parseMarkersFromSignal(item));
}

function buildMockLongMarkers(klines) {
  if (!klines || klines.length < 6) return [];
  return [
    { day: klines[klines.length - 4].day, label: '基准K', type: 'ref' },
    { day: klines[klines.length - 1].day, label: '突破K', type: 'signal' }
  ];
}

function isLongStrategy(strategyId) {
  return adapter.normalizeStrategyId(strategyId) === 'long';
}

function shouldShowLongMarkers(strategyId, period) {
  return isLongStrategy(strategyId) && LONG_MARKER_PERIODS.indexOf(period) >= 0;
}

function resolveBarMarkersForItem(item, strategyId, period, klines) {
  if (!shouldShowLongMarkers(strategyId, period)) return [];
  if (config.useMock) {
    return buildMockLongMarkers(klines);
  }
  return barMarkersFromItem(item);
}

function fetchBarMarkersForItem(item, period) {
  var cached = barMarkersFromItem(item);
  if (cached.length) return Promise.resolve(cached);
  return stockApi.fetchLongMarkers(item.code, period).then(function (m) {
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

function enrichItemsWithLongMarkers(items, strategyId, period) {
  if (!shouldShowLongMarkers(strategyId, period) || !items || !items.length) {
    return Promise.resolve(items || []);
  }
  if (config.useMock) {
    return Promise.resolve(items.map(function (item) {
      var klines = item.klines && item.klines[period] ? item.klines[period] : item.chartKlines;
      return Object.assign({}, item, { barMarkers: buildMockLongMarkers(klines) });
    }));
  }
  return Promise.all(items.map(function (item) {
    return fetchBarMarkersForItem(item, period).then(function (markers) {
      return Object.assign({}, item, { barMarkers: markers });
    });
  }));
}

module.exports = {
  parseMarkersFromSignal: parseMarkersFromSignal,
  markersVoToBarMarkers: markersVoToBarMarkers,
  buildMockLongMarkers: buildMockLongMarkers,
  isLongStrategy: isLongStrategy,
  shouldShowLongMarkers: shouldShowLongMarkers,
  resolveBarMarkersForItem: resolveBarMarkersForItem,
  enrichItemsWithLongMarkers: enrichItemsWithLongMarkers
};
