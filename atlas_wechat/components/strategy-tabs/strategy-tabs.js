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

  data: {
    brokenIcons: {}
  },

  methods: {
    onIconError(e) {
      var id = e.currentTarget.dataset.id;
      if (!id) return;
      this.setData({ ['brokenIcons.' + id]: true });
    },

    onTabTap(e) {
      const id = e.currentTarget.dataset.id;
      if (id && id !== this.data.activeStrategy) {
        this.triggerEvent('change', { strategyId: id });
      }
    }
  }
});
