Component({
  properties: {
    radar: {
      type: Object,
      value: {}
    }
  },

  data: {
    dimensions: [],
    companyPoints: [],
    industryPoints: [],
    rows: [],
    insights: []
  },

  observers: {
    radar(r) {
      this.parse(r);
    }
  },

  lifetimes: {
    attached() {
      this.parse(this.properties.radar);
    }
  },

  methods: {
    parse(radar) {
      if (!radar || !radar.dimensions) {
        this.setData({ dimensions: [], companyPoints: [], industryPoints: [], rows: [], insights: [] });
        return;
      }

      const insights = radar.insights || [];
      const rows = radar.dimensions.map(function (dim, i) {
        const insight = insights[i];
        return {
          dim: dim,
          company: radar.company[i],
          industry: radar.industry[i],
          unit: radar.unit ? radar.unit[i] : '',
          win: insight ? insight.win : radar.company[i] >= radar.industry[i]
        };
      });

      this.setData({
        dimensions: radar.dimensions,
        companyPoints: radar.companyPoints || [],
        industryPoints: radar.industryPoints || [],
        rows: rows,
        insights: insights
      });
    }
  }
});
