package com.yh.bigdata.silkworm.api.utils;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.yh.bigdata.silkworm.api.BaseTest;
import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockDayMapper;
import com.yh.bigdata.tts.common.dao.StockMonthMapper;
import com.yh.bigdata.tts.common.dao.StockWeekMapper;
import com.yh.bigdata.tts.common.dto.Box;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockDay;
import com.yh.bigdata.tts.common.model.StockMonth;
import com.yh.bigdata.tts.common.model.StockWeek;
import com.yh.bigdata.tts.common.utils.PriceUtil;

public class PriceUtilsTest extends BaseTest {
	
	@Autowired
	StockMonthMapper stockMonthMapper;
	
	@Autowired
	StockWeekMapper stockWeekMapper;
	
	@Autowired
	StockDayMapper stockDayMapper;

	@Autowired
	StockBaseMapper stockBaseMapper;
	
	
	@Test
	public void getShangZhangQujianForMonth() {
		List<StockMonth> latestMonthList = stockMonthMapper.selectLatestMonthList("sz000421", 30);
		List<Box> shangZhangQujian = PriceUtil.getBoxSegments(latestMonthList, PeriodTypeEnum.MONTH);
		
		for (Box segment : shangZhangQujian) {
			System.out.println(segment.getHeadDay() + "\t" + segment.getTailDay() + "\t" + segment.length());
		}
	}
	
	
	
	@Test
	public void getShangZhangQujianForWeek() {
		String code = "sz000670";
		
		List<StockWeek> latestWeekhList = stockWeekMapper.selectLatestWeekList(code, 100);
		StockBase stockBase = stockBaseMapper.selectByPrimaryKey(code);
		
		
		List<Box> segments = PriceUtil.getBoxSegments(latestWeekhList, PeriodTypeEnum.WEEK);
		for (Box segment : segments) {
			System.out.println(segment.getHeadDay() + "\t" + segment.getTailDay() + "\t" + segment.length());
		}
		
		System.out.println("================================");
		List<Box> shangZhangQujian = PriceUtil.getBoxSegmentsWioutCurrentSegment(segments, latestWeekhList, PeriodTypeEnum.WEEK, 1);
		for (Box segment : shangZhangQujian) {
			System.out.println(segment.getHeadDay() + "\t" + segment.getTailDay() + "\t" + segment.length());
		}
		
//		System.out.println(PriceUtil.getBoxLocation(shangZhangQujian.get(0), stockBase.getTrade()));
	
	}
	
	
	
	@Test
	public void getShangZhangQujianForDay() {
		List<StockDay> latestDayhList = stockDayMapper.selectLatestDayList("sz002590", 50);
		List<Box> segments = PriceUtil.getBoxSegments(latestDayhList, PeriodTypeEnum.DAY);
		
		
		System.out.println("\n\n=================================target=========================");
		for (Box segment : segments) {
			System.out.println(segment.getHeadDay() + "\t" + segment.getTailDay() + "\t" + segment.length());
		}
		
		System.out.println("================================");
		segments = PriceUtil.getBoxSegmentsWioutCurrentSegment(segments, latestDayhList, PeriodTypeEnum.DAY, 1);
		for (Box segment : segments) {
			System.out.println(segment.getHeadDay() + "\t" + segment.getTailDay() + "\t" + segment.length());
		}
		
	}
	
	
	@Test
	public void checkWeek() {

		StockBase stockBase = stockBaseMapper.selectByPrimaryKey("sh603178");
		
		System.out.println(checkQujianTrade(stockBase, PeriodTypeEnum.WEEK, 0.0, 0.1));		//区间位置
	}
	


	//区间位置
	private Boolean checkQujianTrade(StockBase stockBase, PeriodTypeEnum monthOrWeek, Double minThreshold, Double maxThreshold) {
		try {			
			
			List<Box> segments = null;

			if (monthOrWeek == PeriodTypeEnum.MONTH) {
				List<StockMonth> stockMonths = stockMonthMapper.selectLatestMonthList("sh603178", 30);
				segments = PriceUtil.getBoxSegments(stockMonths, PeriodTypeEnum.MONTH);
			}else if (monthOrWeek == PeriodTypeEnum.WEEK) {
				List<StockWeek> stockWeeks = stockWeekMapper.selectLatestWeekList("sz002771", 30);
				segments = PriceUtil.getBoxSegments(stockWeeks, PeriodTypeEnum.WEEK);
			}else if (monthOrWeek == PeriodTypeEnum.DAY) {
				List<StockDay> stockDays = stockDayMapper.selectLatestDayList("sz000980", 30);
				segments = PriceUtil.getBoxSegments(stockDays, PeriodTypeEnum.DAY);
			}
			
			return segments.stream().anyMatch(x -> {
				boolean flag = false;
				if (minThreshold != null){
					flag = stockBase.getTrade() > minThreshold * (x.getHigh() - x.getLow()) + x.getLow();
				}
				
				if (maxThreshold != null) {
					flag = flag && stockBase.getTrade() < maxThreshold * (x.getHigh() - x.getLow()) + x.getLow();
				}
				return flag;
			});
								
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
