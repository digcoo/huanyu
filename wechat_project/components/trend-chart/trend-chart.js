Component({
  properties: {
    series: {
      type: Array,
      value: []
    },
    height: {
      type: Number,
      value: 160
    }
  },

  data: {
    lines: []
  },

  observers: {
    series(s) {
      this.buildLines(s);
    }
  },

  lifetimes: {
    attached() {
      this.buildLines(this.properties.series);
    }
  },

  methods: {
    buildLines(series) {
      if (!series || !series.length) {
        this.setData({ lines: [] });
        return;
      }

      let allVals = [];
      series.forEach(s => s.data.forEach(d => allVals.push(d.value)));
      const min = Math.min.apply(null, allVals);
      const max = Math.max.apply(null, allVals);
      const span = max - min || 1;
      const margin = span * 0.1;
      const range = { min: min - margin, max: max + margin, span: span + margin * 2 };
      const count = series[0].data.length;

      const lines = series.map(s => {
        const points = s.data.map((d, i) => ({
          x: (i / (count - 1) * 100).toFixed(2),
          y: ((range.max - d.value) / range.span * 100).toFixed(2)
        }));
        return {
          name: s.name,
          unit: s.unit,
          color: s.color,
          points,
          last: s.data[s.data.length - 1]
        };
      });

      this.setData({ lines });
    }
  }
});
