<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue';
import * as echarts from 'echarts';

const props = defineProps({
  type: { type: String, required: true },
  analytics: { type: Object, default: null }
});

const chartRef = ref(null);
let chart = null;

const titles = {
  'equity-full': '累计收益曲线',
  'equity-mini': '收益走势',
  monthly: '月度收益',
  histogram: '收益率分布'
};

function render() {
  if (!chart || !props.analytics) return;
  const a = props.analytics;

  if (props.type === 'equity-full' || props.type === 'equity-mini') {
    const curve = a.curve || [];
    chart.setOption({
      backgroundColor: 'transparent',
      title: props.type === 'equity-full' ? { text: titles[props.type], left: 0, textStyle: { color: '#8b93a8', fontSize: 13 } } : undefined,
      grid: { left: 48, right: 20, top: props.type === 'equity-full' ? 40 : 24, bottom: 32 },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: curve.map((p) => p.day),
        axisLine: { lineStyle: { color: '#2a3142' } },
        axisLabel: { color: '#5c6478', fontSize: 10 }
      },
      yAxis: {
        type: 'value',
        name: '%',
        axisLine: { show: false },
        splitLine: { lineStyle: { color: '#1f2533' } },
        axisLabel: { color: '#5c6478' }
      },
      series: [{
        type: 'line',
        data: curve.map((p) => p.equity),
        smooth: true,
        symbol: 'none',
        lineStyle: { color: '#3b82f6', width: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(59,130,246,0.25)' },
            { offset: 1, color: 'rgba(59,130,246,0)' }
          ])
        }
      }]
    }, true);
    return;
  }

  if (props.type === 'monthly') {
    const rows = a.monthly || [];
    chart.setOption({
      backgroundColor: 'transparent',
      title: { text: titles.monthly, left: 0, textStyle: { color: '#8b93a8', fontSize: 13 } },
      grid: { left: 48, right: 20, top: 40, bottom: 32 },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: rows.map((r) => r.month),
        axisLabel: { color: '#5c6478', fontSize: 10 }
      },
      yAxis: {
        type: 'value',
        name: '均收益%',
        splitLine: { lineStyle: { color: '#1f2533' } },
        axisLabel: { color: '#5c6478' }
      },
      series: [{
        type: 'bar',
        data: rows.map((r) => r.avgPnl),
        itemStyle: {
          color: (p) => (p.value >= 0 ? '#22c55e' : '#ef4444')
        }
      }]
    }, true);
    return;
  }

  if (props.type === 'histogram') {
    const rows = a.histogram || [];
    chart.setOption({
      backgroundColor: 'transparent',
      title: { text: titles.histogram, left: 0, textStyle: { color: '#8b93a8', fontSize: 13 } },
      grid: { left: 48, right: 20, top: 40, bottom: 48 },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: rows.map((r) => r.label),
        axisLabel: { color: '#5c6478', fontSize: 10, rotate: 30 }
      },
      yAxis: {
        type: 'value',
        name: '笔数',
        splitLine: { lineStyle: { color: '#1f2533' } },
        axisLabel: { color: '#5c6478' }
      },
      series: [{
        type: 'bar',
        data: rows.map((r) => r.count),
        itemStyle: { color: '#6366f1' }
      }]
    }, true);
  }
}

function resize() { chart?.resize(); }

onMounted(() => {
  if (!chartRef.value) return;
  chart = echarts.init(chartRef.value, null, { renderer: 'canvas' });
  render();
  window.addEventListener('resize', resize);
});

watch(() => props.analytics, render, { deep: true });
watch(() => props.type, render);

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize);
  chart?.dispose();
});
</script>

<template>
  <div class="chart-card" :class="type">
    <div ref="chartRef" class="chart" />
  </div>
</template>

<style scoped>
.chart-card {
  background: var(--bg-panel);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: 16px;
}
.chart { width: 100%; height: 280px; }
.equity-full .chart { height: 360px; }
</style>
