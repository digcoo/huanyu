package com.yh.bigdata.silkworm.api.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.yh.bigdata.silkworm.api.BaseTest;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockMin30;
import com.yh.bigdata.tts.common.model.StockMin60;
import com.yh.bigdata.tts.common.model.StockMonth;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.service.StockService;

/**
 * Created by zhou1 on 2019/1/14.
 */
public class StockServiceTest extends BaseTest{
	
    @Autowired
    StockService stockService;
    
    @Test
    public void test() {
    	List<String> codes = stockService.findAllStocks().subList(0, 2).stream().map(StockBase::getCode).collect(Collectors.toList());
    	List<StockMin30> findAllStockMin30s = stockService.findAllStockMin30s(codes.subList(0, 1));
    	List<StockMin60> findAllStockMin60s = stockService.findAllStockMin60s(codes.subList(0, 1));
    	System.out.println("=====");
    }
}
