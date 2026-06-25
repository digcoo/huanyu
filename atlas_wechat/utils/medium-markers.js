const config = require('./config');
const stockApi = require('./stock-api');
const strategyParams = require('./strategy-params');

var MEDIUM_MARKER_PERIODS = ['week'];

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

function buildMockMediumMarkers(klines) {
  if (!klines || klines.length < 10) return [];
  return [
    { day: klines[klines.length - 8].day, label: '基准K', type: 'ref' },
    { day: klines[klines.length - 1].day, label: '突破K', type: 'signal' }
  ];
}

function isMediumStrategy(strategyId) {
  return strategyParams.resolveApiStrategyId(strategyId) === 'medium';
}

function shouldShowMediumMarkers(strategyId, period) {
  return isMediumStrategy(strategyId) && MEDIUM_MARKER_PERIODS.indexOf(period) >= 0;
}

function resolveBarMarkersForItem(item, strategyId, period, klines) {
  if (!shouldShowMediumMarkers(strategyId, period)) return [];
  if (config.useMock) {
    return buildMockMediumMarkers(klines);
  }
  return barMarkersFromItem(item);
}

function fetchBarMarkersForItem(item, uiStrategyId, period) {
  var cached = barMarkersFromItem(item);
  if (cached.length) return Promise.resolve(cached);
  return stockApi.fetchMediumMarkers(item.code, period, uiStrategyId).then(function (m) {
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

function enrichItemsWithMediumMarkers(items, strategyId, period) {
  if (!shouldShowMediumMarkers(strategyId, period) || !items || !items.length) {
    return Promise.resolve(items || []);
  }
  if (config.useMock) {
    return Promise.resolve(items.map(function (item) {
      var klines = item.klines && item.klines[period] ? item.klines[period] : item.chartKlines;
      return Object.assign({}, item, { barMarkers: buildMockMediumMarkers(klines) });
    }));
  }
  return Promise.all(items.map(function (item) {
    return fetchBarMarkersForItem(item, strategyId, period).then(function (markers) {
      return Object.assign({}, item, { barMarkers: markers });
    });
  }));
}

module.exports = {
  parseMarkersFromSignal: parseMarkersFromSignal,
  markersVoToBarMarkers: markersVoToBarMarkers,
  buildMockMediumMarkers: buildMockMediumMarkers,
  isMediumStrategy: isMediumStrategy,
  shouldShowMediumMarkers: shouldShowMediumMarkers,
  resolveBarMarkersForItem: resolveBarMarkersForItem,
  enrichItemsWithMediumMarkers: enrichItemsWithMediumMarkers
};
