const {
  loadHistory,
  enrichRecord,
  attachChartKlines,
  computeSummary,
  filterRecords
} = require('../../utils/history');

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
    filterEmpty: false
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
    this.refreshList();
  },

  refreshList() {
    const app = getApp();
    const period = this.data.activePeriod;
    const raw = attachChartKlines(loadHistory().map(enrichRecord), period);
    const summary = computeSummary(raw);
    const activeFilter = this.data.activeFilter;
    const filtered = filterRecords(raw, activeFilter);
    const isEmpty = raw.length === 0;
    const navHeight = this.data.statusBarHeight + 56;
    const contentTop = navHeight + (isEmpty ? 0 : 52);

    this.setData({
      summary: summary,
      items: filtered,
      isEmpty: isEmpty,
      filterEmpty: raw.length > 0 && filtered.length === 0,
      watchlistCount: app.globalData.watchlist.length,
      contentTop: contentTop
    });
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
  }
});
