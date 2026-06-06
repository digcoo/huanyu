const PERIODS = [
  { id: 'year', label: '年K' },
  { id: 'month', label: '月K' },
  { id: 'week', label: '周K' },
  { id: 'day', label: '日K' }
];

Component({
  properties: {
    activePeriod: {
      type: String,
      value: 'week'
    },
    flipped: {
      type: Boolean,
      value: false
    }
  },

  data: {
    periods: PERIODS
  },

  methods: {
    onTap(e) {
      const id = e.currentTarget.dataset.id;
      if (id !== this.data.activePeriod) {
        this.triggerEvent('change', { period: id });
      }
    },

    onFlip() {
      this.triggerEvent('flip', { flipped: !this.properties.flipped });
    }
  }
});
