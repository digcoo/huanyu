const { calcPriceRange } = require('../../utils/kline');

function calcMA(klines, period) {
  return klines.map((_, i) => {
    if (i < period - 1) return null;
    let sum = 0;
    for (let j = i - period + 1; j <= i; j++) sum += klines[j].close;
    return sum / period;
  });
}

function maToPoints(klines, period, range) {
  const ma = calcMA(klines, period);
  const count = klines.length;
  const slotW = 100 / count;
  const points = [];
  ma.forEach((v, i) => {
    if (v == null) return;
    points.push({
      x: (i * slotW + slotW / 2).toFixed(2),
      y: ((range.max - v) / range.span * 100).toFixed(2)
    });
  });
  return points;
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
    }
  },

  data: {
    bars: [],
    ma7Points: [],
    ma25Points: [],
    maLegend: false
  },

  observers: {
    klines(klines) {
      this.render(klines);
    },
    size(size) {
      this.render(this.properties.klines);
    },
    maxBars() {
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
        this.setData({ bars: [], ma7Points: [], ma25Points: [], maLegend: false });
        return;
      }

      const isCard = size === 'card' || size === 'wide';
      const limit = maxBars > 0 ? maxBars : 50;
      const sliced = klines.length > limit ? klines.slice(-limit) : klines.slice();

      const marginRatio = isCard ? 0.04 : 0.05;
      const range = calcPriceRange(sliced, marginRatio);
      const { max, span } = range;
      const count = sliced.length;
      const slotW = 100 / count;
      const bodyRatio = isCard ? 0.94 : 0.88;
      const gapRatio = (1 - bodyRatio) / 2;
      const minBodyPct = isCard ? 2.8 : 1.5;

      const bars = sliced.map((k, i) => {
        const isBull = k.close >= k.open;
        const bodyTopVal = Math.max(k.open, k.close);
        const bodyBotVal = Math.min(k.open, k.close);

        const highTop = ((max - k.high) / span * 100);
        const lowTop = ((max - k.low) / span * 100);
        let bodyTop = ((max - bodyTopVal) / span * 100);
        let bodyH = Math.max((bodyTopVal - bodyBotVal) / span * 100, minBodyPct);

        // 十字星：居中画短柱
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
          dirClass: isBull ? 'bull' : 'bear'
        };
      });

      const showMA = isCard && count >= 7;
      this.setData({
        bars,
        maLegend: showMA,
        ma7Points: showMA ? maToPoints(sliced, 7, range) : [],
        ma25Points: showMA && count >= 25 ? maToPoints(sliced, 25, range) : []
      });
    }
  }
});
