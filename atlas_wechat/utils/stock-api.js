const api = require('./api');
const adapter = require('./adapter');
const strategyParams = require('./strategy-params');

var RECOMMEND_PAGE_SIZE = 12;
/** min30：2 前日 + 1 当日 × 8 根/日，留余量避免截断 */
var MIN30_KLINE_LIMIT = 64;

function klineLimitForPeriod(period) {
  return period === 'min30' ? MIN30_KLINE_LIMIT : 50;
}

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

function resolveHasMore(data, page, itemCount) {
  if (!data) return false;
  var isMore = data.isMore;
  if (isMore === 1 || isMore === true || isMore === '1') return true;
  if (isMore === 0 || isMore === false || isMore === '0') return false;
  if (data.currentPage != null && data.totalPage != null) {
    return data.currentPage < data.totalPage;
  }
  if (data.totalNum != null && itemCount > 0) {
    var size = data.pageSize || RECOMMEND_PAGE_SIZE;
    return page * size < data.totalNum;
  }
  return itemCount >= RECOMMEND_PAGE_SIZE;
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
      hasMore: resolveHasMore(data, data.currentPage || page, items.length)
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
    limit: limit || klineLimitForPeriod(period)
  }).then(function (res) {
    if (!res.ok || !res.data) return [];
    return res.data;
  });
}

/** 雪球实时拉取 K 线（不写库） */
function fetchKlinesRefresh(code, period, limit) {
  period = period || 'week';
  limit = limit || klineLimitForPeriod(period);
  var qs = 'period=' + encodeURIComponent(period) + '&limit=' + encodeURIComponent(String(limit));
  return api.request({
    path: '/stock/' + encodePath(code) + '/klines/refresh?' + qs,
    method: 'POST',
    data: {}
  }).then(function (res) {
    if (!res.ok || !res.data) return [];
    return res.data;
  });
}

/** 梯子突破 · 基准 K / 突破 K 标记 */
function fetchLadderMarkers(code, period) {
  return api.get('/stock/' + encodePath(code) + '/ladder/markers', {
    period: period || 'min30'
  }).then(function (res) {
    if (!res.ok || !res.data) return null;
    return res.data;
  });
}

/** 超短线 · 基准 K / 突破 K 标记 */
function fetchUltraMarkers(code, period) {
  var params = Object.assign({ period: period || 'min30' }, strategyParams.toApiParams('ultra'));
  return api.get('/stock/' + encodePath(code) + '/ultra/markers', params).then(function (res) {
    if (!res.ok || !res.data) return null;
    return res.data;
  });
}

/** 回踩抬升 · L0/H1/L1/介入 标记 */
function fetchRetestMarkers(code, period) {
  return api.get('/stock/' + encodePath(code) + '/retest/markers', {
    period: period || 'day'
  }).then(function (res) {
    if (!res.ok || !res.data) return null;
    return res.data;
  });
}

/** 金叉二次突破 · 金叉K / 突破K 标记 */
function fetchGc2Markers(code, period) {
  return api.get('/stock/' + encodePath(code) + '/gc2/markers', {
    period: period || 'day'
  }).then(function (res) {
    if (!res.ok || !res.data) return null;
    return res.data;
  });
}

/** 死叉突破 · 死叉K / 突破K 标记 */
function fetchDc2Markers(code, period) {
  return api.get('/stock/' + encodePath(code) + '/dc2/markers', {
    period: period || 'day'
  }).then(function (res) {
    if (!res.ok || !res.data) return null;
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
    return fetchKlines(item.code, period, klineLimitForPeriod(period)).then(function (bars) {
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
  MIN30_KLINE_LIMIT: MIN30_KLINE_LIMIT,
  klineLimitForPeriod: klineLimitForPeriod,
  buildStrategyQueryParams: buildStrategyQueryParams,
  fetchHealth: fetchHealth,
  fetchRecommendations: fetchRecommendations,
  triggerStrategyRescan: triggerStrategyRescan,
  fetchKlines: fetchKlines,
  fetchKlinesRefresh: fetchKlinesRefresh,
  fetchLadderMarkers: fetchLadderMarkers,
  fetchUltraMarkers: fetchUltraMarkers,
  fetchRetestMarkers: fetchRetestMarkers,
  fetchGc2Markers: fetchGc2Markers,
  fetchDc2Markers: fetchDc2Markers,
  fetchUlowMarkers: fetchLadderMarkers,
  fetchSummary: fetchSummary,
  search: search,
  fetchDetail: fetchDetail,
  fetchCompass: fetchCompass,
  fetchMarketIndices: fetchMarketIndices,
  attachKlinesToItems: attachKlinesToItems
};
