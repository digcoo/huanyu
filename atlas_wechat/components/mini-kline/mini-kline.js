const { calcPriceRange, buildCloseMaSegments, MA_LINE_CONFIGS, findBarIndexByTimestamp } = require('../../utils/kline');

const MA_LEGEND = MA_LINE_CONFIGS.map(function (cfg) {
  return {
    key: 'ma' + cfg.period,
    label: 'MA' + cfg.period,
    color: cfg.color,
    period: cfg.period
  };
});

function buildMaLegend(count) {
  return MA_LEGEND.filter(function (item) {
    return count >= item.period;
  });
}

Component({
  properties: {
    klines: {
      type: Array,
      value: []
    },
    label: {
      type: String,
      value: ''
    },
    resonance: {
      type: String,
      value: 'none'
    },
    size: {
      type: String,
      value: 'mini'
    },
    maxBars: {
      type: Number,
      value: 50
    },
    flipped: {
      type: Boolean,
      value: false
    },
    markerLabel: {
      type: String,
      value: ''
    },
    markerAt: {
      type: Number,
      value: 0
    },
    activePeriod: {
      type: String,
      value: 'week'
    },
    updatedLabel: {
      type: String,
      value: ''
    }
  },

  data: {
    bars: [],
    maLines: [],
    maSegments: [],
    maLegend: false,
    markerLeft: null
  },

  observers: {
    klines(klines) {
      this.render(klines);
    },
    size() {
      this.render(this.properties.klines);
    },
    maxBars() {
      this.render(this.properties.klines);
    },
    markerLabel() {
      this.render(this.properties.klines);
    },
    markerAt() {
      this.render(this.properties.klines);
    },
    activePeriod() {
      this.render(this.properties.klines);
    }
  },

  lifetimes: {
    attached() {
      this.render(this.properties.klines);
    },
    ready() {
      this.render(this.properties.klines);
    }
  },

  methods: {
    render(klines) {
      const size = this.properties.size;
      const maxBars = this.properties.maxBars || 50;

      if (!klines || !klines.length) {
        this._maBuild = null;
        this.setData({
          bars: [],
          maLines: [],
          maSegments: [],
          maLegend: false,
          markerLeft: null
        });
        return;
      }

      const markerLabel = this.properties.markerLabel;
      const markerAt = this.properties.markerAt;
      const activePeriod = this.properties.activePeriod || 'week';
      const isCard = size === 'card' || size === 'wide';
      const limit = maxBars > 0 ? maxBars : 50;
      const full = klines.slice();
      const sliceOffset = klines.length > limit ? klines.length - limit : 0;
      const sliced = sliceOffset > 0 ? klines.slice(-limit) : full.slice();

      const marginRatio = isCard ? 0.04 : 0.05;
      const range = calcPriceRange(sliced, marginRatio);
      const { max, span } = range;
      const count = sliced.length;
      const slotW = 100 / count;
      const bodyRatio = isCard ? 0.94 : 0.88;
      const gapRatio = (1 - bodyRatio) / 2;
      const minBodyPct = isCard ? 2.8 : 1.5;

      var markerIndex = null;
      if (markerLabel) {
        if (markerAt != null && markerAt !== '' && markerAt !== 0) {
          markerIndex = findBarIndexByTimestamp(full, markerAt, activePeriod, sliceOffset);
        }
        if (markerIndex == null) {
          markerIndex = count - 1;
        }
      }

      const bars = sliced.map((k, i) => {
        const isBull = k.close >= k.open;
        const bodyTopVal = Math.max(k.open, k.close);
        const bodyBotVal = Math.min(k.open, k.close);

        const highTop = ((max - k.high) / span * 100);
        const lowTop = ((max - k.low) / span * 100);
        let bodyTop = ((max - bodyTopVal) / span * 100);
        let bodyH = Math.max((bodyTopVal - bodyBotVal) / span * 100, minBodyPct);

        if ((bodyTopVal - bodyBotVal) / span * 100 < 0.4) {
          bodyH = minBodyPct;
          bodyTop = ((max - (bodyTopVal + bodyBotVal) / 2) / span * 100) - bodyH / 2;
        }

        const bodyBottom = bodyTop + bodyH;
        const upperWickH = Math.max(bodyTop - highTop, 0);
        const lowerWickH = Math.max(lowTop - bodyBottom, 0);

        return {
          left: (i * slotW + slotW * gapRatio).toFixed(2),
          width: (slotW * bodyRatio).toFixed(2),
          upperWickTop: highTop.toFixed(2),
          upperWickH: upperWickH.toFixed(2),
          lowerWickTop: bodyBottom.toFixed(2),
          lowerWickH: lowerWickH.toFixed(2),
          bodyTop: bodyTop.toFixed(2),
          bodyH: bodyH.toFixed(2),
          showUpperWick: upperWickH > 0.2,
          showLowerWick: lowerWickH > 0.2,
          dirClass: isBull ? 'bull' : 'bear',
          isMarker: markerIndex != null && i === markerIndex
        };
      });

      const markerLeft = markerIndex != null && count > 0
        ? ((markerIndex) * slotW + slotW / 2).toFixed(2)
        : null;

      const showMA = isCard && count >= 5;
      this._maBuild = showMA ? { sliced: sliced, range: range } : null;

      this.setData({
        bars: bars,
        markerLeft: markerLeft,
        maLegend: showMA,
        maLines: showMA ? buildMaLegend(count) : [],
        maSegments: []
      }, () => {
        if (showMA) {
          wx.nextTick(() => this.paintMaSegments());
        }
      });
    },

    paintMaSegments() {
      const payload = this._maBuild;
      if (!payload) return;

      const query = this.createSelectorQuery();
      query.select('.chart-wrap').boundingClientRect((rect) => {
        if (!rect || !rect.width || !rect.height) return;
        const segments = buildCloseMaSegments(
          payload.sliced,
          payload.range,
          rect.width,
          rect.height,
          MA_LINE_CONFIGS
        );
        this.setData({ maSegments: segments });
      }).exec();
    }
  }
});
