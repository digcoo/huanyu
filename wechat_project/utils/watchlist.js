const { buildStrategyRecommendations } = require('./mock');

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
  findBaseItem,
  toStoredItem,
  rehydrateItem,
  loadWatchlist,
  saveWatchlist
};
