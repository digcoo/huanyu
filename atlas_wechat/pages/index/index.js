const {
  MARKET_INDICES,
  buildStrategyRecommendations
} = require('../../utils/mock');
const { findStockByIdAsync } = require('../../utils/search');
const { buildMarketsForUI } = require('../../utils/markets');
const config = require('../../utils/config');
const auth = require('../../utils/auth');
const stockApi = require('../../utils/stock-api');
const adapter = require('../../utils/adapter');
const strategyParams = require('../../utils/strategy-params');
const strategyNav = require('../../utils/strategy-nav');
const barMarkers = require('../../utils/bar-markers');
const listMemory = require('../../utils/list-memory');
const listViewCtx = require('../../utils/list-view-context');

const app = getApp();
const DEFAULT_STRATEGY = 'ultra';

const RECOMMEND_PAGE_SIZE = stockApi.RECOMMEND_PAGE_SIZE || 12;

function toPageNum(v) {
  var n = Number(v);
  return n > 0 ? n : 1;
}

function mapChartKlines(list, period, strategyId) {
  return list.map(function (item) {
    var klines = item.klines && item.klines[period] ? item.klines[period] : [];
    var barMarkersList = [];
    var priceLines = item.priceLines || [];
    if (barMarkers.shouldShowBarMarkers(strategyId, period)) {
      barMarkersList = item.barMarkers && item.barMarkers.length
        ? item.barMarkers
        : barMarkers.resolveBarMarkersForItem(item, strategyId, period, klines);
    }
    return Object.assign({}, item, {
      chartKlines: klines,
      barMarkers: barMarkersList,
      priceLines: priceLines
    });
  });
}

function applyHeldList(page, list, ctx, extra) {
  var trimmed = listMemory.trimHeldList(list);
  var slimmed = listMemory.slimListKlines(trimmed, ctx.period);
  page._baseList = slimmed;
  var patch = Object.assign({
    recommendations: mapChartKlines(slimmed, ctx.period, ctx.strategyId)
  }, extra || {});
  if (extra && extra.totalCount != null) {
    patch.emptyHint = buildListEmptyHint(ctx.strategyId, extra.totalCount, slimmed.length);
  }
  if (trimmed.length < (list || []).length) {
    patch.listTrimmedHint = '已释放较早条目以节省内存';
  } else {
    patch.listTrimmedHint = '';
  }
  page.setData(patch);
}

function migrateSavedStrategy(strategyId) {
  return strategyNav.migrateSavedStrategy(strategyId);
}

function dedupeAppend(baseList, newItems) {
  var seen = {};
  (baseList || []).forEach(function (item) {
    if (item && item.id) seen[item.id] = true;
  });
  var appended = [];
  (newItems || []).forEach(function (item) {
    if (item && item.id && !seen[item.id]) {
      seen[item.id] = true;
      appended.push(item);
    }
  });
  return {
    list: (baseList || []).concat(appended),
    appended: appended
  };
}

function resolvePagedHasMore(hasMore, lastPage, totalNum) {
  if (hasMore) return true;
  var page = toPageNum(lastPage);
  var total = Number(totalNum) || 0;
  if (total > 0 && page * RECOMMEND_PAGE_SIZE < total) {
    return true;
  }
  return false;
}

function resolveListHasMore(hasMore, listLength, totalNum) {
  var total = Number(totalNum) || 0;
  var shown = Number(listLength) || 0;
  if (total > 0 && shown < total) {
    return true;
  }
  return !!hasMore;
}

function filterNovelItems(items, seen) {
  return (items || []).filter(function (item) {
    return item && item.id && !seen[item.id];
  });
}

function buildListEmptyHint(strategyId, totalNum, visibleCount) {
  if (totalNum > 0 && visibleCount === 0) {
    return '列表加载异常，请重试或点击 ↻ 重跑策略';
  }
  if (totalNum === 0) {
    var paramHint = strategyParams.emptyResultHint(strategyId);
    if (paramHint) return paramHint;
    return '暂无推荐，可点击 ↻ 重跑策略';
  }
  return '切换市场查看更多标的';
}

