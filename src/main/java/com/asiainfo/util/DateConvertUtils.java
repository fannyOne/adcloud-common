package com.asiainfo.util;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by yangry on 2016/6/14 0014.
 */
@lombok.extern.slf4j.Slf4j
public class DateConvertUtils {

    /**
     * 一天相应的毫秒
     */
    public static final long ONE_DAY_MILLIS = 86400000L;

    /**
     * 一小时相应的毫秒
     */
    public static final long ONE_HOUR_MILLIS = 3600000L;

    /**
     * 一分钟相应的毫秒
     */
    public static final long ONE_MIN_MILLIS = 60000L;

    /**
     * 一秒相应的毫秒
     */
    public static final long ONE_SECOND_MILLIS = 1000L;

    /**
     * 首页时间展示类型
     */
    public static final String S_INDEX_TIME_TYPE_NOTICE = "NOTICE";
    public static final String S_FORMAT_YYYYMMDD = "yyyyMMdd";

    public static Timestamp parse() {
        return null;
    }

    public static String format() {
        return null;
    }

    /**
     * easyUI DateTimeBox数据格式转换成Timestamp格式
     * (yyyy-MM-dd hh:mm:ss)
     *
     * @return
     * @throws ParseException
     * @author cuiwl
     * @date Sep 18, 2014
     */
    public static Timestamp easyuiDateTimeBox2Timestamp(String time)
        throws ParseException {
        if (StringUtils.isNotBlank(time)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            long t = sdf.parse(time).getTime();
            Timestamp ts = new Timestamp(t);
            return ts;
        }
        return null;
    }

    public static String formatToRuleTime(String timeStr) throws ParseException {
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat df3 = new SimpleDateFormat("yyyyMMddHHmmss");

        Date date = df3.parse(timeStr);
        String time = df2.format(date);
        return time;
    }

    /**
     * @param timeStr
     * @return
     * @throws Exception
     * @throws ParseException
     * @Function: formatTime
     * @Description: 把时间格式转化为1:YYYYMMDD,2:YYYYMMDDHHMISS这种格式
     * 支持YYYY-MM-DD,YYYY/MM/DD,YYYY-MM-DD 24HH:MI:SS,YYYY/MM/DD 24HH:MI:SS,YYYY-MM-DD HH:mm:ss.x
     * @version: v1.0.0
     * @author: hewei
     * @date: 2013-11-26 下午04:14:12
     * <p>
     * Modification History:
     * Date          Author          Version            Description
     * ---------------------------------------------------------*
     * 2013-11-26     hewei         v1.0.0                                   修改原因
     */
    public static String formatTime(String timeStr) throws ParseException {
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat df3 = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat df4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        String timeStrTemp = timeStr;

        if (timeStrTemp.length() == 8) {
            return timeStrTemp;
        }

        if (timeStrTemp.length() == 16) {
            if (-1 != timeStrTemp.indexOf("-")) {
                timeStrTemp = timeStrTemp.replace("-", "");
            } else if (-1 != timeStrTemp.indexOf("/")) {
                timeStrTemp = timeStrTemp.replace("/", "");
            }
        }

        if (timeStrTemp.length() == 19) {
            Date date = df2.parse(timeStrTemp);
            timeStrTemp = df3.format(date);
        }

        if (timeStrTemp.length() == 21) {
            Date date = df4.parse(timeStrTemp);
            timeStrTemp = df3.format(date);
        }

        return timeStrTemp;

    }

    /**
     * easyUI DateBox数据格式转换成Timestamp格式
     *
     * @param date (yyyy-MM-dd)
     * @return
     * @throws ParseException
     * @author cuiwl
     * @date Sep 18, 2014
     */
    public static Timestamp easyuiDateBox2Timestamp(String date)
        throws ParseException {
        if (StringUtils.isNotBlank(date)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            long time = sdf.parse(date).getTime();
            Timestamp ts = new Timestamp(time);
            return ts;
        }
        return null;
    }

    /**
     * 时间戳转成标准输出格式（yyyy/MM/dd hh:mm:ss）
     *
     * @param time
     * @return
     * @author cuiwl
     * @date Sep 18, 2014
     */
    public static String longFmt2ShowTimeStd(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
        return sdf.format(new Date(time));
    }

