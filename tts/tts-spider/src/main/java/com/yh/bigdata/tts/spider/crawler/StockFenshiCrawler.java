package com.yh.bigdata.tts.spider.crawler;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.yh.bigdata.tts.common.constants.SourceTypeEnum;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockDayMapper;
import com.yh.bigdata.tts.common.dao.StockMonthMapper;
import com.yh.bigdata.tts.common.dao.StockWeekMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockDay;
import com.yh.bigdata.tts.common.model.StockFenshi;
import com.yh.bigdata.tts.common.model.StockMonth;
import com.yh.bigdata.tts.common.model.StockWeek;
import com.yh.bigdata.tts.common.param.StockPageQuery;
import com.yh.bigdata.tts.common.utils.DateUtil;
import com.yh.bigdata.tts.spider.utils.SinaHttpUtils;

/**
 * @author duyp
 * 
 * @date 2019/04/12
 * 
 * @comment
 */

@Component
public class StockFenshiCrawler {

	Logger logger = LoggerFactory.getLogger(StockFenshiCrawler.class);

//	String base_url = "http://hq.sinajs.cn/list=sh600007";
	private final static String base_url = "http://hq.sinajs.cn/list=%s";

	@Autowired
	StockBaseMapper stockBaseMapper;

	@Autowired
	StockDayMapper stockDayMapper;
	
	@Autowired
	StockWeekMapper stockWeekMapper;

	@Autowired
	StockMonthMapper stockMonthMapper;

	public void run() {

		long start = System.currentTimeMillis();
		
		try {
			
			logger.info("StockFenshiCrawler loop start=========================================================");
			
			int page = 1;
			int size = 100;
			while (true) {

				PageHelper.startPage(page, size);
				StockPageQuery pageQuery = new StockPageQuery(page, size);
				pageQuery.setIsSelectMode(false);
				
				Page<StockBase> pages = stockBaseMapper.selectByPageQuery(pageQuery);
				
				List<String> codes = Lists.newArrayList();
				pages.forEach(stock -> { 
					codes.add(((StockBase)stock).getCode());
					});
				
				String codestr = JSON.toJSONString(codes);
				spider(codestr.substring(1, codestr.length() - 1).replace("\"", ""));
				
				page++;
				
				if (page > pages.getPages()) {
					break;
				}
				
				logger.info("page  = " + page);
			}
			
		} catch (Exception e) {
			logger.error("StockFenshiCrawler exception.....", e);
		}
		
		logger.info("=========================================================StockFenshiCrawler loop finish({}s)",(System.currentTimeMillis() - start)/1000);

	}

