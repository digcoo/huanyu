
package com.yh.bigdata.tts.common.indicator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.yh.bigdata.tts.common.model.Trade;
import org.apache.commons.lang3.time.DateFormatUtils;

import com.google.common.collect.Lists;

public final class KDJIndicatorUtils {

    public static List<KDJPoint> calculateKDJ(List<Ticker> tickers) {
        List<Double> closes = tickers.stream().map(Ticker::getClose).collect(Collectors.toList());
        List<Double> highs = tickers.stream().map(Ticker::getHigh).collect(Collectors.toList());
        List<Double> lows = tickers.stream().map(Ticker::getLow).collect(Collectors.toList());
        return calculateKDJ(tickers, closes, highs, lows, 9, 3, 3);
    }
    
    
    public static KDJPoint getLatestGlodKDJPoint(List<KDJPoint> KDJPoints) {
    	KDJPoint latestGoldKDJPoint = null;
		for (int i = KDJPoints.size() - 1; i > 0; i--) {
			KDJPoint kdjPoint = KDJPoints.get(i);
			if (kdjPoint.isIfGoldCross()) {
				latestGoldKDJPoint = kdjPoint;
				break;
			}
		}
    	return latestGoldKDJPoint;
    }

    // KDJ计算函数
    private static List<KDJPoint> calculateKDJ(List<Ticker> timestamps, List<Double> closes, List<Double> highs, List<Double> lows, int n, int m1, int m2) {
        if (closes.size() < n || highs.size() < n || lows.size() < n) {
//            throw new IllegalArgumentException("数据长度不足");
            return Collections.emptyList();
        }

        List<KDJPoint> kdjPoints = new ArrayList<>(closes.size());

        // 初始值
        double k = 50.0;
        double d = 50.0;

        for (int i = n - 1; i < closes.size(); i++) {
            // 计算RSV
            double close = closes.get(i);
            double high = getMax(highs, i - n + 1, i);
            double low = getMin(lows, i - n + 1, i);
            double rsv = (close - low) / (high - low) * 100;

            // 计算K值
            k = (2.0 / 3) * k + (1.0 / 3) * rsv;

            // 计算D值
            d = (2.0 / 3) * d + (1.0 / 3) * k;

            // 计算J值
            double j = 3 * k - 2 * d;
            

            KDJPoint kdjPoint = new KDJPoint(timestamps.get(i), k, d, j, false, false);
            if (k > d) {
            	kdjPoint.setIfOver(true);
			}
            
            if (i > n - 1) {
            	// 黄金上穿 or 黄金下穿
            	if ((kdjPoints.get(i - n).getK() < kdjPoints.get(i - n).getD() && k > d)
            			|| (kdjPoints.get(i - n).getK() > kdjPoints.get(i - n).getD() && k < d)) {
            		kdjPoint.setIfGoldCross(true);
				}
            	
			}
            kdjPoints.add(kdjPoint);
        }
        return kdjPoints;
    }

    // 获取最大值
    private static double getMax(List<Double> data, int start, int end) {
        double max = data.get(start);
        for (int i = start + 1; i <= end; i++) {
            if (data.get(i) > max) {
                max = data.get(i);
            }
        }
        return max;
    }

    // 获取最小值
    private static double getMin(List<Double> data, int start, int end) {
        double min = data.get(start);
        for (int i = start + 1; i <= end; i++) {
            if (data.get(i) < min) {
                min = data.get(i);
            }
        }
        return min;
    }
    
    public static boolean isRise(List<Ticker> tickers) {
    	List<KDJPoint> kdjPoints = calculateKDJ(tickers);
    	KDJPoint kdjPoint0 = kdjPoints.get(kdjPoints.size() - 1);
    	KDJPoint kdjPoint1 = kdjPoints.get(kdjPoints.size() - 2);
    	
    	if (kdjPoint0.getJ() > kdjPoint1.getJ()) {
			return true;
		}
    	
    	return false;
    }

