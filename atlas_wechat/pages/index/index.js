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

const app = getApp();

function mapChartKlines(list, period) {
  return list.map(function (item) {
    var klines = item.klines && item.klines[period] ? item.klines[period].slice() : [];
    return Object.assign({}, item, { chartKlines: klines });
  });
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
    loading: false
  },

  onLoad() {
    const sys = wx.getSystemInfoSync();
    const menu = wx.getMenuButtonBoundingClientRect();
    const statusBarHeight = sys.statusBarHeight || 20;
    const navPaddingRight = sys.windowWidth - menu.left + 8;

    const savedPeriod = wx.getStorageSync('activePeriod') || 'week';
    const savedStrategy = wx.getStorageSync('activeStrategy') || 'trend';
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
    if (config.useMock) {
      this.loadMarket('cn');
    } else {
      this.loadMarketFromApi('cn');
    }
  },

  onReady() {
    this.measureStickyTops();
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
      return;
    }

    const self = this;
    const strategyId = this.data.activeStrategy;
    const period = this.data.activePeriod;
    const ignored = app.globalData.ignoredIds;

    this.setData({ loading: true });

    stockApi.fetchRecommendations(strategyId).then(function (items) {
      self._baseList = (items || []).filter(function (item) {
        return !ignored.includes(item.id);
      });
      return stockApi.attachKlinesToItems(self._baseList, period);
    }).then(function (withKlines) {
      self._baseList = withKlines;
      self.setData({
        activeMarket: marketId,
        indices: MARKET_INDICES[marketId] || [],
        recommendations: mapChartKlines(withKlines, period),
        totalCount: withKlines.length,
        loading: false
      });
    }).catch(function () {
      if (config.fallbackOnError) {
        self._allRecommendations = buildStrategyRecommendations();
        self.loadMarket(marketId);
        wx.showToast({ title: '已使用离线数据', icon: 'none' });
        return;
      }
      self.setData({ loading: false, recommendations: [], totalCount: 0 });
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
    stockApi.attachKlinesToItems(this._baseList || [], period).then(function (list) {
      self._baseList = list;
      self.setData({
        activePeriod: period,
        recommendations: mapChartKlines(list, period)
      });
    });
  },

  onFlipKline(e) {
    const klineFlipped = e.detail.flipped;
    wx.setStorageSync('klineFlipped', klineFlipped);
    this.setData({ klineFlipped });
    wx.showToast({
      title: klineFlipped ? 'K线已翻转' : 'K线已还原',
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
