const { buildStrategyRecommendations } = require('./mock');
const config = require('./config');
const stockApi = require('./stock-api');
const adapter = require('./adapter');

function findBaseItem(id) {
  const all = buildStrategyRecommendations();
  for (const strategyId in all) {
    const byMarket = all[strategyId];
    for (const market in byMarket) {
      const found = byMarket[market].find(function (item) { return item.id === id; });
      if (found) return found;
    }
  }
  return null;
}

function toStoredItem(item) {
  return {
    id: item.id,
    strategy: item.strategy,
    market: item.market,
    code: item.code,
    name: item.name,
    price: item.price,
    entryPrice: item.entryPrice != null ? item.entryPrice : item.price,
    changePct: item.changePct,
    tags: item.tags,
    summary: item.summary,
    resonance: item.resonance,
    addedAt: item.addedAt
  };
}

function rehydrateItem(item) {
  if (!item || !item.id) return item;
  if (item.klines) return item;
  const base = findBaseItem(item.id);
  if (!base) return item;
  return Object.assign({}, base, item, { klines: base.klines });
}

function rehydrateItemAsync(item, period) {
  period = period || 'week';
  if (!item || !item.id) return Promise.resolve(item);
  if (config.useMock) {
    return Promise.resolve(rehydrateItem(item));
  }
  if (item.klines && item.klines[period]) {
    return Promise.resolve(item);
  }
  return stockApi.fetchKlines(item.code, period).then(function (bars) {
    var klines = adapter.barsToKlines(bars);
    var merged = Object.assign({}, item, {
      klines: Object.assign({}, item.klines || {})
    });
    merged.klines[period] = klines;
    return merged;
  }).catch(function () {
    return rehydrateItem(item);
  });
}

function rehydrateListAsync(list, period) {
  return Promise.all((list || []).map(function (item) {
    return rehydrateItemAsync(item, period);
  }));
}

function loadWatchlist() {
  try {
    const stored = wx.getStorageSync('watchlist');
    if (!Array.isArray(stored)) return [];
    return stored.map(rehydrateItem);
  } catch (e) {
    return [];
  }
}

function saveWatchlist(list) {
  try {
    wx.setStorageSync('watchlist', list.map(toStoredItem));
    return true;
  } catch (e) {
    console.error('[watchlist] storage save failed', e);
    return false;
  }
}

module.exports = {
  findBaseItem: findBaseItem,
  toStoredItem: toStoredItem,
  rehydrateItem: rehydrateItem,
  rehydrateItemAsync: rehydrateItemAsync,
  rehydrateListAsync: rehydrateListAsync,
  loadWatchlist: loadWatchlist,
  saveWatchlist: saveWatchlist
};
