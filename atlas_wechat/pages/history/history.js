const {
  loadHistory,
  enrichRecord,
  attachChartKlines,
  attachChartKlinesAsync,
  computeSummary,
  filterRecords
} = require('../../utils/history');
const auth = require('../../utils/auth');
const config = require('../../utils/config');

Page({
  data: {
    statusBarHeight: 20,
    contentTop: 88,
    watchlistCount: 0,
    activePeriod: 'week',
    klineFlipped: false,
    activeFilter: 'all',
    filters: [
      { id: 'all', label: '全部' },
      { id: 'win', label: '盈利' },
      { id: 'loss', label: '亏损' }
    ],
    summary: {
      total: 0,
      winRateText: '0%',
      avgHoldText: '0天',
      avgPnlText: '0%'
    },
    items: [],
    isEmpty: true,
    filterEmpty: false,
    needLogin: false
  },

  onLoad() {
    const sys = wx.getSystemInfoSync();
    const statusBarHeight = sys.statusBarHeight || 20;
    const savedPeriod = wx.getStorageSync('activePeriod') || 'week';
    const klineFlipped = !!wx.getStorageSync('klineFlipped');

    this.setData({
      statusBarHeight: statusBarHeight,
      activePeriod: savedPeriod,
      klineFlipped: klineFlipped
    });
  },

  onShow() {
    this.setData({
      klineFlipped: !!wx.getStorageSync('klineFlipped'),
      activePeriod: wx.getStorageSync('activePeriod') || this.data.activePeriod
    });
    if (config.requireLoginForWatchlist && !auth.isLoggedIn()) {
      this.setData({ needLogin: true, isEmpty: true, items: [] });
      return;
    }
    this.setData({ needLogin: false });
    this.refreshList();
  },

  refreshList() {
    const app = getApp();
    const period = this.data.activePeriod;
    const activeFilter = this.data.activeFilter;
    const enriched = loadHistory().map(enrichRecord);
    const self = this;

    function renderList(raw) {
      const summary = computeSummary(raw);
      const filtered = filterRecords(raw, activeFilter);
      const isEmpty = raw.length === 0;
      const navHeight = self.data.statusBarHeight + 56;
      const contentTop = navHeight + (isEmpty ? 0 : 52);

      self.setData({
        summary: summary,
        items: filtered,
        isEmpty: isEmpty,
        filterEmpty: raw.length > 0 && filtered.length === 0,
        watchlistCount: app.globalData.watchlist.length,
        contentTop: contentTop
      });
    }

    if (config.useMock) {
      renderList(attachChartKlines(enriched, period));
      return;
    }

    attachChartKlinesAsync(enriched, period).then(renderList);
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
    wx.showToast({
      title: klineFlipped ? 'K线已翻转' : 'K线已还原',
      icon: 'none',
      duration: 800
    });
  },

  onFilterTap(e) {
    const filter = e.currentTarget.dataset.filter;
    if (!filter || filter === this.data.activeFilter) return;
    this.setData({ activeFilter: filter });
    this.refreshList();
  },

  onItemTap(e) {
    const item = e.detail && e.detail.item;
    if (!item || !item.id) return;
    wx.navigateTo({
      url: '/pages/detail/detail?id=' + item.id + '&name=' + encodeURIComponent(item.name)
    });
  },

  onGoWatchlist() {
    wx.reLaunch({ url: '/pages/watchlist/watchlist' });
  },

  onTapLogin() {
    auth.promptLogin().then(function () {
      this.setData({ needLogin: false });
      this.refreshList();
    }.bind(this)).catch(function () {});
  }
});

