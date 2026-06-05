package com.yh.bigdata.tts.spider.buding;

import java.util.*;

import com.yh.bigdata.tts.common.indicator.MAIndicatorUtils;
import com.yh.bigdata.tts.common.model.Trade;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockWeekMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockWeek;
import com.yh.bigdata.tts.common.param.StockPageQuery;

/**
 * @author duyp
 * 
 * @date 2019/04/12
 * 
 * @comment
 */

@Component
@Slf4j
public class StockWeekUpdate {

	Logger logger = LoggerFactory.getLogger(StockWeekUpdate.class);

	@Autowired
	StockBaseMapper stockBaseMapper;

	@Autowired
	StockWeekMapper stockWeekMapper;

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
					for (StockBase stockBase : pages) {
                        List<StockWeek> stockDays = stockWeekMapper.selectAll(Arrays.asList(stockBase.getCode()));

                        Map<String, Trade> maMap = MAIndicatorUtils.calAllMAsAndFill(stockBase, stockDays);
                        for (int i = stockDays.size() -1; i >= 0; i--) {
                            StockWeek stockDay = stockDays.get(i);
                            Trade trade = maMap.get(stockDay.getDay());
                            if (Objects.nonNull(trade)) {
                                stockDay.setMa5(trade.getMa5());
                                stockDay.setMa10(trade.getMa10());
                                stockDay.setMa20(trade.getMa20());
                                stockDay.setMa30(trade.getMa30());
                                stockWeekMapper.updateByPrimaryKey(stockDay);
                            }
                            break;
                        }
					}
				}

				page++;

				if (page > pages.getPages()/* || page > 50 */) {
					break;
				}

			}
		} catch (Exception e) {
			logger.error("StockWeekUpdate exception.....", e);
		}

	}
}
