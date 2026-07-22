package com.binance.client.futures.impl;

import com.binance.client.futures.impl.futures.Account;
import com.binance.client.futures.impl.futures.Market;
import com.binance.client.futures.impl.futures.PortfolioMargin;
import com.binance.client.futures.impl.futures.UserData;

public interface FuturesClient {
    Market market();
    Account account();
    UserData userData();
    PortfolioMargin portfolioMargin();
}
