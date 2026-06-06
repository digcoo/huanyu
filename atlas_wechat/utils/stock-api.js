const api = require('./api');
const adapter = require('./adapter');
const config = require('./config');

function encodePath(code) {
  return encodeURIComponent(adapter.normalizeCode(code));
}

function fetchHealth() {
  return api.get('/stock/health');
}

function fetchRecommendations(strategyId) {
  var params = adapter.getStrategyApiParams(strategyId);
  return api.get('/stock/findMy', Object.assign({}, params, { all: true })).then(function (res) {
    if (!res.ok || !res.data || !res.data.items) {
      return [];
    }
    return res.data.items.map(function (item) {
      return adapter.mapRecommendation(item, strategyId);
    });
  });
}

function fetchKlines(code, period, limit) {
  return api.get('/stock/' + encodePath(code) + '/klines', {
    period: period || 'week',
    limit: limit || 50
  }).then(function (res) {
    if (!res.ok || !res.data) return [];
    return res.data;
  });
}

function fetchSummary(code) {
  return api.get('/stock/' + encodePath(code)).then(function (res) {
    if (!res.ok || !res.data) return null;
    return adapter.mapSearchItem(res.data);
  });
}

function search(keyword, limit) {
  return api.get('/stock/search', { q: keyword, limit: limit || 20 }).then(function (res) {
    if (!res.ok || !res.data) return [];
    return res.data.map(function (item) {
      return adapter.mapSearchItem(item);
    });
  });
}

function fetchDetail(code) {
  return api.get('/stock/' + encodePath(code) + '/detail');
}

function fetchCompass(code) {
  return api.get('/stock/' + encodePath(code) + '/compass');
}

function attachKlinesToItems(items, period, maxItems) {
  maxItems = maxItems || 12;
  var list = (items || []).slice(0, maxItems);
  if (!list.length) return Promise.resolve([]);

  return Promise.all(list.map(function (item) {
    return fetchKlines(item.code, period, 50).then(function (bars) {
      var klines = adapter.barsToKlines(bars);
      var merged = Object.assign({}, item, {
        klines: Object.assign({}, item.klines || {}, {}),
        chartKlines: klines
      });
      merged.klines[period] = klines;
      return merged;
    }).catch(function () {
      return Object.assign({}, item, { chartKlines: [] });
    });
  }));
}

module.exports = {
  fetchHealth: fetchHealth,
  fetchRecommendations: fetchRecommendations,
  fetchKlines: fetchKlines,
  fetchSummary: fetchSummary,
  search: search,
  fetchDetail: fetchDetail,
  fetchCompass: fetchCompass,
  attachKlinesToItems: attachKlinesToItems
};
