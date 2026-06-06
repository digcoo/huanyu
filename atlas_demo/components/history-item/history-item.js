Component({
  properties: {
    item: {
      type: Object,
      value: {}
    },
    activePeriod: {
      type: String,
      value: 'week'
    },
    flipped: {
      type: Boolean,
      value: false
    },
    marketLabel: {
      type: String,
      value: ''
    },
    strategyLabel: {
      type: String,
      value: ''
    }
  },

  methods: {
    onTap() {
      const item = this.properties.item;
      if (!item || !item.id) return;
      this.triggerEvent('itemtap', { item: item });
    }
  }
});
