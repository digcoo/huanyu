package com.yh.bigdata.tts.spider.xueqiu;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.yh.bigdata.tts.common.model.Trade;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yh.bigdata.tts.common.constants.Constants;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockMonthMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockMonth;
import com.yh.bigdata.tts.common.param.StockPageQuery;
import com.yh.bigdata.tts.common.utils.DateUtil;
import com.yh.bigdata.tts.spider.utils.XueQiuHttpUtils;

/**
 * @author duyp
 * 
 * @date 2019/04/12
 * 
 * @comment
 */

@Component
@Slf4j
public class StockMonthXueQiuCrawler {
	
	@Autowired
	StockBaseMapper stockBaseMapper;
	
	@Autowired
	StockMonthMapper stockMonthMapper;
	
	@Value("${spider.day.interval}")
	private String sleepMseconds = "1000";

	@Value("${spider.day.startpage}")
	private String startPage = "1";
	
	public void run(String code, int countX) {
		
		long start = System.currentTimeMillis();
		
		log.info("StockMonthXueQiuCrawler loop start...");

		int page = Integer.parseInt(startPage);
		int size = 100;
		try {
			while (true) {
                log.info("spider month page = {}", page);

                PageHelper.startPage(page, size);
				StockPageQuery pageQuery = new StockPageQuery(page, size);
				pageQuery.setIsSelectMode(false);
				
				Page<StockBase> pages = stockBaseMapper.selectByPageQuery(pageQuery);
				if (!CollectionUtils.isEmpty(pages.getResult())) {
					for (StockBase stockBase : pages) {
						try {
							if (stockBase.getCode().startsWith("sh688")) {
								continue;	
							}
							if(StringUtils.isBlank(code)) {
								
								spider(stockBase, countX);
//								Thread.sleep(Long.parseLong(sleepMseconds));
								
							} else {
								
								if (stockBase.getCode().equals(code)) {
									spider(stockBase, countX);
									break;
								}
								
							}
//							Thread.sleep(Long.parseLong(sleepMseconds));
						} catch (Exception e) {
							log.error("StockMonthXueQiuCrawler run exception, stock: {}", JSON.toJSONString(stockBase), e);
							if (e instanceof HttpResponseException) {
								Thread.sleep(20 * 60 * 1000 + 10 * 1000);
								
								spider(stockBase, countX);
							}
						}
					}
				}
				
				page++;
				
				if (page > pages.getPages()/* || page > 50*/) {
					break;
				}

			}
			
		} catch (Exception e) {
			log.error("StockMonthXueQiuCrawler exception.....page: {}", page, e);
			System.exit(-1);
		}
		
		log.info("StockMonthXueQiuCrawler loop finish({}s)\n\n", (System.currentTimeMillis() - start)/1000);

	}

	public void spider(StockBase stockBase, int countX) throws ClientProtocolException, IOException, InterruptedException {
		
		Long time = System.currentTimeMillis(); 
		
//		String url_day = String.format(base_url, stockBase.getCode().toUpperCase(), time, "month");

		int count = -countX;

		if (stockBase.getName().contains("X")) {
			count = -100;
		}
		String url_day = String.format(XueQiuHttpUtils.base_url, stockBase.getCode().toUpperCase(), time, "month", count);

		
		List<StockMonth> days = null;
		
		String ret_day = XueQiuHttpUtils.getData(url_day);
		
		if (StringUtils.isNotBlank(ret_day) && !ret_day.equals("null")) {
			days = XueQiuHttpUtils.parseStockTrades(ret_day, stockBase, StockMonth.class);
		}
		
//		stockBase.setTrade(days.get(days.size() - 1).getTrade());
//		stockBase.setDay(days.get(days.size() - 1).getDay());
//		
//		stockBaseMapper.updateByPrimaryKeySelective(stockBase);

        for (StockMonth stockMonth : days) {
			try {
				
				if (stockMonth.getOpen() < 0.001 || stockMonth.getLow() < 0.001) {
					continue;
				}
				
//				if (DateUtil.isSameMonth(stockMonth.getDay())) {
					stockMonth.setDay(DateUtil.parse2MonthLastDay(stockMonth.getDay()));
//				}
				
//				logger.info("spider month : " + JSON.toJSONString(stockMonth));

				StockMonth localStockDay = stockMonthMapper.selectByPrimaryKey(stockMonth.getCode(), stockMonth.getDay());
				if (localStockDay == null) {
					stockMonthMapper.insert(stockMonth);
				}else {
					stockMonthMapper.updateByPrimaryKey(stockMonth);
				}
				
			} catch (Exception e) {
				
				if (e instanceof DuplicateKeyException) {
					continue;
				}else {
                    log.error("stock month crawler exception.. url_day = {}", url_day, e);
				}
			}
		}
	}


    private Map<String, Trade> spiderMas(StockBase stockBase, int countX) {
        Map<String, Trade> map = new HashMap<>();

        try {

            Thread.sleep(Long.parseLong(sleepMseconds));

            String base_url = "https://quotes.sina.cn/cn/api/jsonp_v2.php=/CN_MarketDataService.getKLineData?symbol=%s&scale=%s&ma=5,10,20,30&datalen=%s";
            String url_ma5 = String.format(base_url, stockBase.getCode(), 7200, countX);

            List<Trade> mas = null;
            String ret_ma5 = Request.Get(url_ma5).execute().returnContent().asString();
            ret_ma5 = ret_ma5.substring(ret_ma5.indexOf("(")+1, ret_ma5.indexOf(")"));
            if (StringUtils.isNotBlank(ret_ma5) && !ret_ma5.equals("null")) {
                mas = JSON.parseArray(ret_ma5, Trade.class);
            }

            for (Trade trade : mas) {
                map.put(DateUtil.parse2MonthLastDay(trade.getDay()), trade);
            }

        }catch (Exception e) {
            log.error("StockMonthXueQiuCrawler spiderMas exception, stock: {}", JSON.toJSONString(stockBase), e);
        }

        return map;
    }
}
