App({
  globalData: {
    watchlist: [],
    ignoredIds: []
  },

  onLaunch() {
    const { loadWatchlist } = require('./utils/watchlist');
    this.globalData.watchlist = loadWatchlist();
    this.globalData.ignoredIds = wx.getStorageSync('ignoredIds') || [];
  },

  addToWatchlist(item) {
    if (!item || !item.id) return false;
    const exists = this.globalData.watchlist.some(function (w) { return w.id === item.id; });
    if (exists) return false;

    const entry = Object.assign({}, item, { addedAt: Date.now() });
    delete entry.chartKlines;
    this.globalData.watchlist.unshift(entry);
    const { saveWatchlist } = require('./utils/watchlist');
    saveWatchlist(this.globalData.watchlist);
    return true;
  },

  removeFromWatchlist(id) {
    const prev = this.globalData.watchlist.length;
    this.globalData.watchlist = this.globalData.watchlist.filter(function (w) { return w.id !== id; });
    if (this.globalData.watchlist.length !== prev) {
      const { saveWatchlist } = require('./utils/watchlist');
      saveWatchlist(this.globalData.watchlist);
      return true;
    }
    return false;
  },

  getWatchlist() {
    const { rehydrateItem } = require('./utils/watchlist');
    return this.globalData.watchlist.map(rehydrateItem);
  },

  ignoreItem(id) {
    if (!this.globalData.ignoredIds.includes(id)) {
      this.globalData.ignoredIds.push(id);
      wx.setStorageSync('ignoredIds', this.globalData.ignoredIds);
    }
  }
});
