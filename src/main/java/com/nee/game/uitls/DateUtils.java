package com.nee.game.uitls;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 日期工具类
 * 
 * @author 徐明明
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
	private static String[] parsePatterns = { "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss",
			"yyyy/MM/dd HH:mm", "yyyy/MM", "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM" };

	/**
	 * 获取当前时间的毫秒数
	 * 
	 * @return
	 */
	public static Long getCurrentTime() {
		return Calendar.getInstance().getTimeInMillis();
	}

	public static Date getCurrentDate() {
		return Calendar.getInstance().getTime();
	}

	/**
	 * 格式化返回时间，格式为“2015-03-24 19:25:16”
	 * 
	 * @param time
	 * @return
	 */
	public static String formatTime(Long time) {
		Date date = new Date(time);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(date);
	}

	/**
	 * 得到当前日期字符串 格式（yyyy-MM-dd）
	 */
	public static String getDate() {
		return getDate("yyyy-MM-dd");
	}

	/**
	 * 得到当前日期字符串 格式（yyyy-MM-dd） pattern可以为："yyyy-MM-dd" "HH:mm:ss" "E"
	 */
	public static String getDate(String pattern) {
		return DateFormatUtils.format(new Date(), pattern);
	}

	/**
	 * 得到日期字符串 默认格式（yyyy-MM-dd） pattern可以为："yyyy-MM-dd" "HH:mm:ss" "E"
	 */
	public static String formatDate(Date date, Object... pattern) {
		if (null == date) {
			return null;
		}

		String formatDate = null;
		if (pattern != null && pattern.length > 0) {
			formatDate = DateFormatUtils.format(date, pattern[0].toString());
		} else {
			formatDate = DateFormatUtils.format(date, "yyyy-MM-dd");
		}
		return formatDate;
	}

	/**
	 * 得到日期时间字符串，转换格式（yyyy-MM-dd HH:mm:ss）
	 */
	public static String formatDateTime(Date date) {
		return formatDate(date, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 得到当前时间字符串 格式（HH:mm:ss）
	 */
	public static String getTime() {
		return formatDate(new Date(), "HH:mm:ss");
	}

	/**
	 * 得到当前日期和时间字符串 格式（yyyy-MM-dd HH:mm:ss）
	 */
	public static String getDateTime() {
		return formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 得到当前年份字符串 格式（yyyy）
	 */
	public static String getYear() {
		return formatDate(new Date(), "yyyy");
	}

	/**
	 * 得到当前月份字符串 格式（MM）
	 */
	public static String getMonth() {
		return formatDate(new Date(), "MM");
	}

	/**
	 * 得到当天字符串 格式（dd）
	 */
	public static String getDay() {
		return formatDate(new Date(), "dd");
	}

	/**
	 * 得到当前星期字符串 格式（E）星期几
	 */
	public static String getWeek() {
		return formatDate(new Date(), "E");
	}

	/**
	 * 日期型字符串转化为日期 格式 { "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm",
	 * "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy.MM.dd",
	 * "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm" }
	 */
	public static Date parseDate(Object str) {
		if (str == null) {
			return null;
		}
		try {
			return parseDate(str.toString(), parsePatterns);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * 获取过去的天数
	 * 
	 * @param date
	 * @return
	 */
	public static long pastDays(Date date) {
		long t = new Date().getTime() - date.getTime();
		return t / (24 * 60 * 60 * 1000);
	}

	/**
	 * 获取过去的小时
	 * 
	 * @param date
	 * @return
	 */
	public static long pastHour(Date date) {
		long t = new Date().getTime() - date.getTime();
		return t / (60 * 60 * 1000);
	}

	/**
	 * 获取过去的分钟
	 * 
	 * @param date
	 * @return
	 */
	public static long pastMinutes(Date date) {
		long t = new Date().getTime() - date.getTime();
		return t / (60 * 1000);
	}

	/**
	 * 转换为时间（天,时:分:秒.毫秒）
	 * 
	 * @param timeMillis
	 * @return
	 */
	public static String formatDateTime(long timeMillis) {
		long day = timeMillis / (24 * 60 * 60 * 1000);
		long hour = (timeMillis / (60 * 60 * 1000) - day * 24);
		long min = ((timeMillis / (60 * 1000)) - day * 24 * 60 - hour * 60);
		long s = (timeMillis / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
		long sss = (timeMillis - day * 24 * 60 * 60 * 1000 - hour * 60 * 60 * 1000 - min * 60 * 1000 - s * 1000);
		return (day > 0 ? day + "," : "") + hour + ":" + min + ":" + s + "." + sss;
	}

	/**
	 * 获取两个日期之间的天数
	 * 
	 * @param before
	 * @param after
	 * @return
	 */
	public static long getDistanceOfTwoDate(Date before, Date after) {
		long beforeTime = before.getTime();
		long afterTime = after.getTime();
		return (afterTime - beforeTime) / (1000 * 60 * 60 * 24);
	}

	public static Date getDatePreMonth(int preMonth, Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, preMonth);

		return calendar.getTime();
	}

	/**
	 * 获取几个月以前的时间
	 * 
	 * @param preMonth
	 * @return
	 */
	public static Date getDatePreMonth(int preMonth) {
		return getDatePreMonth(preMonth, new Date());
	}

	/**
	 * 获取几分钟以前的时间
	 * 
	 * @param preMinute
	 * @return
	 */
	public static Date getDatePreMinute(int preMinute) {
		Date date = new Date();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, preMinute);

		return calendar.getTime();
	}

	/**
	 * 获取几分钟以前的时间
	 * 
	 * @param preMinute
	 * @return
	 */
	public static Date getDatePreMinute(int preMinute, Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, preMinute);

		return calendar.getTime();
	}

	/**
	 * 获取几秒以前的时间
	 * 
	 * @param preSecond
	 * @return
	 */
	public static Date getDatePreSecond(int preSecond) {
		Date date = new Date();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.SECOND, preSecond);

		return calendar.getTime();
	}

	/**
	 * 获取几秒以前的时间
	 * 
	 * @param preSecond
	 * @param date
	 * @return
	 */
	public static Date getDatePreSecond(int preSecond, Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.SECOND, preSecond);

		return calendar.getTime();
	}

	/**
	 * 获取几天以前的时间
	 * 
	 * @param preDay
	 * @return
	 */
	public static Date getDatePreDay(int preDay) {
		Date date = new Date();
		return getDatePreDay(preDay, date);
	}

	/**
	 * 获取几天以前的时间
	 * 
	 * @param preDay
	 * @param date
	 * @return
	 */
	public static Date getDatePreDay(int preDay, Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, preDay);

		return calendar.getTime();
	}

	/**
	 * 获取当天的起始日期
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String getFirstCurrentDay() {
		Calendar cal = Calendar.getInstance();
		return new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(cal.getTime());
	}

	/**
	 * 获取前几天的结束时间
	 * 
	 * @return
	 */
	public static String getEndCurrentDay() {
		Calendar cal = Calendar.getInstance();
		return new SimpleDateFormat("yyyy-MM-dd 23:59:59").format(cal.getTime());
	}

	/**
	 * 获取前几天的起始时间
	 * 
	 * @param preDay
	 * @return
	 */
	public static String getFirstPreDay(int preDay) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, preDay);
		return new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(cal.getTime());
	}

	/**
	 * 获取前几天的结束时间
	 * 
	 * @param preDay
	 * @return
	 */
	public static String getEndPreDay(int preDay) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, preDay);
		return new SimpleDateFormat("yyyy-MM-dd 23:59:59").format(cal.getTime());
	}

	/**
	 * @author linjie
	 * @date 2015年11月6日 上午10:26:51
	 * @Description 获取上周周一的日期
	 * @return
	 * @throws Exception
	 */
	public static String getLastWeekFirstDay() throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -7);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		return new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(cal.getTime());
	}

	/**
	 * @author linjie
	 * @date 2015年11月6日 上午10:29:43
	 * @Description 获取上周周日的日期
	 * @return
	 * @throws Exception
	 */
	public static String getLastWeekLastDay() throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		return new SimpleDateFormat("yyyy-MM-dd 23:59:59").format(cal.getTime());
	}

	/**
	 * @author linjie
	 * @date 2015年11月6日 上午10:37:13
	 * @Description 获取上个月的第一天
	 * @return
	 * @throws Exception
	 */
	public static String getLastMonthFirstDay() throws Exception {
		return new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(getLastDateMonthFirstDay());
	}

	/**
	 * 获取上个月的第一天
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Date getLastDateMonthFirstDay() throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}

	/**
	 * @author linjie
	 * @date 2015年11月6日 上午10:38:42
	 * @Description 获取上月最后一天
	 * @return
	 * @throws Exception
	 */
	public static String getLastMonthLastDay() throws Exception {
		return new SimpleDateFormat("yyyy-MM-dd 23:59:59").format(getLastDateMonthLastDay());
	}

	/**
	 * 获取上月最后一天
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Date getLastDateMonthLastDay() throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 0);
		return cal.getTime();
	}

	public static Date getDateStart(Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		return calendar.getTime();
	}

	/**
	 * 获取格式化显示时间
	 * 
	 * @param time
	 * @return
	 */
	public static String getShowTime(Date time) {
		if (time == null) {
			return null;
		}

		String timeDate = DateUtils.formatDate(time);
		Date timeDay = DateUtils.parseDate(timeDate);
		// 创建时间距离现在相差的天数
		long day = DateUtils.pastDays(timeDay);
		// 创建时间的年份
		String year = DateUtils.formatDate(time, "yyyy");
		// 现在的年份
		String nowYear = DateUtils.getYear();
		// 是否当年
		boolean isCurrntYear = Integer.parseInt(year) == Integer.parseInt(nowYear);
		// 时分
		String hourMin = DateFormatUtils.format(time, "HH:mm");
		if (day == 0) { // 是今天发布的
			return "今天 " + hourMin;
		} else if (day == 1) {
			return "昨天 " + hourMin;
		} else if (day == 2) {
			return "前天 " + hourMin;
		} else if (day >= 3 && isCurrntYear) {
			return DateFormatUtils.format(time, "MM-dd HH:mm");
		} else if (day >= 3 && !isCurrntYear) {
			return DateFormatUtils.format(time, "yyyy-MM-dd HH:mm");
		}

		return null;
	}
}
