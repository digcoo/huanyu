const config = require('./utils/config');
const auth = require('./utils/auth');
const watchlistApi = require('./utils/watchlist-api');

App({
  globalData: {
    watchlist: [],
    history: [],
    ignoredIds: [],
    loggedIn: false
  },

  onLaunch() {
    const { loadWatchlistLocal, saveWatchlistLocal } = require('./utils/watchlist');
    const { loadHistoryLocal } = require('./utils/history');
    auth.sanitizeStoredSession();
    this.globalData.watchlist = loadWatchlistLocal();
    this.globalData.history = loadHistoryLocal();
    this.globalData.ignoredIds = wx.getStorageSync('ignoredIds') || [];
    this.globalData.loggedIn = auth.isLoggedIn();

    if (typeof console !== 'undefined' && console.info) {
      console.info('[Atlas] env=' + config.env + ', baseUrl=' + config.baseUrl);
    }

    const self = this;
    const afterAuth = function () {
      self.globalData.loggedIn = auth.isLoggedIn();
      if (watchlistApi.isRemoteEnabled() && auth.isLoggedIn()) {
        return self.syncFromServer();
      }
      return Promise.resolve();
    };

    if (watchlistApi.isRemoteEnabled()) {
      auth.ensureLogin().then(afterAuth).catch(function (err) {
        console.warn('[app] login failed', err);
      });
    } else {
      afterAuth();
    }
  },

  syncFromServer() {
    const self = this;
    const { saveWatchlistLocal } = require('./utils/watchlist');
    const { saveHistoryLocal } = require('./utils/history');
    return Promise.all([
      watchlistApi.fetchWatchlist(),
      watchlistApi.fetchHistory('all')
    ]).then(function (results) {
      const remoteList = results[0] || [];
      const historyRes = results[1] || {};
      self.globalData.watchlist = remoteList;
      self.globalData.history = historyRes.items || [];
      saveWatchlistLocal(remoteList);
      saveHistoryLocal(historyRes.items || []);
      return { watchlist: remoteList, history: historyRes.items || [] };
    }).catch(function (err) {
      console.warn('[app] syncFromServer failed', err);
      return null;
    });
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

    function doAddLocal(entry) {
      self.globalData.watchlist.unshift(entry);
      const { saveWatchlistLocal } = require('./utils/watchlist');
      saveWatchlistLocal(self.globalData.watchlist);
    }

    function buildEntry() {
      const entryPrice = item.price;
      const entry = Object.assign({}, item, {
        addedAt: Date.now(),
        entryPrice: entryPrice
      });
      delete entry.chartKlines;
      return entry;
    }

    if (!config.requireLoginForWatchlist) {
      const entry = buildEntry();
      doAddLocal(entry);
      return Promise.resolve({ added: true });
    }

    if (!auth.isLoggedIn()) {
      return Promise.resolve({ added: false, needLogin: true });
    }

    const entry = buildEntry();
    if (!watchlistApi.isRemoteEnabled()) {
      doAddLocal(entry);
      return Promise.resolve({ added: true });
    }

    return watchlistApi.addWatchlist(require('./utils/watchlist').toStoredItem(entry)).then(function (result) {
      if (result && result.added) {
        doAddLocal(entry);
        return { added: true };
      }
      if (result && result.duplicate) {
        return { added: false, duplicate: true };
      }
      doAddLocal(entry);
      return { added: true };
    }).catch(function () {
      doAddLocal(entry);
      return { added: true, offline: true };
    });
  },

  removeFromWatchlist(id, removeReason) {
    const item = this.globalData.watchlist.find(function (w) { return w.id === id; });
    if (!item) return Promise.resolve(false);

    const self = this;
    const reason = removeReason || 'manual';

    function doRemoveLocal() {
      self.globalData.watchlist = self.globalData.watchlist.filter(function (w) { return w.id !== id; });
      const { saveWatchlistLocal } = require('./utils/watchlist');
      saveWatchlistLocal(self.globalData.watchlist);
    }

    if (watchlistApi.isRemoteEnabled() && auth.isLoggedIn()) {
      return watchlistApi.removeWatchlist(id, reason).then(function (result) {
        doRemoveLocal();
        return self.refreshHistoryFromServer().then(function () {
          return !!(result && result.removed);
        });
      }).catch(function () {
        const { archiveRecordAsync } = require('./utils/history');
        return archiveRecordAsync(item, reason).then(function () {
          doRemoveLocal();
          self.globalData.history = require('./utils/history').loadHistoryLocal();
          return true;
        });
      });
    }

    const { archiveRecordAsync } = require('./utils/history');
    return archiveRecordAsync(item, reason).then(function () {
      doRemoveLocal();
      self.globalData.history = require('./utils/history').loadHistoryLocal();
      return true;
    });
  },

  refreshHistoryFromServer() {
    const self = this;
    if (!watchlistApi.isRemoteEnabled() || !auth.isLoggedIn()) {
      return Promise.resolve([]);
    }
    return watchlistApi.fetchHistory('all').then(function (res) {
      const items = (res && res.items) || [];
      self.globalData.history = items;
      require('./utils/history').saveHistoryLocal(items);
      return items;
    });
  },

  getWatchlist() {
    return this.globalData.watchlist.slice();
  },

  isInWatchlist(id) {
    return this.globalData.watchlist.some(function (w) { return w.id === id; });
  },

  ignoreItem(id) {
    if (!this.globalData.ignoredIds.includes(id)) {
      this.globalData.ignoredIds.push(id);
      wx.setStorageSync('ignoredIds', this.globalData.ignoredIds);
    }
  }
});
