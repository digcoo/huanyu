const { searchStocksAsync } = require('../../utils/search');

const HISTORY_KEY = 'searchHistory';
const MAX_HISTORY = 6;

Component({
  properties: {
    visible: {
      type: Boolean,
      value: false
    },
    watchlistIds: {
      type: Array,
      value: []
    },
    statusBarHeight: {
      type: Number,
      value: 20
    }
  },

  data: {
    keyword: '',
    results: [],
    history: [],
    searched: false,
    searching: false
  },

  lifetimes: {
    detached: function () {
      if (this._searchTimer) clearTimeout(this._searchTimer);
    }
  },

  observers: {
    visible: function (visible) {
      if (visible) {
        this.setData({
          keyword: '',
          results: [],
          searched: false,
          searching: false,
          history: wx.getStorageSync(HISTORY_KEY) || []
        });
      }
    },
    watchlistIds: function () {
      if (this.data.keyword) {
        this.setData({
          results: this.decorateResults(this.data.results)
        });
      }
    }
  },

  methods: {
    onMaskTap: function () {
      this.triggerEvent('close');
    },

    onPanelTap: function () {},

    onClose: function () {
      this.triggerEvent('close');
    },

    runSearch: function (keyword) {
      const self = this;
      const q = (keyword || '').trim();
      if (!q) {
        this.setData({ keyword: '', results: [], searched: false, searching: false });
        return;
      }

      this.setData({ searching: true });
      searchStocksAsync(q).then(function (list) {
        self.setData({
          keyword: keyword,
          results: self.decorateResults(list),
          searched: true,
          searching: false
        });
      }).catch(function () {
        self.setData({ searching: false });
      });
    },

    onInput: function (e) {
      const keyword = e.detail.value || '';
      if (this._searchTimer) clearTimeout(this._searchTimer);
      const self = this;
      this._searchTimer = setTimeout(function () {
        self.runSearch(keyword);
      }, 300);
    },

    onClear: function () {
      if (this._searchTimer) clearTimeout(this._searchTimer);
      this.setData({
        keyword: '',
        results: [],
        searched: false,
        searching: false
      });
    },

    decorateResults: function (list) {
      const watchSet = {};
      (this.properties.watchlistIds || []).forEach(function (id) {
        watchSet[id] = true;
      });

      return (list || []).map(function (item) {
        return Object.assign({}, item, {
          inWatchlist: !!watchSet[item.id],
          changeText: (item.changePct >= 0 ? '+' : '') + item.changePct + '%',
          changeClass: item.changePct >= 0 ? 'up' : 'down'
        });
      });
    },

    saveHistory: function (keyword) {
      const q = (keyword || '').trim();
      if (!q) return;

      let history = wx.getStorageSync(HISTORY_KEY) || [];
      history = history.filter(function (h) { return h !== q; });
      history.unshift(q);
      if (history.length > MAX_HISTORY) history = history.slice(0, MAX_HISTORY);
      wx.setStorageSync(HISTORY_KEY, history);
      this.setData({ history: history });
    },

    onHistoryTap: function (e) {
      const keyword = e.currentTarget.dataset.keyword || '';
      this.runSearch(keyword);
    },

    onClearHistory: function () {
      wx.removeStorageSync(HISTORY_KEY);
      this.setData({ history: [] });
    },

    onSelect: function (e) {
      const id = e.currentTarget.dataset.id;
      const item = this.data.results.find(function (r) { return r.id === id; });
      if (!item) return;
      this.saveHistory(this.data.keyword || item.name);
      this.triggerEvent('select', { item: item });
    },

    onAddWatchlist: function (e) {
      const id = e.currentTarget.dataset.id;
      const item = this.data.results.find(function (r) { return r.id === id; });
      if (!item) return;
      this.triggerEvent('addwatchlist', { item: item });
    }
  }
});
