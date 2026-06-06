Component({
  properties: {
    active: {
      type: String,
      value: 'index'
    },
    watchlistCount: {
      type: Number,
      value: 0
    }
  },

  methods: {
    onNavTap(e) {
      const page = e.currentTarget.dataset.page;
      if (!page || page === this.properties.active) return;
      if (this._navigating) return;

      const urls = {
        index: '/pages/index/index',
        watchlist: '/pages/watchlist/watchlist',
        history: '/pages/history/history'
      };
      const url = urls[page];
      if (!url) return;

      this._navigating = true;
      wx.reLaunch({
        url: url,
        complete: function () {
          this._navigating = false;
        }.bind(this)
      });
    }
  }
});
