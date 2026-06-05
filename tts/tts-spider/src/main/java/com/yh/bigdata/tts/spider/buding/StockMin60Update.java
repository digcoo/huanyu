package com.yh.bigdata.tts.spider.buding;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockYearMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockYear;
import com.yh.bigdata.tts.common.param.StockPageQuery;

/**
 * @author duyp
 * 
 * @date 2019/04/12
 * 
 * @comment
 */

@Component
public class StockMin60Update {

	Logger logger = LoggerFactory.getLogger(StockMin60Update.class);

	@Autowired
	StockBaseMapper stockBaseMapper;

	@Autowired
	StockYearMapper stockYearMapper;

//	@Scheduled(fixedDelay = 24 * 60 * 60 * 1000, initialDelay = 0)
	public void run() {
		int page = 1;
		int size = 30;
		try {
			while (true) {

				PageHelper.startPage(page, size);				
				
				StockPageQuery pageQuery = new StockPageQuery(page, size);
				pageQuery.setIsSelectMode(false);
				
				Page<StockBase> pages = stockBaseMapper.selectByPageQuery(pageQuery);
				if (!CollectionUtils.isEmpty(pages.getResult())) {
                    StockPageQuery stockDayPageQuery = new StockPageQuery(1, Integer.MAX_VALUE);
					for (StockBase stockBase : pages) {
						stockDayPageQuery.setCode(stockBase.getCode());
						Page<StockYear> stockDays = stockYearMapper.selectByPageQuery(stockDayPageQuery);
						Collections.sort(stockDays);

						for (int i = 1; i < stockDays.size(); i++) {
							stockDays.get(i).setLastTrade(stockDays.get(i - 1).getTrade());
							stockYearMapper.updateByPrimaryKey(stockDays.get(i));
						}
					}
				}

				page++;

				if (page > pages.getPages()/* || page > 50 */) {
					break;
				}

			}
		} catch (Exception e) {
			logger.error("StockDayUpdate exception.....", e);
		}

	}

}