    /**
     * 时间戳转成标准输出格式（yyyy/MM/dd 不输出时分秒）
     *
     * @param time
     * @return
     * @author cuiwl
     * @date Sep 18, 2014
     */
    public static String longFmt2ShowDateStd(long time) {

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeInMillis(time);

        int year = cal.get(Calendar.YEAR);
        int monthTemp = cal.get(Calendar.MONTH) + 1;
        int dayTemp = cal.get(Calendar.DAY_OF_MONTH);

        String month = String.valueOf(monthTemp < 10 ? "0" + monthTemp
            : monthTemp);
        String day = String.valueOf(dayTemp < 10 ? "0" + dayTemp : dayTemp);
        return year + "年" + month + "月" + day + "日";
    }

    /**
     * 获取某个时间当月第一天的0时0分0秒
     *
     * @param timeOfMonth 当月随便某个时间
     * @return
     * @author cuiwl
     * @date Oct 24, 2014
     */
    public static long getStartTimeInMonth(long timeOfMonth) {
        Calendar cal = Calendar.getInstance();
        long afterClearTime = clearHHMMSS(timeOfMonth);
        cal.clear();
        cal.setTimeInMillis(afterClearTime);

        // 获取某月最大天数
        int firstDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH);

        cal.set(Calendar.DAY_OF_MONTH, firstDay);

