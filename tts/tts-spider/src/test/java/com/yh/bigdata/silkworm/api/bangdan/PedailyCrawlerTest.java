package com.yh.bigdata.silkworm.api.bangdan;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import com.yh.bigdata.tts.common.utils.FileUtil;

public class PedailyCrawlerTest {
	
	/**
	 * 投资公司
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void PedailyCorpTest() throws ClientProtocolException, IOException, InterruptedException {

		File file = new File("D:\\output_pedaily_corps.csv");
		
		for (int i = 1; i < 980; i++) {
			String content = Request.Get("https://zdb.pedaily.cn/company/p" + i).execute().returnContent().asString();
			System.out.println(content);
			
			Document doc = Jsoup.parse(content);

			Elements lis = doc.select("#company-list li");
			for (int j = 0; j < lis.size(); j++) {
				Element element = lis.get(j);
				try {
					StringBuffer stringBuffer = new StringBuffer();
					String name_simple = element.selectFirst("div.txt h3.title a").text();
					String name_detail = element.selectFirst("div.txt div.f a").text();
					String desc = element.selectFirst("div.txt div.desc")!=null?element.selectFirst("div.txt div.desc").text():"无";
					stringBuffer.append((i -1)  * 24 + (j+1)).append("\t").append(name_simple).append("\t").append(desc);
					FileUtil.writeLines(file, Arrays.asList(stringBuffer.toString()), true);
				} catch (Exception e) {
//					e.printStackTrace();
					System.out.println(element.toString());
				}
			}
			Thread.sleep(100);
		}
		
	}
	

	/**
	 * 投资人
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void PedailyPeopleTest() throws ClientProtocolException, IOException, InterruptedException {

		File file = new File("D:\\output_pedaily_peoples.csv");
		
		for (int i = 1; i < 662; i++) {
			String content = Request.Get("https://zdb.pedaily.cn/people/p" + i).execute().returnContent().asString();
			System.out.println(content);
			
			Document doc = Jsoup.parse(content);

			Elements lis = doc.select("#people-list li");
			for (int j = 0; j < lis.size(); j++) {
				Element element = lis.get(j);
				try {
					StringBuffer stringBuffer = new StringBuffer();
					String name_simple = element.selectFirst("div.txt h3.title a").text();
					String name_corp = element.selectFirst("div.txt div.f")!=null?element.selectFirst("div.txt div.f").text():"无";
					String name_position = element.selectFirst("div.txt div.job")!=null?element.selectFirst("div.txt div.job").text():"无";
					String desc = element.selectFirst("div.txt div.desc")!=null?element.selectFirst("div.txt div.desc").text():"无";
					stringBuffer.append((i -1)  * 24 + (j+1)).append("\t").append(name_simple).append("\t").append(name_corp).append("\t").append(name_position).append("\t").append(desc);
					FileUtil.writeLines(file, Arrays.asList(stringBuffer.toString()), true);
				} catch (Exception e) {
//					e.printStackTrace();
					System.out.println(element.toString());
				}
			}
			Thread.sleep(100);
		}
		
	}
	

	/**
	 * 投资事件
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void PedailyEventTest() throws ClientProtocolException, IOException, InterruptedException {

		File file = new File("D:\\output_pedaily_events.csv");
		
		for (int i = 1; i < 791; i++) {
			String content = Request.Get("https://zdb.pedaily.cn/inv/p" + i).execute().returnContent().asString();
			System.out.println(content);
			
			Document doc = Jsoup.parse(content);

			Elements lis = doc.select("#inv-list li");
			for (int j = 0; j < lis.size(); j++) {
				Element element = lis.get(j);
				try {
					StringBuffer stringBuffer = new StringBuffer();
					String name_simple = element.selectFirst("div.top").text();
					String name_corp = element.selectFirst("div.group")!=null?element.selectFirst("div.group").text():"无";
					String event_date = element.selectFirst("div.date")!=null?element.selectFirst("div.date").text():"无";
					String desc = element.selectFirst("div.info")!=null?element.selectFirst("div.info").text():"无";
					stringBuffer.append((i -1)  * 24 + (j+1)).append("\t").append(name_simple).append("\t").append(name_corp).append("\t").append(event_date).append("\t").append(desc);
					FileUtil.writeLines(file, Arrays.asList(stringBuffer.toString()), true);
				} catch (Exception e) {
//					e.printStackTrace();
					System.out.println(element.toString());
				}
			}
//			Thread.sleep(100);
		}
		
	}
}
