const { MARKETS, STRATEGIES } = require('../../utils/mock');
const { findStockByIdAsync } = require('../../utils/search');
const auth = require('../../utils/auth');
const config = require('../../utils/config');
const { rehydrateListAsync } = require('../../utils/watchlist');

function mapChartKlines(list, period) {
  return list.map(function (item) {
    var klines = item.klines && item.klines[period] ? item.klines[period].slice() : [];
    return Object.assign({}, item, { chartKlines: klines });
  });
}

const { normalizeTimestamp } = require('../../utils/time');

function formatAddedAt(ts) {
  if (!ts) return '';
  const ms = normalizeTimestamp(ts);
  if (!ms) return '';
  const d = new Date(ms);
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
    const self = this;
    const app = getApp();
    if (require('../../utils/watchlist-api').isRemoteEnabled()) {
      auth.ensureLogin().then(function () {
        return app.syncFromServer();
      }).finally(function () {
        self.refreshList();
      });
      return;
    }
    this.refreshList();
  },

  refreshList() {
    const app = getApp();
    const period = wx.getStorageSync('activePeriod') || this.data.activePeriod;
    const klineFlipped = !!wx.getStorageSync('klineFlipped');
    const raw = app.getWatchlist();
    const self = this;

    function renderList(hydrated) {
      const marketTabs = buildMarketTabs(hydrated);
      let activeMarket = self.data.activeMarket;

      if (activeMarket !== 'all' && !marketTabs.some(function (t) { return t.id === activeMarket; })) {
        activeMarket = 'all';
      }

      const mapped = mapChartKlines(hydrated, period);
      const enriched = enrichItems(mapped);
      const filtered = activeMarket === 'all'
        ? enriched
        : enriched.filter(function (item) { return item.market === activeMarket; });

      const isEmpty = hydrated.length === 0;

      self.setData({
        activePeriod: period,
        klineFlipped: klineFlipped,
        marketTabs: marketTabs,
        activeMarket: activeMarket,
        items: filtered,
        totalCount: hydrated.length,
        watchlistCount: hydrated.length,
        isEmpty: isEmpty,
        filterEmpty: hydrated.length > 0 && filtered.length === 0,
        watchlistIds: hydrated.map(function (item) { return item.id; })
      }, function () {
        self.measureNavHeight();
      });
    }

    if (config.useMock) {
      renderList(raw);
      return;
    }

    rehydrateListAsync(raw, period).then(renderList);
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
      title: klineFlipped ? '坐标已翻转（低价在上）' : '坐标已还原',
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
    const self = this;
    getApp().removeFromWatchlist(item.id, 'swipe').then(function () {
      self.refreshList();
    });
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
        getApp().removeFromWatchlist(item.id, 'manual').then(function () {
          wx.showToast({ title: '已移出', icon: 'none', duration: 1000 });
          self.refreshList();
        });
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
    const app = getApp();
    const self = this;
    findStockByIdAsync(item.id).then(function (full) {
      if (!full) return;
      return app.addToWatchlist(full).then(function (result) {
        if (result && result.needLogin) {
          return auth.promptLogin().then(function () {
            return app.addToWatchlist(full);
          }).then(function (r2) {
            if (r2 && r2.added) {
              wx.showToast({ title: '已加入自选', icon: 'success' });
              self.refreshList();
            }
          });
        }
        if (result && result.added) {
          wx.showToast({ title: '已加入自选', icon: 'success' });
          self.refreshList();
        } else {
          wx.showToast({ title: '已在自选', icon: 'none' });
        }
      });
    }).catch(function () {});
  },

  onTapLogin() {
    const self = this;
    auth.promptLogin().then(function () {
      return getApp().syncFromServer();
    }).then(function () {
      self.setData({ needLogin: false });
      self.refreshList();
    }).catch(function () {});
  }
});
