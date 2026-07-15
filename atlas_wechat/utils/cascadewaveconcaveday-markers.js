const config = require('./config');
const stockApi = require('./stock-api');
const adapter = require('./adapter');
const markerUtils = require('./marker-utils');

function refLabelFromSignal() {
  return '末阳K';
}

function breakLineLabel(m, item) {
  var text = item ? [item.signalMessage, item.trendMessage].join('|') : '';
  var label = /breakPath=PREV_BAND_HIGH/.test(text) ? '前波段末阳顶' : '末波段末阳顶';
  var price = m && m.referenceHigh != null ? Number(m.referenceHigh).toFixed(2) : '';
  if (!price) return label;
  return label + ' ' + price;
}

function parseBreakLineFromSignal(item) {
  if (!item) return null;
  var text = [item.signalMessage, item.trendMessage].join('|');
  var lineM = text.match(/breakLine=([0-9.]+)/);
  if (lineM) return parseFloat(lineM[1]);
  var highM = text.match(/lastHigh=([0-9.]+)/);
  if (highM) return parseFloat(highM[1]);
  return null;
}

function priceLinesFromVo(m, item) {
  var price = m && m.referenceHigh != null ? Number(m.referenceHigh) : parseBreakLineFromSignal(item);
  if (price == null || isNaN(price)) return [];
  return [{
    price: price,
    label: breakLineLabel({ referenceHigh: price }, item),
    type: 'break'
  }];
}

function markersVoToBarMarkers(m, item) {
  return markerUtils.refSigMarkersVoToBar(m, refLabelFromSignal(item));
}

function overlayFromVo(m, item) {
  return {
    barMarkers: markersVoToBarMarkers(m, item),
    priceLines: priceLinesFromVo(m, item)
  };
}

function overlayFromItem(item) {
  return overlayFromVo(markerUtils.parseRefSigFromSignal(item), item);
}

function shouldShow(id, period) {
  if (adapter.normalizeStrategyId(id) !== 'cascadewaveconcaveday') return false;
  return ['day', 'week', 'month'].indexOf(period) >= 0;
}

function resolveOverlayForItem(item, id, period, klines) {
  if (!shouldShow(id, period)) {
    return { barMarkers: [], priceLines: [] };
  }
  if (config.useMock) {
    var mockPrice = klines && klines.length ? klines[klines.length - 3].close : null;
    return {
      barMarkers: markerUtils.buildMockRefSigMarkers(klines, refLabelFromSignal(item)),
      priceLines: mockPrice != null ? [{
        price: mockPrice,
        label: '突破线 ' + Number(mockPrice).toFixed(2),
        type: 'break'
      }] : []
    };
  }
  return overlayFromItem(item);
}

function fetchOverlayForItem(item, id, period) {
  if (!item || !item.code) return Promise.resolve({ barMarkers: [], priceLines: [] });
  return stockApi.fetchCascadewaveconcavedayMarkers(item.code, period)
    .then(function (vo) {
      if (vo) return overlayFromVo(vo, item);
      return stockApi.fetchSummary(item.code).then(function () {
        return overlayFromItem(item);
      });
    })
    .catch(function () {
      return overlayFromItem(item);
    });
}

function enrichItemsWithCascadewaveconcavedayMarkers(items, strategyId, period) {
  if (!items || !items.length || !shouldShow(strategyId, period)) {
    return Promise.resolve(items);
  }
  if (config.useMock) return Promise.resolve(items);
  return Promise.all(items.map(function (item) {
    return fetchOverlayForItem(item, strategyId, period).then(function (overlay) {
      return Object.assign({}, item, overlay);
    });
  }));
}

function resolveBarMarkersForItem(item, strategyId, period, klines) {
  return resolveOverlayForItem(item, strategyId, period, klines).barMarkers;
}

function shouldShowCascadewaveconcavedayMarkers(id, period) {
  return shouldShow(id, period);
}

module.exports = {
  shouldShowCascadewaveconcavedayMarkers: shouldShowCascadewaveconcavedayMarkers,
  resolveOverlayForItem: resolveOverlayForItem,
  resolveBarMarkersForItem: resolveBarMarkersForItem,
  enrichItemsWithCascadewaveconcavedayMarkers: enrichItemsWithCascadewaveconcavedayMarkers,
  overlayFromVo: overlayFromVo,
  overlayFromItem: overlayFromItem,
  fetchOverlayForItem: fetchOverlayForItem
};
