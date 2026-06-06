const {
  MARKETS,
  MARKET_INDICES,
  STRATEGIES,
  buildStrategyRecommendations
} = require('../../utils/mock');
const { findStockById } = require('../../utils/search');

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

    markets: MARKETS,
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
    watchlistIds: []
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
    this.loadMarket('cn');
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
      totalCount: all.length
    });
  },

  onMarketChange(e) {
    this.loadMarket(e.detail.marketId);
  },

  onStrategyChange(e) {
    const strategyId = e.detail.strategyId;
    wx.setStorageSync('activeStrategy', strategyId);
    this.setData({
      activeStrategy: strategyId,
      activeStrategyMeta: findStrategyMeta(strategyId)
    });
    this.loadMarket(this.data.activeMarket);
  },

  onPeriodChange(e) {
    const period = e.detail.period;
    wx.setStorageSync('activePeriod', period);
    this.setData({
      activePeriod: period,
      recommendations: mapChartKlines(this._baseList || [], period)
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
    const added = app.addToWatchlist(item);
    if (!added) return;
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
    const full = findStockById(item.id);
    if (!full) return;
    const added = app.addToWatchlist(full);
    if (added) {
      this.setData({
        watchlistCount: app.globalData.watchlist.length,
        watchlistIds: app.globalData.watchlist.map(function (w) { return w.id; })
      });
      wx.showToast({ title: '已加入自选', icon: 'success' });
    } else {
      wx.showToast({ title: '已在自选', icon: 'none' });
    }
  }
});
