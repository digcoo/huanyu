import './style.css';

const API_BASE = import.meta.env.VITE_API_BASE || '/tts';

const state = {
  strategies: [],
  cacheReady: false,
  running: false,
  result: null,
  error: null
};

async function api(path, options) {
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

function el(tag, attrs, children) {
  const node = document.createElement(tag);
  if (attrs) {
    Object.entries(attrs).forEach(([k, v]) => {
      if (k === 'className') node.className = v;
      else if (k === 'text') node.textContent = v;
      else if (k.startsWith('on')) node.addEventListener(k.slice(2).toLowerCase(), v);
      else node.setAttribute(k, v);
    });
  }
  (children || []).forEach((c) => {
    if (typeof c === 'string') node.appendChild(document.createTextNode(c));
    else if (c) node.appendChild(c);
  });
  return node;
}

function renderKpi(label, value, sub, valueClass) {
  return el('div', { className: 'kpi' }, [
    el('div', { className: 'kpi-label', text: label }),
    el('div', { className: `kpi-value ${valueClass || ''}`, text: value }),
    sub ? el('div', { className: 'kpi-sub', text: sub }) : null
  ]);
}

function renderResults() {
  const { result, running, error } = state;
  if (running) {
    return el('div', { className: 'loading' }, [
      el('div', { className: 'spinner' }),
      el('span', { text: '正在回测，请稍候…（全市场扫描可能需要 1～3 分钟）' })
    ]);
  }
  if (error) {
    return el('div', { className: 'error-box', text: error });
  }
  if (!result) {
    return el('div', { className: 'empty' }, [
      el('div', { className: 'empty-icon', text: '📊' }),
      el('p', { text: '配置左侧参数后点击「开始回测」' }),
      el('p', { text: '系统将逐日模拟策略信号，并按持有期计算盈亏与胜率' })
    ]);
  }

  const s = result.summary;
  const winClass = (s.winRate || 0) >= 0.5 ? 'win' : 'loss';

  const kpis = el('div', { className: 'kpi-grid' }, [
    renderKpi('胜率', s.winRateText || '0%', `${s.winCount}/${s.signalCount} 笔盈利`, winClass),
    renderKpi('信号数', String(s.signalCount || 0), `扫描 ${s.stockCount} 只股票`),
    renderKpi('平均收益', s.avgPnlText || '0%', `持有 ${s.holdDays} 个交易日`),
    renderKpi('极值', `${s.maxWinPct > 0 ? '+' : ''}${s.maxWinPct}%`, `最大亏损 ${s.maxLossPct}%`)
  ]);

  const meta = el('div', { className: 'meta-row' }, [
    el('span', { text: `策略：${s.strategyName} (${s.strategy})` }),
    el('span', { text: `窗口：${s.scanDays} 日` }),
    el('span', { text: `耗时：${((s.elapsedMs || 0) / 1000).toFixed(1)}s` })
  ]);

  const trades = result.trades || [];
  if (trades.length === 0) {
    return el('div', {}, [kpis, meta, el('div', { className: 'empty', text: '该参数下未产生任何信号' })]);
  }

  const tbody = el('tbody');
  trades.forEach((t) => {
    const pnlClass = t.win ? 'pnl-win' : 'pnl-loss';
    const sign = t.pnlPct > 0 ? '+' : '';
    tbody.appendChild(
      el('tr', {}, [
        el('td', { text: t.code }),
        el('td', { text: t.name || '-' }),
        el('td', { text: t.signalDay }),
        el('td', { text: t.exitDay }),
        el('td', { text: t.tier || '-' }),
        el('td', { className: pnlClass, text: `${sign}${t.pnlPct}%` }),
        el('td', {}, [
          el('span', { className: t.win ? 'badge badge-win' : 'badge badge-loss', text: t.win ? '盈' : '亏' })
        ])
      ])
    );
  });

  const table = el('div', { className: 'table-wrap' }, [
    el('table', {}, [
      el('thead', {}, [
        el('tr', {}, ['代码', '名称', '信号日', '平仓日', '档位', '收益率', '结果'].map((h) => el('th', { text: h })))
      ]),
      tbody
    ])
  ]);

  return el('div', {}, [kpis, meta, table]);
}

function renderForm() {
  const form = el('form', {
    onSubmit: async (e) => {
      e.preventDefault();
      if (state.running) return;

      state.running = true;
      state.error = null;
      state.result = null;
      render();

      const fd = new FormData(form);
      const body = {
        strategy: fd.get('strategy'),
        days: Number(fd.get('days')),
        holdDays: Number(fd.get('holdDays')),
        maxStocks: Number(fd.get('maxStocks')),
        winThreshold: Number(fd.get('winThreshold')),
        codes: String(fd.get('codes') || '').trim() || undefined
      };

      try {
        state.result = await api('/backtest/run', {
          method: 'POST',
          body: JSON.stringify(body)
        });
      } catch (err) {
        state.error = err.message || '回测失败';
      } finally {
        state.running = false;
        render();
      }
    }
  });

  const strategySelect = el('select', { name: 'strategy' });
  (state.strategies.length ? state.strategies : [{ code: 'gc2', name: '金叉二次突破' }]).forEach((s) => {
    strategySelect.appendChild(el('option', { value: s.code, text: `${s.name} (${s.code})` }));
  });

  form.appendChild(
    el('div', { className: 'field' }, [
      el('label', { text: '策略' }),
      strategySelect
    ])
  );

  [
    { name: 'days', label: '回测窗口（日 K 根数）', value: '365', hint: '30 ~ 730' },
    { name: 'holdDays', label: '持有期（交易日）', value: '10', hint: '信号日收盘价买入，N 日后收盘卖出' },
    { name: 'maxStocks', label: '扫描股票上限', value: '200', hint: '留空代码列表时生效' },
    { name: 'winThreshold', label: '盈利阈值 (%)', value: '0', hint: '收益率高于此值计为胜' }
  ].forEach(({ name, label, value, hint }) => {
    form.appendChild(
      el('div', { className: 'field' }, [
        el('label', { text: label }),
        el('input', { type: 'number', name, value, min: name === 'winThreshold' ? '-100' : '1' }),
        el('div', { className: 'field-hint', text: hint })
      ])
    );
  });

  form.appendChild(
    el('div', { className: 'field' }, [
      el('label', { text: '指定股票（可选）' }),
      el('textarea', { name: 'codes', placeholder: '600519,000001\n留空则扫描过滤池前 N 只' })
    ])
  );

  form.appendChild(
    el('button', {
      className: 'btn-primary',
      type: 'submit',
      text: state.running ? '回测中…' : '开始回测',
      disabled: state.running || !state.cacheReady ? '' : undefined
    })
  );

  if (!state.cacheReady) {
    form.appendChild(
      el('div', { className: 'field-hint', text: '后端 K 线缓存未就绪，请先启动 atlas_backend 并等待缓存加载完成' })
    );
  }

  return form;
}

function render() {
  const root = document.getElementById('app');
  root.innerHTML = '';

  const statusDotClass = state.cacheReady ? 'status-dot ok' : 'status-dot bad';
  const statusText = state.cacheReady ? '后端缓存就绪' : '后端缓存未就绪';

  root.appendChild(
    el('header', {}, [
      el('h1', { text: 'Atlas 策略回测' }),
      el('p', { text: '基于现有策略引擎，在历史 K 线上逐日模拟信号，统计胜率与持有期收益。' }),
      el('div', { className: 'status-bar' }, [
        el('span', { className: statusDotClass }),
        el('span', { text: statusText })
      ])
    ])
  );

  root.appendChild(
    el('div', { className: 'layout' }, [
      el('aside', { className: 'panel' }, [el('h2', { text: '回测参数' }), renderForm()]),
      el('section', { className: 'panel' }, [el('h2', { text: '回测结果' }), renderResults()])
    ])
  );
}

async function init() {
  render();
  try {
    const [health, strategies] = await Promise.all([
      api('/backtest/health'),
      api('/backtest/strategies')
    ]);
    state.cacheReady = !!health.cacheReady;
    state.strategies = strategies || [];
  } catch {
    state.cacheReady = false;
  }
  render();
}

init();
