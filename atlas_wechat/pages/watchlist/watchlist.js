const { MARKETS, STRATEGIES } = require('../../utils/mock');
const { findStockById } = require('../../utils/search');
const auth = require('../../utils/auth');
const config = require('../../utils/config');

function mapChartKlines(list, period) {
  return list.map(function (item) {
    var klines = item.klines && item.klines[period] ? item.klines[period].slice() : [];
    return Object.assign({}, item, { chartKlines: klines });
  });
}

function formatAddedAt(ts) {
  if (!ts) return '';
  const d = new Date(ts);
  const now = new Date();
  const pad = function (n) { return n < 10 ? '0' + n : '' + n; };
  const time = pad(d.getHours()) + ':' + pad(d.getMinutes());
  const isToday = d.toDateString() === now.toDateString();
  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  const isYesterday = d.toDateString() === yesterday.toDateString();

  if (isToday) return '今天 ' + time + ' 加入';
  if (isYesterday) return '昨天 ' + time + ' 加入';
  return pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + time + ' 加入';
}

function findMarketMeta(marketId) {
  return MARKETS.find(function (m) { return m.id === marketId; });
}

function findStrategyMeta(strategyId) {
  return STRATEGIES.find(function (s) { return s.id === strategyId; });
}

function buildMarketTabs(watchlist) {
  const marketIds = {};
  watchlist.forEach(function (item) {
    if (item.market) marketIds[item.market] = true;
  });
  const tabs = [{ id: 'all', name: '全部', icon: '☰' }];
  MARKETS.forEach(function (m) {
    if (marketIds[m.id]) tabs.push(m);
  });
  return tabs;
}

function enrichItems(list) {
  return list.map(function (item) {
    const marketMeta = findMarketMeta(item.market);
    const strategyMeta = findStrategyMeta(item.strategy);
    return Object.assign({}, item, {
      marketLabel: marketMeta ? marketMeta.icon + ' ' + marketMeta.name : '',
      strategyLabel: strategyMeta ? strategyMeta.name : '',
      addedLabel: formatAddedAt(item.addedAt)
    });
  });
}

Page({
  data: {
    statusBarHeight: 20,
    navHeight: 88,
    contentTop: 88,
    activePeriod: 'week',
    klineFlipped: false,
    activeMarket: 'all',
    marketTabs: [],
    items: [],
    totalCount: 0,
    watchlistCount: 0,
    isEmpty: true,
    filterEmpty: false,
    searchVisible: false,
    watchlistIds: [],
    needLogin: false
  },

  onLoad() {
    const sys = wx.getSystemInfoSync();
    const statusBarHeight = sys.statusBarHeight || 20;
    const savedPeriod = wx.getStorageSync('activePeriod') || 'week';
    const klineFlipped = !!wx.getStorageSync('klineFlipped');

    this.setData({
      statusBarHeight,
      activePeriod: savedPeriod,
      klineFlipped
    });
  },

  onReady() {
    this.measureNavHeight();
  },

  measureNavHeight() {
    const self = this;
    wx.nextTick(function () {
      const query = wx.createSelectorQuery().in(self);
      query.select('.nav-bar').boundingClientRect(function (rect) {
        if (rect && rect.height) {
          self.setData({ contentTop: rect.height });
        }
      }).exec();
    });
  },

  onShow() {
    if (config.requireLoginForWatchlist && !auth.isLoggedIn()) {
      this.setData({ needLogin: true, isEmpty: true, items: [], totalCount: 0 });
      return;
    }
    this.setData({ needLogin: false });
    this.refreshList();
  },

  refreshList() {
    const app = getApp();
    const period = wx.getStorageSync('activePeriod') || this.data.activePeriod;
    const klineFlipped = !!wx.getStorageSync('klineFlipped');
    const raw = app.getWatchlist();
    const marketTabs = buildMarketTabs(raw);
    let activeMarket = this.data.activeMarket;

    if (activeMarket !== 'all' && !marketTabs.some(function (t) { return t.id === activeMarket; })) {
      activeMarket = 'all';
    }

    const mapped = mapChartKlines(raw, period);
    const enriched = enrichItems(mapped);
    const filtered = activeMarket === 'all'
      ? enriched
      : enriched.filter(function (item) { return item.market === activeMarket; });

    const isEmpty = raw.length === 0;

    this.setData({
      activePeriod: period,
      klineFlipped,
      marketTabs,
      activeMarket,
      items: filtered,
      totalCount: raw.length,
      watchlistCount: raw.length,
      isEmpty: isEmpty,
      filterEmpty: raw.length > 0 && filtered.length === 0,
      watchlistIds: raw.map(function (item) { return item.id; })
    }, function () {
      this.measureNavHeight();
    }.bind(this));
  },

  onPeriodChange(e) {
    const period = e.detail.period;
    wx.setStorageSync('activePeriod', period);
    this.setData({ activePeriod: period });
    this.refreshList();
  },

  onFlipKline(e) {
    const klineFlipped = e.detail.flipped;
    wx.setStorageSync('klineFlipped', klineFlipped);
    this.setData({ klineFlipped: klineFlipped });
    this.refreshList();
    wx.showToast({
      title: klineFlipped ? 'K线已翻转' : 'K线已还原',
      icon: 'none',
      duration: 800
    });
  },

  onMarketChange(e) {
    const marketId = e.detail.marketId;
    if (marketId === this.data.activeMarket) return;
    this.setData({ activeMarket: marketId });
    this.refreshList();
  },

  onItemTap(e) {
    const item = e.detail && e.detail.item;
    if (!item || !item.id) return;
    wx.navigateTo({
      url: '/pages/detail/detail?id=' + item.id + '&name=' + encodeURIComponent(item.name)
    });
  },

  onRemoveItem(e) {
    const item = e.detail && e.detail.item;
    if (!item || !item.id) return;
    getApp().removeFromWatchlist(item.id, 'swipe');
    this.refreshList();
  },

  onRemoveConfirm(e) {
    const item = e.detail && e.detail.item;
    if (!item || !item.id) return;

    const self = this;
    wx.showModal({
      title: '移出自选',
      content: '确定将「' + item.name + '」移出自选池？',
      confirmColor: '#ff6b6b',
      success(res) {
        if (!res.confirm) return;
        getApp().removeFromWatchlist(item.id, 'manual');
        wx.showToast({ title: '已移出', icon: 'none', duration: 1000 });
        self.refreshList();
      }
    });
  },

  onGoIndex() {
    wx.reLaunch({ url: '/pages/index/index' });
  },

  onOpenSearch() {
    this.setData({ searchVisible: true });
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
    const app = getApp();
    const self = this;
    app.addToWatchlist(full).then(function (result) {
      if (result && result.needLogin) {
        auth.promptLogin().then(function () {
          return app.addToWatchlist(full);
        }).then(function (r2) {
          if (r2 && r2.added) {
            wx.showToast({ title: '已加入自选', icon: 'success' });
            self.refreshList();
          }
        }).catch(function () {});
        return;
      }
      if (result && result.added) {
        wx.showToast({ title: '已加入自选', icon: 'success' });
        self.refreshList();
      } else {
        wx.showToast({ title: '已在自选', icon: 'none' });
      }
    });
  },

  onTapLogin() {
    auth.promptLogin().then(function () {
      this.setData({ needLogin: false });
      this.refreshList();
    }.bind(this)).catch(function () {});
  }
});
