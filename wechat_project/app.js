App({
  globalData: {
    watchlist: [],
    ignoredIds: []
  },

  onLaunch() {
    const watchlist = wx.getStorageSync('watchlist') || [];
    const ignoredIds = wx.getStorageSync('ignoredIds') || [];
    this.globalData.watchlist = watchlist;
    this.globalData.ignoredIds = ignoredIds;
  },

  addToWatchlist(item) {
    const exists = this.globalData.watchlist.some(w => w.id === item.id);
    if (!exists) {
      this.globalData.watchlist.unshift({
        ...item,
        addedAt: Date.now()
      });
      wx.setStorageSync('watchlist', this.globalData.watchlist);
    }
  },

  ignoreItem(id) {
    if (!this.globalData.ignoredIds.includes(id)) {
      this.globalData.ignoredIds.push(id);
      wx.setStorageSync('ignoredIds', this.globalData.ignoredIds);
    }
  }
});
