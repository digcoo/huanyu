Component({
  properties: {
    strategies: {
      type: Array,
      value: []
    },
    activeStrategy: {
      type: String,
      value: 'trend'
    }
  },

  methods: {
    onTabTap(e) {
      const id = e.currentTarget.dataset.id;
      if (id && id !== this.data.activeStrategy) {
        this.triggerEvent('change', { strategyId: id });
      }
    }
  }
});
