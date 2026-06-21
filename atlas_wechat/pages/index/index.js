const {
  MARKET_INDICES,
  buildStrategyRecommendations
} = require('../../utils/mock');
const { findStockByIdAsync } = require('../../utils/search');
const { buildMarketsForUI } = require('../../utils/markets');
const config = require('../../utils/config');
const auth = require('../../utils/auth');
const stockApi = require('../../utils/stock-api');
const adapter = require('../../utils/adapter');
const strategyParams = require('../../utils/strategy-params');
const barMarkers = require('../../utils/bar-markers');
const listMemory = require('../../utils/list-memory');

const app = getApp();
const DEFAULT_STRATEGY = 'ultra';

const ACTIVE_STRATEGIES = [
  { id: 'ultra', name: '超短线', icon: '⚡' },
  { id: 'trend', name: '短线', icon: '📈' },
  { id: 'medium', name: '中线', icon: '📊' },
  { id: 'long', name: '长线', icon: '📉' }
];

const STRATEGY_TITLES = {
  ultra: '超短线策略',
  trend: '短线策略',
  medium: '中线策略',
  long: '长线策略'
};

const RECOMMEND_PAGE_SIZE = stockApi.RECOMMEND_PAGE_SIZE || 12;

function mapChartKlines(list, period, strategyId) {
  return list.map(function (item) {
    var klines = item.klines && item.klines[period] ? item.klines[period] : (item.chartKlines || []);
    var markers = barMarkers.shouldShowBarMarkers(strategyId, period)
      ? (item.barMarkers && item.barMarkers.length
        ? item.barMarkers
        : barMarkers.resolveBarMarkersForItem(item, strategyId, period, klines))
      : [];
    return Object.assign({}, item, { chartKlines: klines, barMarkers: markers });
  });
}

function applyHeldList(self, list, period, strategyId, extra) {
  var trimmed = listMemory.trimHeldList(list);
  var slimmed = listMemory.slimListKlines(trimmed, period);
  self._baseList = slimmed;
  var patch = Object.assign({
    recommendations: mapChartKlines(slimmed, period, strategyId)
  }, extra || {});
  if (trimmed.length < (list || []).length) {
    patch.listTrimmedHint = '已释放较早条目以节省内存';
  } else {
    patch.listTrimmedHint = '';
  }
  self.setData(patch);
}

function normalizeSavedStrategy(strategyId) {
  var id = strategyId || DEFAULT_STRATEGY;
  var ok = ACTIVE_STRATEGIES.some(function (s) { return s.id === id; });
  return ok ? id : DEFAULT_STRATEGY;
}

function strategyTitleFor(strategyId) {
  return STRATEGY_TITLES[strategyId] || '策略';
}

function filterIgnored(items, ignored) {
  return (items || []).filter(function (item) {
    return item && item.id && ignored.indexOf(item.id) < 0;
  });
}

function dedupeAppend(baseList, newItems) {
  var seen = {};
  (baseList || []).forEach(function (item) {
    if (item && item.id) seen[item.id] = true;
  });
  var appended = [];
  (newItems || []).forEach(function (item) {
    if (item && item.id && !seen[item.id]) {
      seen[item.id] = true;
      appended.push(item);
    }
  });
  return {
    list: (baseList || []).concat(appended),
    appended: appended
  };
}

function resolvePagedHasMore(hasMore, lastPage, totalNum) {
  if (hasMore) return true;
  if (totalNum > 0 && lastPage > 0 && lastPage * RECOMMEND_PAGE_SIZE < totalNum) {
    return true;
  }
  return false;
}

function buildSeenIdMap(list) {
  var seen = {};
  (list || []).forEach(function (item) {
    if (item && item.id) seen[item.id] = true;
  });
  return seen;
}

function filterNovelItems(items, seen) {
  return (items || []).filter(function (item) {
    return item && item.id && !seen[item.id];
  });
}

