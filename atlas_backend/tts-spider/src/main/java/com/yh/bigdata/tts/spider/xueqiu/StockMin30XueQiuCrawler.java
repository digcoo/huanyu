package com.yh.bigdata.tts.spider.xueqiu;

import java.io.IOException;
import java.util.List;

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
	}
}