        return cal.getTimeInMillis();
    }

    /**
     * 获取下个月第一天的0时0分0秒
     *
     * @param timeOfMonth
     * @return
     * @author cuiwl
     * @date Oct 24, 2014
     */
    public static long getStartTimeInNextMonth(long timeOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeInMillis(timeOfMonth);
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);

        return getStartTimeInMonth(cal.getTimeInMillis());
    }

    /**
     * 获取下个月第一天
     *
     * @return
     */
    public static Date getStartTimeInNextMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }

    /**
     * 获取这个月第一天
     *
     * @return
     */
    public static Date getStartTimeInMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.add(Calendar.MONTH, 0);
        return calendar.getTime();
    }

    /**
     * 时分秒清零
     *
     * @param time
     * @return
     * @author cuiwl
     * @date Oct 24, 2014
     */
    public static long clearHHMMSS(long time) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeInMillis(time);

        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);

        return cal.getTimeInMillis();
    }

    /**
     * 获取年
     *
     * @param time
     * @return
     * @author cuiwl
     * @date Oct 24, 2014
     */
    public static int getYear(long time) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeInMillis(time);
        int year = cal.get(Calendar.YEAR);
        return year;
    }

    /**
     * 获取月
     *
     * @param time
     * @return
     * @author cuiwl
     * @date Oct 24, 2014
     */
    public static int getMonth(long time) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeInMillis(time);
        int month = cal.get(Calendar.MONTH) + 1;
        return month;
    }

    /**
     * 获取日
     *
     * @param time
     * @return
     * @author cuiwl
     * @date Oct 24, 2014
     */
    public static int getDayOfMonth(long time) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeInMillis(time);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return day;
    }

    /**
     * Copyright: Copyright (c) 2015 Asiainfo-Linkage
     *
     * @Description:获取小时
     * @version: v1.0.0
     * @author: zhangyao
     * @date: 2015-5-27下午5:02:35
     * <p>
     * Modification History
     * Date       Author      Version      Description
     * ---------------------------------------------------------*
     */
    public static int getHour(long time) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeInMillis(time);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return hour;
    }

    /**
     * Copyright: Copyright (c) 2015 Asiainfo-Linkage
     *
     * @Description:获取分钟
     * @version: v1.0.0
     * @author: zhangyao
     * @date: 2015-5-27下午5:04:33
     * <p>
     * Modification History
     * Date       Author      Version      Description
     * ---------------------------------------------------------*
     */
    public static int getMinute(long time) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeInMillis(time);
        int minute = cal.get(Calendar.MINUTE);
        return minute;
    }

    /**
     * 获取两个时间之间月数差
     *
     * @param startTime
     * @param endTime
     * @return
     * @author cuiwl
     * @date Oct 24, 2014
     */
    public static int getMonthSpace(long startTime, long endTime) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();

        c1.setTimeInMillis(startTime);
        c2.setTimeInMillis(endTime);

        return Math.abs((c2.get(1) - c1.get(1)) * 12 + c2.get(2) - c1.get(2));
    }

    /**
     * 获取指定月前前的当前时间
     *
     * @param time
     * @return
     * @author cuiwl
     * @date Oct 24, 2014
     */
    public static long getNowInAfterMonth(long time, int monthCount) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + monthCount);

        return cal.getTimeInMillis();
    }

    /**
     * 获取指定月前前的当前时间
     *
     * @param time
     * @return
     * @author yuanzy
     * @date 2014-12-11
     */
    public static Date getNowInBeforeMonth(Date time, int monthCount) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - monthCount);

        return cal.getTime();
    }

    public static String dateFmtToDateStr(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    /**
     * Copyright: Copyright (c) 2014 Asiainfo-Linkage
     *
     * @Description:首页时间展示
     * @version: v1.0.0
     * @author: zhangyao
     * @date: 2014-11-11下午8:12:35 Modification History Date Author Version
     * Description
     * ---------------------------------------------------------*
     */
    public static String longFmt2ShowDateForIndex(long time, String type) {

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeInMillis(time);

        int year = cal.get(Calendar.YEAR);
        int monthTemp = cal.get(Calendar.MONTH) + 1;
        int dayTemp = cal.get(Calendar.DAY_OF_MONTH);

        String month = String.valueOf(monthTemp < 10 ? "0" + monthTemp
            : monthTemp);
        String day = String.valueOf(dayTemp < 10 ? "0" + dayTemp : dayTemp);
        if (S_INDEX_TIME_TYPE_NOTICE.equals(type)) {
            return year + "-" + month + "-" + day;
        } else {
            return year + "." + month + "." + day;
        }
    }

    /**
     * Copyright: Copyright (c) 2014 Asiainfo-Linkage
     *
     * @Description:获取当月天数
     * @version: v1.0.0
     * @author: zhangyao
     * @date: 2014-11-18下午8:19:35 Modification History Date Author Version
     * Description
     * ---------------------------------------------------------*
     */
    public static int getCurrentMonthLastDay() {
        Calendar a = Calendar.getInstance();
        a.set(Calendar.DATE, 1);// 把日期设置为当月第一天
        a.roll(Calendar.DATE, -1);// 日期回滚一天，也就是最后一天
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }

    /**
     * Copyright: Copyright (c) 2014 Asiainfo-Linkage
     *
     * @Description:时间String转为long，用于分表
     * @version: v1.0.0
     * @author: zhangyao
     * @date: 2014-11-20上午10:21:09 Modification History Date Author Version
     * Description
     * ---------------------------------------------------------*
     */
    public static long dateFmt2long(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        return sdf.parse(date).getTime();
    }

    /**
     * Copyright: Copyright (c) 2014 Asiainfo-Linkage
     *
     * @Description:时间long转化为String，用于分表
     * @version: v1.0.0
     * @author: zhangyao
     * @date: 2014-11-20上午10:32:59 Modification History Date Author Version
     * Description
     * ---------------------------------------------------------*
     */
    public static String longFmt2YearMonth(long time) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTimeInMillis(time);
        int year = cal.get(Calendar.YEAR);
        int monthTemp = cal.get(Calendar.MONTH) + 1;
        String month = String.valueOf(monthTemp < 10 ? "0" + monthTemp
            : monthTemp);
        return year + month;
    }

    public static String thisMonth() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int monthTemp = calendar.get(Calendar.MONTH) + 1;
        String month = String.valueOf(monthTemp < 10 ? "0" + monthTemp
            : monthTemp);
        return year + month;
    }

    public static String lastMonth() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int monthTemp = calendar.get(Calendar.MONTH);
        String month = String.valueOf(monthTemp < 10 ? "0" + monthTemp
            : monthTemp);
        return year + month;
    }

    /**
     * Copyright: Copyright (c) 2014 Asiainfo-Linkage
     *
     * @Description:获取天数差
     * @version: v1.0.0
     * @author: zhangyao
     * @date: 2014-11-25上午10:43:57
     * Modification History
     * Date       Author      Version      Description
     * ---------------------------------------------------------*
     */
    public static int getDaySpace(long startTime, long endTime) {
        return (int) ((endTime - startTime) / ONE_DAY_MILLIS);
    }

    /**
     * Copyright: Copyright (c) 2016 Asiainfo-Linkage
     *
     * @Description:获取小时差
     * @version: v1.0.0
     * @author: yangry
     * @date: 2016-03-25下午 5:51
     * Modification History
     * Date       Author      Version      Description
     * ---------------------------------------------------------*
     */
    public static int getHourSpace(long startTime, long endTime) {
        return (int) ((endTime - startTime) / ONE_HOUR_MILLIS);
    }

    /**
     * Copyright: Copyright (c) 2016 Asiainfo-Linkage
     *
     * @Description:获取分钟差
     * @version: v1.0.0
     * @author: yangry
     * @date: 2016-03-25下午 5:51
     * Modification History
     * Date       Author      Version      Description
     * ---------------------------------------------------------*
     */
    public static int getMinSpace(long startTime, long endTime) {
        return (int) ((endTime - startTime) / ONE_MIN_MILLIS);
    }

    /**
     * Copyright: Copyright (c) 2016 Asiainfo-Linkage
     *
     * @Description:获取小时差
     * @version: v1.0.0
     * @author: yangry
     * @date: 2016-03-25下午 5:51
     * Modification History
     * Date       Author      Version      Description
     * ---------------------------------------------------------*
     */
    public static int getSpace(long startTime, long endTime) {
        return (int) ((endTime - startTime));
    }

    /**
     * Copyright: Copyright (c) 2016 Asiainfo-Linkage
     *
     * @Description:获取秒数差
     * @version: v1.0.0
     * @author: yangry
     * @date: 2016-03-25下午 5:51
     * Modification History
     * Date       Author      Version      Description
     * ---------------------------------------------------------*
     */
    public static int getSecSpace(long startTime, long endTime) {
        return (int) ((endTime - startTime) / ONE_SECOND_MILLIS);
    }

    /**
     * Copyright: Copyright (c) 2015 Asiainfo-Linkage
     *
     * @Description:OSB获取的时间转成long
     * @version: v1.0.0
     * @author: zhangyao
     * @date: 2015-5-27下午4:31:23
     * <p>
     * Modification History
     * Date       Author      Version      Description
     * ---------------------------------------------------------*
     */
    public static long string2TimeLong(String time)
        throws ParseException {
        if (StringUtils.isNotBlank(time)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
            return sdf.parse(time).getTime();
        }
        return -1;
    }


    /**
     * Copyright: Copyright (c) 2015 Asiainfo-Linkage
     *
     * @Description:获取当前时间的String格式yyyyMMddHHmmss
     * @version: v1.0.0
     * @author: zhangyao
     * @date: 2015-5-28下午4:00:52
     * <p>
     * Modification History
     * Date       Author      Version      Description
     * ---------------------------------------------------------*
     */
    public static String getNowTime() {
        long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date(time));
    }

    public static String getNowTimes() {
        long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        return sdf.format(new Date(time));
    }

    public static String getYYYYMMDD() {
        long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat(S_FORMAT_YYYYMMDD);
        return sdf.format(new Date(time));
    }

    public static String getYYYYMM() {
        long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        return sdf.format(new Date(time));
    }

    public static String longToString(long longTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date(longTime));
    }

    /**
     * @param date
     * @param currentpattern
     * @param formatPattern
     * @return
     * @author wangjun18
     */
    public static String formatDate(String date, String currentpattern, String formatPattern) {
        String result = "";
        if (StringUtils.isNotEmpty(date)) {
            Date date2 = string2Date(date, currentpattern);
            if (date2 != null) {
                result = date2String(date2, formatPattern);
            }
        }

        return StringUtils.isEmpty(result) ? date : StringUtils.trimToEmpty(result);
    }

    /**
     * @param date
     * @param currentpattern
     * @param formatPattern
     * @return
     * @author wangjun18
     */
    public static String formatDate(String date, DFormat currentpattern, DFormat formatPattern) {
        return formatDate(date, currentpattern.PATTERN, formatPattern.PATTERN);
    }

    /**
     * @param time
     * @param datepattern
     * @return
     * @author wangjun18
     */
    public static String date2String(Date time, String datepattern) {
        String pattern = datepattern;
        if (time != null) {
            if (StringUtils.isEmpty(pattern)) {
                pattern = DFormat.DATA_FORMAT_DEFAULT;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            try {
                return dateFormat.format(time);
            } catch (Exception e) {
                log.error(e.getMessage());
                return null;
            }
        }
        return null;
    }

    /**
     * @param strtime
     * @param datepattern
     * @return
     * @author wangjun18
     */
    public static Date string2Date(String strtime, String datepattern) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(datepattern);
            return dateFormat.parse(strtime, new ParsePosition(0));
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return null;
    }

    /**
     * @param strtime
     * @param datepattern
     * @return
     * @author wangjun18
     */
    public static Date string2Date(String strtime, DFormat datepattern) {
        return string2Date(strtime, datepattern.PATTERN);
    }

    /**
     * @param strtime
     * @param datepattern
     * @return
     * @author wangjun18
     */
    public static Timestamp string2Timestamp(String strtime, String datepattern) {
        Date date = string2Date(strtime, datepattern);
        if (date != null) {
            return new Timestamp(date.getTime());
        }

        return null;
    }

    /**
     * @param dtDate
     * @param strFormatTo
     * @return
     * @author wangjun18
     */
    public static String getFormattedDate(Timestamp dtDate, String strFormatTo) {
        if (dtDate == null) {
            return "";
        }

        if (dtDate.equals(new Timestamp(0))) {
            return "";
        }

        String format = strFormatTo.replace('/', '-');
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
            if (Integer.parseInt(formatter.format(dtDate)) < 1900) {
                return "";
            } else {
                formatter = new SimpleDateFormat(format);
                return formatter.format(dtDate);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return "";
        }
    }

    /**
     * @param dtDate
     * @return
     * @author wangjun18
     */
    public static String getFormattedYYYYMMDDDate(Timestamp dtDate) {
        return getFormattedDate(dtDate, "yyyyMMdd");
    }

    public static String stringDate2String(String date, String oldpattern,
                                           String newpattern) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(newpattern);
        return dateFormat.format(string2Date(date, oldpattern));
    }

    public static java.sql.Date StringToDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(dateString);
            return new java.sql.Date(date.getTime());
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static List<String> getDates(String begin_Date, String end_Date) {
        try {
            if (org.apache.commons.lang.StringUtils.isNotEmpty(begin_Date) && org.apache.commons.lang.StringUtils.isNotEmpty(end_Date)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date beginDate = sdf.parse(begin_Date);
                Date endDate = sdf.parse(end_Date);
                List<Date> lDate = new ArrayList<Date>();
                lDate.add(beginDate);//把开始时间加入集合
                Calendar cal = Calendar.getInstance();
                cal.setTime(beginDate);
                boolean bContinue = true;
                while (bContinue) {
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    if (endDate.after(cal.getTime())) {
                        lDate.add(cal.getTime());
                    } else {
                        break;
                    }
                }
                if (!begin_Date.equals(end_Date)) {
                    lDate.add(endDate);//把结束时间加入集合
                }
                List<String> dateList = new ArrayList<String>();
                if (lDate != null) {
                    String d1;
                    for (Date sdate : lDate) {
                        d1 = date2String(sdate, "yyyy-MM-dd");
                        if (d1.indexOf("-") >= 0) {
                            d1 = d1.substring(d1.indexOf("-") + 1);
                            dateList.add(d1);
                        }
                    }
                }
                return dateList;
            }
        } catch (Exception e) {

        }
        return null;
    }

    @NotNull
    public static String moveDate(SimpleDateFormat sdf, Calendar c, int day) {
        String endDate;
        c.add(Calendar.DAY_OF_MONTH, day);
        endDate = sdf.format(c.getTime());
        return endDate;
    }

    public static String tranTime(long time) {
        long minute = 0, second = 0;
        StringBuilder sb = new StringBuilder();
        time = time / 1000;
        if (time >= 0) {
            if (time / 60 > 0) {
                minute = time / 60;
            }
            second = time % 60;
        }
        if (minute > 0) {
            sb.append(minute + "分");
        }
        if (second >= 0) {
            sb.append(second + "秒");
        }
        return sb.toString();
    }

    public static Date stringToDate(String dateString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        return sdf.parse(dateString);
    }

    public static String dateToString(Date date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        return sdf.format(date);
    }

    /**
     * @author wangjun18
     */
    public enum DFormat {
        YYYY_MM_DDHH_MM_SS("yyyy-MM-dd HH:mm:ss");

        public static final String DATA_FORMAT_DEFAULT = getDefaultEnum().PATTERN;
        public String PATTERN;

        DFormat(String pattern) {
            this.PATTERN = pattern;
        }

        private static DFormat getDefaultEnum() {
            return YYYY_MM_DDHH_MM_SS;
        }
    }
}