function fetchVisibleRecommendations(strategyId, targetCount, startPage) {
  targetCount = targetCount || RECOMMEND_PAGE_SIZE;
  startPage = toPageNum(startPage);

  return stockApi.fetchRecommendations(strategyId, startPage, RECOMMEND_PAGE_SIZE).then(function (result) {
    var lastPage = toPageNum(result.page || startPage);
    var totalNum = result.totalNum != null ? Number(result.totalNum) : 0;
    var hasMore = !!result.hasMore;
    var rawItems = result.items || [];
    var seenAll = listMemory.rememberSeenIds({}, rawItems);
    var list = rawItems.length > targetCount ? rawItems.slice(0, targetCount) : rawItems;
    var more = resolveListHasMore(
      resolvePagedHasMore(hasMore, lastPage, totalNum),
      list.length,
      totalNum
    );
    return {
      items: list,
      page: lastPage,
      totalNum: totalNum,
      hasMore: more,
      seenIds: seenAll
    };
  });
}

function fetchNextRecommendationsPage(strategyId, backendPage, seenIds) {
  var seen = seenIds || {};
  var startPage = toPageNum(backendPage) + 1;
  var maxSkipPages = 30;
  var pagesScanned = 0;

  function fetchFrom(page) {
    page = toPageNum(page);
    pagesScanned++;
    return stockApi.fetchRecommendations(strategyId, page, RECOMMEND_PAGE_SIZE).then(function (result) {
      var pageNum = toPageNum(result.page || page);
      var totalNum = result.totalNum != null ? Number(result.totalNum) : 0;
      var apiMore = !!result.hasMore;
      var more = resolvePagedHasMore(apiMore, pageNum, totalNum);
      var novel = filterNovelItems(result.items, seen);
      seen = listMemory.rememberSeenIds(seen, result.items || []);
      if (novel.length === 0 && more && pagesScanned < maxSkipPages) {
        return fetchFrom(pageNum + 1);
      }
      if (novel.length === 0 && more) {
        more = false;
      }
      return {
        items: novel,
        page: pageNum,
        totalNum: totalNum,
        hasMore: more,
        seenIds: seen
      };
    });
  }

  return fetchFrom(startPage);
}

function attachKlinesInBackground(page, items, ctx) {
  if (!items || !items.length) return Promise.resolve([]);
  var klineGen = listViewCtx.bumpKlineGen(page);
  return stockApi.attachKlinesToItems(items, ctx.period, null, true).then(function (withKlines) {
    if (listViewCtx.isKlineStale(page, klineGen, ctx)) {
      return items;
    }
    var klineById = {};
    withKlines.forEach(function (item) {
      klineById[item.id] = listMemory.slimItemKlines(item, ctx.period);
    });
    var base = (page._baseList && page._baseList.length ? page._baseList : items);
    var merged = base.map(function (item) {
      return klineById[item.id] ? Object.assign({}, item, klineById[item.id]) : item;
    });
    if (!merged.length) return items;
    applyHeldList(page, merged, ctx, {
      totalCount: page.data.totalCount
    });
    barMarkers.enrichItemsWithBarMarkers(merged, ctx.strategyId, ctx.period).then(function (enriched) {
      if (listViewCtx.isKlineStale(page, klineGen, ctx)) return;
      if (enriched && enriched.length) {
        applyHeldList(page, enriched, ctx, {
          totalCount: page.data.totalCount
        });
      }
    }).catch(function () {});
    return merged;
  }).catch(function (err) {
    if (typeof console !== 'undefined' && console.warn) {
      console.warn('[Atlas] attachKlines failed', listViewCtx.contextLabel(ctx), err);
    }
    return items;
  });
}

function refreshIndices(page, ctx) {
  var klineGen = page._klineGen;
  return stockApi.fetchMarketIndices('cn', ctx.period).then(function (raw) {
    return adapter.mapMarketIndices(raw, ctx.period);
  }).catch(function () {
    return page.data.indices.length ? page.data.indices : (MARKET_INDICES.cn || []);
  }).then(function (indices) {
    if (!listViewCtx.isKlineStale(page, klineGen, ctx)) {
      page.setData({ indices: indices });
    }
  });
}

