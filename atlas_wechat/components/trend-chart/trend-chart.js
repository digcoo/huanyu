Component({
  properties: {
    series: {
      type: Array,
      value: []
    }
  },

  data: {
    charts: []
  },

  observers: {
    series: function (s) {
      this.buildCharts(s);
    }
  },

  lifetimes: {
    attached: function () {
      this.buildCharts(this.properties.series);
    },
    ready: function () {
      this.drawAllCharts();
    }
  },

  methods: {
    buildCharts: function (series) {
      if (!series || !series.length) {
        this.setData({ charts: [] });
        return;
      }

      var charts = series.map(function (s) {
        return buildSingleChart(s);
      });

      var self = this;
      this.setData({ charts: charts }, function () {
        wx.nextTick(function () {
          self.drawAllCharts();
        });
      });
    },

    drawAllCharts: function () {
      var charts = this.data.charts;
      for (var i = 0; i < charts.length; i++) {
        this.drawChart(i);
      }
    },

    drawChart: function (index) {
      var chart = this.data.charts[index];
      if (!chart || !chart.points.length) return;

      var query = this.createSelectorQuery().in(this);
      query.select('#tc-' + index).fields({ node: true, size: true }).exec(function (res) {
        if (!res || !res[0] || !res[0].node) return;

        var canvas = res[0].node;
        var ctx = canvas.getContext('2d');
        var width = res[0].width;
        var height = res[0].height;
        if (!width || !height) return;

        var sys = wx.getSystemInfoSync();
        var dpr = sys.pixelRatio || 2;
        canvas.width = width * dpr;
        canvas.height = height * dpr;
        ctx.scale(dpr, dpr);
        ctx.clearRect(0, 0, width, height);

        var padX = 6;
        var padY = 6;
        var plotW = width - padX * 2;
        var plotH = height - padY * 2;

        ctx.strokeStyle = 'rgba(43, 47, 54, 0.75)';
        ctx.lineWidth = 1;
        for (var g = 0; g <= 2; g++) {
          var gy = padY + (plotH * g) / 2;
          ctx.beginPath();
          ctx.moveTo(padX, gy);
          ctx.lineTo(padX + plotW, gy);
          ctx.stroke();
        }

        function toXY(p) {
          return {
            x: padX + (p.x / 100) * plotW,
            y: padY + (p.y / 100) * plotH
          };
        }

        function strokeLine(points, color, dashed) {
          if (!points || !points.length) return;
          var coords = points.map(toXY);
          ctx.beginPath();
          ctx.setLineDash(dashed ? [5, 4] : []);
          coords.forEach(function (c, i) {
            if (i === 0) ctx.moveTo(c.x, c.y);
            else ctx.lineTo(c.x, c.y);
          });
          ctx.strokeStyle = color;
          ctx.lineWidth = dashed ? 1.5 : 2;
          ctx.lineJoin = 'round';
          ctx.lineCap = 'round';
          ctx.stroke();
          ctx.setLineDash([]);
          return coords;
        }

        if (chart.industryPoints && chart.industryPoints.length) {
          strokeLine(chart.industryPoints, '#848e9c', true);
        }

        var coords = strokeLine(chart.points, chart.color, false);

        if (coords) {
          ctx.fillStyle = chart.color;
          coords.forEach(function (c, i) {
            ctx.beginPath();
            ctx.arc(c.x, c.y, i === 0 || i === coords.length - 1 ? 3 : 2, 0, Math.PI * 2);
            ctx.fill();
          });

          if (coords.length >= 2) {
            var last = coords[coords.length - 1];
            ctx.beginPath();
            ctx.arc(last.x, last.y, 4.5, 0, Math.PI * 2);
            ctx.strokeStyle = chart.color;
            ctx.lineWidth = 1.5;
            ctx.stroke();
          }
        }
      });
    }
  }
});

function buildSingleChart(chart) {
  var data = chart.data || [];
  var industryData = chart.industryData || null;

  if (!data.length) {
    return emptyChart(chart);
  }

  var values = data.map(function (d) { return d.value; });
  if (industryData && industryData.length) {
    industryData.forEach(function (d) {
      values.push(d.value);
    });
  }

  var min = Math.min.apply(null, values);
  var max = Math.max.apply(null, values);
  var span = max - min || 1;
  var margin = span * 0.14;
  var rangeMax = max + margin;
  var rangeMin = min - margin;
  var rangeSpan = rangeMax - rangeMin;
  var count = data.length;

  function toPoints(source) {
    return source.map(function (d, i) {
      var x = count > 1 ? (i / (count - 1)) * 100 : 50;
      var y = ((rangeMax - d.value) / rangeSpan) * 100;
      return {
        x: +x.toFixed(2),
        y: +y.toFixed(2),
        year: d.year,
        value: d.value
      };
    });
  }

  var points = toPoints(data);
  var industryPoints = industryData && industryData.length ? toPoints(industryData) : null;

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
  var changeLabel = sign + changePct.toFixed(1) + '%';

  var result = {
    name: chart.name,
    unit: chart.unit || '',
    color: chart.color || '#848e9c',
    points: points,
    industryPoints: industryPoints,
    hasCompare: !!industryPoints,
    firstYear: first.year,
    lastYear: last.year,
    firstValue: formatValue(first.value),
    lastValue: formatValue(last.value),
    changeLabel: changeLabel,
    trend: trend
  };

  if (industryData && industryData.length) {
    var indLast = industryData[industryData.length - 1];
    result.industryLastValue = formatValue(indLast.value);
  }

  return result;
}

function emptyChart(chart) {
  return {
    name: chart.name,
    unit: chart.unit || '',
    color: chart.color || '#848e9c',
    points: [],
    industryPoints: null,
    hasCompare: false,
    firstYear: '',
    lastYear: '',
    firstValue: '',
    lastValue: '',
    changeLabel: '--',
    trend: 'flat'
  };
}

function formatValue(val) {
  if (typeof val !== 'number') return val;
  if (Math.abs(val) >= 100) return val.toFixed(0);
  if (Math.abs(val) >= 10) return val.toFixed(1);
  return val.toFixed(2);
}
