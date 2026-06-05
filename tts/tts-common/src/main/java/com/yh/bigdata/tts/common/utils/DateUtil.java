package com.yh.bigdata.tts.common.utils;

import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;

/**
 * @author duyp
 * 
 * @date 2019/04/25
 * 
 * @comment
 */
@Slf4j
public class DateUtil extends DateUtils {
	
	public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";

	public static final String TIME_FORMAT_HH_MM = "HH:mm";

	public static final String DATE_FORMAT_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
	
	public static String getCurrentDay() {
		try {
			return DateFormatUtils.format(Calendar.getInstance(), DATE_FORMAT_YYYY_MM_DD);
		} catch (Exception e) {
			
		}
		return null;
	}
	
	public static String getCurrentTime() {
		try {
			return DateFormatUtils.format(Calendar.getInstance(), DATE_FORMAT_YYYY_MM_DD_HH_MM_SS);
		} catch (Exception e) {
			
		}
		return null;
	}
	
	public static String parseTime(Long time) {
		try {
			return DateFormatUtils.format(new Date(time), DATE_FORMAT_YYYY_MM_DD_HH_MM_SS);
		} catch (Exception e) {
			
		}
		return null;
	} 
	
	public static Boolean isBefore(String targetDate, String sourceDate) {
        try {
            if (targetDate.length() > 10) {
                return DateUtils.parseDate(sourceDate, DATE_FORMAT_YYYY_MM_DD_HH_MM_SS).getTime() > DateUtils.parseDate(targetDate, DATE_FORMAT_YYYY_MM_DD_HH_MM_SS).getTime();
            }else{
                return DateUtils.parseDate(sourceDate, DATE_FORMAT_YYYY_MM_DD).getTime() > DateUtils.parseDate(targetDate, DATE_FORMAT_YYYY_MM_DD).getTime();
            }
        }catch (Exception ex) {
            log.error("isBefore exception...", ex);
        }
        return false;
	}
	
	public static Boolean isBeforeContains(String targetDate, String sourceDate) {
        try {
            if (targetDate.length() > 10) {
                return DateUtils.parseDate(sourceDate, DATE_FORMAT_YYYY_MM_DD_HH_MM_SS).getTime() >= DateUtils.parseDate(targetDate, DATE_FORMAT_YYYY_MM_DD_HH_MM_SS).getTime();
            }else{
                return DateUtils.parseDate(sourceDate, DATE_FORMAT_YYYY_MM_DD).getTime() >= DateUtils.parseDate(targetDate, DATE_FORMAT_YYYY_MM_DD).getTime();
            }
        }catch (Exception ex) {
            log.error("isBeforeContains exception...", ex);
        }
        return false;
	}
	
	public static Boolean isAfter(String targetDate, String sourceDate) {
        try {
            if (targetDate.length() > 10) {
                return DateUtils.parseDate(targetDate, DATE_FORMAT_YYYY_MM_DD_HH_MM_SS).getTime() > DateUtils.parseDate(sourceDate, DATE_FORMAT_YYYY_MM_DD_HH_MM_SS).getTime();
            }else{
                return DateUtils.parseDate(targetDate, DATE_FORMAT_YYYY_MM_DD).getTime() > DateUtils.parseDate(sourceDate, DATE_FORMAT_YYYY_MM_DD).getTime();
            }
        }catch (Exception ex) {
            log.error("isAfter exception...", ex);
        }
        return false;
	}
	
	public static Boolean isAfterContains(String targetDate, String sourceDate)  {
        try {
            if (targetDate.length() > 10) {
                return DateUtils.parseDate(targetDate, DATE_FORMAT_YYYY_MM_DD_HH_MM_SS).getTime() >= DateUtils.parseDate(sourceDate, DATE_FORMAT_YYYY_MM_DD_HH_MM_SS).getTime();
            }else{
                return DateUtils.parseDate(targetDate, DATE_FORMAT_YYYY_MM_DD).getTime() >= DateUtils.parseDate(sourceDate, DATE_FORMAT_YYYY_MM_DD).getTime();
            }
        }catch (Exception ex) {
            log.error("isAfterContains exception...", ex);
        }
        return false;
	}
	
	
	public static Boolean isSameDay(String day) {
		day = day.substring(0, 10);
		return DateFormatUtils.format(Calendar.getInstance().getTime(), DATE_FORMAT_YYYY_MM_DD).equals(day);
	}
	
