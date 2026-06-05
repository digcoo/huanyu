package com.yh.bigdata.tts.spider.crawler;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.StockPageQuery;
import com.yh.bigdata.tts.spider.utils.SinaHttpUtils;

/**
 * @author duyp
 * 
 * @date 2019/04/12
 * 
 * @comment  财务数据采集(每股收益、每股净资产)
 */

@Component
public class StockFinanceCrawler {

	Logger logger = LoggerFactory.getLogger(StockFinanceCrawler.class);

//	String base_url = "https://w.sinajs.cn/?list=sh601016";
//	String base_url = "https://w.sinajs.cn/?list=gb_aapl";
	String base_url = "https://w.sinajs.cn/?list=%s";

	@Autowired
	StockBaseMapper stockBaseMapper;

	public void run() {

		long start = System.currentTimeMillis();
		
		try {
			
			logger.info("StockFinanceCrawler loop start=========================================================");
			
			int page = 1;
			int size = 100;
			while (true) {

				PageHelper.startPage(page, size);
				StockPageQuery pageQuery = new StockPageQuery(page, size);
				pageQuery.setIsSelectMode(false);
				
				Page<StockBase> pages = stockBaseMapper.selectByPageQuery(pageQuery);
				
				List<String> codes = Lists.newArrayList();
				pages.forEach(stock -> { 
					codes.add(((StockBase)stock).getCode()+"_i");
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
			logger.error("StockFinanceCrawler exception.....", e);
		}
		
		logger.info("=========================================================StockFinanceCrawler loop finish({}s)",(System.currentTimeMillis() - start)/1000);

	}

	public void spider(String code) throws ClientProtocolException, IOException, InterruptedException {
		
		String url_finance = String.format(base_url, code);
		try {
			String ret_renshi = Request.Get(url_finance).execute().returnContent().asString();
			if (StringUtils.isNotBlank(ret_renshi) && !ret_renshi.equals("null")) {
				
				String[] split = ret_renshi.split("\n");
				for (int i = 0; i < split.length; i++) {
					StockBase stockBase = SinaHttpUtils.parseStockFinance(split[i]);
					logger.info("stock finance " + JSON.toJSONString(stockBase));
					
					stockBaseMapper.updateByPrimaryKeySelective(stockBase);
					
				}
			}
		} catch (Exception e) {
			logger.error("stock finance crawler exception.. url_finance = " + url_finance, e);
		}
		
	}
		
	public static void main(String[] args) {
		new StockFinanceCrawler().run();
	}
}
