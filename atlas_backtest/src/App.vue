<script setup>
import { ref, computed, onMounted } from 'vue';
import { fetchHealth, fetchStrategies, runBacktest } from './api/backtest';
import { buildAnalytics, exportTradesCsv } from './utils/analytics';
import ConfigPanel from './components/ConfigPanel.vue';
import KpiGrid from './components/KpiGrid.vue';
import ChartPanel from './components/ChartPanel.vue';
import TradeTable from './components/TradeTable.vue';
import TierStats from './components/TierStats.vue';

const cacheReady = ref(false);
const initError = ref('');
const strategies = ref([]);
const running = ref(false);
const error = ref('');
const result = ref(null);
const activeTab = ref('overview');
const lastRunAt = ref('');

const config = ref({
  strategy: 'ultra',
  days: 365,
  holdDays: 10,
  maxStocks: 200,
  winThreshold: 0,
  signalCooldown: 0,
  codes: ''
});

const analytics = computed(() => (result.value ? buildAnalytics(result.value) : null));
const summary = computed(() => result.value?.summary || null);
const trades = computed(() => result.value?.trades || []);

const tabs = [
  { id: 'overview', label: '概览' },
  { id: 'equity', label: '收益曲线' },
  { id: 'distribution', label: '分布分析' },
  { id: 'trades', label: '交易明细' }
];

async function init() {
  try {
    const health = await fetchHealth();
    cacheReady.value = !!health.cacheReady;
    initError.value = '';
  } catch (e) {
    cacheReady.value = false;
    initError.value = e.message || '无法连接后端';
  }
  try {
    strategies.value = await fetchStrategies() || [];
    if (strategies.value.length && !strategies.value.find(s => s.code === config.value.strategy)) {
      config.value.strategy = strategies.value[0].code;
    }
  } catch {
    strategies.value = [{ code: 'ultra', name: '超短线策略' }];
  }
}

async function onRun() {
  if (running.value || !cacheReady.value) return;
  running.value = true;
  error.value = '';
  result.value = null;
  try {
    const body = {
      strategy: config.value.strategy,
      days: Number(config.value.days),
      holdDays: Number(config.value.holdDays),
      maxStocks: Number(config.value.maxStocks),
      winThreshold: Number(config.value.winThreshold),
      signalCooldown: config.value.signalCooldown > 0 ? Number(config.value.signalCooldown) : undefined,
      codes: config.value.codes.trim() || undefined
    };
    result.value = await runBacktest(body);
    lastRunAt.value = new Date().toLocaleString('zh-CN');
    activeTab.value = 'overview';
  } catch (e) {
    error.value = e.message || '回测失败';
  } finally {
    running.value = false;
  }
}

function onExport() {
  if (!trades.value.length) return;
  const csv = exportTradesCsv(trades.value);
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `backtest_${config.value.strategy}_${Date.now()}.csv`;
  a.click();
  URL.revokeObjectURL(url);
}

onMounted(init);
</script>