	public static Boolean isSameDay(String sourceDay, String targetDay) {
		sourceDay = sourceDay.substring(0, 10);
		targetDay = targetDay.substring(0, 10);

		return sourceDay.equals(targetDay);
	}

	
	public static boolean isSameDay(Date day1, Date day2) {
		return DateFormatUtils.format(day1, DATE_FORMAT_YYYY_MM_DD).equals(DateFormatUtils.format(day2, DATE_FORMAT_YYYY_MM_DD));
	}
	
	public static String parse2Friday(String someDay) throws ParseException {
		someDay = someDay.substring(0, 10);
		Date someDate = DateUtil.parseDate(someDay, DATE_FORMAT_YYYY_MM_DD);
		LocalDate localDate = someDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate Localfriday = localDate.with(DayOfWeek.FRIDAY);
		return DateFormatUtils.format(Date.from(Localfriday.atStartOfDay(ZoneId.systemDefault()).toInstant()), DATE_FORMAT_YYYY_MM_DD);
	}
	
	public static String parse2MonthLastDay(String someDay) throws ParseException {
		someDay = someDay.substring(0, 10);
		Date someDate = DateUtil.parseDate(someDay, DATE_FORMAT_YYYY_MM_DD);
		LocalDate localDate = someDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate LocallastDayOfMonth = localDate.with(TemporalAdjusters.lastDayOfMonth());
		return DateFormatUtils.format(Date.from(LocallastDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant()), DATE_FORMAT_YYYY_MM_DD);
	}
	
	public static String parse2QuarterLastDay(String someDay) throws ParseException {
		String year = someDay.substring(0, 4);
		int month = Integer.parseInt(someDay.substring(5, 7));
		String day = someDay.substring(8, 10);

		int quarter = (month + 2) / 3;
		int lastQuarterMonth = quarter * 3;
		String lastQuarterMonthString = lastQuarterMonth < 10?"0"+lastQuarterMonth : String.valueOf(lastQuarterMonth);
		
		return parse2MonthLastDay(year + "-" + lastQuarterMonthString + "-" + day);
	}
	
	
	public static String parse2YearLastDay(String someDay) throws ParseException {

		someDay = someDay.substring(0, 10);
		Date someDate = DateUtil.parseDate(someDay, DATE_FORMAT_YYYY_MM_DD);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(someDate);
		return calendar.get(Calendar.YEAR)+ "-12-31";
	}
	
	public static Boolean isSameWeek(String day) {
		
		try {
			day = day.substring(0, 10);		
			
			Calendar calendar1 = Calendar.getInstance();
			
			calendar1.setTime(DateUtils.parseDate(day, DATE_FORMAT_YYYY_MM_DD));
			if (calendar1.get(Calendar.DAY_OF_WEEK) == 1) {		//日期为周天
				calendar1.add(Calendar.DAY_OF_YEAR, -1);
			}
			
			Calendar calendar2  = Calendar.getInstance();
			if (calendar2.get(Calendar.DAY_OF_WEEK) == 1) {
				calendar2.add(Calendar.DAY_OF_YEAR, -1);
			}
			
			return calendar1.get(Calendar.WEEK_OF_YEAR) == calendar2.get(Calendar.WEEK_OF_YEAR)
					&& calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
					;
			
		} catch (Exception e) {
			
		}
		
		return false;
	}
	

	public static Boolean isSamePeriod(String p1, String p2, PeriodTypeEnum periodTypeEnum) {
		switch (periodTypeEnum) {
		case MONTH:
			return isSameMonth(p1, p2);
		case WEEK:
			return isSameWeek(p1, p2);
		case DAY:
			return isSameDay(p1, p2);
		default:
			break;
		}
		return false;
	}
	