/**
 * 拉取推荐列表，跳过已忽略项；单页全部被忽略时自动继续下一页
 */
function fetchVisibleRecommendations(strategyId, ignored, targetCount, startPage) {
  targetCount = targetCount || RECOMMEND_PAGE_SIZE;
  startPage = startPage || 1;
  var accumulated = [];
  var totalNum = 0;
  var lastPage = startPage - 1;
  var hasMore = false;

  function fetchPage(page) {
    return stockApi.fetchRecommendations(strategyId, page, RECOMMEND_PAGE_SIZE).then(function (result) {
      totalNum = result.totalNum != null ? result.totalNum : 0;
      hasMore = !!result.hasMore;
      lastPage = result.page || page;
      accumulated = accumulated.concat(filterIgnored(result.items, ignored));
      if (accumulated.length >= targetCount || !hasMore) {
        return {
          items: accumulated,
          page: lastPage,
          totalNum: totalNum,
          hasMore: resolvePagedHasMore(hasMore, lastPage, totalNum)
        };
      }
      return fetchPage(page + 1);
    });
  }

  return fetchPage(startPage);
}

/**
 * 加载更多：严格按后端页码追加，避免与首屏重复
 */
function fetchNextRecommendationsPage(strategyId, ignored, backendPage, seenIds) {
  var nextPage = (backendPage || 1) + 1;
  var seen = seenIds || {};

  function fetchFrom(page) {
    return stockApi.fetchRecommendations(strategyId, page, RECOMMEND_PAGE_SIZE).then(function (result) {
      var visible = filterIgnored(result.items, ignored);
      var novel = filterNovelItems(visible, seen);
      if (novel.length === 0 && result.hasMore) {
        return fetchFrom(page + 1);
      }
      seen = listMemory.rememberSeenIds(seen, novel);
      return {
        items: novel,
        page: result.page || page,
        totalNum: result.totalNum != null ? result.totalNum : 0,
        hasMore: resolvePagedHasMore(!!result.hasMore, result.page || page, result.totalNum),
        seenIds: seen
      };
    });
  }

  return fetchFrom(nextPage);
}

function attachKlinesInBackground(self, items, period) {
  if (!items || !items.length) return Promise.resolve([]);
  return stockApi.attachKlinesToItems(items, period, null, true).then(function (withKlines) {
    var klineById = {};
    withKlines.forEach(function (item) {
      klineById[item.id] = listMemory.slimItemKlines(item, period);
    });
    if (self._baseList && self._baseList.length) {
      var strategyId = self.data.activeStrategy;
      var activePeriod = period || self.data.activePeriod;
      var merged = self._baseList.map(function (item) {
        return klineById[item.id] ? Object.assign({}, item, klineById[item.id]) : item;
      });
      return barMarkers.enrichItemsWithBarMarkers(withKlines, strategyId, activePeriod).then(function (enriched) {
        var markerById = {};
        enriched.forEach(function (item) {
          markerById[item.id] = item;
        });
        merged = merged.map(function (item) {
          return markerById[item.id] ? Object.assign({}, item, markerById[item.id]) : item;
        });
        applyHeldList(self, merged, activePeriod, strategyId);
        return merged;
      });
    }
    return withKlines;
  }).catch(function () {
    return items;
  });
}

function applyKlinesForItems(self, items, period) {
  if (!items || !items.length) return Promise.resolve([]);
  return attachKlinesInBackground(self, items, period);
}