function formatRescanToast(result, page) {
  var listed = result && result.listed != null ? result.listed : 0;
  var shown = page && page.data && page.data.recommendations
    ? page.data.recommendations.length
    : (result && result.visible != null ? result.visible : 0);
  if (!result || !result.ok) {
    return '重跑失败';
  }
  if (result.loadFailed) {
    return '已扫描 ' + (result.rescanned || 0) + ' 只，列表加载失败';
  }
  if (listed <= 0 && shown <= 0) {
    return '扫描完成，无匹配';
  }
  var total = listed > 0 ? listed : shown;
  if (shown > 0 && total > shown) {
    return '已重跑，共 ' + total + ' 条，首屏 ' + shown + ' 条';
  }
  return '已重跑，共 ' + total + ' 条';
}

Page({
  data: {
    statusBarHeight: 20,
    toolbarStickyTop: 88,
    navPaddingRight: 96,

    markets: buildMarketsForUI(),
    activeStrategy: DEFAULT_STRATEGY,
    activeStrategyFamily: strategyNav.FAMILY_ULTRA,
    activeCascadeTier: 'short',
    showCascadeTierRow: false,
    allowedPeriods: listViewCtx.allowedPeriods(),
    strategyTitle: strategyNav.strategyTitleFor(DEFAULT_STRATEGY),

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
    paramsCustomized: false,
    rescanning: false,
    showBackTop: false,
    emptyHint: ''
  },

  onLoad() {
    const sys = wx.getSystemInfoSync();
    const menu = wx.getMenuButtonBoundingClientRect();
    const statusBarHeight = sys.statusBarHeight || 20;
    const navPaddingRight = sys.windowWidth - menu.left + 8;

    const savedPeriod = wx.getStorageSync('activePeriod') || 'week';
    const migrated = migrateSavedStrategy(wx.getStorageSync('activeStrategy'));
    if (migrated.strategy !== wx.getStorageSync('activeStrategy')) {
      wx.setStorageSync('activeStrategy', migrated.strategy);
    }
    const savedStrategy = migrated.strategy;
    const klineFlipped = !!wx.getStorageSync('klineFlipped');

    listViewCtx.initSession(this);
    const ctx = listViewCtx.createContext(
      savedStrategy,
      listViewCtx.normalizePeriod(savedStrategy, savedPeriod),
      'cn'
    );
    listViewCtx.commitContext(this, ctx, {
      statusBarHeight: statusBarHeight,
      navPaddingRight: navPaddingRight,
      klineFlipped: klineFlipped
    });

    this._allRecommendations = buildStrategyRecommendations();
    this.refreshParamsBadge(ctx.strategyId);
    if (config.useMock) {
      this.loadMarket(ctx);
    } else {
      this.loadMarketFromApi(ctx);
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
    strategyId = strategyId || listViewCtx.readContext(this).strategyId;
    var show = !config.useMock && strategyParams.hasCustomParams(strategyId);
    this.setData({
      showStrategyParams: show,
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
    var ctx = listViewCtx.readContext(this);
    if (detail.strategyId) {
      ctx = listViewCtx.createContext(detail.strategyId, ctx.period, ctx.marketId);
    }
    this.refreshParamsBadge(ctx.strategyId);

    if (config.useMock) {
      wx.showToast({ title: 'Mock 模式参数不生效', icon: 'none' });
      this.setData({ paramsVisible: false });
      return;
    }

    var self = this;
    var shouldRescan = detail.rescan !== false;

    if (shouldRescan) {
      this.runStrategyRescanAndReload(true).then(function () {
        self.setData({ paramsVisible: false });
      });
      return;
    }

    this.setData({ paramsVisible: false });
    this.loadMarketFromApi(ctx).then(function () {
      wx.showToast({ title: '已按新参数预览', icon: 'none' });
    });
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
    var ctx = listViewCtx.readContext(this);
    var panel = this.selectComponent('#strategyParamsPanel');
    var loadingShown = false;

    function hideRescanLoading() {
      if (!loadingShown) return;
      loadingShown = false;
      wx.hideLoading();
    }

    this.setData({ rescanning: true });
    if (panel) panel.setApplying(true);
    wx.showLoading({ title: '重跑策略中…', mask: true });
    loadingShown = true;
    listViewCtx.bumpLoadGen(self);

    return stockApi.triggerStrategyRescan(ctx.strategyId).then(function (result) {
      return self.loadMarketFromApi(ctx).then(function (loadMeta) {
        return {
          ok: result && result.ok,
          rescanned: result && result.saved != null ? result.saved : 0,
          listed: loadMeta && loadMeta.totalCount != null ? loadMeta.totalCount : 0,
          visible: loadMeta && loadMeta.visibleCount != null ? loadMeta.visibleCount : 0,
          loadFailed: false,
          strategy: result && result.strategy
        };
      }).catch(function () {
        return {
          ok: result && result.ok,
          rescanned: result && result.saved != null ? result.saved : 0,
          listed: 0,
          visible: 0,
          loadFailed: true,
          strategy: result && result.strategy
        };
      });
    }).then(function (result) {
      hideRescanLoading();
      if (showToast) {
        var ok = result && result.ok;
        var shown = self.data.recommendations ? self.data.recommendations.length : 0;
        var title = formatRescanToast(Object.assign({}, result, {
          visible: shown > 0 ? shown : (result && result.visible)
        }), self);
        wx.showToast({
          title: title,
          icon: ok && (shown > 0 || (result && result.listed > 0)) ? 'success' : 'none',
          duration: 2500
        });
      }
      return result;
    }).catch(function () {
      hideRescanLoading();
      if (showToast) {
        wx.showToast({ title: '重跑失败', icon: 'none' });
      }
    }).finally(function () {
      hideRescanLoading();
      self.setData({ rescanning: false });
      if (panel) panel.setApplying(false);
    });
  },

  onReachBottom() {
    if (config.useMock || this.data.activeMarket !== 'cn') return;
    this.loadMoreRecommendations();
  },

  onLoadMoreTap() {
    if (config.useMock || this.data.activeMarket !== 'cn') return;
    if (!this.data.hasMore || this.data.loadingMore || this.data.rescanning) return;
    this.loadMoreRecommendations();
  },

  loadMoreRecommendations() {
    var totalCount = Number(this.data.totalCount) || 0;
    var shown = (this.data.recommendations || []).length;
    if (totalCount > 0 && shown < totalCount && !this.data.hasMore) {
      this.setData({ hasMore: true });
    }
    if (!this.data.hasMore || this.data.loadingMore) return;
    if (this.data.loading && (!this.data.recommendations || this.data.recommendations.length === 0)) return;

    const self = this;
    const ctx = listViewCtx.readContext(this);
    const loadGen = this._loadGen;
    const backendPage = this._backendPage || this._loadPage || 1;
    const nextPage = toPageNum(backendPage) + 1;

    if (typeof console !== 'undefined' && console.info) {
      console.info('[Atlas] loadMore', listViewCtx.contextLabel(ctx), {
        nextPage: nextPage,
        shown: shown,
        total: totalCount
      });
    }

    this.setData({ loadingMore: true });

    fetchNextRecommendationsPage(ctx.strategyId, backendPage, this._seenRecommendationIds).then(function (result) {
      if (listViewCtx.isLoadStale(self, loadGen)) return;
      if (result.seenIds) {
        self._seenRecommendationIds = result.seenIds;
      }
      var merged = dedupeAppend(self._baseList, result.items);
      var fetchedPage = toPageNum(result.page || nextPage);
      self._backendPage = fetchedPage;
      self._loadPage = fetchedPage;
      var hasMore = resolveListHasMore(result.hasMore, merged.list.length, result.totalNum);
      applyHeldList(self, merged.list, ctx, {
        totalCount: result.totalNum,
        hasMore: hasMore
      });
      if (merged.appended.length) {
        attachKlinesInBackground(self, merged.appended, ctx);
      } else if (hasMore) {
        self.loadMoreRecommendations();
      }
    }).catch(function () {
      wx.showToast({ title: '加载更多失败', icon: 'none' });
    }).finally(function () {
      if (self.data.loadingMore) {
        self.setData({ loadingMore: false });
      }
    });
  },

  loadMarket(ctx) {
    const strategyId = ctx.strategyId;
    const period = ctx.period;
    const marketId = ctx.marketId;
    const indices = MARKET_INDICES[marketId] || [];
    const strategyPool = this._allRecommendations[strategyId] || {};
    const all = strategyPool[marketId] || [];

    this._baseList = all.slice();

    this.setData({
      activeMarket: marketId,
      indices: indices,
      recommendations: mapChartKlines(this._baseList, period, strategyId),
      totalCount: all.length,
      loading: false
    });
  },

  loadMarketFromApi(ctx) {
    if (ctx.marketId !== 'cn') {
      this.setData({
        activeMarket: ctx.marketId,
        indices: MARKET_INDICES[ctx.marketId] || [],
        recommendations: [],
        totalCount: 0,
        loading: false
      });
      return Promise.resolve({
        totalCount: 0,
        visibleCount: 0
      });
    }

    const self = this;
    const loadGen = listViewCtx.bumpLoadGen(this);

    this.setData({
      loading: true,
      recommendations: [],
      hasMore: false,
      loadingMore: false,
      totalCount: 0,
      emptyHint: ''
    });
    this._loadPage = 1;
    this._backendPage = 1;
    this._seenRecommendationIds = {};

    return fetchVisibleRecommendations(ctx.strategyId, RECOMMEND_PAGE_SIZE, 1).then(function (result) {
      if (listViewCtx.isLoadStale(self, loadGen)) return null;
      return Promise.all([
        Promise.resolve(result),
        stockApi.fetchMarketIndices('cn', ctx.period).then(function (raw) {
          return adapter.mapMarketIndices(raw, ctx.period);
        }).catch(function () {
          return MARKET_INDICES.cn || [];
        })
      ]);
    }).then(function (results) {
      if (!results || listViewCtx.isLoadStale(self, loadGen)) {
        if (!listViewCtx.isLoadStale(self, loadGen)) {
          self.setData({ loading: false });
        }
        return { totalCount: 0, visibleCount: 0 };
      }
      var pageResult = results[0];
      var indices = results[1];
      var visibleItems = pageResult.items || [];
      if (typeof console !== 'undefined' && console.info) {
        console.info('[Atlas] recommend list', listViewCtx.contextLabel(ctx), {
          totalNum: pageResult.totalNum,
          visible: visibleItems.length,
          page: pageResult.page
        });
      }
      self._loadPage = toPageNum(pageResult.page);
      self._backendPage = toPageNum(pageResult.page);
      self._seenRecommendationIds = pageResult.seenIds
        ? pageResult.seenIds
        : listMemory.initSeenIds(visibleItems);
      applyHeldList(self, visibleItems, ctx, {
        activeMarket: ctx.marketId,
        indices: indices,
        totalCount: pageResult.totalNum,
        hasMore: resolveListHasMore(pageResult.hasMore, visibleItems.length, pageResult.totalNum),
        loading: false
      });
      if (visibleItems.length) {
        attachKlinesInBackground(self, visibleItems, ctx);
      }
      return {
        totalCount: pageResult.totalNum != null ? pageResult.totalNum : 0,
        visibleCount: visibleItems.length
      };
    }).catch(function (err) {
      if (listViewCtx.isLoadStale(self, loadGen)) return { totalCount: 0, visibleCount: 0 };
      if (typeof console !== 'undefined' && console.warn) {
        console.warn('[Atlas] loadMarketFromApi failed', listViewCtx.contextLabel(ctx), err);
      }
      self.setData({
        loading: false,
        recommendations: [],
        totalCount: 0,
        hasMore: false,
        emptyHint: config.fallbackOnError
          ? ('无法连接后端 ' + config.baseUrl + '，请检查 config.local.js 与防火墙')
          : '加载失败，请稍后重试'
      });
      wx.showToast({ title: '加载失败', icon: 'none' });
    });
  },

  onMarketChange(e) {
    const marketId = e.detail.marketId;
    const cur = listViewCtx.readContext(this);
    const ctx = listViewCtx.createContext(cur.strategyId, cur.period, marketId);
    listViewCtx.commitContext(this, ctx);
    if (config.useMock) {
      this.loadMarket(ctx);
    } else {
      this.loadMarketFromApi(ctx);
    }
  },

  onMarketDisabled() {
    wx.showToast({
      title: config.marketComingSoonTip,
      icon: 'none',
      duration: 2000
    });
  },

  switchStrategy(strategyId) {
    if (!strategyId || strategyId === listViewCtx.readContext(this).strategyId) return;
    var migrated = migrateSavedStrategy(strategyId);
    if (!strategyNav.isActiveStrategy(migrated.strategy)) return;

    strategyId = migrated.strategy;
    var cur = listViewCtx.readContext(this);
    var ctx = listViewCtx.createContext(
      strategyId,
      listViewCtx.periodForStrategySwitch(strategyId),
      cur.marketId
    );
    listViewCtx.commitContext(this, ctx);
    this.refreshParamsBadge(ctx.strategyId);

    if (config.useMock) {
      this.loadMarket(ctx);
    } else {
      this.loadMarketFromApi(ctx);
    }
  },

  onStrategyFamilyChange(e) {
    var family = e.detail && e.detail.family;
    if (!family || family === this.data.activeStrategyFamily) return;
    if (!strategyNav.showTierRow(family)) {
      this.switchStrategy(strategyNav.strategyIdFor(family, 'short'));
      return;
    }
    var tier = this.data.activeCascadeTier || 'short';
    this.switchStrategy(strategyNav.strategyIdFor(family, tier));
  },

  onStrategyTierChange(e) {
    var tier = e.detail && e.detail.tier;
    if (!tier || tier === this.data.activeCascadeTier) return;
    this.switchStrategy(strategyNav.strategyIdFor(this.data.activeStrategyFamily, tier));
  },

  onStrategyChange(e) {
    var strategyId = e.detail && e.detail.strategyId;
    this.switchStrategy(strategyId);
  },

  onPeriodChange(e) {
    const period = e.detail && e.detail.period;
    if (!period) return;

    const cur = listViewCtx.readContext(this);
    const ctx = listViewCtx.createContext(cur.strategyId, period, cur.marketId);
    const samePeriod = cur.period === ctx.period;

    listViewCtx.commitContext(this, ctx);
    listViewCtx.bumpKlineGen(this);

    if (config.useMock) {
      this.setData({
        recommendations: mapChartKlines(this._baseList || [], ctx.period, ctx.strategyId)
      });
      return;
    }

    const baseList = samePeriod
      ? (this._baseList || [])
      : listMemory.slimListKlines(this._baseList || [], ctx.period);
    if (!samePeriod) {
      this._baseList = baseList;
    }
    this.setData({
      recommendations: mapChartKlines(baseList, ctx.period, ctx.strategyId)
    });
    attachKlinesInBackground(this, baseList, ctx);
    refreshIndices(this, ctx);
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
    const ctx = listViewCtx.readContext(this);
    this._baseList = (this._baseList || []).filter(function (r) { return r.id !== item.id; });
    applyHeldList(this, this._baseList, ctx, {
      totalCount: this.data.totalCount,
      watchlistCount: app.globalData.watchlist.length
    });
  },

  onCardTap(e) {
    const item = e.detail && e.detail.item;
    if (!item || !item.id) return;
    const app = getApp();
    if (item.signalMessage) {
      app.globalData.detailSignalHint = {
        id: item.id,
        signalMessage: item.signalMessage
      };
    }
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
    const app = getApp();
    if (item.signalMessage) {
      app.globalData.detailSignalHint = {
        id: item.id,
        signalMessage: item.signalMessage
      };
    }
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