	public static Boolean isSameWeek(String day1, String day2) {

		try {
			day1 = day1.substring(0, 10);
			day2 = day2.substring(0, 10);		

			
			Calendar calendar1 = Calendar.getInstance();
			calendar1.setTime(DateUtils.parseDate(day1, DATE_FORMAT_YYYY_MM_DD));
			if (calendar1.get(Calendar.DAY_OF_WEEK) == 1) {		//日期为周天
				calendar1.add(Calendar.DAY_OF_YEAR, -1);
			}
			
			Calendar calendar2 = Calendar.getInstance();
			calendar2.setTime(DateUtils.parseDate(day2, DATE_FORMAT_YYYY_MM_DD));
			if (calendar2.get(Calendar.DAY_OF_WEEK) == 1) {
				calendar2.add(Calendar.DAY_OF_YEAR, -1);
			}
			
			return calendar1.get(Calendar.WEEK_OF_YEAR) == calendar2.get(Calendar.WEEK_OF_YEAR)
					&& calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
					;
			
		} catch (Exception e) {
			
		}
		
		return false;
	}
	
	public static Boolean isSameMonth(String day) {
		
		try {

			day = day.substring(0, 10);		
			
			Calendar calendar = Calendar.getInstance();
			
			calendar.setTime(DateUtils.parseDate(day, DATE_FORMAT_YYYY_MM_DD));
			
			return calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH)
					&& calendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)
					;
			
		} catch (Exception e) {
			
		}
		
		return false;
	}
	
	
	public static Boolean isSameMonth(String day1, String day2) {

		try {

			day1 = day1.substring(0, 10);		
			day2 = day2.substring(0, 10);		
			
			Calendar calendar1 = Calendar.getInstance();
			calendar1.setTime(DateUtils.parseDate(day1, DATE_FORMAT_YYYY_MM_DD));
			int year1 = calendar1.get(Calendar.YEAR);
			int month1 = calendar1.get(Calendar.MONTH);
			
			
			Calendar calendar2 = Calendar.getInstance();
			calendar2.setTime(DateUtils.parseDate(day2, DATE_FORMAT_YYYY_MM_DD));
			int year2 = calendar2.get(Calendar.YEAR);
			int month2 = calendar2.get(Calendar.MONTH);
			
			return year1 == year2
					&& month1 == month2
					;
			
		} catch (Exception e) {
			
		}
		
		return false;
	}
	
	public static Boolean isSameQuarter(String day1, String day2) {

		try {

			day1 = day1.substring(0, 10);		
			day2 = day2.substring(0, 10);	
			
			Calendar calendar1 = Calendar.getInstance();
			calendar1.setTime(DateUtils.parseDate(day1, DATE_FORMAT_YYYY_MM_DD));
			int year1 = calendar1.get(Calendar.YEAR);
			int month1 = calendar1.get(Calendar.MONTH);
			
			
			Calendar calendar2 = Calendar.getInstance();
			calendar2.setTime(DateUtils.parseDate(day2, DATE_FORMAT_YYYY_MM_DD));
			int year2 = calendar2.get(Calendar.YEAR);
			int month2 = calendar2.get(Calendar.MONTH);
			
			return year1 == year2
					&& (month1 + 2) % 3 == (month2 + 2) % 3
					;
			
		} catch (Exception e) {
			
		}
		
		return false;
	}
	
	public static boolean isSameYear(String day) {
		
		try {
			
			day = day.substring(0, 10);		
			
			Calendar calendar = Calendar.getInstance();
			
			calendar.setTime(DateUtils.parseDate(day, DATE_FORMAT_YYYY_MM_DD));
			
			return calendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR);
			
		} catch (Exception e) {
			
		}
		
		return false;
	}
	
	public static Boolean isSameYear(String day1, String day2) {

		try {

			day1 = day1.substring(0, 10);		
			day2 = day2.substring(0, 10);
			
			Calendar calendar1 = Calendar.getInstance();
			calendar1.setTime(DateUtils.parseDate(day1, DATE_FORMAT_YYYY_MM_DD));
			
			Calendar calendar2 = Calendar.getInstance();
			calendar2.setTime(DateUtils.parseDate(day2, DATE_FORMAT_YYYY_MM_DD));
			
			return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
			
		} catch (Exception e) {
			
		}
		
		return false;
	}
	
	public static String getTodayWeek() {
		Date toweekFriday = Date.from(LocalDate.now().with(DayOfWeek.FRIDAY).atStartOfDay(ZoneId.systemDefault()).toInstant());
		String toweekFridayStr = DateFormatUtils.format(toweekFriday, "yyyy-MM-dd");
		return toweekFridayStr;
	}
	
	public static String getMonthLastDay() {
		try {
			return parse2MonthLastDay(getCurrentDay());
		} catch (Exception e) {
		}
		return null;
	}
	
	public static String getQuarterLastDay() throws ParseException {
		return parse2QuarterLastDay(getCurrentDay());
	}
	
	public static String getYearLastDay() {
		try {
			return parse2YearLastDay(getCurrentDay());
		} catch (Exception e) {
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public static int getMANumFrom0930(int ma) {
		Date time0930 = Calendar.getInstance().getTime();
		time0930.setHours(9);
		time0930.setMinutes(30);
		time0930.setSeconds(0);
		Date time1300 = Calendar.getInstance().getTime();
		time1300.setHours(13);
		time1300.setMinutes(0);
		time1300.setSeconds(0);
		
		Date now = Calendar.getInstance().getTime();
		String time = DateFormatUtils.format(now, TIME_FORMAT_HH_MM);
		
		if (time.compareTo("09:30") < 0) {
			return 0;
		}else if (time.compareTo("15:00") > 0) {
//			return 240 / min;
			return 0;
		}else if (time.compareTo("11:30") < 0) {
			return (int)(((now.getTime() - time0930.getTime()) / (1000 * 60)) / ma);	
		}else if (time.compareTo("13:00") < 0) {
			return  120 / ma;	
		}else if (time.compareTo("15:00") < 0) {
			return  120 / ma + (int)(((now.getTime() - time1300.getTime()) / (1000 * 60)) / ma);	
		}
		return 0;
	}
	
	public static String getMATime(String time, int ma) {
		int hour = Integer.parseInt(time.substring(0, 2));
		int minute = Integer.parseInt(time.substring(3, 5));
		int seconds = Integer.parseInt(time.substring(6, 8));
 		String formatTime = String.format("%02d", hour) + ":" + String.format("%02d", minute) + ":" + String.format("%02d", seconds);
 		if (formatTime.compareTo("09:31:00") <= 0) {
 			if(ma == 5) {
				time = "09:35:00";
			}else if (ma == 15) {
				time = "09:45:00";
			}else if (ma == 30) {
				time = "10:00:00";
			}else if (ma == 60) {
				time = "10:30:00";
			}
		}else if (formatTime.compareTo("11:30:00") >= 0 && formatTime.compareTo("12:59:59") <= 0) {
			time = "11:30:00";
		}else if (formatTime.compareTo("15:00:00") >= 0) {
			time = "15:00:00";
		}else if (time.compareTo("09:30:00") > 0 && time.compareTo("15:00:00") < 0) {
			if(ma == 5 || ma == 15 || ma == 30) {
				minute = (minute - 1 + (seconds >=0?1:0)) >= 0? ((minute - 1)/ma  + 1) * ma : 0;		//如果seconds >= 0 则放入下一个
				if(minute == 60) {
					time = String.valueOf(hour + 1) + ":00:00";
				}else {
					time = String.format("%02d", hour) + ":" + String.format("%02d", minute) + ":00";
				}
			}else if(ma == 60){
				if (time.compareTo("10:30:00") <= 0) {
					time = "10:30:00";
				}else if (time.compareTo("12:59:59") <= 0) {
					time = "11:30:00";
				}else if (time.compareTo("14:00:00") <= 0) {
					time = "14:00:00";
				}else if (time.compareTo("15:00:00") <= 0) {
					time = "15:00:00";
				}
			}
		}
		return time;
	}
	
	/**
	 * 在时间区间内
	 * @param startTime
	 * @param endTime
	 * @param valueTime
	 * @return
	 */
	public static boolean isIn(String startTime, String endTime, String valueTime) {
		return startTime.compareTo(valueTime) <= 0 && endTime.compareTo(valueTime) >= 0;
	}
	
	public static String getLastTradeDay() {
		Calendar instance = Calendar.getInstance();
		instance.add(Calendar.DAY_OF_WEEK, -1);
		while(instance.get(Calendar.DAY_OF_WEEK) == 1 
				|| instance.get(Calendar.DAY_OF_WEEK) == 7){
			instance.add(Calendar.DAY_OF_WEEK, -1);
		}
		return DateFormatUtils.format(instance, DATE_FORMAT_YYYY_MM_DD);
	}
	
	public static Date parseDate(String day) {
		try {
			if (day.length() > 10) {
				return DateUtils.parseDate(day, DATE_FORMAT_YYYY_MM_DD_HH_MM_SS);
			}else {
				return DateUtils.parseDate(day, DATE_FORMAT_YYYY_MM_DD);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	private static LocalDate convertToLocalDate(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
	
	public static Date convertToDate(LocalDate localDate) {
		return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
	
	public static Date convertToDateStartOfDay(Date date) {
		LocalDate localDate = convertToLocalDate(date);
		LocalDateTime startOfDayDate = localDate.atTime(0, 0, 0);
		return Date.from(startOfDayDate.atZone(ZoneId.systemDefault()).toInstant());
	}
	
	public static Date convertToDateEndOfDay(Date date) {
		LocalDate localDate = convertToLocalDate(date);
		LocalDateTime endOfDayDate = localDate.atTime(23, 59, 59);
		return Date.from(endOfDayDate.atZone(ZoneId.systemDefault()).toInstant());
	}
	
	public static Date subDaysToDate(Date date, int days) {
		LocalDate localDate = convertToLocalDate(date).minusDays(days);
		return convertToDate(localDate);
	}
	
	public static Pair<Long, Long> getFirstAndLastDayOfPeriod(PeriodTypeEnum trendPperiodTypeEnum, String date) {
		LocalDate localDate = convertToLocalDate(parseDate(date));
		
		LocalDate firstDay = null, lastDay = null;
		switch (trendPperiodTypeEnum) {
		case WEEK:
			firstDay = localDate.with(DayOfWeek.MONDAY);
			lastDay = localDate.with(DayOfWeek.SUNDAY);			
			break;
		case MONTH:
			firstDay = localDate.with(TemporalAdjusters.firstDayOfMonth());
			lastDay = localDate.with(TemporalAdjusters.lastDayOfMonth());		
			break;
		case QUARTER:
			int currentMonth = localDate.getMonthValue();
			int firstMonthOfQuarter = ((currentMonth - 1) / 3) * 3 + 1;	
			int lastMonthOfQuarter = firstMonthOfQuarter + 2;	
			firstDay = LocalDate.of(localDate.getYear(), firstMonthOfQuarter, 1);
			lastDay = LocalDate.of(localDate.getYear(), lastMonthOfQuarter, 1)
					.with(TemporalAdjusters.lastDayOfMonth());
			break;
		case YEAR:
			firstDay = localDate.with(TemporalAdjusters.firstDayOfYear());
			lastDay = localDate.with(TemporalAdjusters.lastDayOfYear());
			break;

		default:
			break;
		}
		
		
		return Pair.of(convertToDateStartOfDay(convertToDate(firstDay)).getTime(), convertToDateEndOfDay(convertToDate(lastDay)).getTime());
		
	}
	
	public static void main(String[] args) throws ParseException {
//		Calendar instance = Calendar.getInstance();
//		instance.add(Calendar.DAY_OF_WEEK, 1);
//		System.out.println(instance.get(Calendar.DAY_OF_WEEK));
//		System.out.println(getLastTradeDay());
		
//		System.out.println(isSameMonth("2023-11-08 00:00:00"));
		
//		System.out.println(DateUtil.isSamePeriod("2023-12-15", "2023-12-15", TiziPeriodTypeEnum.WEEK));
		
//		System.out.println(parse2QuarterLastDay("2024-11-01"));
		
//		System.out.println(getFirstAndLastDayOfPeriod(PeriodTypeEnum.YEAR, "2025-03-12"));
		
	}
}
