/** 从交易列表派生专业回测指标 */

export function sortTradesAsc(trades) {
  return [...(trades || [])].sort((a, b) => String(a.signalDay).localeCompare(String(b.signalDay)));
}

export function buildEquityCurve(trades) {
  const sorted = sortTradesAsc(trades);
  let cum = 0;
  return sorted.map((t) => {
    cum += t.pnlPct || 0;
    return { day: t.signalDay, pnl: t.pnlPct || 0, equity: Math.round(cum * 100) / 100 };
  });
}

export function calcMaxDrawdown(curve) {
  if (!curve.length) return { maxDrawdown: 0, peak: 0, trough: 0 };
  let peak = curve[0].equity;
  let maxDd = 0;
  let peakVal = peak;
  let troughVal = peak;
  curve.forEach((p) => {
    if (p.equity > peak) peak = p.equity;
    const dd = peak - p.equity;
    if (dd > maxDd) {
      maxDd = dd;
      peakVal = peak;
      troughVal = p.equity;
    }
  });
  return { maxDrawdown: Math.round(maxDd * 100) / 100, peak: peakVal, trough: troughVal };
}

export function calcProfitFactor(trades) {
  let grossWin = 0;
  let grossLoss = 0;
  (trades || []).forEach((t) => {
    const p = t.pnlPct || 0;
    if (p > 0) grossWin += p;
    else grossLoss += Math.abs(p);
  });
  if (grossLoss === 0) return grossWin > 0 ? Infinity : 0;
  return Math.round((grossWin / grossLoss) * 100) / 100;
}

export function calcExpectancy(trades) {
  const n = trades?.length || 0;
  if (!n) return 0;
  const sum = trades.reduce((s, t) => s + (t.pnlPct || 0), 0);
  return Math.round((sum / n) * 100) / 100;
}

export function groupByMonth(trades) {
  const map = new Map();
  (trades || []).forEach((t) => {
    const month = String(t.signalDay || '').slice(0, 7);
    if (!month) return;
    if (!map.has(month)) map.set(month, { month, count: 0, win: 0, pnlSum: 0 });
    const row = map.get(month);
    row.count += 1;
    if (t.win) row.win += 1;
    row.pnlSum += t.pnlPct || 0;
  });
  return [...map.values()]
    .sort((a, b) => a.month.localeCompare(b.month))
    .map((r) => ({
      ...r,
      winRate: r.count ? Math.round((r.win / r.count) * 1000) / 10 : 0,
      avgPnl: r.count ? Math.round((r.pnlSum / r.count) * 100) / 100 : 0
    }));
}

export function buildPnlHistogram(trades, step = 2) {
  const buckets = new Map();
  (trades || []).forEach((t) => {
    const p = t.pnlPct || 0;
    const key = Math.floor(p / step) * step;
    const label = `${key >= 0 ? '+' : ''}${key}%`;
    buckets.set(key, (buckets.get(key) || 0) + 1);
  });
  return [...buckets.entries()]
    .sort((a, b) => a[0] - b[0])
    .map(([k, count]) => ({ label: `${k >= 0 ? '+' : ''}${k}%`, count, key: k }));
}

export function tierBreakdown(trades) {
  const map = new Map();
  (trades || []).forEach((t) => {
    const tier = t.tier || '—';
    if (!map.has(tier)) map.set(tier, { tier, count: 0, win: 0, pnlSum: 0 });
    const row = map.get(tier);
    row.count += 1;
    if (t.win) row.win += 1;
    row.pnlSum += t.pnlPct || 0;
  });
  return [...map.values()].map((r) => ({
    ...r,
    winRate: r.count ? Math.round((r.win / r.count) * 1000) / 10 : 0,
    avgPnl: r.count ? Math.round((r.pnlSum / r.count) * 100) / 100 : 0
  }));
}

export function buildAnalytics(result) {
  const trades = result?.trades || [];
  const curve = buildEquityCurve(trades);
  const dd = calcMaxDrawdown(curve);
  return {
    curve,
    maxDrawdown: dd.maxDrawdown,
    profitFactor: calcProfitFactor(trades),
    expectancy: calcExpectancy(trades),
    monthly: groupByMonth(trades),
    histogram: buildPnlHistogram(trades),
    tiers: tierBreakdown(trades)
  };
}

export function exportTradesCsv(trades) {
  const headers = ['code', 'name', 'signalDay', 'exitDay', 'entryPrice', 'exitPrice', 'pnlPct', 'win', 'tier'];
  const lines = [headers.join(',')];
  (trades || []).forEach((t) => {
    lines.push([
      t.code,
      `"${(t.name || '').replace(/"/g, '""')}"`,
      t.signalDay,
      t.exitDay,
      t.entryPrice,
      t.exitPrice,
      t.pnlPct,
      t.win ? 1 : 0,
      t.tier || ''
    ].join(','));
  });
  return lines.join('\n');
}