Page({
  data: {
    statusBarHeight: 20,
    toolbarStickyTop: 88,
    navPaddingRight: 96,

    markets: buildMarketsForUI(),
    strategies: ACTIVE_STRATEGIES,
    activeStrategy: DEFAULT_STRATEGY,
    strategyTitle: strategyTitleFor(DEFAULT_STRATEGY),

    activeMarket: 'cn',
    indices: [],
    recommendations: [],
    totalCount: 0,
    watchlistCount: 0,
    activePeriod: 'week',
    klineFlipped: false,
    searchVisible: false,
    watchlistIds: [],
    loading: false,
    loadingMore: false,
    hasMore: false,
    paramsVisible: false,
    showStrategyParams: false,
    paramsSummary: '',
    paramsCustomized: false,
    rescanning: false,
    showBackTop: false
  },

  onLoad() {
    const sys = wx.getSystemInfoSync();
    const menu = wx.getMenuButtonBoundingClientRect();
    const statusBarHeight = sys.statusBarHeight || 20;
    const navPaddingRight = sys.windowWidth - menu.left + 8;

    const savedPeriod = wx.getStorageSync('activePeriod') || 'week';
    const savedStrategy = normalizeSavedStrategy(wx.getStorageSync('activeStrategy'));
    if (savedStrategy !== wx.getStorageSync('activeStrategy')) {
      wx.setStorageSync('activeStrategy', savedStrategy);
    }
    const initialPeriod = strategyParams.chartPrimaryPeriod(savedStrategy, strategyParams.load(savedStrategy))
      || savedPeriod;
    const klineFlipped = !!wx.getStorageSync('klineFlipped');

    this.setData({
      statusBarHeight,
      navPaddingRight,
      activePeriod: initialPeriod,
      activeStrategy: savedStrategy,
      strategyTitle: strategyTitleFor(savedStrategy),
      klineFlipped
    });

    this._allRecommendations = buildStrategyRecommendations();
    this.refreshParamsBadge(savedStrategy);
    if (config.useMock) {
      this.loadMarket('cn');
    } else {
      this.loadMarketFromApi('cn');
    }
  },

  onReady() {
    this.measureStickyTops();
  },

  onPageScroll(e) {
    var show = (e.scrollTop || 0) > 480;
    if (show !== this._showBackTop) {
      this._showBackTop = show;
      this.setData({ showBackTop: show });
    }
  },

  onBackToTop() {
    wx.pageScrollTo({ scrollTop: 0, duration: 280 });
  },

  measureStickyTops() {
    const query = wx.createSelectorQuery().in(this);
    query.select('.nav-sticky').boundingClientRect(function (rect) {
      if (rect && rect.height) {
        this.setData({ toolbarStickyTop: rect.height });
      }
    }.bind(this)).exec();
  },

  onShow() {
    this.setData({
      watchlistCount: app.globalData.watchlist.length,
      watchlistIds: app.globalData.watchlist.map(function (w) { return w.id; }),
      klineFlipped: !!wx.getStorageSync('klineFlipped')
    });
  },

  refreshParamsBadge(strategyId) {
    strategyId = strategyId || this.data.activeStrategy;
    var show = !config.useMock && strategyParams.hasCustomParams(strategyId);
    this.setData({
      showStrategyParams: show,
      paramsSummary: show ? strategyParams.formatSummary(strategyId) : '',
      paramsCustomized: show && strategyParams.isCustomized(strategyId)
    });
  },

  onOpenStrategyParams() {
    this.setData({ paramsVisible: true });
  },

  onParamsClose() {
    this.setData({ paramsVisible: false });
  },

  onParamsApply(e) {
    var detail = (e && e.detail) || {};
    var strategyId = detail.strategyId || this.data.activeStrategy;
    this.refreshParamsBadge(strategyId);

    if (config.useMock) {
      wx.showToast({ title: 'Mock 模式参数不生效', icon: 'none' });
      this.setData({ paramsVisible: false });
      return;
    }

    var self = this;
    this.runStrategyRescanAndReload(true).then(function () {
      self.setData({ paramsVisible: false });
    });
  },

  onRescanStrategy() {
    if (this.data.rescanning || this.data.loading) return;
    this.runStrategyRescanAndReload(true);
  },

  runStrategyRescanAndReload(showToast) {
    if (config.useMock) {
      return Promise.resolve({ ok: false, saved: 0 });
    }
    if (this.data.rescanning) {
      return Promise.resolve();
    }

    var self = this;
    var strategyId = this.data.activeStrategy;
    var panel = this.selectComponent('#strategyParamsPanel');
    var loadingShown = false;

    function hideRescanLoading() {
      if (!loadingShown) return;
      loadingShown = false;
      wx.hideLoading();
    }

    this.setData({ rescanning: true });
    if (panel) panel.setApplying(true);
    wx.showLoading({ title: '重跑策略中…', mask: true });
    loadingShown = true;

    return stockApi.triggerStrategyRescan(strategyId).then(function (result) {
      return self.loadMarketFromApi(self.data.activeMarket).then(function () {
        return result;
      });
    }).then(function (result) {
      hideRescanLoading();
      if (showToast) {
        var saved = result && result.saved != null ? result.saved : 0;
        var ok = result && result.ok;
        wx.showToast({
          title: ok ? '已重跑 ' + saved + ' 只' : '重跑失败',
          icon: ok ? 'success' : 'none',
          duration: 2000
        });
      }
      return result;
    }).catch(function () {
      hideRescanLoading();
      if (showToast) {
        wx.showToast({ title: '重跑失败', icon: 'none' });
      }
    }).finally(function () {
      hideRescanLoading();
      self.setData({ rescanning: false });
      if (panel) panel.setApplying(false);
    });
  },

  _fetchRecommendPage(strategyId, period, page, ignored) {
    return fetchVisibleRecommendations(strategyId, ignored, RECOMMEND_PAGE_SIZE, page);
  },

  onReachBottom() {
    if (config.useMock || this.data.activeMarket !== 'cn') return;
    this.loadMoreRecommendations();
  },

  onLoadMoreTap() {
    if (config.useMock || this.data.activeMarket !== 'cn') return;
    if (!this.data.hasMore || this.data.loadingMore || this.data.rescanning) return;
    this.loadMoreRecommendations();
  },

  loadMoreRecommendations() {
    if (!this.data.hasMore || this.data.loadingMore) return;
    if (this.data.loading && (!this.data.recommendations || this.data.recommendations.length === 0)) return;

    const self = this;
    const strategyId = this.data.activeStrategy;
    const period = this.data.activePeriod;
    const ignored = app.globalData.ignoredIds;
    const backendPage = this._backendPage || this._loadPage || 1;

    this.setData({ loadingMore: true });

    fetchNextRecommendationsPage(strategyId, ignored, backendPage, this._seenRecommendationIds).then(function (result) {
      if (result.seenIds) {
        self._seenRecommendationIds = result.seenIds;
      }
      var merged = dedupeAppend(self._baseList, result.items);
      self._backendPage = result.page;
      self._loadPage = result.page;
      applyHeldList(self, merged.list, period, strategyId, {
        totalCount: result.totalNum,
        hasMore: result.hasMore,
        loadingMore: false
      });
      if (merged.appended.length) {
        applyKlinesForItems(self, merged.appended, period);
      } else if (result.hasMore) {
        wx.showToast({ title: '暂无新条目，请再试', icon: 'none' });
      }
    }).catch(function () {
      self.setData({ loadingMore: false });
      wx.showToast({ title: '加载更多失败', icon: 'none' });
    });
  },

  loadMarket(marketId) {
    const strategyId = this.data.activeStrategy;
    const indices = MARKET_INDICES[marketId] || [];
    const strategyPool = this._allRecommendations[strategyId] || {};
    const all = strategyPool[marketId] || [];
    const ignored = app.globalData.ignoredIds;
    const period = this.data.activePeriod;

    this._baseList = all.filter(function (item) {
      return !ignored.includes(item.id);
    });

    this.setData({
      activeMarket: marketId,
      indices,
      recommendations: mapChartKlines(this._baseList, period, strategyId),
      totalCount: all.length,
      loading: false
    });
  },

  loadMarketFromApi(marketId) {
    if (marketId !== 'cn') {
      this.setData({
        activeMarket: marketId,
        indices: MARKET_INDICES[marketId] || [],
        recommendations: [],
        totalCount: 0,
        loading: false
      });
      return Promise.resolve();
    }

    const self = this;
    const strategyId = this.data.activeStrategy;
    const period = this.data.activePeriod;
    const ignored = app.globalData.ignoredIds;

    this.setData({ loading: true, hasMore: false, loadingMore: false, totalCount: 0 });
    this._loadPage = 1;
    this._backendPage = 1;
    this._seenRecommendationIds = {};

    return this._fetchRecommendPage(strategyId, period, 1, ignored).then(function (result) {
      return Promise.all([
        Promise.resolve(result),
        stockApi.fetchMarketIndices('cn', period).then(function (raw) {
          return adapter.mapMarketIndices(raw, period);
        }).catch(function () {
          return MARKET_INDICES.cn || [];
        })
      ]);
    }).then(function (results) {
      var pageResult = results[0];
      var indices = results[1];
      self._loadPage = pageResult.page;
      self._backendPage = pageResult.page;
      self._seenRecommendationIds = listMemory.initSeenIds(pageResult.items);
      applyHeldList(self, pageResult.items, period, strategyId, {
        activeMarket: marketId,
        indices: indices,
        totalCount: pageResult.totalNum,
        hasMore: pageResult.hasMore,
        loading: false
      });
      applyKlinesForItems(self, pageResult.items, period);
    }).catch(function () {
      if (config.fallbackOnError) {
        self._allRecommendations = buildStrategyRecommendations();
        self.loadMarket(marketId);
        wx.showToast({ title: '已使用离线数据', icon: 'none' });
        return;
      }
      self.setData({ loading: false, recommendations: [], totalCount: 0, hasMore: false });
      wx.showToast({ title: '加载失败', icon: 'none' });
    });
  },

  onMarketChange(e) {
    const marketId = e.detail.marketId;
    if (config.useMock) {
      this.loadMarket(marketId);
    } else {
      this.loadMarketFromApi(marketId);
    }
  },

  onMarketDisabled() {
    wx.showToast({
      title: config.marketComingSoonTip,
      icon: 'none',
      duration: 2000
    });
  },

  onStrategyChange(e) {
    var strategyId = e.detail && e.detail.strategyId;
    if (!strategyId || strategyId === this.data.activeStrategy) return;
    if (!normalizeSavedStrategy(strategyId)) return;

    wx.setStorageSync('activeStrategy', strategyId);
    var period = strategyParams.chartPrimaryPeriod(strategyId, strategyParams.load(strategyId))
      || this.data.activePeriod;
    wx.setStorageSync('activePeriod', period);

    this.setData({
      activeStrategy: strategyId,
      activePeriod: period,
      strategyTitle: strategyTitleFor(strategyId)
    });
    this.refreshParamsBadge(strategyId);

    if (config.useMock) {
      this.loadMarket(this.data.activeMarket);
    } else {
      this.loadMarketFromApi(this.data.activeMarket);
    }
  },

  onPeriodChange(e) {
    const period = e.detail.period;
    const strategyId = this.data.activeStrategy;
    wx.setStorageSync('activePeriod', period);
    if (config.useMock) {
      this.setData({
        activePeriod: period,
        recommendations: mapChartKlines(this._baseList || [], period, strategyId)
      });
      return;
    }
    const self = this;
    const baseList = listMemory.slimListKlines(this._baseList || [], period);
    this._baseList = baseList;
    this.setData({
      activePeriod: period,
      recommendations: mapChartKlines(baseList, period, strategyId)
    });
    applyKlinesForItems(this, baseList, period);
    stockApi.fetchMarketIndices('cn', period).then(function (raw) {
      return adapter.mapMarketIndices(raw, period);
    }).catch(function () {
      return self.data.indices.length ? self.data.indices : (MARKET_INDICES.cn || []);
    }).then(function (indices) {
      self.setData({ indices: indices });
    });
  },

  onFlipKline(e) {
    const klineFlipped = e.detail.flipped;
    wx.setStorageSync('klineFlipped', klineFlipped);
    this.setData({ klineFlipped });
    wx.showToast({
      title: klineFlipped ? '坐标已翻转（低价在上）' : '坐标已还原',
      icon: 'none',
      duration: 800
    });
  },

  onAddWatchlist(e) {
    const { item } = e.detail;
    if (!item || !item.id) return;
    const self = this;
    app.addToWatchlist(item).then(function (result) {
      if (result && result.needLogin) {
        auth.promptLogin().then(function () {
          return app.addToWatchlist(item);
        }).then(function (r2) {
          if (r2 && r2.added) self._afterWatchlistAdd(item);
        }).catch(function () {});
        return;
      }
      if (result && result.added) self._afterWatchlistAdd(item);
    });
  },

  _afterWatchlistAdd(item) {
    const strategyId = this.data.activeStrategy;
    const period = this.data.activePeriod;
    this._baseList = (this._baseList || []).filter(function (r) { return r.id !== item.id; });
    this.setData({
      recommendations: mapChartKlines(this._baseList, period, strategyId),
      watchlistCount: app.globalData.watchlist.length
    });
  },

  onIgnore(e) {
    const { item } = e.detail;
    app.ignoreItem(item.id);
    this._baseList = (this._baseList || []).filter(function (r) { return r.id !== item.id; });
    this.setData({
      recommendations: mapChartKlines(this._baseList, this.data.activePeriod, this.data.activeStrategy)
    });
  },

  onCardTap(e) {
    const item = e.detail && e.detail.item;
    if (!item || !item.id) return;
    const app = getApp();
    if (item.signalMessage) {
      app.globalData.detailSignalHint = {
        id: item.id,
        signalMessage: item.signalMessage
      };
    }
    wx.navigateTo({
      url: '/pages/detail/detail?id=' + item.id + '&name=' + encodeURIComponent(item.name)
    });
  },

  onOpenSearch() {
    this.setData({
      searchVisible: true,
      watchlistIds: app.globalData.watchlist.map(function (w) { return w.id; })
    });
  },

  onSearchClose() {
    this.setData({ searchVisible: false });
  },

  onSearchSelect(e) {
    const item = e.detail && e.detail.item;
    if (!item || !item.id) return;
    const app = getApp();
    if (item.signalMessage) {
      app.globalData.detailSignalHint = {
        id: item.id,
        signalMessage: item.signalMessage
      };
    }
    this.setData({ searchVisible: false });
    wx.navigateTo({
      url: '/pages/detail/detail?id=' + item.id + '&name=' + encodeURIComponent(item.name)
    });
  },

  onSearchAddWatchlist(e) {
    const item = e.detail && e.detail.item;
    if (!item || !item.id) return;
    const self = this;
    findStockByIdAsync(item.id).then(function (full) {
      if (!full) return;
      return app.addToWatchlist(full).then(function (result) {
        if (result && result.needLogin) {
          return auth.promptLogin().then(function () {
            return app.addToWatchlist(full);
          }).then(function (r2) {
            if (r2 && r2.added) self._afterSearchWatchlistAdd();
          });
        }
        if (result && result.added) self._afterSearchWatchlistAdd();
        else if (result && result.duplicate) {
          wx.showToast({ title: '已在自选', icon: 'none' });
        }
      });
    }).catch(function () {});
  },

  _afterSearchWatchlistAdd() {
    this.setData({
      watchlistCount: app.globalData.watchlist.length,
      watchlistIds: app.globalData.watchlist.map(function (w) { return w.id; })
    });
    wx.showToast({ title: '已加入自选', icon: 'success' });
  }
});
