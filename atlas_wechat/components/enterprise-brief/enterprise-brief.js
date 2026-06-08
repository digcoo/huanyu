Component({
  properties: {
    profile: {
      type: Object,
      value: null
    }
  },

  data: {
    showScope: false
  },

  methods: {
    onToggleScope() {
      this.setData({ showScope: !this.data.showScope });
    }
  }
});