<template>
  <div class="workstation">
    <header class="topbar">
      <div class="brand">
        <span class="brand-icon">◈</span>
        <div>
          <h1>Atlas 回测工作站</h1>
          <p>策略引擎快照回放 · 持有期收益统计 · 专业量化分析</p>
        </div>
      </div>
      <div class="topbar-status">
        <span :class="['dot', cacheReady ? 'ok' : 'bad']" />
        <span>{{ cacheReady ? 'K线缓存就绪' : (initError || '缓存未就绪') }}</span>
        <span v-if="lastRunAt" class="last-run">上次回测 {{ lastRunAt }}</span>
      </div>
    </header>

    <div class="workspace">
      <aside class="sidebar">
        <ConfigPanel
          v-model="config"
          :strategies="strategies"
          :running="running"
          :cache-ready="cacheReady"
          @run="onRun"
        />
      </aside>

      <main class="main">
        <div v-if="running" class="state-panel">
          <div class="spinner" />
          <h3>回测运行中</h3>
          <p>正在逐日模拟策略信号，全市场扫描约需 1～5 分钟</p>
        </div>

        <div v-else-if="error" class="state-panel error">
          <h3>回测失败</h3>
          <p>{{ error }}</p>
        </div>

        <div v-else-if="!result" class="state-panel empty">
          <div class="empty-icon">📈</div>
          <h3>配置参数并开始回测</h3>
          <p>系统将在历史 K 线上逐日调用与实盘相同的策略引擎，按持有期结算盈亏。</p>
          <ul>
            <li>支持全部活跃策略（超短线、波段、级联突破等）</li>
            <li>收益曲线、最大回撤、盈亏分布、月度统计</li>
            <li>交易明细导出 CSV</li>
          </ul>
        </div>

        <template v-else>
          <nav class="tab-bar">
            <button
              v-for="t in tabs"
              :key="t.id"
              :class="['tab', { active: activeTab === t.id }]"
              @click="activeTab = t.id"
            >{{ t.label }}</button>
            <div class="tab-actions">
              <button class="btn-ghost" @click="onExport">导出 CSV</button>
            </div>
          </nav>

          <div v-show="activeTab === 'overview'" class="tab-content">
            <KpiGrid :summary="summary" :analytics="analytics" />
            <div class="overview-charts">
              <ChartPanel type="equity-mini" :analytics="analytics" />
              <ChartPanel type="monthly" :analytics="analytics" />
            </div>
            <TierStats :tiers="analytics?.tiers" />
          </div>

          <div v-show="activeTab === 'equity'" class="tab-content">
            <ChartPanel type="equity-full" :analytics="analytics" />
            <ChartPanel type="monthly" :analytics="analytics" />
          </div>

          <div v-show="activeTab === 'distribution'" class="tab-content">
            <ChartPanel type="histogram" :analytics="analytics" />
            <TierStats :tiers="analytics?.tiers" />
          </div>

          <div v-show="activeTab === 'trades'" class="tab-content">
            <TradeTable :trades="trades" />
          </div>
        </template>
      </main>
    </div>
  </div>
</template>

<style scoped>
.workstation {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 24px;
  background: var(--bg-panel);
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}

.brand { display: flex; align-items: center; gap: 14px; }
.brand-icon { font-size: 28px; color: var(--accent); }
.brand h1 { font-size: 18px; font-weight: 600; letter-spacing: -0.02em; }
.brand p { font-size: 12px; color: var(--text-muted); margin-top: 2px; }

.topbar-status {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: var(--text-muted);
}
.dot { width: 8px; height: 8px; border-radius: 50%; }
.dot.ok { background: var(--green); box-shadow: 0 0 8px var(--green); }
.dot.bad { background: var(--red); }
.last-run { margin-left: 12px; padding-left: 12px; border-left: 1px solid var(--border); }

.workspace {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  width: var(--sidebar-w);
  flex-shrink: 0;
  border-right: 1px solid var(--border);
  background: var(--bg-panel);
  overflow-y: auto;
}

.main {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  background: var(--bg-root);
}

.state-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  text-align: center;
  color: var(--text-muted);
  gap: 12px;
}
.state-panel h3 { color: var(--text); font-size: 18px; }
.state-panel.error h3 { color: var(--red); }
.state-panel.empty .empty-icon { font-size: 48px; opacity: 0.5; }
.state-panel ul {
  text-align: left;
  margin-top: 8px;
  padding-left: 20px;
  font-size: 13px;
  line-height: 1.8;
}

.tab-bar {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--border);
  padding-bottom: 0;
}
.tab {
  padding: 10px 16px;
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: color 0.15s;
}
.tab:hover { color: var(--text); }
.tab.active { color: var(--accent); border-bottom-color: var(--accent); }
.tab-actions { margin-left: auto; }

.btn-ghost {
  padding: 6px 14px;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  color: var(--text-muted);
  cursor: pointer;
}
.btn-ghost:hover { color: var(--text); border-color: var(--border-light); }

.tab-content { display: flex; flex-direction: column; gap: 20px; }

.overview-charts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
@media (max-width: 1100px) {
  .overview-charts { grid-template-columns: 1fr; }
}
</style>
