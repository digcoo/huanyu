const detailApi = require('../../utils/detail-api');
const config = require('../../utils/config');
const auth = require('../../utils/auth');
const adapter = require('../../utils/adapter');
const stockApi = require('../../utils/stock-api');
const retestMarkers = require('../../utils/retest-markers');
const gc2Markers = require('../../utils/gc2-markers');
const bogoMarkers = require('../../utils/bogo-markers');
const trendmMarkers = require('../../utils/trendm-markers');
const dc2Markers = require('../../utils/dc2-markers');
const strategyParams = require('../../utils/strategy-params');
const ultraMarkers = require('../../utils/ultra-markers');
const trendMarkers = require('../../utils/trend-markers');
const mediumMarkers = require('../../utils/medium-markers');
const longMarkers = require('../../utils/long-markers');

function mapChartKlines(detail, period) {
  if (!detail || !detail.klines) return [];
  return detail.klines[period] ? detail.klines[period].slice() : [];
}

function applyBarMarkers(page, markers) {
  page.setData({ ladderBarMarkers: markers, ladderMarkerEpoch: Date.now() });
  return markers;
}

function clearBarMarkers(page) {
  page.setData({ ladderBarMarkers: [], ladderMarkerEpoch: 0 });
  return [];
}

function syncRetestMarkers(page, detail, period, klines) {
  if (!detail || !retestMarkers.isRetestStrategy(adapter.extractStrategy(detail.id))
      || !retestMarkers.shouldShowRetestMarkers(adapter.extractStrategy(detail.id), period)) {
    return Promise.resolve(clearBarMarkers(page));
  }
  if (config.useMock) {
    return Promise.resolve(applyBarMarkers(page, retestMarkers.buildMockRetestMarkers(klines || page.data.chartKlines)));
  }

  function applyMarkerVo(m) {
    return applyBarMarkers(page, retestMarkers.markersVoToBarMarkers(m));
  }

  var cached = retestMarkers.parseMarkersFromSignal(detail);
  if (cached && (cached.l0Day || cached.signalDay)) {
    return Promise.resolve(applyMarkerVo(cached));
  }

  return stockApi.fetchRetestMarkers(detail.code, period).then(function (m) {
    if (m && (m.l0Day || m.signalDay)) {
      return applyMarkerVo(m);
    }
    return stockApi.fetchSummary(detail.code).then(function (item) {
      if (item && item.signalMessage) {
        detail.signalMessage = item.signalMessage;
      }
      var parsed = retestMarkers.parseMarkersFromSignal(detail);
      if (parsed && (parsed.l0Day || parsed.signalDay)) {
        return applyMarkerVo(parsed);
      }
      return clearBarMarkers(page);
    });
  }).catch(function () {
    return clearBarMarkers(page);
  });
}

function syncGc2Markers(page, detail, period, klines) {
  if (!detail || !gc2Markers.isGc2Strategy(adapter.extractStrategy(detail.id))
      || !gc2Markers.shouldShowGc2Markers(adapter.extractStrategy(detail.id), period)) {
    return Promise.resolve(clearBarMarkers(page));
  }
  if (config.useMock) {
    return Promise.resolve(applyBarMarkers(page, gc2Markers.buildMockGc2Markers(klines || page.data.chartKlines)));
  }

  function applyMarkerVo(m) {
    return applyBarMarkers(page, gc2Markers.markersVoToBarMarkers(m));
  }

  var cached = gc2Markers.parseMarkersFromSignal(detail);
  if (cached && (cached.referenceDay || cached.signalDay)) {
    return Promise.resolve(applyMarkerVo(cached));
  }

  return stockApi.fetchGc2Markers(detail.code, period).then(function (m) {
    if (m && (m.referenceDay || m.signalDay)) {
      return applyMarkerVo(m);
    }
    return stockApi.fetchSummary(detail.code).then(function (item) {
      if (item && item.signalMessage) {
        detail.signalMessage = item.signalMessage;
      }
      var parsed = gc2Markers.parseMarkersFromSignal(detail);
      if (parsed && (parsed.referenceDay || parsed.signalDay)) {
        return applyMarkerVo(parsed);
      }
      return clearBarMarkers(page);
    });
  }).catch(function () {
    return clearBarMarkers(page);
  });
}

