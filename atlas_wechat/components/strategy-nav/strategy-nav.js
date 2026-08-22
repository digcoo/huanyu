const strategyNav = require('../../utils/strategy-nav');

Component({
  properties: {
    activeFamily: {
      type: String,
      value: strategyNav.STRATEGY_MA_GOLD_BREAK
    },
    activeTier: {
      type: String,
      value: 'short'
    }
  },

  data: {
    families: strategyNav.STRATEGY_FAMILIES,
    tierTabs: [],
    showTierRow: false
  },

  observers: {
    'activeFamily': function (family) {
      this.syncTierRow(family);
    }
  },

  lifetimes: {
    attached: function () {
      this.syncTierRow(this.data.activeFamily);
    }
  },

  methods: {
    syncTierRow: function (family) {
      var showTierRow = strategyNav.showTierRow(family);
      this.setData({
        showTierRow: showTierRow,
        tierTabs: showTierRow ? strategyNav.tierTabsForFamily(family) : []
      });
    },

    onFamilyTap: function (e) {
      var id = e.currentTarget.dataset.id;
      if (!id || id === this.data.activeFamily) return;
      this.triggerEvent('familychange', { family: id });
    },

    onTierTap: function (e) {
      var id = e.currentTarget.dataset.id;
      if (!id || id === this.data.activeTier) return;
      this.triggerEvent('tierchange', { tier: id });
    }
  }
});
