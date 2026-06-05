Component({
  properties: {
    module: {
      type: Object,
      value: {}
    },
    moduleKey: {
      type: String,
      value: ''
    },
    expanded: {
      type: Boolean,
      value: true
    }
  },

  methods: {
    onToggle() {
      this.triggerEvent('toggle', { key: this.properties.moduleKey });
    }
  }
});
