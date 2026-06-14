const config = require('./config');
const stockApi = require('./stock-api');
const adapter = require('./adapter');

function parseMarkersFromSignal(item) {
  if (!item) return null;
  var text = [item.signalMessage, item.trendMessage, item.summary]
    .filter(function (s) { return s && String(s).trim(); })
    .join('|');
  if (!text) return null;
  var refM = text.match(/refDay=([^,|]+)/);
  var sigM = text.match(/sigDay=([^,|]+)/);
  if (!refM && !sigM) return null;
  return {
    referenceDay: refM ? refM[1].trim() : '',
    signalDay: sigM ? sigM[1].trim() : ''
  };
}

function markersVoToBarMarkers(m) {
  var markers = [];
  if (m && m.referenceDay) {
    markers.push({ day: m.referenceDay, label: '前高K', type: 'ref' });
  }
  if (m && m.signalDay) {
    markers.push({ day: m.signalDay, label: '突破K', type: 'signal' });
  }
  return markers;
}

function barMarkersFromItem(item) {
  return markersVoToBarMarkers(parseMarkersFromSignal(item));
}

function buildMockLadderMarkers(klines) {
  if (!klines || klines.length < 10) return [];
  return [
    { day: klines[klines.length - 8].day, label: '前高K', type: 'ref' },
    { day: klines[klines.length - 1].day, label: '突破K', type: 'signal' }
  ];
}

function isLadderStrategy(strategyId) {
  return adapter.normalizeStrategyId(strategyId) === 'ladder';
}

function shouldShowLadderMarkers(strategyId, period) {
  return isLadderStrategy(strategyId) && period === 'min30';
}

function resolveBarMarkersForItem(item, strategyId, period, klines) {
  if (!shouldShowLadderMarkers(strategyId, period)) return [];
  if (config.useMock) {
    return buildMockLadderMarkers(klines);
  }
  return barMarkersFromItem(item);
}

function fetchBarMarkersForItem(item) {
  var cached = barMarkersFromItem(item);
  if (cached.length) return Promise.resolve(cached);
  return stockApi.fetchLadderMarkers(item.code).then(function (m) {
    if (m && (m.referenceDay || m.signalDay)) {
      return markersVoToBarMarkers(m);
    }
    return stockApi.fetchSummary(item.code).then(function (summary) {
      var merged = Object.assign({}, item, {
        signalMessage: (summary && summary.signalMessage) || item.signalMessage || ''
      });
      return barMarkersFromItem(merged);
    });
  }).catch(function () {
    return [];
  });
}

function enrichItemsWithLadderMarkers(items, strategyId, period) {
  if (!items || !items.length || !shouldShowLadderMarkers(strategyId, period)) {
    return Promise.resolve(items || []);
  }
  if (config.useMock) {
    return Promise.resolve(items.map(function (item) {
      var klines = item.chartKlines
        || (item.klines && item.klines[period] ? item.klines[period] : []);
      return Object.assign({}, item, { barMarkers: buildMockLadderMarkers(klines) });
    }));
  }
  return Promise.all(items.map(function (item) {
    var cached = barMarkersFromItem(item);
    if (cached.length) {
      return Promise.resolve(Object.assign({}, item, { barMarkers: cached }));
    }
    return fetchBarMarkersForItem(item).then(function (markers) {
      return Object.assign({}, item, { barMarkers: markers });
    });
  }));
}

module.exports = {
  parseMarkersFromSignal: parseMarkersFromSignal,
  markersVoToBarMarkers: markersVoToBarMarkers,
  barMarkersFromItem: barMarkersFromItem,
  buildMockLadderMarkers: buildMockLadderMarkers,
  isLadderStrategy: isLadderStrategy,
  shouldShowLadderMarkers: shouldShowLadderMarkers,
  resolveBarMarkersForItem: resolveBarMarkersForItem,
  fetchBarMarkersForItem: fetchBarMarkersForItem,
  enrichItemsWithLadderMarkers: enrichItemsWithLadderMarkers
};
