import { defineStore } from 'pinia';

export const useAppStore = defineStore('app', {
  state: () => ({
    activeStrategy: localStorage.getItem('activeStrategy') || 'ultra',
    activePeriod: localStorage.getItem('activePeriod') || 'week'
  }),
  actions: {
    setStrategy(strategyId) {
      this.activeStrategy = strategyId;
      localStorage.setItem('activeStrategy', strategyId);
    },
    setPeriod(period) {
      this.activePeriod = period;
      localStorage.setItem('activePeriod', period);
    }
  }
});
