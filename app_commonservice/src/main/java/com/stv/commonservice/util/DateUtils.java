
package com.stv.commonservice.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Date工具类
 */
public class DateUtils {
    private static LogUtils sLog = LogUtils.getInstance("Common", DateUtils.class.getSimpleName());

    /**
     * 格林治时间
     */
    public static String getNowDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat greenwichDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'",
                Locale.US);
        greenwichDate.setTimeZone(TimeZone.getTimeZone("GMT"));
        return greenwichDate.format(cal.getTime());
    }

    /**
     * UTC时间
     */
    public static String getUtcTime() {
        Calendar cal = Calendar.getInstance();
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
        int dstOffset = cal.get(Calendar.DST_OFFSET);
        cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        Date date = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMMM yyyy HH:mm:ss Z", Locale.US);
        return sdf.format(date);
    }

    public static final String formatDate(long time, String format) {
        SimpleDateFormat formater = new SimpleDateFormat(format, Locale.CHINA);
        Date d = new Date(time);
        return formater.format(d);
    }

    /**
     * 解析String类型date数据
     * @param time
     * @param format
     * @return long型time数据，0如果解析失败
     */
    public static final long parseDate(String time, String format) {
        if (null == time || null == format) {
            return 0;
        }
        SimpleDateFormat formater = new SimpleDateFormat(format, Locale.CHINA);
        Date date;
        long time2 = 0;
        try {
            date = formater.parse(time);
            time2 = date.getTime();
        } catch (ParseException e) {
            sLog.e(e);
        }
        return time2;
    }
}
