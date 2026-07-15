<script setup>
const props = defineProps({
  modelValue: { type: Object, required: true },
  strategies: { type: Array, default: () => [] },
  running: Boolean,
  cacheReady: Boolean
});
const emit = defineEmits(['update:modelValue', 'run']);

function setField(key, e) {
  const v = e.target.type === 'number' ? Number(e.target.value) : e.target.value;
  emit('update:modelValue', { ...props.modelValue, [key]: v });
}
</script>

<template>
  <div class="config">
    <h2>回测配置</h2>

    <section class="section">
      <label>策略</label>
      <select :value="modelValue.strategy" @change="setField('strategy', $event)">
        <option v-for="s in strategies" :key="s.code" :value="s.code">
          {{ s.name }} ({{ s.code }})
        </option>
      </select>
    </section>

    <section class="section">
      <label>回测窗口 <span class="hint">日 K 根数</span></label>
      <input type="number" min="30" max="730" :value="modelValue.days" @input="setField('days', $event)" />
    </section>

    <section class="section">
      <label>持有期 <span class="hint">交易日</span></label>
      <input type="number" min="1" max="60" :value="modelValue.holdDays" @input="setField('holdDays', $event)" />
      <p class="field-desc">信号日收盘价买入，N 日后收盘价卖出</p>
    </section>

    <section class="section">
      <label>扫描上限 <span class="hint">只股票</span></label>
      <input type="number" min="1" max="2000" :value="modelValue.maxStocks" @input="setField('maxStocks', $event)" />
    </section>

    <section class="section">
      <label>盈利阈值 <span class="hint">%</span></label>
      <input type="number" step="0.1" :value="modelValue.winThreshold" @input="setField('winThreshold', $event)" />
      <p class="field-desc">收益率高于此值计为胜</p>
    </section>

    <section class="section">
      <label>信号冷却 <span class="hint">日 K，0=同持有期</span></label>
      <input type="number" min="0" max="120" :value="modelValue.signalCooldown" @input="setField('signalCooldown', $event)" />
    </section>

    <section class="section">
      <label>指定股票 <span class="hint">可选</span></label>
      <textarea
        :value="modelValue.codes"
        placeholder="600519,000001&#10;留空扫描过滤池"
        rows="3"
        @input="setField('codes', $event)"
      />
    </section>

    <button class="btn-run" :disabled="running || !cacheReady" @click="emit('run')">
      {{ running ? '回测中…' : '▶ 开始回测' }}
    </button>

    <p v-if="!cacheReady" class="warn">请先启动 atlas_backend (9010) 并等待 K 线缓存加载</p>
  </div>
</template>

<style scoped>
.config { padding: 20px; }
.config h2 {
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--text-dim);
  margin-bottom: 20px;
}
.section { margin-bottom: 18px; }
.section label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 6px;
}
.hint { font-weight: 400; color: var(--text-dim); font-size: 12px; }
.section input,
.section select,
.section textarea {
  width: 100%;
  padding: 9px 12px;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  color: var(--text);
}
.section input:focus,
.section select:focus,
.section textarea:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 2px var(--accent-dim);
}
.field-desc { font-size: 11px; color: var(--text-dim); margin-top: 4px; }
.btn-run {
  width: 100%;
  padding: 12px;
  margin-top: 8px;
  background: var(--accent);
  border: none;
  border-radius: var(--radius);
  color: #fff;
  font-weight: 600;
  cursor: pointer;
}
.btn-run:disabled { opacity: 0.45; cursor: not-allowed; }
.btn-run:not(:disabled):hover { filter: brightness(1.08); }
.warn { font-size: 12px; color: var(--amber); margin-top: 12px; }
</style>
