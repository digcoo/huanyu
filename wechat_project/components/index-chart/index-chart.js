const { PERIOD_MAX_BARS } = require('../../utils/kline');

Component({
  properties: {
    indices: {
      type: Array,
      value: []
    },
    activeIndex: {
      type: Number,
      value: 0
    },
    activePeriod: {
      type: String,
      value: 'week'
    },
    flipped: {
      type: Boolean,
      value: false
    }
  },

  data: {
    currentIndex: 0,
    chartKlines: [],
    chartLabel: '',
    maxBars: 50
  },

  observers: {
    indices(indices) {
      this.setData({ currentIndex: 0 });
      this.syncChart(indices, 0, this.properties.activePeriod);
    },
    activePeriod(period) {
      this.syncChart(this.properties.indices, this.data.currentIndex, period);
    }
  },

  lifetimes: {
    attached() {
      this.setData({ currentIndex: this.properties.activeIndex || 0 });
      this.syncChart(
        this.properties.indices,
        this.properties.activeIndex || 0,
        this.properties.activePeriod
      );
    },
    ready() {
      this.syncChart(
        this.properties.indices,
        this.data.currentIndex,
        this.properties.activePeriod
      );
    }
  },

  methods: {
    onTabTap(e) {
      const idx = Number(e.currentTarget.dataset.index);
      this.setData({ currentIndex: idx });
      this.syncChart(this.properties.indices, idx, this.properties.activePeriod);
      this.triggerEvent('change', { index: idx });
    },

    syncChart(indices, activeIndex, period) {
      indices = indices || this.properties.indices || [];
      period = period || this.properties.activePeriod || 'week';
      activeIndex = activeIndex == null ? 0 : activeIndex;

      const item = indices[activeIndex];
      if (!item) {
        this.setData({ chartKlines: [], chartLabel: '' });
        return;
      }

      var klines = [];
      if (item.klines && item.klines[period]) {
        klines = item.klines[period].slice();
      } else if (Array.isArray(item.klines)) {
        klines = item.klines.slice();
      }

      this.setData({
        chartKlines: klines,
        chartLabel: '',
        maxBars: PERIOD_MAX_BARS[period] || 50
      });
    }
  }
});
