const config = require('./config');
const stockApi = require('./stock-api');
const adapter = require('./adapter');
const listMemory = require('./list-memory');

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

function rehydrateItemAsync(item, period) {
  period = period || 'week';
  if (!item || !item.id) return Promise.resolve(item);
  if (config.useMock) {
    return Promise.resolve(item);
  }
  if (item.chartKlines && item.chartKlines.length) {
    return Promise.resolve(item);
  }
  return stockApi.fetchKlines(item.code, period, listMemory.klineLimitForList(period)).then(function (bars) {
    return Object.assign({}, item, {
      chartKlines: adapter.barsToKlines(bars)
    });
  }).catch(function () {
    return Object.assign({}, item, { chartKlines: [] });
  });
}

function rehydrateListAsync(list, period) {
  return Promise.all((list || []).map(function (item) {
    return rehydrateItemAsync(item, period);
  }));
}

function loadWatchlistLocal() {
  try {
    const stored = wx.getStorageSync('watchlist');
    if (!Array.isArray(stored)) return [];
    return stored;
  } catch (e) {
    return [];
  }
}

function saveWatchlistLocal(list) {
  try {
    wx.setStorageSync('watchlist', (list || []).map(toStoredItem));
    return true;
  } catch (e) {
    console.error('[watchlist] storage save failed', e);
    return false;
  }
}

function loadWatchlist() {
  return loadWatchlistLocal();
}

function saveWatchlist(list) {
  return saveWatchlistLocal(list);
}

module.exports = {
  toStoredItem: toStoredItem,
  rehydrateItemAsync: rehydrateItemAsync,
  rehydrateListAsync: rehydrateListAsync,
  loadWatchlist: loadWatchlist,
  saveWatchlist: saveWatchlist,
  loadWatchlistLocal: loadWatchlistLocal,
  saveWatchlistLocal: saveWatchlistLocal
};
