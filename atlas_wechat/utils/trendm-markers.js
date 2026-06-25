const config = require('./config');
const stockApi = require('./stock-api');
const adapter = require('./adapter');

var TRENDM_MARKER_PERIODS = ['min30', 'day', 'week', 'month'];

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

function markersVoToBarMarkers(m, item, period) {
  var markers = [];
  if (period === 'min30') {
    if (m && m.referenceDay) {
      markers.push({ day: m.referenceDay, label: '基准K', type: 'ref' });
    }
    if (m && m.signalDay) {
      markers.push({ day: m.signalDay, label: '突破K', type: 'signal' });
    }
    return markers;
  }
  var refLabel = item ? refLabelFromSignal(item) : '基准K';
  if (m && m.referenceDay) {
    markers.push({ day: m.referenceDay, label: refLabel, type: 'ref' });
  }
  if (m && m.signalDay) {
    markers.push({ day: m.signalDay, label: '突破K', type: 'signal' });
  }
  return markers;
}

function barMarkersFromItem(item, period) {
  return markersVoToBarMarkers(parseMarkersFromSignal(item), item, period);
}

function buildMockTrendmMarkers(klines, period) {
  if (!klines || klines.length < 10) return [];
  return [
    { day: klines[klines.length - 8].day, label: period === 'min30' ? '基准K' : '基准K', type: 'ref' },
    { day: klines[klines.length - 1].day, label: '突破K', type: 'signal' }
  ];
}

function isTrendmStrategy(strategyId) {
  return adapter.normalizeStrategyId(strategyId) === 'trendm';
}

function shouldShowTrendmMarkers(strategyId, period) {
  return isTrendmStrategy(strategyId) && TRENDM_MARKER_PERIODS.indexOf(period) >= 0;
}

function resolveBarMarkersForItem(item, strategyId, period, klines) {
  if (!shouldShowTrendmMarkers(strategyId, period)) return [];
  if (config.useMock) {
    return buildMockTrendmMarkers(klines, period);
  }
  return barMarkersFromItem(item, period);
}

function fetchBarMarkersForItem(item, period, uiStrategyId) {
  if (period === 'min30') {
    var ultraMarkers = require('./ultra-markers');
    return ultraMarkers.fetchBarMarkersForItem(item, uiStrategyId || 'trendm', period);
  }
  var cached = barMarkersFromItem(item, period);
  if (cached.length) return Promise.resolve(cached);
  return stockApi.fetchTrendmMarkers(item.code, period).then(function (m) {
    if (m && (m.referenceDay || m.signalDay)) {
      return markersVoToBarMarkers(m, item, period);
    }
    return stockApi.fetchSummary(item.code).then(function (summary) {
      var merged = Object.assign({}, item, {
        signalMessage: (summary && summary.signalMessage) || item.signalMessage || '',
        trendMessage: (summary && summary.trendMessage) || item.trendMessage || ''
      });
      return barMarkersFromItem(merged, period);
    });
  }).catch(function () {
    return barMarkersFromItem(item, period);
  });
}

function enrichItemsWithTrendmMarkers(items, strategyId, period) {
  if (!items || !items.length || !shouldShowTrendmMarkers(strategyId, period)) {
    return Promise.resolve(items || []);
  }
  if (config.useMock) {
    return Promise.resolve(items.map(function (item) {
      var klines = item.chartKlines
        || (item.klines && item.klines[period] ? item.klines[period] : []);
      return Object.assign({}, item, {
        barMarkers: buildMockTrendmMarkers(klines, period),
        markerEpoch: Date.now()
      });
    }));
  }
  return Promise.all(items.map(function (item) {
    return fetchBarMarkersForItem(item, period, strategyId).then(function (markers) {
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
  buildMockTrendmMarkers: buildMockTrendmMarkers,
  isTrendmStrategy: isTrendmStrategy,
  shouldShowTrendmMarkers: shouldShowTrendmMarkers,
  resolveBarMarkersForItem: resolveBarMarkersForItem,
  fetchBarMarkersForItem: fetchBarMarkersForItem,
  enrichItemsWithTrendmMarkers: enrichItemsWithTrendmMarkers
};
