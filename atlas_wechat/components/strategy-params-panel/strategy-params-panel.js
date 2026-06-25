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
    strategyTitle: {
      type: String,
      value: ''
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
    applying: false,
    tierMode: false,
    ladderTier: 'short',
    ladderTiers: strategyParams.NRF_TIERS
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
    resolvePickerState: function (schema, form) {
      var tierField = (schema || []).find(function (f) {
        return f.type === 'picker' && f.options && f.options.length;
      });
      var tierIndex = 0;
      var tierLabels = [];
      var tierKey = tierField ? tierField.key : 'uTierMin';
      if (tierField && tierField.options) {
        tierLabels = tierField.options.map(function (o) { return o.label; });
        tierField.options.forEach(function (o, idx) {
          if (o.value === form[tierKey]) tierIndex = idx;
        });
      }
      return { tierIndex: tierIndex, tierLabels: tierLabels, tierKey: tierKey };
    },

    initForm: function () {
      var strategyId = this.properties.strategyId || 'trend';
      var tierMode = strategyId === 'nrf';
      var tierList = strategyParams.getTierListFor(strategyId);
      var ladderTier = tierMode
        ? (strategyParams.load(strategyId).activeTier || 'short')
        : 'short';
      var schema = tierMode
        ? strategyParams.getPanelSchema(strategyId, ladderTier)
        : strategyParams.getSchema(strategyId);
      var form = tierMode
        ? strategyParams.loadTierFormFor(strategyId, ladderTier)
        : strategyParams.load(strategyId);
      var pickerState = this.resolvePickerState(schema, form);

      this.setData(Object.assign({
        schema: schema,
        form: form,
        tierMode: tierMode,
        ladderTier: ladderTier,
        ladderTiers: tierList
      }, pickerState));
    },

    onLadderTierTabChange: function (e) {
      var tier = e.currentTarget.dataset.tier;
      if (!tier || tier === this.data.ladderTier) return;

      var strategyId = this.properties.strategyId || 'trend';
      strategyParams.saveTierFormFor(strategyId, this.data.ladderTier, this.data.form, false);

      var schema = strategyParams.getPanelSchema(strategyId, tier);
      var form = strategyParams.loadTierFormFor(strategyId, tier);
      var pickerState = this.resolvePickerState(schema, form);

      this.setData(Object.assign({
        ladderTier: tier,
        schema: schema,
        form: form
      }, pickerState));
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
      if (strategyId === 'nrf') {
        strategyParams.resetTierFor(strategyId, this.data.ladderTier);
        this.initForm();
        wx.showToast({ title: '已恢复当前档位默认', icon: 'none' });
        return;
      }
      strategyParams.reset(strategyId);
      this.initForm();
      wx.showToast({ title: '已恢复默认', icon: 'none' });
    },

    onApply: function () {
      var strategyId = this.properties.strategyId || 'trend';
      var saved;
      if (strategyId === 'nrf') {
        saved = strategyParams.saveTierFormFor(strategyId, this.data.ladderTier, this.data.form, true);
      } else {
        saved = strategyParams.save(strategyId, this.data.form);
      }
      this.setData({ applying: true });
      this.triggerEvent('apply', { strategyId: strategyId, params: saved, rescan: true });
    },

    onPreviewOnly: function () {
      var strategyId = this.properties.strategyId || 'trend';
      var saved;
      if (strategyId === 'nrf') {
        saved = strategyParams.saveTierFormFor(strategyId, this.data.ladderTier, this.data.form, true);
      } else {
        saved = strategyParams.save(strategyId, this.data.form);
      }
      this.triggerEvent('apply', { strategyId: strategyId, params: saved, rescan: false });
      this.triggerEvent('close');
    },

    setApplying: function (applying) {
      this.setData({ applying: !!applying });
    }
  }
});
