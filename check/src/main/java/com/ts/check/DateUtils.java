package com.ts.check;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    /**
     * 获得日期的星期几，第一天为星期一。例如是星期一，则返回1
     * 
     * @param date
     * @return
     */
    public static int getChinaDayOfWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int enDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int chinaDayOfWeek = enDayOfWeek - 1;
        if (0 == chinaDayOfWeek) {
            chinaDayOfWeek = 7;
        }
        return chinaDayOfWeek;
    }

    public static DayType getDayDes(Date date) {
        int day = getChinaDayOfWeek(date);
        if (day == 6 || day == 7) {
            return DayType.WEEKEND;
        } else {
            return DayType.WORKDAY;
        }
    }

    /**
     * @param checkDate
     * @return
     */
    public static String getLastDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 1);
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());

    }
}
