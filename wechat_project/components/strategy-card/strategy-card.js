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
    }
  },

  data: {
    offsetX: 0,
    swiping: false,
    actionHint: ''
  },

  methods: {
    onTouchStart(e) {
      this._startX = e.touches[0].clientX;
      this._startY = e.touches[0].clientY;
      this._locked = false;
      this.setData({ swiping: true });
    },

    onTouchMove(e) {
      const dx = e.touches[0].clientX - this._startX;
      const dy = e.touches[0].clientY - this._startY;

      if (!this._locked) {
        if (Math.abs(dy) > Math.abs(dx) && Math.abs(dy) > 10) {
          this._locked = true;
          this.setData({ swiping: false, offsetX: 0, actionHint: '' });
          return;
        }
        if (Math.abs(dx) > 10) this._locked = 'h';
      }

      if (this._locked !== 'h') return;

      const maxOffset = 120;
      const clamped = Math.max(-maxOffset, Math.min(maxOffset, dx));
      let hint = '';
      if (clamped > 40) hint = 'watchlist';
      else if (clamped < -40) hint = 'ignore';

      this.setData({ offsetX: clamped, actionHint: hint });
    },

    onTouchEnd() {
      const { offsetX, actionHint, item } = this.data;

      if (actionHint === 'watchlist' && offsetX > 60) {
        this.triggerEvent('watchlist', { item });
        wx.vibrateShort({ type: 'light' });
        wx.showToast({ title: '已加入自选', icon: 'success', duration: 1200 });
      } else if (actionHint === 'ignore' && offsetX < -60) {
        this.triggerEvent('ignore', { item });
        wx.vibrateShort({ type: 'light' });
        wx.showToast({ title: '已忽略', icon: 'none', duration: 1200 });
      }

      this.setData({ offsetX: 0, swiping: false, actionHint: '' });
    },

    onTap() {
      if (Math.abs(this.data.offsetX) > 10) return;
      this.triggerEvent('tap', { item: this.data.item });
    }
  }
});
