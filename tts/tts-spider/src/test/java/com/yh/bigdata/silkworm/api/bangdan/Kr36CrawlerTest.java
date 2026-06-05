package com.yh.bigdata.silkworm.api.bangdan;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yh.bigdata.tts.common.utils.FileUtil;

public class Kr36CrawlerTest {
	
	@Test
	public void Kr36Test() throws ClientProtocolException, IOException {
		String content = Request.Get("https://gateway.36kr.com/api/mrs/organ/organization/catalogue?param.pageNo=1&param.pageSize=200&partner_id=web").execute().returnContent().asString();
		System.out.println(content);
		
		File file = new File("D:\\output_36kr.csv");
		
		JSONArray jsonArray = JSON.parseObject(content).getJSONObject("data").getJSONArray("organizationList");
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			StringBuffer stringBuffer = new StringBuffer();
			String identityName = jsonObject.getString("identityName");
			String name = jsonObject.getString("name");
			String logo = jsonObject.getString("logo");
			String briefIntro = jsonObject.getString("briefIntro");
			String linkUrl = "https://36kr.com/organization/" + jsonObject.getString("identityName");
			stringBuffer.append(i+1).append("\t").append(name).append("\t").append(linkUrl).append("\t").append(briefIntro);
			FileUtil.writeLines(file, Arrays.asList(stringBuffer.toString()), true);
		}
	}
}
