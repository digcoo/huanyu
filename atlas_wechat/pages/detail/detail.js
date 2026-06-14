const detailApi = require('../../utils/detail-api');
const config = require('../../utils/config');
const auth = require('../../utils/auth');
const adapter = require('../../utils/adapter');
const stockApi = require('../../utils/stock-api');
const ladderMarkers = require('../../utils/ladder-markers');
const strategyParams = require('../../utils/strategy-params');

function mapChartKlines(detail, period) {
  if (!detail || !detail.klines) return [];
  return detail.klines[period] ? detail.klines[period].slice() : [];
}

function syncLadderMarkers(page, detail, period, klines) {
  if (!detail || !ladderMarkers.isLadderStrategy(adapter.extractStrategy(detail.id)) || period !== 'min30') {
    page.setData({ ladderBarMarkers: [], ladderMarkerEpoch: 0 });
    return Promise.resolve([]);
  }
  if (config.useMock) {
    var mockMarkers = ladderMarkers.buildMockLadderMarkers(klines || page.data.chartKlines);
    page.setData({ ladderBarMarkers: mockMarkers, ladderMarkerEpoch: Date.now() });
    return Promise.resolve(mockMarkers);
  }

  function applyMarkerVo(m) {
    var markers = ladderMarkers.markersVoToBarMarkers(m);
    page.setData({ ladderBarMarkers: markers, ladderMarkerEpoch: Date.now() });
    return markers;
  }

  var cached = ladderMarkers.parseMarkersFromSignal(detail);
  if (cached && (cached.referenceDay || cached.signalDay)) {
    return Promise.resolve(applyMarkerVo(cached));
  }

  return stockApi.fetchLadderMarkers(detail.code).then(function (m) {
    if (m && (m.referenceDay || m.signalDay)) {
      return applyMarkerVo(m);
    }
    return stockApi.fetchSummary(detail.code).then(function (item) {
      if (item && item.signalMessage) {
        detail.signalMessage = item.signalMessage;
      }
      var parsed = ladderMarkers.parseMarkersFromSignal(detail);
      if (parsed && (parsed.referenceDay || parsed.signalDay)) {
        return applyMarkerVo(parsed);
      }
      page.setData({ ladderBarMarkers: [], ladderMarkerEpoch: Date.now() });
      return [];
    });
  }).catch(function () {
    page.setData({ ladderBarMarkers: [], ladderMarkerEpoch: Date.now() });
    return [];
  });
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
    notFound: false,
    inWatchlist: false,
    klineRefreshing: false,
    klineLive: false,
    ladderBarMarkers: [],
    ladderMarkerEpoch: 0
  },

  onLoad(options) {
    const sys = wx.getSystemInfoSync();
    const id = options.id || '';
    const strategy = adapter.extractStrategy(id);
    const savedPeriod = wx.getStorageSync('activePeriod') || 'week';
    const initialPeriod = strategy === 'ladder'
      ? strategyParams.ladderPrimaryPeriod(strategyParams.load('ladder'))
      : savedPeriod;
    const klineFlipped = !!wx.getStorageSync('klineFlipped');
    const self = this;

    this.setData({
      statusBarHeight: sys.statusBarHeight || 20,
      activePeriod: initialPeriod,
      klineFlipped,
      loading: true,
      notFound: false
    });

    detailApi.loadDetail(id, initialPeriod).then(function (detail) {
      if (!detail) {
        self.setData({ loading: false, notFound: true });
        return;
      }
      var app = getApp();
      var hint = app.globalData.detailSignalHint;
      if (hint && hint.id === id && hint.signalMessage) {
        detail.signalMessage = hint.signalMessage;
        app.globalData.detailSignalHint = null;
      }
      self.setData({
        detail: detail,
        chartKlines: mapChartKlines(detail, initialPeriod),
        loading: false,
        notFound: false
      }, function () {
        self.updateWatchState();
        syncLadderMarkers(self, detail, initialPeriod, mapChartKlines(detail, initialPeriod));
      });
    }).catch(function () {
      self.setData({ loading: false, notFound: true });
    });
  },

  onShow() {
    const detail = this.data.detail;
    const period = this.data.activePeriod;
    this.setData({
      klineFlipped: !!wx.getStorageSync('klineFlipped')
    });
    if (detail && ladderMarkers.isLadderStrategy(adapter.extractStrategy(detail.id)) && period === 'min30') {
      syncLadderMarkers(this, detail, period, this.data.chartKlines);
    }
    this.updateWatchState();
  },

  updateWatchState() {
    const app = getApp();
    const detail = this.data.detail;
    if (!detail) return;
    this.setData({
      inWatchlist: app.isInWatchlist(detail.id)
    });
  },

  onBack() {
    wx.navigateBack();
  },

  onCopyCode() {
    const detail = this.data.detail;
    if (!detail || !detail.code) return;
    const code = adapter.normalizeCode(detail.code);
    if (!code) return;
    wx.setClipboardData({
      data: code,
      success: function () {
        wx.showToast({ title: '代码已复制', icon: 'success', duration: 1200 });
      }
    });
  },

  onPeriodChange(e) {
    const period = e.detail.period;
    wx.setStorageSync('activePeriod', period);
    const detail = this.data.detail;
    if (!detail) return;

    if (config.useMock || (detail.klines && detail.klines[period])) {
      var klines = mapChartKlines(detail, period);
      this.setData({
        activePeriod: period,
        chartKlines: klines,
        klineLive: false
      });
      syncLadderMarkers(this, detail, period, klines);
      return;
    }

    const self = this;
    detailApi.loadKlinesForPeriod(detail.id, period).then(function (klines) {
      detail.klines[period] = klines;
      self.setData({
        activePeriod: period,
        detail: detail,
        chartKlines: klines.slice(),
        klineLive: false
      });
      syncLadderMarkers(self, detail, period, klines);
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

  onRefreshKlines() {
    const detail = this.data.detail;
    const period = this.data.activePeriod;
    if (!detail || !detail.id || this.data.klineRefreshing) return;

    const self = this;
    this.setData({ klineRefreshing: true });

    detailApi.refreshKlinesForPeriod(detail.id, period).then(function (klines) {
      if (!klines || !klines.length) {
        self.setData({ klineRefreshing: false });
        wx.showToast({ title: '拉取失败，库内数据未更新时可重试', icon: 'none' });
        return;
      }

      detail.klines = detail.klines || {};
      detail.klines[period] = klines;

      const last = klines[klines.length - 1];
      if (last && last.close != null && period === 'day') {
        detail.price = last.close;
      }

      self.setData({
        detail: detail,
        chartKlines: klines.slice(),
        klineRefreshing: false,
        klineLive: !config.useMock
      });
      syncLadderMarkers(self, detail, period, klines);
      wx.showToast({ title: '已拉取最新 K 线', icon: 'success', duration: 1200 });
    }).catch(function () {
      self.setData({ klineRefreshing: false });
      wx.showToast({ title: '拉取失败', icon: 'none' });
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

  onCompetitorTap(e) {
    const code = e.currentTarget.dataset.code;
    const detail = this.data.detail;
    if (!code || !detail) return;
    const strategy = adapter.extractStrategy(detail.id);
    const id = adapter.makeStockId(strategy, 'cn', code);
    wx.navigateTo({ url: '/pages/detail/detail?id=' + encodeURIComponent(id) });
  },

  onAddWatchlist() {
    const app = getApp();
    const detail = this.data.detail;
    if (!detail) return;
    const self = this;
    app.addToWatchlist(detail).then(function (result) {
      if (result && result.needLogin) {
        auth.promptLogin().then(function () {
          return app.addToWatchlist(detail);
        }).then(function (r2) {
          if (r2 && r2.added) {
            wx.showToast({ title: '已加入自选', icon: 'success' });
            self.setData({ inWatchlist: true });
          } else if (r2 && r2.duplicate) {
            wx.showToast({ title: '已在自选', icon: 'none' });
            self.setData({ inWatchlist: true });
          }
        }).catch(function () {});
        return;
      }
      if (result && result.added) {
        wx.showToast({ title: '已加入自选', icon: 'success' });
        self.setData({ inWatchlist: true });
      } else if (result && result.duplicate) {
        wx.showToast({ title: '已在自选', icon: 'none' });
        self.setData({ inWatchlist: true });
      }
    });
  }
});
