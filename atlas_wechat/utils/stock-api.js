const api = require('./api');
const adapter = require('./adapter');
const strategyParams = require('./strategy-params');
const listMemory = require('./list-memory');

var RECOMMEND_PAGE_SIZE = 12;
/** min30：2 前日 + 1 当日 × 8 根/日，留余量避免截断 */
var MIN30_KLINE_LIMIT = 64;

function klineLimitForPeriod(period) {
  return period === 'min30' ? MIN30_KLINE_LIMIT : 50;
}

function klineLimitForList(period) {
  return listMemory.klineLimitForList(period);
}

function encodePath(code) {
  return encodeURIComponent(adapter.normalizeCode(code));
}

/** 策略 API 查询参数（findMy / rescan 共用） */
function buildStrategyQueryParams(strategyId) {
  var apiStrategyId = strategyParams.resolveApiStrategyId(strategyId);
  var apiConfigId = strategyId === 'nrf' ? 'nrf' : apiStrategyId;
  var params = Object.assign(
    {},
    adapter.getStrategyApiParams(apiConfigId),
    strategyParams.toApiParams(strategyId)
  );
  if (strategyId === 'nrf') {
    Object.assign(params, strategyParams.toApiParams('ultra'));
  } else if (strategyId === 'cladder') {
    var clParams = strategyParams.load('cladder');
    if (clParams.clRequireUltra !== false) {
      Object.assign(params, strategyParams.toApiParams('ultra'));
    }
  }
  return params;
}

function fetchHealth() {
  return api.get('/stock/health');
}

function toPageNum(v) {
  var n = Number(v);
  return n > 0 ? n : 1;
}

function buildQueryString(params) {
  return Object.keys(params || {})
    .filter(function (k) { return params[k] != null && params[k] !== ''; })
    .map(function (k) { return encodeURIComponent(k) + '=' + encodeURIComponent(params[k]); })
    .join('&');
}

function resolveHasMore(data, page, itemCount) {
  if (!data) return false;
  page = toPageNum(page);
  var isMore = data.isMore;
  if (isMore === 1 || isMore === true || isMore === '1') return true;
  if (data.currentPage != null && data.totalPage != null) {
    return toPageNum(data.currentPage) < toPageNum(data.totalPage);
  }
  if (isMore === 0 || isMore === false || isMore === '0') return false;
  if (data.totalNum != null && itemCount > 0) {
    var size = Number(data.pageSize) || RECOMMEND_PAGE_SIZE;
    return page * size < Number(data.totalNum);
  }
  return itemCount >= RECOMMEND_PAGE_SIZE;
}

