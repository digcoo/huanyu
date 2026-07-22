package com.binance.client.model.enums;

public enum MAType {
    MA5(5),
    MA10(10),
    MA20(20),
    MA30(30),
    ;

    private final int val;

    MAType(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }
}
