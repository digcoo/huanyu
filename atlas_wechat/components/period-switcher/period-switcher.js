const PERIODS = [
  { id: 'year', label: '年K' },
  { id: 'month', label: '月K' },
  { id: 'week', label: '周K' },
  { id: 'day', label: '日K' },
  { id: 'min30', label: '30分' }
];

function filterPeriods(allowed) {
  if (!allowed || !allowed.length) return PERIODS;
  var map = {};
  allowed.forEach(function (id) { map[id] = true; });
  return PERIODS.filter(function (item) { return map[item.id]; });
}

Component({
  properties: {
    activePeriod: {
      type: String,
      value: 'week'
    },
    flipped: {
      type: Boolean,
      value: false
    },
    allowedPeriods: {
      type: Array,
      value: []
    }
  },

  data: {
    periods: PERIODS
  },

  observers: {
    allowedPeriods: function (allowed) {
      this.setData({ periods: filterPeriods(allowed) });
    }
  },

  lifetimes: {
    attached: function () {
      this.setData({ periods: filterPeriods(this.properties.allowedPeriods) });
    }
  },

  methods: {
    onTap(e) {
      const id = e.currentTarget.dataset.id;
      if (!id) return;
      // 允许重复点击当前周期（如短线默认日K）以触发 K 线刷新
      this.triggerEvent('change', { period: id });
    },

    onFlip() {
      this.triggerEvent('flip', { flipped: !this.properties.flipped });
    }
  }
});
