Component({
  properties: {
    markets: {
      type: Array,
      value: []
    },
    activeMarket: {
      type: String,
      value: 'cn'
    }
  },

  methods: {
    onTabTap(e) {
      const id = e.currentTarget.dataset.id;
      if (id !== this.data.activeMarket) {
        this.triggerEvent('change', { marketId: id });
      }
    }
  }
});
