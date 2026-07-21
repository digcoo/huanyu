package com.yh.bigdata.tts.spider.xueqiu;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockMin30Mapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockMin30;
import com.yh.bigdata.tts.common.param.StockPageQuery;
import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.param.TradeConvertHelper;
import com.yh.bigdata.tts.spider.utils.XueQiuHttpUtils;

@Component
@Slf4j
public class StockMin30XueQiuCrawler {

	private static final String PERIOD = "30m";

	@Autowired
	StockBaseMapper stockBaseMapper;

	@Autowired
	StockMin30Mapper stockMin30Mapper;

	@Value("${spider.min30.interval:200}")
	private String sleepMseconds = "200";

	@Value("${spider.min30.startpage:1}")
	private String startPage = "1";

	private volatile boolean persist = true;

	public void setPersist(boolean persist) {
		this.persist = persist;
	}

	/** 盘中仅刷新 RealtimeStockCache 股票池的 min30（不写库） */
	public void refreshRealtimePool(int countX) {
		long start = System.currentTimeMillis();
		log.info("StockMin30XueQiuCrawler refreshRealtimePool start, codes={}",
				RealtimeStockCache.filterStockMap.size());
		boolean prev = persist;
		persist = false;
		try {
			for (StockBase stockBase : RealtimeStockCache.filterStockMap.values()) {
				if (stockBase.getCode().startsWith("sh688")) {
					continue;
				}
				try {
					spider(stockBase, countX);
					Thread.sleep(Long.parseLong(sleepMseconds));
				} catch (Exception e) {
					log.error("refreshRealtimePool exception, code={}", stockBase.getCode(), e);
					if (e instanceof HttpResponseException) {
						try {
							Thread.sleep(20 * 60 * 1000 + 10 * 1000);
							spider(stockBase, countX);
						} catch (Exception retryEx) {
							log.error("refreshRealtimePool retry failed, code={}", stockBase.getCode(), retryEx);
						}
					}
				}
			}
		} finally {
			persist = prev;
		}
		log.info("StockMin30XueQiuCrawler refreshRealtimePool finish({}s)",
				(System.currentTimeMillis() - start) / 1000);
	}

	public void run(String code, int countX) {
		long start = System.currentTimeMillis();
		log.info("StockMin30XueQiuCrawler loop start...");

		int page = Integer.parseInt(startPage);
		int size = 100;
		try {
			while (true) {
				log.info("spider min30 page = {}", page);

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

							if (StringUtils.isBlank(code)) {
								spider(stockBase, countX);
							} else if (stockBase.getCode().equals(code)) {
								spider(stockBase, countX);
								break;
							}
						} catch (Exception e) {
							log.error("StockMin30XueQiuCrawler run exception, stock: {}", JSON.toJSONString(stockBase), e);
							if (e instanceof HttpResponseException) {
								Thread.sleep(20 * 60 * 1000 + 10 * 1000);
								spider(stockBase, countX);
							}
						}
					}
				}

				page++;
				if (page > pages.getPages()) {
					break;
				}
			}
		} catch (Exception e) {
			log.error("StockMin30Crawler exception.....page: {}", page, e);
			System.exit(-1);
		}

		log.info("StockMin30Crawler loop finish({}s)\n\n", (System.currentTimeMillis() - start) / 1000);
	}

	public void spider(StockBase stockBase, int countX) throws ClientProtocolException, IOException, InterruptedException {
		Long time = System.currentTimeMillis();
		int count = -countX;
		if (stockBase.getName().contains("X")) {
			count = -150;
		}

		String url = String.format(XueQiuHttpUtils.base_url, stockBase.getCode().toUpperCase(), time, PERIOD, count);
		String ret = XueQiuHttpUtils.getData(url);
		if (StringUtils.isBlank(ret) || "null".equals(ret)) {
			return;
		}

		List<StockMin30> bars = XueQiuHttpUtils.parseStockTrades(ret, stockBase, StockMin30.class);
		if (CollectionUtils.isEmpty(bars)) {
			return;
		}

		if (!persist) {
			mergeMin30IntoCache(stockBase.getCode(), bars);
			return;
		}

		for (StockMin30 bar : bars) {
			try {
				if (bar.getOpen() == null || bar.getLow() == null || bar.getOpen() < 0.001 || bar.getLow() < 0.001) {
					continue;
				}

				StockMin30 local = stockMin30Mapper.selectByPrimaryKey(bar.getCode(), bar.getDay());
				if (local == null) {
					stockMin30Mapper.insert(bar);
				} else {
					stockMin30Mapper.updateByPrimaryKey(bar);
				}
			} catch (Exception e) {
				if (e instanceof DuplicateKeyException) {
					continue;
				}
				log.error("StockMin30 upsert failed, code={}, day={}", bar.getCode(), bar.getDay(), e);
			}
		}
		refreshMin30Cache(stockBase.getCode());
	}

	private void mergeMin30IntoCache(String code, List<StockMin30> newBars) {
		List<StockMin30> existBars = RealtimeStockCache.min30Map.get(code);
		if (existBars == null) {
			RealtimeStockCache.min30Map.put(code, newBars);
			return;
		}

		Map<String, StockMin30> byDay = existBars.stream()
				.collect(Collectors.toMap(StockMin30::getDay, x -> x, (a, b) -> a));
		for (StockMin30 bar : newBars) {
			if (bar.getOpen() == null || bar.getLow() == null || bar.getOpen() < 0.001 || bar.getLow() < 0.001) {
				continue;
			}
			StockMin30 exist = byDay.get(bar.getDay());
			if (exist != null) {
				exist.replace(bar);
			} else {
				existBars.add(bar);
				byDay.put(bar.getDay(), bar);
			}
		}
		Collections.sort(existBars);
	}

	private void refreshMin30Cache(String code) {
		List<StockMin30> rows = stockMin30Mapper.selectAll(Collections.singletonList(code));
		if (CollectionUtils.isEmpty(rows)) {
			return;
		}
		Map<String, List> map = TradeConvertHelper.parseSortMapList(rows, PeriodTypeEnum.MIN30);
		List cached = map.get(code);
		if (cached != null) {
			RealtimeStockCache.min30Map.put(code, cached);
		}
	}
}
