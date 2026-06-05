package com.yh.bigdata.silkworm.api.utils;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.junit.Test;

public class QueqiuTest {

	@Test
	public void spider() throws ClientProtocolException, IOException {
		long start = System.currentTimeMillis();
		int i = 0;
		while(i < 5000) {

			String url = "https://stock.xueqiu.com/v5/stock/chart/kline.json?symbol=SZ000721&begin=1698935762607&period=60m&type=before&count=-8&indicator=kline,pe,pb,ps,pcf,market_capital,agt,ggt,balance";
			System.out.println(Request.Get(url)
					.addHeader("Cookie", "device_id=c4883faa405a62bb13f16da661fb385b; s=c911j9lykm; xq_is_login=1; u=1818535471; cookiesu=651692796904231; Hm_lvt_1db88642e346389874251b5a1eded6e3=1697260103; xq_a_token=b600551ed706c317cb2ad67ea9d11d5a681031f0; xqat=b600551ed706c317cb2ad67ea9d11d5a681031f0; xq_id_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOjE4MTg1MzU0NzEsImlzcyI6InVjIiwiZXhwIjoxNzAxMTY0OTUxLCJjdG0iOjE2OTg1NzI5NTE0MDcsImNpZCI6ImQ5ZDBuNEFadXAifQ.p08d8zzXK9b5bEPVGwVQUG0dlpfUT1nLzEhI9LAvWHym-3_Fsykf62yej1U4oekLgjELpXQm-oJhwX-KYAQMTgV45lkwjBneP16E4-4Nd9Jp3cB0mDqmuc7vg3LaWwXOv9_v8v4ZmHb0QxOaAbGa3seyRLARwcEkcSQeu9IoYKSH0KMwOq2C9QdGfHwf1LhAuV7BaP9zF7RFa4WaleG1r1Rcvb4EgsjXCY4JRk0Ca2k9xulhXoxP0GiDnyXUJ7uiMfnxzuK13xG1DtYCu1M9QwF2Y4syoqTKRC6fmDFsFoL2Ti71yBxzEJEtiEoS2xYu4X-9oDG51sD7zpzIuztObw; xq_r_token=6bce89eebcbe536fc0bec718aeb1227543679ccc; Hm_lpvt_1db88642e346389874251b5a1eded6e3=1698849344")
					
					.execute().returnContent().asString());
			
			i++;
			
		}
		System.out.println((System.currentTimeMillis()-start) / 1000);
	}
}
