package com.metropolis.util;


import de.jollyday.HolidayCalendar;
import de.jollyday.HolidayManager;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.util.Locale;

public class Dates {

    private static final HolidayManager holidayManager = HolidayManager.getInstance(HolidayCalendar.NYSE);


    public static String dateToString(final DateTime dateTime) {
        return String.format("%d-%02d-%02d", dateTime.getYearOfEra(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
    }

    public static boolean isWeekend(final DateTime dateTime) {
        return (dateTime.getDayOfWeek() == DateTimeConstants.SATURDAY ||
                dateTime.getDayOfWeek() == DateTimeConstants.SUNDAY);
    }

    public static boolean isHoliday(final DateTime dateTime) {
        return holidayManager.isHoliday(dateTime.toCalendar(Locale.US));
    }

    public static DateTime lastBusinessDayFrom(final DateTime dateTime) {
        DateTime lastDayCandidate = dateTime.minusDays(1);
        while (isHoliday(lastDayCandidate) || isWeekend(lastDayCandidate)) {
            lastDayCandidate = lastDayCandidate.minusDays(1);
        }
        return lastDayCandidate;
    }

    public static DateTime businessDaysAgo(final DateTime dateTime, int daysAgo) {
        DateTime lastDayCandidate = dateTime.minusDays(daysAgo);
        while (isHoliday(lastDayCandidate) || isWeekend(lastDayCandidate)) {
            lastDayCandidate = lastDayCandidate.minusDays(1);
        }
        return lastDayCandidate;
    }

}
