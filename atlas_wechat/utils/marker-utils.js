const config = require('./config');
const stockApi = require('./stock-api');
const adapter = require('./adapter');

function parseRefSigFromSignal(item) {
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

function refSigMarkersVoToBar(m, refLabel) {
  var label = refLabel || '基准K';
  var markers = [];
  if (m && m.referenceDay) {
    markers.push({ day: m.referenceDay, label: label, type: 'ref' });
  }
  if (m && m.signalDay) {
    markers.push({ day: m.signalDay, label: '突破K', type: 'signal' });
  }
  return markers;
}

function buildMockRefSigMarkers(klines, refLabel) {
  if (!klines || klines.length < 10) return [];
  return [
    { day: klines[klines.length - 8].day, label: refLabel || '基准K', type: 'ref' },
    { day: klines[klines.length - 1].day, label: '突破K', type: 'signal' }
  ];
}

function hasRefSigParsed(parsed) {
  return parsed && (parsed.referenceDay || parsed.signalDay);
}

/**
 * 标准 ref/sig 双标记模块工厂（列表 enrich + 单条 resolve）
 */
function createRefSigMarkerModule(opts) {
  var strategyId = opts.strategyId;
  var periods = opts.periods || [];
  var refLabel = opts.refLabel || '基准K';
  var fetchMarkers = opts.fetchMarkers;
  var shouldShowPeriod = opts.shouldShowPeriod;

  function isStrategy(id) {
    return adapter.normalizeStrategyId(id) === strategyId;
  }

  function shouldShow(id, period) {
    if (shouldShowPeriod) return shouldShowPeriod(id, period);
    if (!isStrategy(id)) return false;
    return periods.indexOf(period) >= 0;
  }

  function resolveRefLabel(item) {
    if (typeof refLabel === 'function') return refLabel(item);
    return refLabel;
  }

  function markersVoToBarMarkers(m, item) {
    return refSigMarkersVoToBar(m, resolveRefLabel(item));
  }

  function barMarkersFromItem(item) {
    return markersVoToBarMarkers(parseRefSigFromSignal(item), item);
  }

  function buildMockMarkers(klines, item) {
    return buildMockRefSigMarkers(klines, resolveRefLabel(item));
  }

  function resolveBarMarkersForItem(item, id, period, klines) {
    if (!shouldShow(id, period)) return [];
    if (config.useMock) return buildMockMarkers(klines, item);
    return barMarkersFromItem(item);
  }

  function fetchBarMarkersForItem(item, period, uiStrategyId) {
    var cached = barMarkersFromItem(item);
    if (cached.length) return Promise.resolve(cached);
    return fetchMarkers(item.code, period, uiStrategyId).then(function (m) {
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

  function enrichItems(items, id, period) {
    var enrichName = opts.enrichExportName || ('enrichItemsWith' + strategyId.charAt(0).toUpperCase() + strategyId.slice(1) + 'Markers');
    if (!items || !items.length || !shouldShow(id, period)) {
      return Promise.resolve(items || []);
    }
    if (config.useMock) {
      return Promise.resolve(items.map(function (item) {
        var klines = item.chartKlines
          || (item.klines && item.klines[period] ? item.klines[period] : []);
        return Object.assign({}, item, {
          barMarkers: buildMockMarkers(klines, item),
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
      return fetchBarMarkersForItem(item, period, id).then(function (markers) {
        return Object.assign({}, item, {
          barMarkers: markers,
          markerEpoch: Date.now()
        });
      });
    }));
  }

  var mod = {
    parseMarkersFromSignal: parseRefSigFromSignal,
    markersVoToBarMarkers: markersVoToBarMarkers,
    barMarkersFromItem: barMarkersFromItem,
    buildMockMarkers: buildMockMarkers,
    isStrategy: isStrategy,
    shouldShow: shouldShow,
    resolveBarMarkersForItem: resolveBarMarkersForItem,
    fetchBarMarkersForItem: fetchBarMarkersForItem,
    enrichItems: enrichItems,
    hasRefSigParsed: hasRefSigParsed
  };

  if (strategyId === 'ultra') {
    mod.shouldShowUltraMarkers = shouldShow;
    mod.buildMockUltraMarkers = function (klines) { return buildMockMarkers(klines); };
    mod.enrichItemsWithUltraMarkers = enrichItems;
  } else if (strategyId === 'nrf') {
    mod.shouldShowNrfMarkers = shouldShow;
    mod.buildMockNrfMarkers = function (klines) { return buildMockMarkers(klines); };
    mod.enrichItemsWithNrfMarkers = enrichItems;
  } else if (strategyId === 'cascade') {
    mod.shouldShowCascadeMarkers = shouldShow;
    mod.isCascadeStrategy = isStrategy;
    mod.buildMockCascadeMarkers = function (klines) { return buildMockMarkers(klines); };
    mod.enrichItemsWithCascadeMarkers = enrichItems;
  } else if (strategyId === 'trend') {
    mod.shouldShowTrendMarkers = shouldShow;
    mod.buildMockTrendMarkers = function (klines) { return buildMockMarkers(klines); };
    mod.enrichItemsWithTrendMarkers = enrichItems;
  } else if (strategyId === 'medium') {
    mod.shouldShowMediumMarkers = shouldShow;
    mod.buildMockMediumMarkers = function (klines) { return buildMockMarkers(klines); };
    mod.enrichItemsWithMediumMarkers = enrichItems;
  } else if (strategyId === 'long') {
    mod.shouldShowLongMarkers = shouldShow;
    mod.buildMockLongMarkers = function (klines) { return buildMockMarkers(klines); };
    mod.enrichItemsWithLongMarkers = enrichItems;
  }

  return mod;
}

module.exports = {
  parseRefSigFromSignal: parseRefSigFromSignal,
  refSigMarkersVoToBar: refSigMarkersVoToBar,
  buildMockRefSigMarkers: buildMockRefSigMarkers,
  hasRefSigParsed: hasRefSigParsed,
  createRefSigMarkerModule: createRefSigMarkerModule
};
