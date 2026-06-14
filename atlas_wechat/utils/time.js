function pad(n) {
  return n < 10 ? '0' + n : '' + n;
}

/** 统一毫秒时间戳（兼容秒级） */
function normalizeTimestamp(ts) {
  if (ts == null || ts === '') return null;
  var n = Number(ts);
  if (!n || isNaN(n)) return null;
  if (n < 1e12) n *= 1000;
  return n;
}

function startOfDay(d) {
  var copy = new Date(d.getTime());
  copy.setHours(0, 0, 0, 0);
  return copy;
}

function addDays(d, n) {
  var copy = new Date(d.getTime());
  copy.setDate(copy.getDate() + n);
  return startOfDay(copy);
}

function dayKey(d) {
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate());
}

function parseDay(raw) {
  if (!raw) return null;
  var s = String(raw).trim();
  var datePart = s.indexOf(' ') >= 0 ? s.split(' ')[0] : s;
  var timePart = s.indexOf(' ') >= 0 ? s.split(' ')[1] : '';
  var parts = datePart.split('-');
  if (parts.length < 3) return null;
  var d = new Date(parseInt(parts[0], 10), parseInt(parts[1], 10) - 1, parseInt(parts[2], 10));
  if (isNaN(d.getTime())) return null;
  return { date: d, timePart: timePart ? timePart.slice(0, 5) : '' };
}

function formatDataUpdatedLabel(raw) {
  var parsed = parseDay(raw);
  if (!parsed) return raw ? String(raw) : '';

  var d = parsed.date;
  var now = new Date();
  var timeSuffix = parsed.timePart ? ' ' + parsed.timePart : '';
  var md = pad(d.getMonth() + 1) + '-' + pad(d.getDate());

  if (d.toDateString() === now.toDateString()) {
    return '今天' + timeSuffix;
  }

  var yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (d.toDateString() === yesterday.toDateString()) {
    return '昨天' + timeSuffix;
  }

  if (d.getFullYear() === now.getFullYear()) {
    return md + timeSuffix;
  }

  return d.getFullYear() + '-' + md + timeSuffix;
}

function todayDayStr() {
  var now = new Date();
  return now.getFullYear() + '-' + pad(now.getMonth() + 1) + '-' + pad(now.getDate());
}

module.exports = {
  normalizeTimestamp: normalizeTimestamp,
  formatDataUpdatedLabel: formatDataUpdatedLabel,
  todayDayStr: todayDayStr,
  parseDay: parseDay,
  startOfDay: startOfDay,
  addDays: addDays,
  dayKey: dayKey
};