    public static List<KDJSegment> calKDJSegment(List<KDJPoint> kdjPoints) {
        if (kdjPoints == null || kdjPoints.size() == 0) {
            return Lists.newArrayList();
        }

        List<KDJSegment> sequences = new ArrayList<>();
        
        List<KDJPoint> currentSequence = new ArrayList<>();
        currentSequence.add(kdjPoints.get(0));

        for (int i = 1; i < kdjPoints.size(); i++) {        	
            if ((kdjPoints.get(i).getJ() > kdjPoints.get(i - 1).getJ() 
            			|| kdjPoints.get(i).getTicker().getClose() > kdjPoints.get(i-1).getTicker().getClose())
            		&& kdjPoints.get(i).getTicker().getClose() > kdjPoints.get(i-1).getTicker().getClose()
            		&& kdjPoints.get(i).getTicker().getHigh() > kdjPoints.get(i-1).getTicker().getHigh()
            		) {
                currentSequence.add(kdjPoints.get(i));
            } else if (kdjPoints.get(i).getTicker().getClose() < kdjPoints.get(i-1).getTicker().getClose()
            		&& i < kdjPoints.size() - 1
        			&& kdjPoints.get(i).getTicker().getClose() > kdjPoints.get(i-1).getTicker().getLow()
        			&& kdjPoints.get(i+1).getTicker().getClose() > kdjPoints.get(i-1).getTicker().getShitiMax()
            		&& kdjPoints.get(i+1).getTicker().getHigh() > kdjPoints.get(i-1).getTicker().getHigh()
            		) {
                    currentSequence.add(kdjPoints.get(i));
			}else {
                if (currentSequence.size() >= 2) {
                    sequences.add(new KDJSegment(new ArrayList<>(currentSequence)));
                }
                currentSequence.clear();
                currentSequence.add(kdjPoints.get(i));
            }
        }

        if (currentSequence.size() >= 2) {
            sequences.add(new KDJSegment(currentSequence));
        }
        
        return sequences.stream().filter(x -> x.getChangeRate() > 0.01).collect(Collectors.toList());
        
    }
    
    public static boolean isRiseSegment(List<KDJPoint> kdjPoints) {
    	KDJPoint kdjPoint0 = kdjPoints.get(kdjPoints.size() - 1);
    	
    	List<KDJSegment> kdjSegments = calKDJSegment(kdjPoints);
    	KDJSegment kdjSegment0 = kdjSegments.get(kdjSegments.size() - 1);
    	KDJSegment kdjSegment1 = null;
    	if (kdjSegment0.contains(kdjPoint0)) {
        	kdjSegment0 = kdjSegments.get(kdjSegments.size() - 2);
        	kdjSegment1 = kdjSegments.get(kdjSegments.size() - 3);
		} else {
        	kdjSegment0 = kdjSegments.get(kdjSegments.size() - 1);
        	kdjSegment1 = kdjSegments.get(kdjSegments.size() - 2);
		}
    	
    	if (kdjSegment0.getMaxClose() > kdjSegment1.getMaxClose()
    			&& kdjPoint0.getTicker().getClose() > kdjSegment0.getMinLow()
    			) {
			return true;
		}
    	
    	return false;
    }
    

    public static boolean isShockSegment(List<KDJPoint> kdjPoints) {
    	KDJPoint kdjPoint0 = kdjPoints.get(kdjPoints.size() - 1);
    	KDJPoint kdjPoint1 = kdjPoints.get(kdjPoints.size() - 2);
    	KDJPoint kdjPoint2 = kdjPoints.get(kdjPoints.size() - 3);
    	
    	List<KDJSegment> kdjSegments = calKDJSegment(kdjPoints);
    	KDJSegment kdjSegment0 = kdjSegments.get(kdjSegments.size() - 1);
    	
    	if (kdjSegment0.contains(kdjPoint0)) {
        	kdjSegment0 = kdjSegments.get(kdjSegments.size() - 2);
		} else {
        	kdjSegment0 = kdjSegments.get(kdjSegments.size() - 1);
		}
    	
    	if (kdjPoint0.getTicker().getClose() > kdjSegment0.getMinLow()
    			&& kdjPoint0.getTicker().getClose() < kdjSegment0.getMaxHigh()
    			
    			&& kdjPoint0.getTicker().getLow() < kdjSegment0.getStartKDJPoint().getTicker().getHigh()
    			&& kdjPoint0.getTicker().getClose() < kdjSegment0.getStartKDJPoint().getTicker().getHigh()
    			
    			&& (kdjPoint0.getTicker().getClose() >= kdjPoint0.getTicker().getOpen()
//    					&& kdjPoint0.getJ() > kdjPoint1.getJ()
    					)
    			    			
    			) {
			return true;
		}
    	
    	return false;
    }


