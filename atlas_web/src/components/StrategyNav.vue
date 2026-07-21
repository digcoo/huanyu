<template>
  <div class="strategy-nav">
    <div class="families">
      <button
        v-for="f in families"
        :key="f.id"
        :class="{ active: family === f.id }"
        @click="$emit('family-change', f.id)"
      >
        {{ f.icon }} {{ f.name }}
      </button>
    </div>
    <div v-if="showTierRow" class="tiers">
      <button
        v-for="t in tierTabs"
        :key="t.id"
        :class="{ active: tier === t.id }"
        @click="$emit('tier-change', t.id)"
      >
        {{ t.name }}
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  family: { type: String, default: 'ultra' },
  tier: { type: String, default: 'bucket' },
  showTierRow: { type: Boolean, default: true },
  families: { type: Array, required: true },
  tierTabs: { type: Array, default: () => [] }
});

defineEmits(['family-change', 'tier-change']);
</script>

<style scoped>
.strategy-nav {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}
.families, .tiers {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
button {
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid var(--border);
  background: var(--bg-elevated);
  color: var(--text-muted);
}
button.active {
  border-color: var(--accent);
  color: var(--accent);
}
</style>
