const config = require('./utils/config');
const auth = require('./utils/auth');

App({
  globalData: {
    watchlist: [],
    history: [],
    ignoredIds: [],
    loggedIn: false
  },

  onLaunch() {
    const { loadWatchlist } = require('./utils/watchlist');
    const { loadHistory } = require('./utils/history');
    this.globalData.watchlist = loadWatchlist();
    this.globalData.history = loadHistory();
    this.globalData.ignoredIds = wx.getStorageSync('ignoredIds') || [];
    this.globalData.loggedIn = auth.isLoggedIn();

    if (config.requireLoginForWatchlist && !auth.isLoggedIn()) {
      auth.login().then(function () {
        this.globalData.loggedIn = true;
      }.bind(this)).catch(function () {});
    }
  },

  addToWatchlist(item) {
    if (!item || !item.id) {
      return Promise.resolve({ added: false });
    }

    const self = this;
    const exists = this.globalData.watchlist.some(function (w) { return w.id === item.id; });
    if (exists) {
      return Promise.resolve({ added: false, duplicate: true });
    }

    function doAdd() {
      const entryPrice = item.price;
      const entry = Object.assign({}, item, {
        addedAt: Date.now(),
        entryPrice: entryPrice
      });
      delete entry.chartKlines;
      self.globalData.watchlist.unshift(entry);
      const { saveWatchlist } = require('./utils/watchlist');
      saveWatchlist(self.globalData.watchlist);
      return { added: true };
    }

    if (!config.requireLoginForWatchlist) {
      return Promise.resolve(doAdd());
    }

    if (!auth.isLoggedIn()) {
      return Promise.resolve({ added: false, needLogin: true });
    }

    return Promise.resolve(doAdd());
  },

  removeFromWatchlist(id, removeReason) {
    const item = this.globalData.watchlist.find(function (w) { return w.id === id; });
    if (!item) return false;

    const self = this;
    const { archiveRecordAsync } = require('./utils/history');
    archiveRecordAsync(item, removeReason || 'manual').then(function () {
      self.globalData.history = require('./utils/history').loadHistory();
    });

    this.globalData.watchlist = this.globalData.watchlist.filter(function (w) { return w.id !== id; });
    const { saveWatchlist } = require('./utils/watchlist');
    saveWatchlist(this.globalData.watchlist);
    return true;
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
