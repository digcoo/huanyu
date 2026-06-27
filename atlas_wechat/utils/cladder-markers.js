const config = require('./config');
const stockApi = require('./stock-api');
const adapter = require('./adapter');

var CLADDER_PERIODS = ['day', 'week', 'month'];

var REF_LABELS = {
  day: '日基准K',
  week: '周基准K',
  month: '月基准K'
};

function refLabelFor(period) {
  return REF_LABELS[period] || '基准K';
}

/** 从 signalMessage 解析指定周期的 refDay / sigDay */
function parseMarkersFromSignal(item, period) {
  if (!item || !period) return null;
  var text = [item.signalMessage, item.trendMessage, item.summary]
    .filter(function (s) { return s && String(s).trim(); })
    .join('|');
  if (!text) return null;
  var re = /period=(day|week|month),refDay=([^,]+),refHigh=[^,]+,refLow=[^,]+,sigDay=([^,]+)/g;
  var m;
  while ((m = re.exec(text)) !== null) {
    if (m[1] === period) {
      return {
        referenceDay: m[2].trim(),
        signalDay: m[3].trim()
      };
    }
  }
  return null;
}

function markersVoToBarMarkers(m, period) {
  var markers = [];
  var refLabel = refLabelFor(period);
  if (m && m.referenceDay) {
    markers.push({ day: m.referenceDay, label: refLabel, type: 'ref' });
  }
  if (m && m.signalDay) {
    markers.push({ day: m.signalDay, label: '突破K', type: 'signal' });
  }
  return markers;
}

function barMarkersFromItem(item, period) {
  return markersVoToBarMarkers(parseMarkersFromSignal(item, period), period);
}

function buildMockCladderMarkers(klines, period) {
  if (!klines || klines.length < 10) return [];
  return [
    { day: klines[klines.length - 8].day, label: refLabelFor(period), type: 'ref' },
    { day: klines[klines.length - 1].day, label: '突破K', type: 'signal' }
  ];
}

function isCladderStrategy(strategyId) {
  return adapter.normalizeStrategyId(strategyId) === 'cladder';
}

function shouldShowCladderMarkers(strategyId, period) {
  return isCladderStrategy(strategyId) && CLADDER_PERIODS.indexOf(period) >= 0;
}

function resolveBarMarkersForItem(item, strategyId, period, klines) {
  if (!shouldShowCladderMarkers(strategyId, period)) return [];
  if (config.useMock) return buildMockCladderMarkers(klines, period);
  return barMarkersFromItem(item, period);
}

function fetchBarMarkersForItem(item, period) {
  var cached = barMarkersFromItem(item, period);
  if (cached.length) return Promise.resolve(cached);
  return stockApi.fetchCladderMarkers(item.code, period).then(function (m) {
    if (m && (m.referenceDay || m.signalDay)) {
      return markersVoToBarMarkers(m, period);
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

function enrichItemsWithCladderMarkers(items, strategyId, period) {
  if (!items || !items.length || !shouldShowCladderMarkers(strategyId, period)) {
    return Promise.resolve(items || []);
  }
  if (config.useMock) {
    return Promise.resolve(items.map(function (item) {
      var klines = item.chartKlines
        || (item.klines && item.klines[period] ? item.klines[period] : []);
      return Object.assign({}, item, {
        barMarkers: buildMockCladderMarkers(klines, period),
        markerEpoch: Date.now()
      });
    }));
  }
  return Promise.all(items.map(function (item) {
    var cached = barMarkersFromItem(item, period);
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
  buildMockCladderMarkers: buildMockCladderMarkers,
  isCladderStrategy: isCladderStrategy,
  shouldShowCladderMarkers: shouldShowCladderMarkers,
  resolveBarMarkersForItem: resolveBarMarkersForItem,
  fetchBarMarkersForItem: fetchBarMarkersForItem,
  enrichItemsWithCladderMarkers: enrichItemsWithCladderMarkers
};