function syncBogoMarkers(page, detail, period, klines) {
  if (!detail || !bogoMarkers.isBogoStrategy(adapter.extractStrategy(detail.id))
      || !bogoMarkers.shouldShowBogoMarkers(adapter.extractStrategy(detail.id), period)) {
    return Promise.resolve(clearBarMarkers(page));
  }
  if (config.useMock) {
    return Promise.resolve(applyBarMarkers(page, bogoMarkers.buildMockBogoMarkers(klines || page.data.chartKlines)));
  }

  function applyMarkerVo(m) {
    return applyBarMarkers(page, bogoMarkers.markersVoToBarMarkers(m, detail));
  }

  var cached = bogoMarkers.parseMarkersFromSignal(detail);
  if (cached && (cached.referenceDay || cached.signalDay)) {
    return Promise.resolve(applyBarMarkers(page, bogoMarkers.markersVoToBarMarkers(cached, detail)));
  }

  return stockApi.fetchBogoMarkers(detail.code, period).then(function (m) {
    if (m && (m.referenceDay || m.signalDay)) {
      return applyMarkerVo(m);
    }
    return stockApi.fetchSummary(detail.code).then(function (item) {
      if (item && item.signalMessage) {
        detail.signalMessage = item.signalMessage;
      }
      if (item && item.trendMessage) {
        detail.trendMessage = item.trendMessage;
      }
      var parsed = bogoMarkers.parseMarkersFromSignal(detail);
      if (parsed && (parsed.referenceDay || parsed.signalDay)) {
        return applyBarMarkers(page, bogoMarkers.markersVoToBarMarkers(parsed, detail));
      }
      return clearBarMarkers(page);
    });
  }).catch(function () {
    return clearBarMarkers(page);
  });
}

function syncTrendmMarkers(page, detail, period, klines) {
  if (!detail || !trendmMarkers.isTrendmStrategy(adapter.extractStrategy(detail.id))
      || !trendmMarkers.shouldShowTrendmMarkers(adapter.extractStrategy(detail.id), period)) {
    return Promise.resolve(clearBarMarkers(page));
  }
  if (period === 'min30') {
    return syncUltraMarkers(page, detail, period, klines);
  }
  if (config.useMock) {
    return Promise.resolve(applyBarMarkers(page, trendmMarkers.buildMockTrendmMarkers(klines || page.data.chartKlines, period)));
  }

  function applyMarkerVo(m) {
    return applyBarMarkers(page, trendmMarkers.markersVoToBarMarkers(m, detail, period));
  }

  var cached = trendmMarkers.parseMarkersFromSignal(detail);
  if (cached && (cached.referenceDay || cached.signalDay)) {
    return Promise.resolve(applyBarMarkers(page, trendmMarkers.markersVoToBarMarkers(cached, detail, period)));
  }

  return stockApi.fetchTrendmMarkers(detail.code, period).then(function (m) {
    if (m && (m.referenceDay || m.signalDay)) {
      return applyMarkerVo(m);
    }
    return stockApi.fetchSummary(detail.code).then(function (item) {
      if (item && item.signalMessage) {
        detail.signalMessage = item.signalMessage;
      }
      if (item && item.trendMessage) {
        detail.trendMessage = item.trendMessage;
      }
      var parsed = trendmMarkers.parseMarkersFromSignal(detail);
      if (parsed && (parsed.referenceDay || parsed.signalDay)) {
        return applyBarMarkers(page, trendmMarkers.markersVoToBarMarkers(parsed, detail, period));
      }
      return clearBarMarkers(page);
    });
  }).catch(function () {
    return clearBarMarkers(page);
  });
}

function syncDc2Markers(page, detail, period, klines) {
  if (!detail || !dc2Markers.isDc2Strategy(adapter.extractStrategy(detail.id))
      || !dc2Markers.shouldShowDc2Markers(adapter.extractStrategy(detail.id), period)) {
    return Promise.resolve(clearBarMarkers(page));
  }
  if (config.useMock) {
    return Promise.resolve(applyBarMarkers(page, dc2Markers.buildMockDc2Markers(klines || page.data.chartKlines)));
  }

  function applyMarkerVo(m) {
    return applyBarMarkers(page, dc2Markers.markersVoToBarMarkers(m));
  }

  var cached = dc2Markers.parseMarkersFromSignal(detail);
  if (cached && (cached.referenceDay || cached.signalDay)) {
    return Promise.resolve(applyMarkerVo(cached));
  }

  return stockApi.fetchDc2Markers(detail.code, period).then(function (m) {
    if (m && (m.referenceDay || m.signalDay)) {
      return applyMarkerVo(m);
    }
    return stockApi.fetchSummary(detail.code).then(function (item) {
      if (item && item.signalMessage) {
        detail.signalMessage = item.signalMessage;
      }
      var parsed = dc2Markers.parseMarkersFromSignal(detail);
      if (parsed && (parsed.referenceDay || parsed.signalDay)) {
        return applyMarkerVo(parsed);
      }
      return clearBarMarkers(page);
    });
  }).catch(function () {
    return clearBarMarkers(page);
  });
}

