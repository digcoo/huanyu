/**
 * 币安 Pro 风格 K 线绘制引擎
 * A 股深色主题：实心蜡烛、红涨绿跌、水平细网格、价格区留白
 */

const BINANCE = {
  bg: '#1e2026',
  bgDeep: '#161a1e',
  grid: '#2b2f36',
  gridLight: 'rgba(43, 47, 54, 0.55)',
  bullish: '#f6465d',
  bearish: '#0ecb81',
  ma7: '#f0b90b',
  ma25: '#e040fb',
  text: '#848e9c',
  border: '#2b2f36'
};

/**
 * 计算带留白的价格区间（币安图表上下约 6% 留白）
 */
function calcPriceRange(klines, marginRatio) {
  marginRatio = marginRatio == null ? 0.06 : marginRatio;
  let min = Infinity;
  let max = -Infinity;
  klines.forEach(k => {
    min = Math.min(min, k.low);
    max = Math.max(max, k.high);
  });
  const span = max - min || Math.abs(max) * 0.02 || 1;
  const margin = span * marginRatio;
  return { min: min - margin, max: max + margin, span: span + margin * 2 };
}

function priceToY(price, range, chartTop, chartH) {
  return chartTop + chartH - ((price - range.min) / range.span) * chartH;
}

/** 简单 MA 均线 */
function calcMA(klines, period) {
  return klines.map((_, i) => {
    if (i < period - 1) return null;
    let sum = 0;
    for (let j = i - period + 1; j <= i; j++) sum += klines[j].close;
    return sum / period;
  });
}

/**
 * 绘制币安风格 K 线
 */
function drawBinanceKlines(ctx, klines, width, height, options) {
  options = options || {};
  const padding = options.padding || { top: 6, right: 4, bottom: 6, left: 4 };
  const showGrid = options.showGrid !== false;
  const showMA = options.showMA || false;
  const bodyRatio = options.bodyRatio || 0.68;

  if (!klines || !klines.length) return;

  ctx.clearRect(0, 0, width, height);

  // 背景
  ctx.fillStyle = BINANCE.bg;
  ctx.fillRect(0, 0, width, height);

  const chartW = width - padding.left - padding.right;
  const chartH = height - padding.top - padding.bottom;
  const range = calcPriceRange(klines);
  const count = klines.length;
  const slotW = chartW / count;

  // 水平网格（仅横线，币安特征）
  if (showGrid) {
    const gridLines = 4;
    ctx.strokeStyle = BINANCE.gridLight;
    ctx.lineWidth = 1;
    for (let i = 0; i <= gridLines; i++) {
      const y = padding.top + (chartH / gridLines) * i;
      ctx.beginPath();
      ctx.moveTo(padding.left, y + 0.5);
      ctx.lineTo(width - padding.right, y + 0.5);
      ctx.stroke();
    }
  }

  // 蜡烛
  klines.forEach((k, i) => {
    const isBull = k.close >= k.open;
    const color = isBull ? BINANCE.bullish : BINANCE.bearish;
    const bodyW = Math.max(1, slotW * bodyRatio);
    const x = padding.left + i * slotW + (slotW - bodyW) / 2;
    const cx = x + bodyW / 2;

    const highY = priceToY(k.high, range, padding.top, chartH);
    const lowY = priceToY(k.low, range, padding.top, chartH);
    const openY = priceToY(k.open, range, padding.top, chartH);
    const closeY = priceToY(k.close, range, padding.top, chartH);

    const bodyTop = Math.min(openY, closeY);
    let bodyH = Math.abs(closeY - openY);
    // 十字星：最小 1px 实体
    if (bodyH < 1) bodyH = 1;

    // 影线（细线，与实体同色）
    ctx.strokeStyle = color;
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(cx, highY);
    ctx.lineTo(cx, lowY);
    ctx.stroke();

    // 实心实体（币安风格，无空心）
    ctx.fillStyle = color;
    ctx.fillRect(x, bodyTop, bodyW, bodyH);
  });

  // MA 均线（宽图可选）
  if (showMA && count >= 7) {
    const ma7 = calcMA(klines, 7);
    ctx.strokeStyle = BINANCE.ma7;
    ctx.lineWidth = 1;
    ctx.beginPath();
    let started = false;
    ma7.forEach((v, i) => {
      if (v == null) return;
      const x = padding.left + i * slotW + slotW / 2;
      const y = priceToY(v, range, padding.top, chartH);
      if (!started) { ctx.moveTo(x, y); started = true; }
      else ctx.lineTo(x, y);
    });
    ctx.stroke();

    if (count >= 25) {
      const ma25 = calcMA(klines, 25);
      ctx.strokeStyle = BINANCE.ma25;
      ctx.beginPath();
      started = false;
      ma25.forEach((v, i) => {
        if (v == null) return;
        const x = padding.left + i * slotW + slotW / 2;
        const y = priceToY(v, range, padding.top, chartH);
        if (!started) { ctx.moveTo(x, y); started = true; }
        else ctx.lineTo(x, y);
      });
      ctx.stroke();
    }
  }
}

/**
 * 币安风格多线对比（大盘指数）
 */
function drawBinanceMultiLine(ctx, series, width, height, colors) {
  if (!series || !series.length) return;

  ctx.clearRect(0, 0, width, height);
  ctx.fillStyle = BINANCE.bg;
  ctx.fillRect(0, 0, width, height);

  const padding = { top: 12, right: 8, bottom: 14, left: 8 };
  const chartW = width - padding.left - padding.right;
  const chartH = height - padding.top - padding.bottom;

  let allCloses = [];
  series.forEach(s => s.klines.forEach(k => allCloses.push(k.close)));
  const min = Math.min.apply(null, allCloses);
  const max = Math.max.apply(null, allCloses);
  const span = max - min || 1;
  const margin = span * 0.08;
  const range = { min: min - margin, max: max + margin, span: span + margin * 2 };

  const count = series[0].klines.length;

  // 网格
  ctx.strokeStyle = BINANCE.gridLight;
  ctx.lineWidth = 1;
  for (let i = 0; i <= 3; i++) {
    const y = padding.top + (chartH / 3) * i;
    ctx.beginPath();
    ctx.moveTo(padding.left, y + 0.5);
    ctx.lineTo(width - padding.right, y + 0.5);
    ctx.stroke();
  }

  const defaultColors = [BINANCE.bullish, BINANCE.ma7, '#848e9c'];
  series.forEach((s, si) => {
    const color = (colors && colors[si]) || defaultColors[si] || BINANCE.text;
    ctx.strokeStyle = color;
    ctx.lineWidth = 1.5;
    ctx.lineJoin = 'round';
    ctx.beginPath();
    s.klines.forEach((k, i) => {
      const x = padding.left + (i / (count - 1)) * chartW;
      const y = priceToY(k.close, range, padding.top, chartH);
      if (i === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    });
    ctx.stroke();
  });
}

/** 兼容旧接口 */
function drawKlines(ctx, klines, width, height, options) {
  drawBinanceKlines(ctx, klines, width, height, options);
}

function drawMultiLineChart(ctx, series, width, height, colors) {
  drawBinanceMultiLine(ctx, series, width, height, colors);
}

module.exports = {
  BINANCE,
  BINANCE_COLORS: BINANCE,
  COLORS: BINANCE,
  PERIOD_MAX_BARS: {
    year: 50,
    month: 50,
    week: 50,
    day: 50
  },
  calcPriceRange,
  drawBinanceKlines,
  drawKlines,
  drawMultiLineChart,
  drawBinanceMultiLine
};
