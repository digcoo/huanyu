Component({
  properties: {
    active: {
      type: String,
      value: 'index'
    }
  },

  methods: {
    onNavTap(e) {
      const page = e.currentTarget.dataset.page;
      if (page === this.data.active) return;
      const urls = {
        index: '/pages/index/index',
        watchlist: '/pages/watchlist/watchlist'
      };
      wx.redirectTo({ url: urls[page] });
    }
  }
});
