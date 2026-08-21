/** 列表页内存控制：K 线条数、持有上限 */

/** 列表内存持有上限（约 4 页 × 12 条） */
var MAX_HELD_RECOMMENDATIONS = 48;
/** 分页去重 id 记录上限 */
var MAX_SEEN_RECOMMENDATION_IDS = 400;

/**
 * 列表拉取 = 可见根数 + MA 预热。
 * 可见 64 根时需再多 59 根才能让 MA60 铺满整段可见区。
 */
var LIST_VISIBLE_BARS = 64;
var LIST_MA_WARMUP = 59;
var LIST_KLINE_LIMIT = {
  min30: LIST_VISIBLE_BARS + LIST_MA_WARMUP,
  min60: LIST_VISIBLE_BARS + LIST_MA_WARMUP,
  day: LIST_VISIBLE_BARS + LIST_MA_WARMUP,
  week: LIST_VISIBLE_BARS + LIST_MA_WARMUP,
  month: LIST_VISIBLE_BARS + LIST_MA_WARMUP,
  year: LIST_VISIBLE_BARS + LIST_MA_WARMUP
};

function klineLimitForList(period) {
  return LIST_KLINE_LIMIT[period] || (LIST_VISIBLE_BARS + LIST_MA_WARMUP);
}

function cardMaxBars(period) {
  return LIST_VISIBLE_BARS;
}

/** 只保留当前周期 chartKlines，丢弃 klines 多周期 map 与标记缓存 */
function slimItemKlines(item, period) {
  if (!item) return item;
  var bars = (item.chartKlines && item.chartKlines.length)
    ? item.chartKlines
    : (item.klines && item.klines[period] ? item.klines[period] : []);
  var slim = Object.assign({}, item, { chartKlines: bars });
  delete slim.klines;
  delete slim.barMarkers;
  delete slim.priceLines;
  delete slim.markerEpoch;
  return slim;
}

function slimListKlines(list, period) {
  return (list || []).map(function (item) {
    return slimItemKlines(item, period);
  });
}

/** 列表过长时保留最新一段（加载更多在尾部追加） */
function trimHeldList(list, max) {
  max = max != null ? max : MAX_HELD_RECOMMENDATIONS;
  if (!list || list.length <= max) return list || [];
  return list.slice(list.length - max);
}

/** 分页去重：记录已加载过的推荐 id（轻量，可大于列表持有上限） */
function rememberSeenIds(seen, items) {
  var map = seen || {};
  (items || []).forEach(function (item) {
    if (item && item.id) map[item.id] = true;
  });
  var keys = Object.keys(map);
  if (keys.length <= MAX_SEEN_RECOMMENDATION_IDS) {
    return map;
  }
  var trimmed = {};
  keys.slice(keys.length - MAX_SEEN_RECOMMENDATION_IDS).forEach(function (id) {
    trimmed[id] = true;
  });
  return trimmed;
}

function initSeenIds(items) {
  return rememberSeenIds({}, items);
}

module.exports = {
  MAX_HELD_RECOMMENDATIONS: MAX_HELD_RECOMMENDATIONS,
  MAX_SEEN_RECOMMENDATION_IDS: MAX_SEEN_RECOMMENDATION_IDS,
  klineLimitForList: klineLimitForList,
  cardMaxBars: cardMaxBars,
  slimItemKlines: slimItemKlines,
  slimListKlines: slimListKlines,
  trimHeldList: trimHeldList,
  rememberSeenIds: rememberSeenIds,
  initSeenIds: initSeenIds
};
