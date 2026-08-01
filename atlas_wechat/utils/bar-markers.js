/**
 * 列表 K 线标记（当前策略无额外标记）
 */
function shouldShowBarMarkers() {
  return false;
}

function resolveBarMarkersForItem() {
  return [];
}

function enrichItemsWithBarMarkers(items) {
  return Promise.resolve(items || []);
}

module.exports = {
  shouldShowBarMarkers: shouldShowBarMarkers,
  resolveBarMarkersForItem: resolveBarMarkersForItem,
  enrichItemsWithBarMarkers: enrichItemsWithBarMarkers
};
