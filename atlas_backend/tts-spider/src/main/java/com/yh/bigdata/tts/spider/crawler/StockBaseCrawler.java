package com.yh.bigdata.tts.spider.crawler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.druid.sql.visitor.functions.If;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yh.bigdata.tts.common.constants.SourceTypeEnum;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockDayMapper;
import com.yh.bigdata.tts.common.dao.StockWeekMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockDay;
import com.yh.bigdata.tts.common.model.StockFenshi;
import com.yh.bigdata.tts.common.model.StockWeek;
import com.yh.bigdata.tts.common.utils.DateUtil;
import com.yh.bigdata.tts.spider.service.StockService;
import com.yh.bigdata.tts.spider.utils.SinaHttpUtils;

/**
 * @author duyp
 * 
 * @date 2019/04/12
 * 
 * @comment
 */

@Component
@EnableScheduling
public class StockBaseCrawler {

	Logger logger = LoggerFactory.getLogger(StockBaseCrawler.class);

	String base_url = "http://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?page=%s&num=80&sort=symbol&asc=1&node=%s&symbol=&_s_r_a=sort";

	private final static String realtime_base_url = "http://hq.sinajs.cn/list=%s";
	
	@Autowired
	StockBaseMapper stockBaseMapper;

	@Autowired
	StockDayMapper stockDayMapper;
	
	@Autowired
	StockWeekMapper stockWeekMapper;
	
	@Autowired
	StockService stockService;
	
	@Value("${spider.day.interval}")
	private String sleepMseconds = "1000";

	public void run() {
		long start = System.currentTimeMillis();
		
		logger.info("StockBaseCrawler loop start...");

		try {
			spider(1, "sh_a");

			spider(1, "sz_a");

//			update();
		
		} catch (Exception e) {
			logger.error("StockBaseCrawler exception.....", e);
		}

		logger.info("StockBaseCrawler loop finish, cost = {}s", (System.currentTimeMillis() - start)/1000);
	}

	public void spider(int page, String exchange) throws ClientProtocolException, IOException, InterruptedException {
		String url = String.format(base_url, page, exchange);
		logger.info("page = " + page);
		String ret = Request.Get(url).execute().returnContent().asString();
		if (StringUtils.isNotBlank(ret) && !ret.equals("null") && !ret.equals("[]")) {
			JSONArray parseArray = JSON.parseArray(ret);
			for (int i = 0; i < parseArray.size(); i++) {
				JSONObject jsonObject = parseArray.getJSONObject(i);
				StockBase stockBase = new StockBase(jsonObject.getString("symbol"),
						jsonObject.getString("symbol").substring(0, 2), jsonObject.getString("name"),
						jsonObject.getString("name").toLowerCase().contains("st"), jsonObject.getDouble("trade"));
				stockBase.setLastTrade(jsonObject.getDouble("settlement"));
				stockBase.setOpen(jsonObject.getDouble("open"));
				stockBase.setHigh(jsonObject.getDouble("high"));
				stockBase.setLow(jsonObject.getDouble("low"));
				stockBase.setVolume(jsonObject.getLong("volume"));
				stockBase.setAmount(jsonObject.getDouble("amount"));
				stockBase.setPercent(jsonObject.getDouble("changepercent"));
				stockBase.setTurnoverRate(jsonObject.getDouble("turnoverratio"));
				stockBase.setDay(DateUtil.getCurrentDay());
				
				Boolean isTrade = (jsonObject.getDouble("open") < 1 || jsonObject.getDouble("amount") < 1 || jsonObject.getDouble("volume") < 1 || stockBase.getName().contains("退"))? false : true; 				
				stockBase.setIsTrade(isTrade);
				
//				spiderMainBusiness(stockBase); // 主营业务由 StockCompanyDetailCrawler 全量爬取
				
//				checkIsTrade(stockBase);		//标记是否退市

				if (stockBaseMapper.selectByPrimaryKey(stockBase.getCode()) != null) {
					stockBaseMapper.updateByPrimaryKeySelective(stockBase);
				} else {
					stockBaseMapper.insert(stockBase);
				}
				
//				updateStockDay(stockBase);
//				updateStockWeek(stockBase);
			}

			Thread.sleep(Long.parseLong(sleepMseconds));
			spider(++page, exchange);
		} else {
			return;
		}
	}
	
