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
    rows: []
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
        this.setData({ dimensions: [], companyPoints: [], industryPoints: [], rows: [] });
        return;
      }

      const rows = radar.dimensions.map((dim, i) => ({
        dim,
        company: radar.company[i],
        industry: radar.industry[i],
        unit: radar.unit ? radar.unit[i] : '',
        win: radar.company[i] >= radar.industry[i]
      }));

      this.setData({
        dimensions: radar.dimensions,
        companyPoints: radar.companyPoints || [],
        industryPoints: radar.industryPoints || [],
        rows
      });
    }
  }
});
