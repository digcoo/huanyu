const detailMock = require('../../utils/detail-mock');
const getDetailById = detailMock.getDetailById;
const auth = require('../../utils/auth');

function mapChartKlines(detail, period) {
  if (!detail || !detail.klines) return [];
  return detail.klines[period] ? detail.klines[period].slice() : [];
}

Page({
  data: {
    statusBarHeight: 20,
    activePeriod: 'week',
    klineFlipped: false,
    detail: null,
    chartKlines: [],
    expandedModules: {
      financial: false,
      operation: false,
      chain: false,
      capital: false
    },
    loading: true,
    notFound: false
  },

  onLoad(options) {
    const sys = wx.getSystemInfoSync();
    const savedPeriod = wx.getStorageSync('activePeriod') || 'week';
    const klineFlipped = !!wx.getStorageSync('klineFlipped');
    const id = options.id || '';

    this.setData({
      statusBarHeight: sys.statusBarHeight || 20,
      activePeriod: savedPeriod,
      klineFlipped
    });

    const detail = getDetailById(id);
    if (!detail) {
      this.setData({ loading: false, notFound: true });
      return;
    }

    this.setData({
      detail,
      chartKlines: mapChartKlines(detail, savedPeriod),
      loading: false,
      notFound: false
    });
  },

  onShow() {
    this.setData({
      klineFlipped: !!wx.getStorageSync('klineFlipped'),
      activePeriod: wx.getStorageSync('activePeriod') || this.data.activePeriod
    });
  },

  onBack() {
    wx.navigateBack();
  },

  onPeriodChange(e) {
    const period = e.detail.period;
    wx.setStorageSync('activePeriod', period);
    this.setData({
      activePeriod: period,
      chartKlines: mapChartKlines(this.data.detail, period)
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

  onToggleModule(e) {
    const key = e.detail.key;
    if (!key) return;
    const expanded = this.data.expandedModules[key];
    var patch = {};
    patch['expandedModules.' + key] = !expanded;
    this.setData(patch);
  },

  onAddWatchlist() {
    const app = getApp();
    const detail = this.data.detail;
    if (!detail) return;
    app.addToWatchlist(detail).then(function (result) {
      if (result && result.needLogin) {
        auth.promptLogin().then(function () {
          return app.addToWatchlist(detail);
        }).then(function (r2) {
          if (r2 && r2.added) wx.showToast({ title: '已加入自选', icon: 'success' });
          else if (r2 && r2.duplicate) wx.showToast({ title: '已在自选', icon: 'none' });
        }).catch(function () {});
        return;
      }
      if (result && result.added) wx.showToast({ title: '已加入自选', icon: 'success' });
      else if (result && result.duplicate) wx.showToast({ title: '已在自选', icon: 'none' });
    });
  }
});