function syncRefSigMarkers(page, detail, period, klines, opts) {
  if (!detail || !opts.shouldShow(adapter.extractStrategy(detail.id), period)) {
    return Promise.resolve(clearBarMarkers(page));
  }
  if (config.useMock) {
    return Promise.resolve(applyBarMarkers(page, opts.buildMock(klines || page.data.chartKlines)));
  }

  function applyMarkerVo(m) {
    return applyBarMarkers(page, opts.markersVoToBarMarkers(m));
  }

  var cached = opts.parseMarkersFromSignal(detail);
  if (cached && (cached.referenceDay || cached.signalDay)) {
    return Promise.resolve(applyMarkerVo(cached));
  }

  var uiStrategyId = adapter.extractStrategy(detail.id);

  return opts.fetchMarkers(detail.code, period, uiStrategyId).then(function (m) {
    if (m && (m.referenceDay || m.signalDay)) {
      return applyMarkerVo(m);
    }
    return stockApi.fetchSummary(detail.code).then(function (item) {
      if (item && item.signalMessage) {
        detail.signalMessage = item.signalMessage;
      }
      if (item && item.trendMessage) {
        detail.trendMessage = item.trendMessage;
      }
      var parsed = opts.parseMarkersFromSignal(detail);
      if (parsed && (parsed.referenceDay || parsed.signalDay)) {
        return applyMarkerVo(parsed);
      }
      return clearBarMarkers(page);
    });
  }).catch(function () {
    return clearBarMarkers(page);
  });
}

function syncUltraMarkers(page, detail, period, klines) {
  return syncRefSigMarkers(page, detail, period, klines, {
    shouldShow: ultraMarkers.shouldShowUltraMarkers,
    buildMock: ultraMarkers.buildMockUltraMarkers,
    markersVoToBarMarkers: ultraMarkers.markersVoToBarMarkers,
    parseMarkersFromSignal: ultraMarkers.parseMarkersFromSignal,
    fetchMarkers: stockApi.fetchUltraMarkers
  });
}

function syncTrendMarkers(page, detail, period, klines) {
  return syncRefSigMarkers(page, detail, period, klines, {
    shouldShow: trendMarkers.shouldShowTrendMarkers,
    buildMock: trendMarkers.buildMockTrendMarkers,
    markersVoToBarMarkers: trendMarkers.markersVoToBarMarkers,
    parseMarkersFromSignal: trendMarkers.parseMarkersFromSignal,
    fetchMarkers: stockApi.fetchTrendMarkers
  });
}

function syncMediumMarkers(page, detail, period, klines) {
  return syncRefSigMarkers(page, detail, period, klines, {
    shouldShow: mediumMarkers.shouldShowMediumMarkers,
    buildMock: mediumMarkers.buildMockMediumMarkers,
    markersVoToBarMarkers: mediumMarkers.markersVoToBarMarkers,
    parseMarkersFromSignal: mediumMarkers.parseMarkersFromSignal,
    fetchMarkers: stockApi.fetchMediumMarkers
  });
}

function syncLongMarkers(page, detail, period, klines) {
  return syncRefSigMarkers(page, detail, period, klines, {
    shouldShow: longMarkers.shouldShowLongMarkers,
    buildMock: longMarkers.buildMockLongMarkers,
    markersVoToBarMarkers: longMarkers.markersVoToBarMarkers,
    parseMarkersFromSignal: longMarkers.parseMarkersFromSignal,
    fetchMarkers: stockApi.fetchLongMarkers
  });
}

function syncBarMarkers(page, detail, period, klines) {
  var strategyId = adapter.extractStrategy(detail && detail.id);
  if (ultraMarkers.shouldShowUltraMarkers(strategyId, period)) {
    return syncUltraMarkers(page, detail, period, klines);
  }
  if (trendMarkers.shouldShowTrendMarkers(strategyId, period)) {
    return syncTrendMarkers(page, detail, period, klines);
  }
  if (mediumMarkers.shouldShowMediumMarkers(strategyId, period)) {
    return syncMediumMarkers(page, detail, period, klines);
  }
  if (longMarkers.shouldShowLongMarkers(strategyId, period)) {
    return syncLongMarkers(page, detail, period, klines);
  }
  if (trendmMarkers.shouldShowTrendmMarkers(strategyId, period)) {
    return syncTrendmMarkers(page, detail, period, klines);
  }
  if (bogoMarkers.shouldShowBogoMarkers(strategyId, period)) {
    return syncBogoMarkers(page, detail, period, klines);
  }
  if (gc2Markers.shouldShowGc2Markers(strategyId, period)) {
    return syncGc2Markers(page, detail, period, klines);
  }
  if (dc2Markers.shouldShowDc2Markers(strategyId, period)) {
    return syncDc2Markers(page, detail, period, klines);
  }
  if (retestMarkers.shouldShowRetestMarkers(strategyId, period)) {
    return syncRetestMarkers(page, detail, period, klines);
  }
  return Promise.resolve(clearBarMarkers(page));
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
    const initialPeriod = strategyParams.chartPrimaryPeriod(strategy, strategyParams.load(strategy))
      || savedPeriod;
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
        syncBarMarkers(self, detail, initialPeriod, mapChartKlines(detail, initialPeriod));
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
    if (detail) {
      syncBarMarkers(this, detail, period, this.data.chartKlines);
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
      syncBarMarkers(this, detail, period, klines);
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
      syncBarMarkers(self, detail, period, klines);
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
      syncBarMarkers(self, detail, period, klines);
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
  },

  onUnload() {
    this.setData({
      detail: null,
      chartKlines: [],
      ladderBarMarkers: [],
      ladderMarkerEpoch: 0
    });
  }
});
