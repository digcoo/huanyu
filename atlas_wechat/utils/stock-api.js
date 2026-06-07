const api = require('./api');
const adapter = require('./adapter');
const strategyParams = require('./strategy-params');

var RECOMMEND_PAGE_SIZE = 12;

function encodePath(code) {
  return encodeURIComponent(adapter.normalizeCode(code));
}

/** 策略 API 查询参数（findMy / rescan 共用） */
function buildStrategyQueryParams(strategyId) {
  return Object.assign(
    {},
    adapter.getStrategyApiParams(strategyId),
    strategyParams.toApiParams(strategyId)
  );
}

function fetchHealth() {
  return api.get('/stock/health');
}

function fetchRecommendations(strategyId, page, size) {
  page = page || 1;
  size = size || RECOMMEND_PAGE_SIZE;
  return api.get('/stock/findMy', Object.assign({}, buildStrategyQueryParams(strategyId), {
    all: true,
    page: page,
    size: size
  })).then(function (res) {
    if (!res.ok || !res.data) {
      return { items: [], page: page, totalNum: 0, hasMore: false };
    }
    var data = res.data;
    var items = (data.items || []).map(function (item) {
      return adapter.mapRecommendation(item, strategyId);
    });
    return {
      items: items,
      page: data.currentPage || page,
      totalNum: data.totalNum != null ? data.totalNum : items.length,
      hasMore: data.isMore === 1
    };
  });
}

/**
 * 重跑策略扫描并写入 stock_target（携带当前自定义参数）
 * @returns {Promise<{ok:boolean, saved:number, strategy:string}>}
 */
function triggerStrategyRescan(strategyId) {
  var params = buildStrategyQueryParams(strategyId);
  var qs = Object.keys(params)
    .filter(function (k) { return params[k] != null && params[k] !== ''; })
    .map(function (k) { return encodeURIComponent(k) + '=' + encodeURIComponent(params[k]); })
    .join('&');
  var path = '/stock/strategy/rescan' + (qs ? '?' + qs : '');
  return api.request({ path: path, method: 'POST', data: {} }).then(function (res) {
    if (!res.ok || !res.data) {
      return { ok: false, saved: 0, strategy: params.strategy || 'qsn' };
    }
    return {
      ok: true,
      saved: res.data.saved != null ? res.data.saved : 0,
      strategy: res.data.strategy || params.strategy
    };
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

function fetchMarketIndices(market, period, limit) {
  return api.get('/stock/indices', {
    market: market || 'cn',
    period: period || 'week',
    limit: limit || 50
  }).then(function (res) {
    if (!res.ok || !res.data) return [];
    return res.data;
  });
}

function attachKlinesToItems(items, period, maxItems) {
  var list = items || [];
  if (maxItems != null && maxItems > 0) {
    list = list.slice(0, maxItems);
  }
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
  RECOMMEND_PAGE_SIZE: RECOMMEND_PAGE_SIZE,
  buildStrategyQueryParams: buildStrategyQueryParams,
  fetchHealth: fetchHealth,
  fetchRecommendations: fetchRecommendations,
  triggerStrategyRescan: triggerStrategyRescan,
  fetchKlines: fetchKlines,
  fetchSummary: fetchSummary,
  search: search,
  fetchDetail: fetchDetail,
  fetchCompass: fetchCompass,
  fetchMarketIndices: fetchMarketIndices,
  attachKlinesToItems: attachKlinesToItems
};
