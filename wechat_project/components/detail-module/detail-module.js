Component({
  properties: {
    module: {
      type: Object,
      value: {}
    },
    moduleKey: {
      type: String,
      value: ''
    },
    expanded: {
      type: Boolean,
      value: false
    }
  },

  data: {
    kpiSummary: null
  },

  observers: {
    module: function (m) {
      this.buildKpiSummary(m);
    },
    expanded: function (val) {
      if (!val) return;
      var self = this;
      wx.nextTick(function () {
        var chart = self.selectComponent('#trendChart');
        if (chart && chart.drawAllCharts) {
          chart.drawAllCharts();
        }
      });
    }
  },

  lifetimes: {
    attached: function () {
      this.buildKpiSummary(this.properties.module);
    }
  },

  methods: {
    buildKpiSummary: function (module) {
      if (!module || !module.charts || !module.charts.length) {
        this.setData({ kpiSummary: null });
        return;
      }

      var primary = module.charts[0];
      var data = primary.data || [];
      if (!data.length) {
        this.setData({ kpiSummary: null });
        return;
      }

      var first = data[0];
      var last = data[data.length - 1];
      var changePct = 0;
      if (first.value !== 0) {
        changePct = ((last.value - first.value) / Math.abs(first.value)) * 100;
      }
      var trend = 'flat';
      if (changePct > 0.5) trend = 'up';
      else if (changePct < -0.5) trend = 'down';

      var sign = changePct > 0 ? '+' : '';
      this.setData({
        kpiSummary: {
          name: primary.name,
          changeLabel: sign + changePct.toFixed(1) + '%',
          trend: trend
        }
      });
    },

    onToggle: function () {
      this.triggerEvent('toggle', { key: this.properties.moduleKey });
    }
  }
});
