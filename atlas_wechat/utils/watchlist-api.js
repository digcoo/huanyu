const api = require('./api');
const config = require('./config');

function isRemoteEnabled() {
  return !config.useMock && config.syncRemote !== false;
}

function fetchWatchlist() {
  return api.get('/watchlist').then(function (res) {
    if (!res.ok || !Array.isArray(res.data)) return [];
    return res.data;
  });
}

function addWatchlist(item) {
  return api.post('/watchlist', item).then(function (res) {
    if (!res.ok || !res.data) return { added: false };
    return res.data;
  });
}

function removeWatchlist(stockId, reason) {
  var path = '/watchlist/' + encodeURIComponent(stockId);
  if (reason) path += '?reason=' + encodeURIComponent(reason);
  return api.del(path).then(function (res) {
    if (!res.ok || !res.data) return { removed: false };
    return res.data;
  });
}

function fetchHistory(filter) {
  return api.get('/history', { filter: filter || 'all' }).then(function (res) {
    if (!res.ok || !res.data) return { items: [], summary: null };
    return {
      items: res.data.items || [],
      summary: res.data.summary || null
    };
  });
}

module.exports = {
  isRemoteEnabled: isRemoteEnabled,
  fetchWatchlist: fetchWatchlist,
  addWatchlist: addWatchlist,
  removeWatchlist: removeWatchlist,
  fetchHistory: fetchHistory
};
