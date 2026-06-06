Component({
  properties: {
    markets: {
      type: Array,
      value: []
    },
    activeMarket: {
      type: String,
      value: 'cn'
    },
    compact: {
      type: Boolean,
      value: false
    },
    inline: {
      type: Boolean,
      value: false
    }
  },

  methods: {
    onTabTap(e) {
      const id = e.currentTarget.dataset.id;
      const enabled = e.currentTarget.dataset.enabled;
      if (enabled === false || enabled === 'false') {
        this.triggerEvent('disabled', { marketId: id });
        return;
      }
      if (id !== this.data.activeMarket) {
        this.triggerEvent('change', { marketId: id });
      }
    }
  }
});
