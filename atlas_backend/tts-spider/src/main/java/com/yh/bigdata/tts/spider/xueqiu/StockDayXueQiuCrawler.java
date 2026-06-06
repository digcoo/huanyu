package com.yh.bigdata.tts.spider.xueqiu;

import java.io.IOException;
import java.util.*;

import com.yh.bigdata.tts.common.model.Trade;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockDayMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockDay;
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
public class StockDayXueQiuCrawler {

	@Autowired
	StockBaseMapper stockBaseMapper;

	@Autowired
	StockDayMapper stockDayMapper;

	@Value("${spider.day.interval}")
	private String sleepMseconds = "200";

	@Value("${spider.day.startpage}")
	private String startPage = "1";

	public void run(String code, int countX) {

		long start = System.currentTimeMillis();

		log.info("StockDayXueQiuCrawler loop start...");

		int page = Integer.parseInt(startPage);
		int size = 100;
		try {
			while (true) {

                log.info("spider day page = {}", page);

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
							
						} catch (Exception e) {
							log.error("StockDayXueQiuCrawler run exception, stock: {}", JSON.toJSONString(stockBase),
									e);
							if (e instanceof HttpResponseException) {
								Thread.sleep(20 * 60 * 1000 + 10 * 1000);

								spider(stockBase, countX);
							}
						}
					}
				}

				page++;

				if (page > pages.getPages()/* || page > 50 */) {
					break;
				}

			}

		} catch (Exception e) {
			log.error("StockDayCrawler exception.....page: {}", page, e);
			System.exit(-1);
		}

        log.info("StockDayCrawler loop finish({}s)\n\n", (System.currentTimeMillis() - start) / 1000);

	}

	public void spider(StockBase stockBase, int countX) throws ClientProtocolException, IOException, InterruptedException {
//
//		List<String> asList = Arrays.asList("sz001299", "sz301163", "sz301022", "sh600389", "sz301027", "sh605180");
//		if (!asList.contains(stockBase.getCode())) {			
//			return;
//		}
		
		Long time = System.currentTimeMillis();
		
		int count = -countX;

		if (stockBase.getName().contains("X")) {
			count = -150;
		}
		String url_day = String.format(XueQiuHttpUtils.base_url, stockBase.getCode().toUpperCase(), time, "day", count);

		List<StockDay> days = null;

		String ret_day = XueQiuHttpUtils.getData(url_day);

		if (StringUtils.isNotBlank(ret_day) && !ret_day.equals("null")) {
			days = XueQiuHttpUtils.parseStockTrades(ret_day, stockBase, StockDay.class);
		}

//		stockBase.setTrade(days.get(days.size() - 1).getTrade());
//		stockBase.setDay(days.get(days.size() - 1).getDay());
//		
//		stockBaseMapper.updateByPrimaryKeySelective(stockBase);

        for (StockDay stockDay : days) {
			try {

				if (stockDay.getOpen() < 0.001 || stockDay.getLow() < 0.001) {
					continue;
				}

				StockDay localStockDay = stockDayMapper.selectByPrimaryKey(stockDay.getCode(), stockDay.getDay());
				if (localStockDay == null) {
					stockDayMapper.insert(stockDay);
				} else {
					stockDayMapper.updateByPrimaryKey(stockDay);
				}

			} catch (Exception e) {

				if (e instanceof DuplicateKeyException) {
					continue;
				} else {
					e.printStackTrace();
				}
			}
		}
	}


    private Map<String, Trade> spiderMas(StockBase stockBase, int countX) {
        Map<String, Trade> map = new HashMap<>();

        try {

            Thread.sleep(Long.parseLong(sleepMseconds));

            String base_url = "https://quotes.sina.cn/cn/api/jsonp_v2.php=/CN_MarketDataService.getKLineData?symbol=%s&scale=%s&ma=5,10,20,30&datalen=%s";
            String url_ma5 = String.format(base_url, stockBase.getCode(), 86400, countX);

            List<Trade> mas = null;
            String ret_ma5 = Request.Get(url_ma5).execute().returnContent().asString();
            ret_ma5 = ret_ma5.substring(ret_ma5.indexOf("(")+1, ret_ma5.indexOf(")"));
            if (StringUtils.isNotBlank(ret_ma5) && !ret_ma5.equals("null")) {
                mas = JSON.parseArray(ret_ma5, Trade.class);
            }

            for (Trade trade : mas) {
                map.put(DateUtil.parse2YearLastDay(trade.getDay()), trade);
            }

        }catch (Exception e) {
            log.error("StockQuarterXueQiuCrawler spiderMas exception, stock: {}", JSON.toJSONString(stockBase), e);
        }

        return map;
    }
}
