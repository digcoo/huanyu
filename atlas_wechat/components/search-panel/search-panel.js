const { searchStocks } = require('../../utils/search');

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
    searched: false
  },

  observers: {
    visible: function (visible) {
      if (visible) {
        this.setData({
          keyword: '',
          results: [],
          searched: false,
          history: wx.getStorageSync(HISTORY_KEY) || []
        });
      }
    },
    watchlistIds: function () {
      if (this.data.keyword) {
        this.setData({
          results: this.decorateResults(searchStocks(this.data.keyword))
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

    onInput: function (e) {
      const keyword = e.detail.value || '';
      const results = keyword.trim()
        ? this.decorateResults(searchStocks(keyword))
        : [];

      this.setData({
        keyword: keyword,
        results: results,
        searched: !!keyword.trim()
      });
    },

    onClear: function () {
      this.setData({
        keyword: '',
        results: [],
        searched: false
      });
    },

    decorateResults: function (list) {
      const watchSet = {};
      (this.properties.watchlistIds || []).forEach(function (id) {
        watchSet[id] = true;
      });

      return list.map(function (item) {
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
      const results = this.decorateResults(searchStocks(keyword));
      this.setData({
        keyword: keyword,
        results: results,
        searched: true
      });
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
