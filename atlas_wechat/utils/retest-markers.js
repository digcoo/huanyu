const config = require('./config');
const stockApi = require('./stock-api');
const adapter = require('./adapter');

var RETEST_MARKER_PERIODS = ['min30', 'day', 'week', 'month'];

function parseMarkersFromSignal(item) {
  if (!item) return null;
  var text = [item.signalMessage, item.trendMessage, item.summary]
    .filter(function (s) { return s && String(s).trim(); })
    .join('|');
  if (!text) return null;
  var l0M = text.match(/l0Day=([^,|]+)/);
  var h1M = text.match(/h1Day=([^,|]+)/);
  var l1M = text.match(/l1Day=([^,|]+)/);
  var sigM = text.match(/sigDay=([^,|]+)/);
  if (!l0M && !sigM) return null;
  return {
    l0Day: l0M ? l0M[1].trim() : '',
    h1Day: h1M ? h1M[1].trim() : '',
    l1Day: l1M ? l1M[1].trim() : '',
    signalDay: sigM ? sigM[1].trim() : ''
  };
}

function markersVoToBarMarkers(m) {
  var markers = [];
  if (m && m.l0Day) {
    markers.push({ day: m.l0Day, label: 'L0', type: 'l0' });
  }
  if (m && m.h1Day) {
    markers.push({ day: m.h1Day, label: 'H1', type: 'h1' });
  }
  if (m && m.l1Day) {
    markers.push({ day: m.l1Day, label: 'L1', type: 'l1' });
  }
  if (m && m.signalDay) {
    markers.push({ day: m.signalDay, label: '介入', type: 'signal' });
  }
  return markers;
}

function barMarkersFromItem(item) {
  return markersVoToBarMarkers(parseMarkersFromSignal(item));
}

function buildMockRetestMarkers(klines) {
  if (!klines || klines.length < 12) return [];
  return [
    { day: klines[klines.length - 10].day, label: 'L0', type: 'l0' },
    { day: klines[klines.length - 7].day, label: 'H1', type: 'h1' },
    { day: klines[klines.length - 4].day, label: 'L1', type: 'l1' },
    { day: klines[klines.length - 1].day, label: '介入', type: 'signal' }
  ];
}

function isRetestStrategy(strategyId) {
  return adapter.normalizeStrategyId(strategyId) === 'retest';
}

function shouldShowRetestMarkers(strategyId, period) {
  return isRetestStrategy(strategyId) && RETEST_MARKER_PERIODS.indexOf(period) >= 0;
}

function resolveBarMarkersForItem(item, strategyId, period, klines) {
  if (!shouldShowRetestMarkers(strategyId, period)) return [];
  if (config.useMock) {
    return buildMockRetestMarkers(klines);
  }
  return barMarkersFromItem(item);
}

function fetchBarMarkersForItem(item, period) {
  var cached = barMarkersFromItem(item);
  if (cached.length) return Promise.resolve(cached);
  return stockApi.fetchRetestMarkers(item.code, period).then(function (m) {
    if (m && (m.l0Day || m.signalDay)) {
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

function enrichItemsWithRetestMarkers(items, strategyId, period) {
  if (!items || !items.length || !shouldShowRetestMarkers(strategyId, period)) {
    return Promise.resolve(items || []);
  }
  if (config.useMock) {
    return Promise.resolve(items.map(function (item) {
      var klines = item.chartKlines
        || (item.klines && item.klines[period] ? item.klines[period] : []);
      return Object.assign({}, item, {
        barMarkers: buildMockRetestMarkers(klines),
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
  buildMockRetestMarkers: buildMockRetestMarkers,
  isRetestStrategy: isRetestStrategy,
  shouldShowRetestMarkers: shouldShowRetestMarkers,
  resolveBarMarkersForItem: resolveBarMarkersForItem,
  fetchBarMarkersForItem: fetchBarMarkersForItem,
  enrichItemsWithRetestMarkers: enrichItemsWithRetestMarkers
};
