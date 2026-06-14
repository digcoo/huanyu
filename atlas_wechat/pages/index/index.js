const {
  MARKET_INDICES,
  STRATEGIES,
  buildStrategyRecommendations
} = require('../../utils/mock');
const { findStockByIdAsync } = require('../../utils/search');
const { buildMarketsForUI } = require('../../utils/markets');
const config = require('../../utils/config');
const auth = require('../../utils/auth');
const stockApi = require('../../utils/stock-api');
const adapter = require('../../utils/adapter');
const strategyParams = require('../../utils/strategy-params');

const app = getApp();

const RECOMMEND_PAGE_SIZE = stockApi.RECOMMEND_PAGE_SIZE || 12;

function mapChartKlines(list, period) {
  return list.map(function (item) {
    var fromStore = item.klines && item.klines[period] ? item.klines[period].slice() : null;
    var klines = fromStore || (item.chartKlines ? item.chartKlines.slice() : []);
    return Object.assign({}, item, { chartKlines: klines });
  });
}

function filterIgnored(items, ignored) {
  return (items || []).filter(function (item) {
    return item && item.id && ignored.indexOf(item.id) < 0;
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
  var lastPage = startPage;
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
          hasMore: hasMore
        };
      }
      return fetchPage(page + 1);
    });
  }

  return fetchPage(startPage);
}

function attachKlinesInBackground(self, items, period) {
  if (!items || !items.length) return Promise.resolve([]);
  return stockApi.attachKlinesToItems(items, period).then(function (withKlines) {
    var klineById = {};
    withKlines.forEach(function (item) {
      klineById[item.id] = item;
    });
    if (self._baseList && self._baseList.length) {
      self._baseList = self._baseList.map(function (item) {
        return klineById[item.id] ? Object.assign({}, item, klineById[item.id]) : item;
      });
      self.setData({
        recommendations: mapChartKlines(self._baseList, period || self.data.activePeriod)
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

function findStrategyMeta(strategyId) {
  return STRATEGIES.find(function (s) { return s.id === strategyId; }) || STRATEGIES[0];
}

Page({
  data: {
    statusBarHeight: 20,
    toolbarStickyTop: 88,
    navPaddingRight: 96,

    markets: buildMarketsForUI(),
    strategies: STRATEGIES,
    activeStrategy: 'trend',
    activeStrategyMeta: STRATEGIES[0],

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
    let savedStrategy = wx.getStorageSync('activeStrategy') || 'trend';
    if (savedStrategy === 'multi') {
      savedStrategy = 'trend';
      wx.setStorageSync('activeStrategy', savedStrategy);
    }
    const klineFlipped = !!wx.getStorageSync('klineFlipped');

    this.setData({
      statusBarHeight,
      navPaddingRight,
      activePeriod: savedPeriod,
      activeStrategy: savedStrategy,
      activeStrategyMeta: findStrategyMeta(savedStrategy),
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
    var shouldRescan = detail.rescan !== false;
    this.refreshParamsBadge(strategyId);

    if (config.useMock) {
      wx.showToast({ title: 'Mock 模式参数不生效', icon: 'none' });
      this.setData({ paramsVisible: false });
      return;
    }

    if (shouldRescan) {
      var self = this;
      this.runStrategyRescanAndReload(true).then(function () {
        self.setData({ paramsVisible: false });
      });
      return;
    }

    this.setData({ paramsVisible: false });
    this.loadMarketFromApi(this.data.activeMarket);
    wx.showToast({ title: '参数已应用（预览）', icon: 'success' });
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

    this.setData({ rescanning: true });
    if (panel) panel.setApplying(true);
    wx.showLoading({ title: '重跑策略中…', mask: true });

    return stockApi.triggerStrategyRescan(strategyId).then(function (result) {
      return self.loadMarketFromApi(self.data.activeMarket).then(function () {
        return result;
      });
    }).then(function (result) {
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
      if (showToast) {
        wx.showToast({ title: '重跑失败', icon: 'none' });
      }
    }).finally(function () {
      wx.hideLoading();
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

  loadMoreRecommendations() {
    if (!this.data.hasMore || this.data.loadingMore || this.data.loading) return;

    const self = this;
    const strategyId = this.data.activeStrategy;
    const period = this.data.activePeriod;
    const ignored = app.globalData.ignoredIds;
    const nextPage = (this._loadPage || 1) + 1;

    this.setData({ loadingMore: true });

    this._fetchRecommendPage(strategyId, period, nextPage, ignored).then(function (result) {
      self._loadPage = result.page;
      self._baseList = (self._baseList || []).concat(result.items);
      self.setData({
        recommendations: mapChartKlines(self._baseList, period),
        totalCount: result.totalNum,
        hasMore: result.hasMore,
        loadingMore: false
      });
      applyKlinesForItems(self, result.items, period);
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
      recommendations: mapChartKlines(this._baseList, period),
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
      self._baseList = pageResult.items;
      self.setData({
        activeMarket: marketId,
        indices: indices,
        recommendations: mapChartKlines(pageResult.items, period),
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
    const strategyId = e.detail.strategyId;
    wx.setStorageSync('activeStrategy', strategyId);
    this.setData({
      activeStrategy: strategyId,
      activeStrategyMeta: findStrategyMeta(strategyId)
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
    wx.setStorageSync('activePeriod', period);
    if (config.useMock) {
      this.setData({
        activePeriod: period,
        recommendations: mapChartKlines(this._baseList || [], period)
      });
      return;
    }
    const self = this;
    const baseList = this._baseList || [];
    this.setData({
      activePeriod: period,
      recommendations: mapChartKlines(baseList, period)
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
    this._baseList = (this._baseList || []).filter(function (r) { return r.id !== item.id; });
    this.setData({
      recommendations: mapChartKlines(this._baseList, this.data.activePeriod),
      watchlistCount: app.globalData.watchlist.length
    });
  },

  onIgnore(e) {
    const { item } = e.detail;
    app.ignoreItem(item.id);
    this._baseList = (this._baseList || []).filter(function (r) { return r.id !== item.id; });
    this.setData({
      recommendations: mapChartKlines(this._baseList, this.data.activePeriod)
    });
  },

  onCardTap(e) {
    const item = e.detail && e.detail.item;
    if (!item || !item.id) return;
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
