package com.binance.client.utils.indicator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.apache.http.client.fluent.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XueQiuUtils {

    public static List<Ticker> getXueQiuJson() throws IOException {
        String url = "https://stock.xueqiu.com/v5/stock/chart/kline.json?symbol=SH600892&begin=1742782561143&period=day&type=before&count=-500&indicator=kline,pe,pb,ps,pcf,market_capital,agt,ggt,balance";
        String cookie = "cookiesu=101729952232908; s=cb1diqv40c; device_id=1087b84935b10a1740504ea35d0cd6e6; _c_WBKFRo=J90CN1z4TjCTWkj333nsZMlxWgNzkK48yO3oOfre; xq_a_token=cc9943aa6d41f0ae420f49b428f2f90a472b070a; xqat=cc9943aa6d41f0ae420f49b428f2f90a472b070a; xq_r_token=20869bd02083b2ef75d4d4b7654f827f00fdcd22; xq_id_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOi0xLCJpc3MiOiJ1YyIsImV4cCI6MTc0NDc2NTA3MCwiY3RtIjoxNzQyNjk2MDMxMDYwLCJjaWQiOiJkOWQwbjRBWnVwIn0.gjX5vGzuhrKi8YFzZNROcKt4Zu3856AAlmkVgqd0iU_UcWKhmi2vAw97ZsY26-6SdxwmxFXwIj14Su0-mRNNZirbtoMd6RSiTVyRCnhHhHY08W7V-Ty5-CLFPbtnfcaGNCOtKjk0F1M9igFRYj979AEnMl9Hf_rLdNPBPz9vN99AA_O81VnP-MXeUo1wHYu6cjYftzpY-aV7p8LJHqDscK9ua1QN6209RAOkEH2K1MqKMjvsl8Hyg6Ry9Y1PTm7mLrJFDYcbvPOAPLgA_n6byxlM9zLj6fQhLQ60nqYlalpPkeMeJm3rcQ11kppbNp0uipsRNi0MoxfoxOZLfsarWQ; u=101729952232908; Hm_lvt_1db88642e346389874251b5a1eded6e3=1740621725,1742628944,1742696092; HMACCOUNT=5E156ED48B683EF5; is_overseas=0; Hm_lpvt_1db88642e346389874251b5a1eded6e3=1742696149; ssxmod_itna=eqmxyQ0Qi=DtM47qYjxYvPGIPoxeuu4GQDXDUqAQNGgexFqAP4DHAH35QRY+j+KRnYb+emFDDKbqD0yGPmix0=7Df40WmGPQgGYKtU70pI5jQiOIQ5H8gB8GfHzYwlPdsvz1kUmDnneDKqGm+4DSixD9YExbDYYDC4GwDGoD34DiDDpKxDttrePD7gbYEeu/iE0oilbDmb3rixDgIPD1YcTiXjN4KpfAFhTWDeGnD0Q+rrmdx0fOHh7Dzd3DbOEYwt8DtTH4xCK=MlTWAImeFMCbb+DK6Thx804cbY+QY2GuMfhZ=X/QjBCNAYvhNoGDmDDi1NMODcr+QnPbG66c6Al6Pr44i5PiGInmKC5uODzDIYnDq7xuEe10qsnP27iw7xsmhrRxcoD; ssxmod_itna2=eqmxyQ0Qi=DtM47qYjxYvPGIPoxeuu4GQDXDUqAQNGgexFqAP4DHAH35QRY+j+KRnYb+emFDDKbDG+e0xrYOh5Ga3iA8IvDBL2nQTof10+fGuYUMr0on8Ofjc8hqd2nYKmT70MY2h=/=RbBQSzMf0/M6Z4T6erCNcKWU/+m=7AnQ2Ib6QtSY6bANG+uG/b7r9SeLBKcGQyqcl/SdohOqqZdWPEcbDVuc8XSXqOLrToiLKNChmaQUKhcTgCqKyzncVFm0xQ9NR3e2L+lQYHw6L+P9QvkNmIl=zpuyUzjXjZkGZUC1P06FUdq+Cc2nbiQqjQDx/e1i0UYTKj07mvhAcDn+0YGGtqSD7qY0KsqZ7w/nBepr1bDRIT4jF2G531gsjKRYEstnW/Yumh8nq7WoxSRtRp8GDVBOlGxOIOuFnvWw8/2eoYCRySiHmiKW2HDu8OC5NZ+PsSFraTlrK8+8+AkBILUieYyzFkat+Lj0vDAW/+PfwPjiqewCVBWBd16DAWTqUptI+xnh7BnxDwxCw+1Hayx0ACaD4oYrbD=o3bt9rN/2NWDx/lfrLfN+DD";
        String jsonStr = Request.Get(url)
                .addHeader("Cookie", cookie)
                .execute().returnContent().asString();

        JSONArray jsonArray = JSON.parseObject(jsonStr).getJSONObject("data").getJSONArray("item");
        List<Ticker> tickers = new ArrayList<>(jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            tickers.add(Ticker.builder()
                    .timestamp(jsonArray.getJSONArray(i).getLong(0))
                    .open(jsonArray.getJSONArray(i).getDouble(2))
                    .high(jsonArray.getJSONArray(i).getDouble(3))
                    .low(jsonArray.getJSONArray(i).getDouble(4))
                    .close(jsonArray.getJSONArray(i).getDouble(5))
                    .build());
        }

        return tickers;
    }

}
