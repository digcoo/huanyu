const API_BASE = import.meta.env.VITE_API_BASE || '/tts';

async function request(path, options = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options
  });
  const json = await res.json();
  if (json.statusCode !== '200') {
    throw new Error(json.errorMessage || '请求失败');
  }
  return json.data;
}

export function fetchHealth() {
  return request('/backtest/health');
}

export function fetchStrategies() {
  return request('/backtest/strategies');
}

export function runBacktest(body) {
  return request('/backtest/run', {
    method: 'POST',
    body: JSON.stringify(body)
  });
}