	public void updateStockDay(StockBase stockBase) {
		String thisDay = DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd");
		
		StockDay stockDay = new StockDay();
		stockDay.setCode(stockBase.getCode());
		stockDay.setName(stockBase.getName());
		stockDay.setTrade(stockBase.getTrade());
		stockDay.setOpen(stockBase.getOpen());
		stockDay.setHigh(stockBase.getHigh());
		stockDay.setLow(stockBase.getLow());
		stockDay.setLastTrade(stockBase.getLastTrade());
		stockDay.setVolume(stockBase.getVolume());
		stockDay.setAmount(stockBase.getAmount());
		stockDay.setDay(thisDay);
		if (stockDayMapper.selectByPrimaryKey(stockBase.getCode(), thisDay) != null) {
			stockDayMapper.updateByPrimaryKey(stockDay);
		} else {
			stockDayMapper.insert(stockDay);
		}
	}
	
	public void updateStockWeek(StockBase stockBase) {
		String thisWeek = DateUtil.getTodayWeek();

		StockWeek localStockWeek = stockWeekMapper.selectByPrimaryKey(stockBase.getCode(), thisWeek);
		if (localStockWeek == null) {
			StockWeek stockWeek = new StockWeek();
			stockWeek.setCode(stockBase.getCode());
			stockWeek.setName(stockBase.getName());
			stockWeek.setTrade(stockBase.getTrade());
			stockWeek.setOpen(stockBase.getOpen());
			stockWeek.setHigh(stockBase.getHigh());
			stockWeek.setLow(stockBase.getLow());
			stockWeek.setLastTrade(stockBase.getLastTrade());
			stockWeek.setVolume(stockBase.getVolume());
			stockWeek.setAmount(stockBase.getAmount());
			stockWeek.setDay(thisWeek);
			stockWeek.setSourceType(SourceTypeEnum.DAY.getCode());
			stockWeekMapper.insert(stockWeek);
		}else {
			localStockWeek.setTrade(stockBase.getTrade());
			localStockWeek.setHigh(stockBase.getHigh() > localStockWeek.getHigh()?stockBase.getHigh():localStockWeek.getHigh());
			localStockWeek.setLow(stockBase.getLow() < localStockWeek.getLow()?stockBase.getLow():localStockWeek.getLow());
			localStockWeek.setCode(stockBase.getCode());
			localStockWeek.setDay(thisWeek);
			stockWeekMapper.updateByPrimaryKey(localStockWeek);
		}
	}
	
	public void spiderMainBusiness(StockBase stockBase) {
		try {
			String url_format = "http://finance.sina.com.cn/realstock/company/%s/nc.shtml";
			String content = Request.Get(String.format(url_format, stockBase.getCode())).execute().returnContent().asString(Charset.forName("gbk"));
			
			Document doc = Jsoup.parse(content);
			Elements ps = doc.select("div.com_overview p");
			String corp_quancheng = ps.get(1).text();
			String corp_main_business = ps.get(3).attr("title") != ""?ps.get(3).attr("title") : ps.get(3).text();
			stockBase.setQuancheng(corp_quancheng);
			stockBase.setMainBusiness(corp_main_business);
		} catch (Exception e) {
			logger.error("spider main business exception...code = " + stockBase.getCode(), e);
		}
	}
	
	public void update() {
		try {
			List<StockBase> stocks = stockBaseMapper.selectAll();
			for (StockBase stockBase : stocks) {
				checkIsTrade(stockBase);
			}
			
		} catch (Exception e) {
			logger.error("udpate StockBase exception... ", e);
		}
	}
	
	
	public void checkIsTrade(StockBase stockBase) {
		
		String url_realtime = String.format(realtime_base_url, stockBase.getCode());

		try {
//			
//			if (stockBase.getCode().equals("sh600209")) {
//				System.out.println("======");
//			}
			
			String ret_realtime = Request.Get(url_realtime)
					.addHeader("Referer", "https://finance.sina.com.cn/realstock/company/sh000001/nc.shtml")			
					.execute().returnContent().asString();
			if (StringUtils.isNotBlank(ret_realtime) && !ret_realtime.equals("null")) {
				
				String[] split = ret_realtime.split("\n");
				for (int i = 0; i < split.length; i++) {
					StockFenshi stockFenshi = SinaHttpUtils.parseStockFenshi(split[i]);
				
					
					stockBase.setName(stockFenshi.getName());
					stockBase.setIsTrade(!stockBase.getName().contains("退") && stockFenshi.getTrade() > 0.1);	
					
					if(!stockBase.getIsTrade())	{
						stockBaseMapper.updateByPrimaryKeySelective(stockBase);
					}
					
				}
			}
				
		} catch (Exception e) {
			logger.error("stock base crawler - checkIsTrade  exception.. url_realtime = " + url_realtime, e);
		}
	}
	
	public static void main(String[] args) {
//		new StockBaseCrawler().run();
		System.out.println(Date.from(LocalDate.now().with(DayOfWeek.FRIDAY).atStartOfDay(ZoneId.systemDefault()).toInstant()));
	}
}
