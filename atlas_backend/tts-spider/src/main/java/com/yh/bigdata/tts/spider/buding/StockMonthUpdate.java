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
import com.yh.bigdata.tts.common.dao.StockMonthMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockMonth;
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
public class StockMonthUpdate {

	Logger logger = LoggerFactory.getLogger(StockMonthUpdate.class);

	@Autowired
	StockBaseMapper stockBaseMapper;

	@Autowired
	StockMonthMapper stockMonthMapper;

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
                        List<StockMonth> stockDays = stockMonthMapper.selectAll(Arrays.asList(stockBase.getCode()));

                        Map<String, Trade> maMap = MAIndicatorUtils.calAllMAsAndFill(stockBase, stockDays);
                        int from = Math.max(0, stockDays.size() - 80);
                        for (int i = from; i < stockDays.size(); i++) {
                            StockMonth stockDay = stockDays.get(i);
                            Trade trade = maMap.get(stockDay.getDay());
                            if (Objects.nonNull(trade)) {
                                MAIndicatorUtils.copyMaFields(trade, stockDay);
                                stockMonthMapper.updateByPrimaryKey(stockDay);
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
			logger.error("StockDayUpdate exception.....", e);
		}

	}

}