	public void spider(String code) throws ClientProtocolException, IOException, InterruptedException {
		
		String url_fenshi = String.format(base_url, code);
		try {
			String ret_renshi = Request.Get(url_fenshi)
					.addHeader("Referer", "https://finance.sina.com.cn/realstock/company/sz002015/nc.shtml")			
					.execute().returnContent().asString();
			if (StringUtils.isNotBlank(ret_renshi) && !ret_renshi.equals("null")) {
				
				String[] split = ret_renshi.split("\n");
				for (int i = 0; i < split.length; i++) {
					StockFenshi stockFenshi = SinaHttpUtils.parseStockFenshi(split[i]);
					logger.info("stock fenshi " + JSON.toJSONString(stockFenshi));
					
					if (!stockFenshi.getDay().equals(DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd"))) {
						continue;
					}
					
					StockBase stockBase = new StockBase();
					stockBase.setName(stockFenshi.getName());
					stockBase.setCode(stockFenshi.getCode());
					stockBase.setOpen(stockFenshi.getOpen());
					stockBase.setTrade(stockFenshi.getTrade());
					stockBase.setDay(stockFenshi.getDay());
					stockBase.setLastTrade(stockFenshi.getLastTrade());
					stockBase.setHigh(stockFenshi.getHigh());
					stockBase.setLow(stockFenshi.getLow());
					stockBase.setAmount(stockFenshi.getAmount());
					stockBase.setVolume(stockFenshi.getVolume());
					stockBase.setIsTrade(stockFenshi.getOpen() < 0.1 || stockBase.getName().contains("退")? false : true);
					
//					stockBase.setBuy1Price(stockFenshi.getBuy1Price());
//					stockBase.setBuy1Volume(stockFenshi.getBuy1Volume());
//					stockBase.setBuy2Price(stockFenshi.getBuy2Price());
//					stockBase.setBuy2Volume(stockFenshi.getBuy2Volume());
//					stockBase.setBuy3Price(stockFenshi.getBuy3Price());
//					stockBase.setBuy3Volume(stockFenshi.getBuy3Volume());
//					stockBase.setBuy4Price(stockFenshi.getBuy4Price());
//					stockBase.setBuy4Volume(stockFenshi.getBuy4Volume());
//					stockBase.setBuy5Price(stockFenshi.getBuy5Price());
//					stockBase.setBuy5Volume(stockFenshi.getBuy5Volume());
//					stockBase.setSell1Price(stockFenshi.getSell1Price());
//					stockBase.setSell1Volume(stockFenshi.getSell1Volume());
//					stockBase.setSell2Price(stockFenshi.getSell2Price());
//					stockBase.setSell2Volume(stockFenshi.getSell2Volume());
//					stockBase.setSell3Price(stockFenshi.getSell3Price());
//					stockBase.setSell3Volume(stockFenshi.getSell3Volume());
//					stockBase.setSell4Price(stockFenshi.getSell4Price());
//					stockBase.setSell4Volume(stockFenshi.getSell4Volume());
//					stockBase.setSell5Price(stockFenshi.getSell5Price());
//					stockBase.setSell5Volume(stockFenshi.getSell5Volume());
					
					stockBaseMapper.updateByPrimaryKeySelective(stockBase);
					
					updateStockDay(stockBase);
					updateStockWeek(stockBase);
					updateStockMonth(stockBase);					

//					//只记录当天15:00时的数据
//					if (stockFenshi.getTime().compareTo("15:00:00") >= 0) {
//						stockFenshi.setTime("15:00:00");
//						if (CollectionUtils.isEmpty(stockFenshiMapper.selectByCodeAndDay(stockFenshi.getCode(), stockFenshi.getDay()))) {
//							stockFenshiMapper.insert(stockFenshi);
//						}else {
//							stockFenshiMapper.updateByPrimaryKey(stockFenshi);
//						}
//					}
				}
			}
		} catch (Exception e) {
			logger.error("stock fenshi crawler exception.. url_fenshi = " + url_fenshi, e);
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
			stockWeek.setSourceType(SourceTypeEnum.MIN.getCode());
			stockWeekMapper.insert(stockWeek);
		}else {
//			if (SourceTypeEnum.WEEK.getCode() == localStockWeek.getSourceType()) {
				localStockWeek.setTrade(stockBase.getTrade());
				localStockWeek.setHigh(stockBase.getHigh() > localStockWeek.getHigh()?stockBase.getHigh():localStockWeek.getHigh());
				localStockWeek.setLow(stockBase.getLow() < localStockWeek.getLow()?stockBase.getLow():localStockWeek.getLow());
				localStockWeek.setCode(stockBase.getCode());
				localStockWeek.setDay(thisWeek);
				stockWeekMapper.updateByPrimaryKey(localStockWeek);
//			}else {
//				localStockWeek.setCode(stockBase.getCode());
//				localStockWeek.setTrade(stockBase.getTrade());
//				localStockWeek.setOpen(stockBase.getOpen());
//				localStockWeek.setHigh(stockBase.getHigh());
//				localStockWeek.setLow(stockBase.getLow());
//				localStockWeek.setLastTrade(stockBase.getLastTrade());
//				localStockWeek.setVolume(stockBase.getVolume());
//				localStockWeek.setAmount(stockBase.getAmount());
//				stockWeekMapper.updateByPrimaryKey(localStockWeek);
//			}
		}
	}

	public void updateStockMonth(StockBase stockBase) throws ParseException {
		
		String thisMonth = DateUtil.getMonthLastDay();

		StockMonth localStockMonth = stockMonthMapper.selectByPrimaryKey(stockBase.getCode(), thisMonth);
		if (localStockMonth == null) {
			StockMonth stockMonth = new StockMonth();
			stockMonth.setCode(stockBase.getCode());
			stockMonth.setName(stockBase.getName());
			stockMonth.setTrade(stockBase.getTrade());
			stockMonth.setOpen(stockBase.getOpen());
			stockMonth.setHigh(stockBase.getHigh());
			stockMonth.setLow(stockBase.getLow());
			stockMonth.setLastTrade(stockBase.getLastTrade());
			stockMonth.setVolume(stockBase.getVolume());
			stockMonth.setAmount(stockBase.getAmount());
			stockMonth.setDay(thisMonth);
			stockMonth.setSourceType(SourceTypeEnum.MIN.getCode());

//			parseStockMonthMA(stockBase, stockMonth);
			
			stockMonthMapper.insert(stockMonth);
		}else {
			localStockMonth.setTrade(stockBase.getTrade());
			localStockMonth.setHigh(stockBase.getHigh() > localStockMonth.getHigh()?stockBase.getHigh():localStockMonth.getHigh());
			localStockMonth.setLow(stockBase.getLow() < localStockMonth.getLow()?stockBase.getLow():localStockMonth.getLow());
			localStockMonth.setCode(stockBase.getCode());
			localStockMonth.setDay(thisMonth);
			
//			parseStockMonthMA(stockBase, localStockMonth);
			
			stockMonthMapper.updateByPrimaryKey(localStockMonth);
		}
	}
	

	public static void main(String[] args) throws ClientProtocolException, IOException {
//		new StockFenshiCrawler().run();
		String url_fenshi = String.format(base_url, "sz002015");
		String ret_renshi = Request.Get(url_fenshi)
				.addHeader("Referer", "https://finance.sina.com.cn/realstock/company/sz002015/nc.shtml")			
				.execute().returnContent().asString();
		System.out.println(ret_renshi);
	}
}
