<template>
  <div>
    <router-link to="/" class="back muted">← 返回列表</router-link>
    <h2>{{ detail?.name || code }}</h2>
    <p class="muted">{{ code }}</p>

    <div class="toolbar">
      <select v-model="period" @change="loadKlines">
        <option value="min30">30分</option>
        <option value="min60">60分</option>
        <option value="day">日</option>
        <option value="week">周</option>
        <option value="month">月</option>
      </select>
    </div>

    <p v-if="loading" class="muted">加载中…</p>
    <div v-if="klines.length" class="card chart-placeholder">
      <p>K 线 {{ klines.length }} 根（ECharts 图表待接入）</p>
      <p class="muted">最新收盘：{{ klines[klines.length - 1]?.close }}</p>
    </div>

    <div v-if="detail" class="card detail-block">
      <h3>信号</h3>
      <p>{{ detail.signalMessage || detail.trendMessage || '—' }}</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { fetchKlines, fetchStockDetail } from '@/api/stock';

const route = useRoute();
const code = ref(route.params.code);
const period = ref('week');
const klines = ref([]);
const detail = ref(null);
const loading = ref(false);

async function loadDetail() {
  try {
    detail.value = await fetchStockDetail(code.value);
  } catch {
    detail.value = null;
  }
}

async function loadKlines() {
  loading.value = true;
  try {
    klines.value = await fetchKlines(code.value, period.value, 120) || [];
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadDetail();
  loadKlines();
});

watch(() => route.params.code, (c) => {
  code.value = c;
  loadDetail();
  loadKlines();
});
</script>

<style scoped>
.back {
  display: inline-block;
  margin-bottom: 12px;
}
.toolbar {
  margin: 16px 0;
}
select {
  background: var(--bg-elevated);
  color: var(--text);
  border: 1px solid var(--border);
  border-radius: 6px;
  padding: 6px 10px;
}
.chart-placeholder, .detail-block {
  margin-top: 16px;
}
</style>
