package com.yh.bigdata.tts.spider.xueqiu;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.yh.bigdata.tts.common.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockMin30Mapper;
import com.yh.bigdata.tts.common.param.StockPageQuery;
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
public class StockMin30XueQiuCrawler {
	
	@Autowired
	StockBaseMapper stockBaseMapper;
	
	@Autowired
	StockMin30Mapper stockMin30Mapper;
	
	@Value("${spider.day.interval}")
	private String sleepMseconds = "1000";

	@Value("${spider.day.startpage}")
	private String startPage = "1";

	private boolean persist = true;
	
	public void run(String code, int countX) {
		
		long start = System.currentTimeMillis();
		
		log.info("StockMin30XueQiuCrawler loop start...");

		int page = Integer.parseInt(startPage);
		int size = 100;
		try {
			while (true) {
				
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
							log.error("StockMin30XueQiuCrawler run exception, stock = " + JSON.toJSONString(stockBase), e);
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
				
				log.info("page = " + page);

			}
			
		} catch (Exception e) {
			log.error("StockMin30XueQiuCrawler exception.....page = " + page, e);
			System.exit(-1);
		}
		
		log.info("StockMin30XueQiuCrawler loop finish({}s)\n\n",(System.currentTimeMillis() - start)/1000);

	}

    public void spider(StockBase stockBase, int countX) throws ClientProtocolException, IOException, InterruptedException {
		
		Long time = System.currentTimeMillis();
        int count = -countX;

        if (stockBase.getName().contains("X")) {
            count = -150;
        }

		String url_day = String.format(XueQiuHttpUtils.base_url, stockBase.getCode().toUpperCase(), time, "30m", count);

		
		List<StockMin30> newMin30s = null;
		
		String ret_day = XueQiuHttpUtils.getData(url_day);
		
		if (StringUtils.isNotBlank(ret_day) && !ret_day.equals("null")) {
            newMin30s = XueQiuHttpUtils.parseStockTrades(ret_day, stockBase, StockMin30.class);
		}
//		
//		stockBase.setTrade(days.get(days.size() - 1).getTrade());
//		stockBase.setDay(days.get(days.size() - 1).getDay());
//		
//		stockBaseMapper.updateByPrimaryKeySelective(stockBase);
		
		//覆盖已存在的数据
		if (!persist) {
			
			List<StockMin30> existMin30s = RealtimeStockCache.min30Map.get(stockBase.getCode());
			if (existMin30s == null) {
				RealtimeStockCache.min30Map.put(stockBase.getCode(), newMin30s);
			} else {
				Map<String, StockMin30> existMin30Map = existMin30s.stream().collect(Collectors.toMap(
								x -> x.getDay(),
								y -> y, 
								(key1, key2) -> key1));
				
				for (StockMin30 newMin30 : newMin30s) {
					StockMin30 existMin30 = existMin30Map.get(newMin30.getDay());
					if(existMin30 != null) {//覆盖
						existMin30.replace(newMin30);
					} else {
						existMin30s.add(newMin30);
					}
				}
			}

//			if (stockBase.getCode().equals("sz301141")) {
//				List<StockMin30> min30s = RealtimeStockCache.min30Map.get(stockBase.getCode());
//				System.out.println(JSON.toJSONString(min30s.subList(min30s.size() - 5, min30s.size())));
//			}
			
			return;
		}


//        Map<String, Trade> maMap = MAIndicator.calAllMas(stockBase, newMin30s);
//        StockMin30 stockMin30_0 = newMin30s.get(newMin30s.size() - 1);
		for (int i = 0; i < newMin30s.size(); i++) {
			
			try {
				StockMin30 stockMin30 = newMin30s.get(i);
				
				if (stockMin30.getOpen() < 0.001 || stockMin30.getLow() < 0.001) {
					continue;
				}
				
//				logger.info("spider min30 : " + JSON.toJSONString(stockMin30));
//                Trade trade = maMap.get(stockMin30.getDay());
//                if (Objects.nonNull(trade) && stockMin30_0.getDay().equals(stockMin30.getDay())) {   //仅计算当日的MA
//                    stockMin30.setMa5(trade.getMa5());
//                    stockMin30.setMa10(trade.getMa10());
//                    stockMin30.setMa20(trade.getMa20());
//                    stockMin30.setMa30(trade.getMa30());
//                }
				
				StockMin30 localStockDay = stockMin30Mapper.selectByPrimaryKey(stockMin30.getCode(), stockMin30.getDay());
				if (localStockDay == null) {
					stockMin30Mapper.insert(stockMin30);
				}else {
					stockMin30Mapper.updateByPrimaryKey(stockMin30);
				}
				
			} catch (Exception e) {
				
				if (e instanceof DuplicateKeyException) {
					continue;
				}else {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
	public void setPersist(Boolean persist) {
		this.persist = persist;
	}
}