function fetchRecommendations(strategyId, page, size) {
  page = toPageNum(page);
  size = size || RECOMMEND_PAGE_SIZE;
  var apiStrategyId = strategyParams.resolveApiStrategyId(strategyId);
  var query = Object.assign({}, buildStrategyQueryParams(strategyId), {
    all: 1,
    page: page,
    size: size
  });
  var qs = buildQueryString(query);
  var path = '/stock/findMy' + (qs ? '?' + qs : '');
  return api.request({
    path: path,
    method: 'GET',
    data: {},
    timeout: 120000
  }).then(function (res) {
    if (!res.ok) {
      var err = new Error(res.message || 'findMy failed');
      err.apiCode = res.code;
      return Promise.reject(err);
    }
    var data = res.data || {};
    var currentPage = toPageNum(data.currentPage || page);
    var rawItems = data.items || data.list || [];
    var items = rawItems.map(function (item) {
      var mapId = strategyId === 'nrf' ? 'nrf' : apiStrategyId;
      return adapter.mapRecommendation(item, mapId);
    }).filter(function (item) { return item && item.id; });
    return {
      items: items,
      page: currentPage,
      totalNum: data.totalNum != null ? Number(data.totalNum) : items.length,
      hasMore: resolveHasMore(data, currentPage, items.length)
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

function markerQueryParams(uiStrategyId, apiStrategyId) {
  if (uiStrategyId === 'nrf') {
    return strategyParams.toApiParams('nrf');
  }
  return strategyParams.toApiParams(apiStrategyId);
}

/** 超短线 · 基准 K / 突破 K 标记 */
function fetchUltraMarkers(code, period, uiStrategyId) {
  var params = Object.assign(
    { period: period || 'min30' },
    markerQueryParams(uiStrategyId || 'ultra', 'ultra')
  );
  return api.get('/stock/' + encodePath(code) + '/ultra/markers', params).then(function (res) {
    if (!res.ok || !res.data) return null;
    return res.data;
  });
}

/** 短线 · 基准 K / 突破 K 标记 */
function fetchTrendMarkers(code, period, uiStrategyId) {
  var params = Object.assign(
    { period: period || 'day' },
    markerQueryParams(uiStrategyId, 'trend')
  );
  return api.get('/stock/' + encodePath(code) + '/trend/markers', params).then(function (res) {
    if (!res.ok || !res.data) return null;
    return res.data;
  });
}

/** 中线 · 基准 K / 突破 K 标记 */
function fetchMediumMarkers(code, period, uiStrategyId) {
  var params = Object.assign(
    { period: period || 'week' },
    markerQueryParams(uiStrategyId, 'medium')
  );
  return api.get('/stock/' + encodePath(code) + '/medium/markers', params).then(function (res) {
    if (!res.ok || !res.data) return null;
    return res.data;
  });
}

/** 长线 · 基准 K / 突破 K 标记 */
function fetchLongMarkers(code, period, uiStrategyId) {
  var params = Object.assign(
    { period: period || 'month' },
    markerQueryParams(uiStrategyId, 'long')
  );
  return api.get('/stock/' + encodePath(code) + '/long/markers', params).then(function (res) {
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

/** 级联交叉突破 · 基准 K / 触发日 K 标记 */
function fetchCascadeMarkers(code, period) {
  return api.get('/stock/' + encodePath(code) + '/cascade/markers', {
    period: period || 'day'
  }).then(function (res) {
    if (!res.ok || !res.data) return null;
    return res.data;
  });
}

/** 级联梯子突破 · 基准 K / 触发 K 标记 */
function fetchCladderMarkers(code, period) {
  return api.get('/stock/' + encodePath(code) + '/cladder/markers', {
    period: period || 'day'
  }).then(function (res) {
    if (!res.ok || !res.data) return null;
    return res.data;
  });
}

/** 跨周期内梯子上移 · 基准 K / 突破 K 标记 */
function fetchNrfMarkers(code, period) {
  var params = strategyParams.toApiParams('nrf');
  return api.get('/stock/' + encodePath(code) + '/nrf/markers', Object.assign({
    period: period || 'day'
  }, params)).then(function (res) {
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

function attachKlinesToItems(items, period, maxItems, forListCard) {
  var list = items || [];
  if (maxItems != null && maxItems > 0) {
    list = list.slice(0, maxItems);
  }
  if (!list.length) return Promise.resolve([]);

  var limit = forListCard ? klineLimitForList(period) : klineLimitForPeriod(period);

  return Promise.all(list.map(function (item) {
    return fetchKlines(item.code, period, limit).then(function (bars) {
      var klines = adapter.barsToKlines(bars);
      var merged = Object.assign({}, item, {
        klines: {},
        chartKlines: klines
      });
      merged.klines[period] = klines;
      return merged;
    }).catch(function () {
      return Object.assign({}, item, { chartKlines: [], klines: {} });
    });
  }));
}

module.exports = {
  RECOMMEND_PAGE_SIZE: RECOMMEND_PAGE_SIZE,
  MIN30_KLINE_LIMIT: MIN30_KLINE_LIMIT,
  klineLimitForPeriod: klineLimitForPeriod,
  klineLimitForList: klineLimitForList,
  buildStrategyQueryParams: buildStrategyQueryParams,
  fetchHealth: fetchHealth,
  fetchRecommendations: fetchRecommendations,
  triggerStrategyRescan: triggerStrategyRescan,
  fetchKlines: fetchKlines,
  fetchKlinesRefresh: fetchKlinesRefresh,
  fetchUltraMarkers: fetchUltraMarkers,
  fetchTrendMarkers: fetchTrendMarkers,
  fetchMediumMarkers: fetchMediumMarkers,
  fetchLongMarkers: fetchLongMarkers,
  fetchRetestMarkers: fetchRetestMarkers,
  fetchGc2Markers: fetchGc2Markers,
  fetchCascadeMarkers: fetchCascadeMarkers,
  fetchCladderMarkers: fetchCladderMarkers,
  fetchNrfMarkers: fetchNrfMarkers,
  fetchDc2Markers: fetchDc2Markers,
  fetchSummary: fetchSummary,
  search: search,
  fetchDetail: fetchDetail,
  fetchCompass: fetchCompass,
  fetchMarketIndices: fetchMarketIndices,
  attachKlinesToItems: attachKlinesToItems
};
