const {
  MARKETS,
  MARKET_INDICES,
  buildRecommendations
} = require('../../utils/mock');

const app = getApp();



function mapChartKlines(list, period) {
  return list.map(function (item) {
    var klines = item.klines && item.klines[period] ? item.klines[period].slice() : [];
    return Object.assign({}, item, { chartKlines: klines });
  });
}



Page({

  data: {

    statusBarHeight: 20,

    markets: MARKETS,

    activeMarket: 'cn',

    indices: [],
    recommendations: [],

    totalCount: 0,

    watchlistCount: 0,

    activePeriod: 'week',

    klineFlipped: false

  },



  onLoad() {

    const sys = wx.getSystemInfoSync();

    const savedPeriod = wx.getStorageSync('activePeriod') || 'week';
    const klineFlipped = !!wx.getStorageSync('klineFlipped');

    this.setData({

      statusBarHeight: sys.statusBarHeight || 20,

      activePeriod: savedPeriod,

      klineFlipped

    });

    this._allRecommendations = buildRecommendations();

    this.loadMarket('cn');

  },



  onShow() {

    this.setData({
      watchlistCount: app.globalData.watchlist.length,
      klineFlipped: !!wx.getStorageSync('klineFlipped')
    });

  },



  loadMarket(marketId) {

    const indices = MARKET_INDICES[marketId] || [];
    const all = this._allRecommendations[marketId] || [];

    const ignored = app.globalData.ignoredIds;

    this._baseList = all.filter(item => !ignored.includes(item.id));

    const period = this.data.activePeriod;



    this.setData({

      activeMarket: marketId,
      indices,
      recommendations: mapChartKlines(this._baseList, period),

      totalCount: all.length

    });

  },



  onMarketChange(e) {

    const { marketId } = e.detail;

    this.loadMarket(marketId);

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

    app.addToWatchlist(item);

    this._baseList = (this._baseList || []).filter(r => r.id !== item.id);

    this.setData({

      recommendations: mapChartKlines(this._baseList, this.data.activePeriod),

      watchlistCount: app.globalData.watchlist.length

    });

  },



  onIgnore(e) {

    const { item } = e.detail;

    app.ignoreItem(item.id);

    this._baseList = (this._baseList || []).filter(r => r.id !== item.id);

    this.setData({

      recommendations: mapChartKlines(this._baseList, this.data.activePeriod)

    });

  },



  onCardTap(e) {

    const { item } = e.detail;

    wx.navigateTo({

      url: `/pages/detail/detail?id=${item.id}&name=${encodeURIComponent(item.name)}`

    });

  }

});


