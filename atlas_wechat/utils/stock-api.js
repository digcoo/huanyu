const api = require('./api');
const adapter = require('./adapter');
const strategyParams = require('./strategy-params');
const listMemory = require('./list-memory');

var RECOMMEND_PAGE_SIZE = 12;
/** 列表卡片 K 线条数上限（与 list-memory 一致） */
var MIN30_KLINE_LIMIT = 24;

function klineLimitForPeriod(period) {
  return listMemory.klineLimitForList(period || 'week');
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
  var adapterKey = strategyId === 'nrf' ? 'nrf' : strategyId;
  var params = Object.assign(
    {},
    adapter.getStrategyApiParams(adapterKey),
    strategyParams.toApiParams(strategyId)
  );
  if (strategyId === 'nrf') {
    Object.assign(params, strategyParams.toApiParams('ultra'));
  }
  return params;
}

function fetchSingleRecommendations(strategyId, page, size, mapStrategyId) {
  page = toPageNum(page);
  size = size || RECOMMEND_PAGE_SIZE;
  var apiStrategyId = strategyParams.resolveApiStrategyId(strategyId);
  var mapId = mapStrategyId || (strategyId === 'nrf' ? 'nrf' : strategyId);
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
    timeout: 300000
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
      return adapter.mapRecommendation(item, mapId);
    }).filter(function (item) { return item && item.id; });
    if (rawItems.length > 0 && items.length === 0) {
      console.error('[Atlas] mapRecommendation dropped all items, strategy=', mapId,
        'raw=', rawItems.length);
    }
    var pageSize = Number(data.pageSize) || size || RECOMMEND_PAGE_SIZE;
    var totalNum = data.totalNum != null ? Number(data.totalNum) : items.length;
    return {
      items: items,
      page: currentPage,
      totalNum: totalNum,
      hasMore: resolveHasMore(data, currentPage, rawItems.length)
    };
  });
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
  return resolveHasMoreWithSize(data, page, itemCount, RECOMMEND_PAGE_SIZE, data && data.totalNum);
}

function resolveHasMoreWithSize(data, page, itemCount, pageSize, totalNum) {
  if (!data) return false;
  page = toPageNum(page);
  pageSize = pageSize || RECOMMEND_PAGE_SIZE;
  var total = totalNum != null ? Number(totalNum) : (data.totalNum != null ? Number(data.totalNum) : 0);
  if (total > 0 && page * pageSize < total) {
    return true;
  }
  if (data.currentPage != null && data.totalPage != null) {
    var totalPage = toPageNum(data.totalPage);
    if (totalPage > 0) {
      return toPageNum(data.currentPage) < totalPage;
    }
  }
  var isMore = data.isMore;
  if (isMore === 1 || isMore === true || isMore === '1') return true;
  if (isMore === 0 || isMore === false || isMore === '0') return false;
  return itemCount >= pageSize;
}

function fetchRecommendations(strategyId, page, size) {
  return fetchSingleRecommendations(strategyId, page, size);
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
  return api.request({ path: path, method: 'POST', data: {}, timeout: 600000 }).then(function (res) {
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

function fetchKlines(code, period, limit, extraOptions) {
  return api.get('/stock/' + encodePath(code) + '/klines', {
    period: period || 'week',
    limit: limit || klineLimitForPeriod(period)
  }, null, extraOptions || {}).then(function (res) {
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

/** MACD 交叉边沿突破 · 基准 K / 边沿突破 K 标记 */
function fetchMacdEdgeMarkers(code, period) {
  return api.get('/stock/' + encodePath(code) + '/macedge/markers', {
    period: period || 'day'
  }).then(function (res) {
    if (!res.ok || !res.data) return null;
    return res.data;
  });
}

/** 级联 MACD 凸波段突破 · 末阳 K / 同档突破 K 标记 */
function fetchCascadewaveconvexMarkers(code, period) {
  return api.get('/stock/' + encodePath(code) + '/cascadewaveconvex/markers', {
    period: period || 'day'
  }).then(function (res) {
    if (!res.ok || !res.data) return null;
    return res.data;
  });
}

/** 级联 MACD 凹波段突破 · 末阳 K / 同档突破 K 标记 */
function fetchCascadewaveconcaveMarkers(code, period) {
  return api.get('/stock/' + encodePath(code) + '/cascadewaveconcave/markers', {
    period: period || 'day'
  }).then(function (res) {
    if (!res.ok || !res.data) return null;
    return res.data;
  });
}


function fetchCascadewaveconcavedayMarkers(code, period) {
  return api.get('/stock/' + encodePath(code) + '/cascadewaveconcaveday/markers', {
    period: period || 'day'
  }).then(function (res) {
    if (!res.ok || !res.data) return null;
    return res.data;
  });
}

/** 级联 MACD 凸波段日突破 · 末阳 K / 日 K 突破 K 标记 */
function fetchCascadewaveconvexdayMarkers(code, period) {
  return api.get('/stock/' + encodePath(code) + '/cascadewaveconvexday/markers', {
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
    limit: limit || listMemory.klineLimitForList(period || 'week')
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
    return fetchKlines(item.code, period, limit, { timeout: 60000 }).then(function (bars) {
      return Object.assign({}, item, {
        chartKlines: adapter.barsToKlines(bars)
      });
    }).catch(function () {
      return Object.assign({}, item, { chartKlines: [] });
    });
  }));
}

module.exports = {
  RECOMMEND_PAGE_SIZE: RECOMMEND_PAGE_SIZE,
  MIN30_KLINE_LIMIT: MIN30_KLINE_LIMIT,
  klineLimitForPeriod: klineLimitForPeriod,
  klineLimitForList: klineLimitForList,
  buildStrategyQueryParams: buildStrategyQueryParams,
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
  fetchMacdEdgeMarkers: fetchMacdEdgeMarkers,
  fetchCascadewaveconvexMarkers: fetchCascadewaveconvexMarkers,
  fetchCascadewaveconcaveMarkers: fetchCascadewaveconcaveMarkers,
  fetchCascadewaveconvexdayMarkers: fetchCascadewaveconvexdayMarkers,
  fetchCascadewaveconcavedayMarkers: fetchCascadewaveconcavedayMarkers,
  fetchNrfMarkers: fetchNrfMarkers,
  fetchDc2Markers: fetchDc2Markers,
  fetchSummary: fetchSummary,
  search: search,
  fetchDetail: fetchDetail,
  fetchCompass: fetchCompass,
  fetchMarketIndices: fetchMarketIndices,
  attachKlinesToItems: attachKlinesToItems
};
