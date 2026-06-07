const strategyParams = require('../../utils/strategy-params');

Component({
  properties: {
    visible: {
      type: Boolean,
      value: false
    },
    strategyId: {
      type: String,
      value: 'trend'
    },
    statusBarHeight: {
      type: Number,
      value: 20
    }
  },

  data: {
    schema: [],
    form: {},
    tierIndex: 0,
    tierLabels: [],
    tierKey: 'uTierMin',
    applying: false
  },

  observers: {
    visible: function (visible) {
      if (visible) {
        this.initForm();
      }
    },
    strategyId: function () {
      if (this.data.visible) {
        this.initForm();
      }
    }
  },

  methods: {
    initForm: function () {
      var strategyId = this.properties.strategyId || 'trend';
      var schema = strategyParams.getSchema(strategyId);
      var form = strategyParams.load(strategyId);
      var tierField = schema.find(function (f) { return f.key === 'uTierMin' || f.key === 'rTierMin'; });
      var tierIndex = 0;
      var tierLabels = [];
      var tierKey = tierField ? tierField.key : 'uTierMin';
      if (tierField && tierField.options) {
        tierLabels = tierField.options.map(function (o) { return o.label; });
        tierField.options.forEach(function (o, idx) {
          if (o.value === form[tierKey]) tierIndex = idx;
        });
      }
      this.setData({ schema: schema, form: form, tierIndex: tierIndex, tierLabels: tierLabels, tierKey: tierKey });
    },

    onMaskTap: function () {
      this.triggerEvent('close');
    },

    onPanelTap: function () {},

    onClose: function () {
      this.triggerEvent('close');
    },

    onSliderChange: function (e) {
      var key = e.currentTarget.dataset.key;
      var value = e.detail.value;
      var patch = {};
      patch['form.' + key] = value;
      this.setData(patch);
    },

    onSwitchChange: function (e) {
      var key = e.currentTarget.dataset.key;
      var checked = e.detail.value;
      var patch = {};
      patch['form.' + key] = checked;
      if (key === 'uEnableModeB' && !checked) {
        patch['form.uEnableModeBWeak'] = false;
      }
      this.setData(patch);
    },

    onTierChange: function (e) {
      var idx = Number(e.detail.value) || 0;
      var tierKey = this.data.tierKey || 'uTierMin';
      var tierField = this.data.schema.find(function (f) { return f.key === tierKey; });
      if (!tierField || !tierField.options || !tierField.options[idx]) return;
      var patch = { tierIndex: idx };
      patch['form.' + tierKey] = tierField.options[idx].value;
      this.setData(patch);
    },

    onReset: function () {
      var strategyId = this.properties.strategyId || 'trend';
      var form = strategyParams.reset(strategyId);
      this.initForm();
      this.setData({ form: form });
      wx.showToast({ title: '已恢复默认', icon: 'none' });
    },

    onApply: function () {
      var self = this;
      var strategyId = this.properties.strategyId || 'trend';
      var saved = strategyParams.save(strategyId, this.data.form);
      this.setData({ applying: true });
      this.triggerEvent('apply', { strategyId: strategyId, params: saved, rescan: true });
    },

    onPreviewOnly: function () {
      var strategyId = this.properties.strategyId || 'trend';
      var saved = strategyParams.save(strategyId, this.data.form);
      this.triggerEvent('apply', { strategyId: strategyId, params: saved, rescan: false });
      this.triggerEvent('close');
    },

    setApplying: function (applying) {
      this.setData({ applying: !!applying });
    }
  }
});
