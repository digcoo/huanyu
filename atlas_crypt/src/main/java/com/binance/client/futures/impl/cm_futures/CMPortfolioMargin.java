package com.binance.client.futures.impl.cm_futures;


import com.binance.client.futures.impl.futures.PortfolioMargin;
import com.binance.client.futures.utils.ProxyAuth;

import java.util.LinkedHashMap;

/**
 * <h2>Coin-Margined Portfolio Margin Endpoints</h2>
 * All endpoints under the
 * <a href="https://binance-docs.github.io/apidocs/delivery/en/#portfolio-margin-endpoints">PortfolioMargin Endpoint</a>
 * section of the API documentation will be implemented in this class.
 * <br>
 * Response will be returned in <i>String format</i>.
 */
public class CMPortfolioMargin extends PortfolioMargin {
    public CMPortfolioMargin(String productUrl, String apiKey, String secretKey, boolean showLimitUsage, ProxyAuth proxy) {
        super(productUrl, apiKey, secretKey, showLimitUsage, proxy);
    }

    /**
     * Current Portfolio Margin exchange trading rules.
     * GET /v1/pmExchangeInfo
     * <br>
     * @param
     * parameters LinkedHashedMap of String,Object pair
     *            where String is the name of the parameter and Object is the value of the parameter
     * <br><br>
     * symbol -- optional/string <br>
     * pair -- optional/string <br>
     * @return String
     * @see <a href="https://binance-docs.github.io/apidocs/delivery/en/#portfolio-margin-exchange-information">
     *     https://binance-docs.github.io/apidocs/delivery/en/#portfolio-margin-exchange-information</a>
     */
    public String portfolioMarginExchangeInfo(LinkedHashMap<String, Object> parameters) {
        return super.portfolioMarginExchangeInfo(parameters);
    }
}