package com.yh.bigdata.tts.common.indicator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.yh.bigdata.tts.common.model.Trade;
import lombok.Data;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MACDIndicatorUtils {
	
	 final static Logger logger = LoggerFactory.getLogger(MACDIndicatorUtils.class);

    public static List<MACDPoint> calculateMACD(List<Ticker> tickers) {
        List<Double> closes = tickers.stream().map(Ticker::getClose).collect(Collectors.toList());
        return calculateMACD(tickers, closes);
    }

    // 计算 EMA 指数移动平均线
    private static double calculateEMA(double previousEMA, double currentPrice, int period) {
        double multiplier = 2.0 / (period + 1);
        return (currentPrice - previousEMA) * multiplier + previousEMA;
    }

    // 计算 MACD 指标
    private static List<MACDPoint> calculateMACD(List<Ticker> tickers, List<Double> closes) {
        if (closes == null || closes.size() < 10) {
//            throw new IllegalArgumentException("数据点不足，至少需要26个数据点来计算 MACD");
            return Collections.emptyList();
        }

        List<MACDPoint> macdPoints = new ArrayList<>(closes.size());

        double ema12 = closes.get(0);
        double ema26 = closes.get(0);
        double dea = 0;

        for (int i = 0; i < closes.size(); i++) {
            double price = closes.get(i);

            // 计算 12 日 EMA 和 26 日 EMA
            if (i == 0) {
                ema12 = price;
                ema26 = price;
            } else {
                ema12 = calculateEMA(ema12, price, 12);
                ema26 = calculateEMA(ema26, price, 26);
            }

            double dif = ema12 - ema26; // 计算 DIF
            dea = calculateEMA(dea, dif, 9); // 计算 DEA
            double macd = 2 * (dif - dea); // 计算 MACD 柱状图
            
            MACDPoint macdPoint = new MACDPoint(tickers.get(i), dif, dea, macd, false, false, false);
            if (macdPoint.getMacd() > 0) {
            	macdPoint.setIfOver(true);
			}
            
            if (i > 0) {
            	if (macdPoints.get(i - 1).getMacd() < 0 && macd > 0) {
                	macdPoint.setIfGoldCross(true);
                	macdPoint.setIfGreenGoldCross(false);
				}

            	if (macdPoints.get(i - 1).getMacd() > 0 && macd < 0) {
                	macdPoint.setIfGoldCross(true);
                	macdPoint.setIfGreenGoldCross(true);
				}
			}
            macdPoints.add(macdPoint);
        }

        return macdPoints;
    }
    
    public static Pair<MACDPoint, MACDPoint> getLatestRedGlodMACDPoint(List<MACDPoint> MACDPoints) {
		MACDPoint leftLatestRedGoldMACDPoint = null;
		MACDPoint righLatestRedGoldMACDPoint = null;
		for (int i = MACDPoints.size() - 1; i > 0; i--) {
			MACDPoint macdPoint = MACDPoints.get(i);
            MACDPoint macdPoint1 = MACDPoints.get(i - 1);
			if (macdPoint.isIfRedGoldCross()) {
				leftLatestRedGoldMACDPoint = macdPoint;
				righLatestRedGoldMACDPoint = macdPoint;
				
				//往前推3个，确保有效的黄金位
				int offset = 3;
				for(int k = i-1; k >= i - 1 - offset && k >= 0; k--) {
					MACDPoint kMacdPoint = MACDPoints.get(k);
					if (kMacdPoint.getTicker().getClose() >= kMacdPoint.getTicker().getOpen()
							&& kMacdPoint.getTicker().getHigh() >= righLatestRedGoldMACDPoint.getTicker().getHigh()
							) {
						righLatestRedGoldMACDPoint = MACDPoints.get(k);
					}
				}
                leftLatestRedGoldMACDPoint.setPreTicker(macdPoint1.getTicker());
//		        logger.info("redGold: l: {}, r: {}", leftLatestRedGoldMACDPoint.getTimestampStr(), righLatestRedGoldMACDPoint.getTimestampStr());
				
				break;
			}
		}
		
		if (leftLatestRedGoldMACDPoint != null) {
	    	return Pair.of(leftLatestRedGoldMACDPoint, righLatestRedGoldMACDPoint);
		} else {
			return null;
		}
		
    }
    

    public static Pair<MACDPoint, MACDPoint> getLatestGreenGlodMACDPoint(List<MACDPoint> MACDPoints) {
		MACDPoint leftLatestGreenGoldMACDPoint = null;
		MACDPoint righLatestGreenGoldMACDPoint = null;
		
		for (int i = MACDPoints.size() - 1; i > 0; i--) {
			MACDPoint macdPoint = MACDPoints.get(i);
			if (macdPoint.isIfGreenGoldCross()) {
				leftLatestGreenGoldMACDPoint = macdPoint;
				righLatestGreenGoldMACDPoint = macdPoint;

				//往前推3个，确保有效的黄金位
				int offset = 3;
				for(int k = i-1; k >= i - 1 - offset && k >= 0; k--) {
					MACDPoint kMacdPoint = MACDPoints.get(k);
					if (kMacdPoint.getTicker().getClose() <= kMacdPoint.getTicker().getOpen()
							&& kMacdPoint.getTicker().getLow() <= righLatestGreenGoldMACDPoint.getTicker().getLow()
							) {
						righLatestGreenGoldMACDPoint = MACDPoints.get(k);
					}
				}
				
//		        logger.info("redGold: l: {}, r: {}", leftLatestGreenGoldMACDPoint.getTimestampStr(), righLatestGreenGoldMACDPoint.getTimestampStr());
				
				break;
			}
		}
		
		if (leftLatestGreenGoldMACDPoint != null) {
	    	return Pair.of(leftLatestGreenGoldMACDPoint, righLatestGreenGoldMACDPoint);
		} else {
			return null;
		}
    }
    


    public static Pair<MACDPoint, MACDPoint> getLatestGlodMACDPoint(List<MACDPoint> macdPoints) {
    	Pair<MACDPoint, MACDPoint> latestRedGlodMACDPoint = getLatestRedGlodMACDPoint(macdPoints);
    	Pair<MACDPoint, MACDPoint> latestGreenGlodMACDPoint = getLatestGreenGlodMACDPoint(macdPoints);
    	if (latestRedGlodMACDPoint != null && latestGreenGlodMACDPoint != null) {
    		return latestRedGlodMACDPoint.getLeft().getTicker().getTimestamp() > latestGreenGlodMACDPoint.getLeft().getTicker().getTimestamp()?
    				latestRedGlodMACDPoint : latestGreenGlodMACDPoint;
		}else if (latestRedGlodMACDPoint != null) {
			return latestRedGlodMACDPoint;
		}else {
			return latestGreenGlodMACDPoint;
		}
    }
    
    
//    public static boolean isOverRedAndGreenGoldMACD(List<MACDPoint> MACDPoints) {
//    	Pair<MACDPoint, MACDPoint> latestRedGlodMACDPoint = getLatestRedGlodMACDPoint(MACDPoints);
//    	Pair<MACDPoint, MACDPoint> latestGreenGlodMACDPoint = getLatestGreenGlodMACDPoint(MACDPoints);
//		MACDPoint macdPoint0 = MACDPoints.get(MACDPoints.size() - 1);
//		if (latestRedGlodMACDPoint == null && latestGreenGlodMACDPoint == null) {
//			return false;
//		}
//		
//		if (macdPoint0.getTicker().getClose() >= latestRedGlodMACDPoint.getTicker().getHigh()
//				&& macdPoint0.getTicker().getClose() >= latestGreenGlodMACDPoint.getTicker().getHigh()
//				) {
//			
//			return true;
//
//		}
//    	
//    	return false;
//    }
    
    
    /**
     * 是否已突破绿黄金High
     * @param macdPoints
     * @return
     */
    public static boolean isTryUpCrossGoldHigh(List<MACDPoint> macdPoints, boolean green) {
    	MACDPoint macdPoint0 = macdPoints.get(macdPoints.size() - 1);
    	MACDPoint latestGlodMACDPoint = null;
    	if (green) {
    		latestGlodMACDPoint = getLatestGreenGlodMACDPoint(macdPoints).getLeft();
		}else {
			latestGlodMACDPoint = getLatestRedGlodMACDPoint(macdPoints).getLeft();
		}

    	if (latestGlodMACDPoint != null
//    			&& macdPoint1.getMacd() < 0
    			) {
    		
    		//当前周期之前是否有突破HIGH
			boolean hasUpCrossHigh = false;
			
			for (int i = macdPoints.size() - 2; i >= 0; i--) {
				MACDPoint tmpMacdPoint = macdPoints.get(i);
				if (tmpMacdPoint.getTicker().getTimestamp() > latestGlodMACDPoint.getTicker().getTimestamp()
						
						&& tmpMacdPoint.getTicker().getHigh() >= latestGlodMACDPoint.getTicker().getHigh()
													
						) {
					hasUpCrossHigh = true;
					break;
				}
				
			}
			
			if (hasUpCrossHigh
					&& macdPoint0.getTicker().getClose() > latestGlodMACDPoint.getTicker().getLow()
					) {
				return true;
			}
    		
		}
    	
        return false;
    }
    
    /**
     * 是否已突破绿黄金High
     * @param macdPoints
     * @return
     */
    public static boolean isTryUpCrossGoldLow(List<MACDPoint> macdPoints, boolean green) {
    	MACDPoint macdPoint0 = macdPoints.get(macdPoints.size() - 1);
    	MACDPoint latestGlodMACDPoint = null;
    	if (green) {
    		latestGlodMACDPoint = getLatestGreenGlodMACDPoint(macdPoints).getLeft();
		}else {
			latestGlodMACDPoint = getLatestRedGlodMACDPoint(macdPoints).getLeft();
		}

    	if (latestGlodMACDPoint != null
//    			&& macdPoint1.getMacd() < 0
    			) {
    		
    		//当前周期之前是否有突破Low
			boolean hasUpCrossLow = false;
			
			for (int i = macdPoints.size() - 2; i >= 0; i--) {
				MACDPoint tmpMacdPoint = macdPoints.get(i);
				if (tmpMacdPoint.getTicker().getTimestamp() > latestGlodMACDPoint.getTicker().getTimestamp()
						
						&& tmpMacdPoint.getTicker().getHigh() > latestGlodMACDPoint.getTicker().getLow()
													
						) {
					hasUpCrossLow = true;
					break;
				}
				
			}
			
			if (hasUpCrossLow
					&& macdPoint0.getTicker().getClose() >= latestGlodMACDPoint.getTicker().getLow()
					) {
				return true;
			}
    		
		}
    	
        return false;
    }
    
    
    /**
     * 是否已突破绿黄金High
     * @param macdPoints
     * @return
     */
    public static boolean isTryDownCrossGoldLow(List<MACDPoint> macdPoints, boolean green) {
    	MACDPoint macdPoint0 = macdPoints.get(macdPoints.size() - 1);
    	MACDPoint latestGlodMACDPoint = null;
    	if (green) {
    		latestGlodMACDPoint = getLatestGreenGlodMACDPoint(macdPoints).getLeft();
		}else {
			latestGlodMACDPoint = getLatestRedGlodMACDPoint(macdPoints).getLeft();
		}

    	if (latestGlodMACDPoint != null
//    			&& macdPoint1.getMacd() < 0
    			) {
    		
    		//当前周期之前是否有突破HIGH
			boolean hasDownCrossLow = false;
			
			for (int i = macdPoints.size() - 2; i >= 0; i--) {
				MACDPoint tmpMacdPoint = macdPoints.get(i);
				if (tmpMacdPoint.getTicker().getTimestamp() > latestGlodMACDPoint.getTicker().getTimestamp()
						
						&& tmpMacdPoint.getTicker().getClose() <= latestGlodMACDPoint.getTicker().getLow()
													
						) {
					hasDownCrossLow = true;
					break;
				}
			}
			
			if (hasDownCrossLow
					&& macdPoint0.getTicker().getClose() > latestGlodMACDPoint.getTicker().getLow()
					) {
				return true;
			}
    		
		}
    	
        return false;
    }


    @Data
    public static class MACDPoint {
        private double dif;
        private double dea;
        private double macd;
        private boolean ifOver;
        private boolean ifGoldCross;
        private boolean ifGreenGoldCross;
        private Ticker ticker;
        private Ticker preTicker;
        
        public MACDPoint(Ticker ticker, double dif, double dea, double macd, boolean ifOver, boolean ifGoldCross, boolean ifGreenGoldCross) {
			super();
			this.ticker = ticker;
			this.dif = dif;
			this.dea = dea;
			this.macd = macd;
			this.ifOver = ifOver;
			this.ifGoldCross = ifGoldCross;
			this.ifGreenGoldCross = ifGreenGoldCross;
		}


		public boolean isIfGreenGoldCross() {
			return ifGoldCross && ifGreenGoldCross;
		}
		
		public boolean isIfRedGoldCross() {
			return ifGoldCross && !ifGreenGoldCross;
		}

		private String getTimestampStr() {
            return DateFormatUtils.format(this.ticker.getTimestamp(), "yyyy-MM-dd");
        }
    }

    public static void main(String[] args) throws IOException {

        List<Trade> tickers = XueQiuUtils.getXueQiuJson("sz300678", "day");
        List<MACDPoint> macdPoints = calculateMACD(Ticker.from(tickers));
        for (int i = 0; i < macdPoints.size(); i++) {
            if (macdPoints.get(i).ifGoldCross) {
                System.out.println("MACD 黄金位，发生在" + macdPoints.get(i).getTimestampStr());
            }
        }
        
		System.out.println(tickers.stream().map(Trade::getClose).collect(Collectors.toList()));
		

//        for (int i = 0; i < macdPoints.size(); i++) {
////        	System.out.println("MACD: " + macdPoints.get(i).getTimestampStr() + "\t" + macdPoints.get(i).getDif() + "\t" + macdPoints.get(i).getDea() +  "\t" + macdPoints.get(i).getMacd());
//            if (macdPoints.get(i).ifGoldCross) {
//                System.out.println("MACD 红色柱，发生在" + macdPoints.get(i).getTimestampStr());
//            }
//        }
        
//        Pair<MACDPoint, MACDPoint> latestRedGlodMACDPoint = getLatestRedGlodMACDPoint(macdPoints);
        Pair<MACDPoint, MACDPoint> latestGreenGlodMACDPoint = getLatestGreenGlodMACDPoint(macdPoints);
        
//        logger.info("redGold: l: {}, r: {}", latestRedGlodMACDPoint.getLeft().getTimestampStr(), latestRedGlodMACDPoint.getRight().getTimestampStr());
        
        logger.info("greenGold: l: {}, r: {}", latestGreenGlodMACDPoint.getLeft().getTimestampStr(), latestGreenGlodMACDPoint.getRight().getTimestampStr());

   
    }
}
