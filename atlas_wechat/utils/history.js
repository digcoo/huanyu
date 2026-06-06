const { MARKETS, STRATEGIES } = require('./mock');
const config = require('./config');
const stockApi = require('./stock-api');
const adapter = require('./adapter');

const STORAGE_KEY = 'watchHistory';
const MAX_RECORDS = 200;

function findMarketLabel(marketId) {
  const meta = MARKETS.find(function (m) { return m.id === marketId; });
  return meta ? meta.icon + ' ' + meta.name : marketId;
}

function findStrategyLabel(strategyId) {
  const meta = STRATEGIES.find(function (s) { return s.id === strategyId; });
  return meta ? meta.name : strategyId;
}

function pad(n) {
  return n < 10 ? '0' + n : '' + n;
}

function formatDate(ts) {
  if (!ts) return '--';
  const d = new Date(ts);
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate());
}

function calcHoldDays(addedAt, removedAt) {
  if (!addedAt || !removedAt) return 0;
  const days = Math.floor((removedAt - addedAt) / (24 * 60 * 60 * 1000));
  return Math.max(days, 0);
}

function round2(n) {
  return +Number(n).toFixed(2);
}

function buildRecord(item, exitPrice, removeReason) {
  const entryPrice = item.entryPrice != null ? item.entryPrice : item.price;
  const exit = exitPrice != null ? exitPrice : item.price;
  const removedAt = Date.now();
  const addedAt = item.addedAt || removedAt;
  const pnlPct = entryPrice
    ? ((exit - entryPrice) / Math.abs(entryPrice)) * 100
    : 0;

  return {
    recordId: 'wh_' + removedAt + '_' + (item.id || 'unknown'),
    id: item.id,
    code: item.code,
    name: item.name,
    market: item.market,
    strategy: item.strategy,
    resonance: item.resonance,
    tags: item.tags || [],
    addedAt: addedAt,
    removedAt: removedAt,
    holdDays: calcHoldDays(addedAt, removedAt),
    entryPrice: round2(entryPrice),
    exitPrice: round2(exit),
    pnlPct: round2(pnlPct),
    pnlAmount: round2(exit - entryPrice),
    removeReason: removeReason || 'manual'
  };
}

function enrichRecord(record) {
  const pnlPct = record.pnlPct || 0;
  return Object.assign({}, record, {
    marketLabel: findMarketLabel(record.market),
    strategyLabel: findStrategyLabel(record.strategy),
    addedDate: formatDate(record.addedAt),
    removedDate: formatDate(record.removedAt),
    periodLabel: formatDate(record.addedAt) + ' → ' + formatDate(record.removedAt),
    holdLabel: '持有' + (record.holdDays || 0) + '天',
    markerLabel: formatDate(record.addedAt) + ' 加入 · ' + formatDate(record.removedAt) + ' 移出',
    pnlText: (pnlPct >= 0 ? '+' : '') + pnlPct + '%',
    pnlClass: pnlPct > 0 ? 'up' : pnlPct < 0 ? 'down' : 'flat',
    priceRange: record.entryPrice + ' → ' + record.exitPrice
  });
}

function attachChartKlines(records, period) {
  const { findStockById } = require('./search');
  return records.map(function (record) {
    const stock = findStockById(record.id);
    const chartKlines = stock && stock.klines && stock.klines[period]
      ? stock.klines[period].slice()
      : [];
    return Object.assign({}, record, {
      chartKlines: chartKlines,
      resonance: record.resonance || (stock && stock.resonance) || 'medium'
    });
  });
}

function attachChartKlinesAsync(records, period) {
  if (config.useMock) {
    return Promise.resolve(attachChartKlines(records, period));
  }
  return Promise.all((records || []).map(function (record) {
    var code = adapter.extractCode(record.id || record.code);
    return stockApi.fetchKlines(code, period).then(function (bars) {
      return Object.assign({}, record, {
        chartKlines: adapter.barsToKlines(bars),
        resonance: record.resonance || 'medium'
      });
    }).catch(function () {
      return Object.assign({}, record, { chartKlines: [], resonance: record.resonance || 'medium' });
    });
  }));
}

function loadHistory() {
  try {
    const stored = wx.getStorageSync(STORAGE_KEY);
    if (!Array.isArray(stored)) return [];
    return stored;
  } catch (e) {
    return [];
  }
}

function saveHistory(list) {
  try {
    wx.setStorageSync(STORAGE_KEY, list);
    return true;
  } catch (e) {
    console.error('[history] storage save failed', e);
    return false;
  }
}

function archiveRecord(item, removeReason) {
  if (!item || !item.id) return null;

  const { findStockById } = require('./search');
  const latest = findStockById(item.id);
  const exitPrice = latest && latest.price != null ? latest.price : item.price;
  const record = buildRecord(item, exitPrice, removeReason);
  let list = loadHistory();
  list.unshift(record);
  if (list.length > MAX_RECORDS) {
    list = list.slice(0, MAX_RECORDS);
  }
  saveHistory(list);
  return record;
}

function archiveRecordAsync(item, removeReason) {
  if (!item || !item.id) return Promise.resolve(null);

  function saveWithExit(exitPrice) {
    const record = buildRecord(item, exitPrice, removeReason);
    let list = loadHistory();
    list.unshift(record);
    if (list.length > MAX_RECORDS) {
      list = list.slice(0, MAX_RECORDS);
    }
    saveHistory(list);
    return record;
  }

  if (config.useMock) {
    return Promise.resolve(archiveRecord(item, removeReason));
  }

  var code = adapter.extractCode(item.id || item.code);
  return stockApi.fetchSummary(code).then(function (summary) {
    var exitPrice = summary && summary.price != null ? summary.price : item.price;
    return saveWithExit(exitPrice);
  }).catch(function () {
    return saveWithExit(item.price);
  });
}

function computeSummary(records) {
  if (!records.length) {
    return {
      total: 0,
      winRate: 0,
      winRateText: '0%',
      avgHoldDays: 0,
      avgHoldText: '0天',
      avgPnlPct: 0,
      avgPnlText: '0%'
    };
  }

  let wins = 0;
  let holdSum = 0;
  let pnlSum = 0;

  records.forEach(function (r) {
    if (r.pnlPct > 0) wins += 1;
    holdSum += r.holdDays || 0;
    pnlSum += r.pnlPct || 0;
  });

  const avgPnl = pnlSum / records.length;
  const avgHold = Math.round(holdSum / records.length);

  return {
    total: records.length,
    winRate: Math.round((wins / records.length) * 100),
    winRateText: Math.round((wins / records.length) * 100) + '%',
    avgHoldDays: avgHold,
    avgHoldText: avgHold + '天',
    avgPnlPct: round2(avgPnl),
    avgPnlText: (avgPnl >= 0 ? '+' : '') + round2(avgPnl) + '%'
  };
}

function filterRecords(records, filter) {
  if (filter === 'win') {
    return records.filter(function (r) { return r.pnlPct > 0; });
  }
  if (filter === 'loss') {
    return records.filter(function (r) { return r.pnlPct <= 0; });
  }
  return records;
}

module.exports = {
  loadHistory,
  saveHistory,
  archiveRecord,
  archiveRecordAsync,
  enrichRecord,
  attachChartKlines,
  attachChartKlinesAsync,
  computeSummary,
  filterRecords
};