    public static class KDJPoint {
        private Ticker ticker;
        private double k;
        private double d;
        private double j;
        private boolean ifOver;
        private boolean ifGoldCross;
        
        
        public KDJPoint(Ticker ticker, double k, double d, double j, boolean ifOver, boolean ifGoldCross) {
			super();
			this.ticker = ticker;
			this.k = k;
			this.d = d;
			this.j = j;
			this.ifOver = ifOver;
			this.ifGoldCross = ifGoldCross;
		}
       
		public Ticker getTicker() {
			return ticker;
		}
		public void setTicker(Ticker ticker) {
			this.ticker = ticker;
		}

		public double getK() {
			return k;
		}

		public void setK(double k) {
			this.k = k;
		}

		public double getD() {
			return d;
		}

		public void setD(double d) {
			this.d = d;
		}
		
		public double getJ() {
			return j;
		}

		public void setJ(double j) {
			this.j = j;
		}

		public boolean isIfOver() {
			return ifOver;
		}

		public void setIfOver(boolean ifOver) {
			this.ifOver = ifOver;
		}

		public boolean isIfGoldCross() {
			return ifGoldCross;
		}

		public void setIfGoldCross(boolean ifGoldCross) {
			this.ifGoldCross = ifGoldCross;
		}

		private String getTimestampStr() {
            return DateFormatUtils.format(this.ticker.getTimestamp(), "yyyy-MM-dd");
        }
    }
    
    
    public static class KDJSegment {
    	private List<KDJPoint> kdjPoints;
    	
    	public KDJSegment(List<KDJPoint> kdjPoints) { 
    		this.kdjPoints = kdjPoints;
    	}

		public List<KDJPoint> getKdjPoints() {
			return kdjPoints;
		}
		
		public KDJPoint getStartKDJPoint() {
			return kdjPoints.get(0);
		}

		public KDJPoint getEndKDJPoint() {
			return kdjPoints.get(kdjPoints.size() - 1);
		}
		
		public double getMaxClose() {
			return this.kdjPoints.stream().map(x -> x.getTicker().getClose()).max(Double::compare).orElse(0.0);
		}
		
		
		public double getMinLow() {
			return this.kdjPoints.stream().map(x -> x.getTicker().getLow()).min(Double::compare).orElse(0.0);
		}

		public double getMaxHigh() {
			return this.kdjPoints.stream().map(x -> x.getTicker().getHigh()).max(Double::compare).orElse(0.0);
		}
		
		public double getChangeRate() {
			KDJPoint startKdjPoint = getStartKDJPoint();
			KDJPoint endKdjPoint = getEndKDJPoint();
			return (endKdjPoint.getTicker().getShitiMax() - startKdjPoint.getTicker().getShitiMin())/ startKdjPoint.getTicker().getShitiMin();
		}
		
		public boolean contains(KDJPoint kdjPoint) {
			return this.kdjPoints.contains(kdjPoint);
		}
		
    }

    public static void main(String[] args) throws IOException {

        List<Trade> trades = XueQiuUtils.getXueQiuJson("sz002006", "day");

        List<KDJPoint> kdjPoints = calculateKDJ(Ticker.from(trades));
        for (int i = 0; i < kdjPoints.size(); i++) {
//            if (kdjPoints.get(i).ifGoldCross) {
                System.out.println("K值上穿D值，发生在" + kdjPoints.get(i).getTimestampStr()
                		+ "\t" + kdjPoints.get(i).getK()
                		+ "\t" + kdjPoints.get(i).getD()
                		+ "\t" + kdjPoints.get(i).getJ());
//            }
        }

        List<KDJSegment> kdjSegments = calKDJSegment(kdjPoints);
        for (int i = 0; i < kdjSegments.size(); i++) {
        	System.out.println("segment 区间：" + kdjSegments.get(i).getStartKDJPoint().getTimestampStr()
        			+ "\t" + kdjSegments.get(i).getEndKDJPoint().getTimestampStr());
		}
    }

  

}
