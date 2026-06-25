const config = require('./config');
const stockApi = require('./stock-api');
const adapter = require('./adapter');

var BOGO_MARKER_PERIODS = ['day', 'week', 'month'];

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

function refLabelFromSignal(item) {
  var text = [item.signalMessage, item.trendMessage].join('|');
  if (/crossType=GC/.test(text)) return '金叉K';
  if (/crossType=DC/.test(text)) return '死叉K';
  return '基准K';
}

function markersVoToBarMarkers(m, item) {
  var markers = [];
  var refLabel = item ? refLabelFromSignal(item) : '基准K';
  if (m && m.referenceDay) {
    markers.push({ day: m.referenceDay, label: refLabel, type: 'ref' });
  }
  if (m && m.signalDay) {
    markers.push({ day: m.signalDay, label: '突破K', type: 'signal' });
  }
  return markers;
}

function barMarkersFromItem(item) {
  return markersVoToBarMarkers(parseMarkersFromSignal(item), item);
}

function buildMockBogoMarkers(klines) {
  if (!klines || klines.length < 10) return [];
  return [
    { day: klines[klines.length - 8].day, label: '基准K', type: 'ref' },
    { day: klines[klines.length - 1].day, label: '突破K', type: 'signal' }
  ];
}

function isBogoStrategy(strategyId) {
  return adapter.normalizeStrategyId(strategyId) === 'bogo';
}

function shouldShowBogoMarkers(strategyId, period) {
  return isBogoStrategy(strategyId) && BOGO_MARKER_PERIODS.indexOf(period) >= 0;
}

function resolveBarMarkersForItem(item, strategyId, period, klines) {
  if (!shouldShowBogoMarkers(strategyId, period)) return [];
  if (config.useMock) {
    return buildMockBogoMarkers(klines);
  }
  return barMarkersFromItem(item);
}

function fetchBarMarkersForItem(item, period) {
  var cached = barMarkersFromItem(item);
  if (cached.length) return Promise.resolve(cached);
  return stockApi.fetchBogoMarkers(item.code, period).then(function (m) {
    if (m && (m.referenceDay || m.signalDay)) {
      return markersVoToBarMarkers(m, item);
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

function enrichItemsWithBogoMarkers(items, strategyId, period) {
  if (!items || !items.length || !shouldShowBogoMarkers(strategyId, period)) {
    return Promise.resolve(items || []);
  }
  if (config.useMock) {
    return Promise.resolve(items.map(function (item) {
      var klines = item.chartKlines
        || (item.klines && item.klines[period] ? item.klines[period] : []);
      return Object.assign({}, item, {
        barMarkers: buildMockBogoMarkers(klines),
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
  buildMockBogoMarkers: buildMockBogoMarkers,
  isBogoStrategy: isBogoStrategy,
  shouldShowBogoMarkers: shouldShowBogoMarkers,
  resolveBarMarkersForItem: resolveBarMarkersForItem,
  fetchBarMarkersForItem: fetchBarMarkersForItem,
  enrichItemsWithBogoMarkers: enrichItemsWithBogoMarkers
};
