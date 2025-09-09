package net.redhogs.cronparser;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

/**
 * @author grhodes
 * @since 10 Dec 2012 14:18:58
 */
public final class DateAndTimeUtils {

    /**
     * @param hoursExpression
     * @param minutesExpression
     * @param opts
     * @return
     */
    public static String formatTime(String hoursExpression, String minutesExpression, Options opts) {
        return formatTime(hoursExpression, minutesExpression, "", opts);
    }

    /**
     * @param hoursExpression
     * @param minutesExpression
     * @param secondsExpression
     * @param opts
     * @return
     */
    public static String formatTime(String hoursExpression, String minutesExpression, String secondsExpression, Options opts) {
        int hour = Integer.parseInt(hoursExpression);
        int minutes = Integer.parseInt(minutesExpression);

        LocalTime localTime;
        DateTimeFormatter timeFormat;

        if (opts.isTwentyFourHourTime()) {
            if (!StringUtils.isEmpty(secondsExpression)) {
                final int seconds = Integer.parseInt(secondsExpression);
                localTime = LocalTime.of(hour, minutes, seconds);
                timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);
            } else {
                localTime = LocalTime.of(hour, minutes);
                timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
            }
        } else {
            if (!StringUtils.isEmpty(secondsExpression)) {
                final int seconds = Integer.parseInt(secondsExpression);
                localTime = LocalTime.of(hour, minutes, seconds);
                timeFormat = DateTimeFormatter.ofPattern("h:mm:ss a");
            } else {
                localTime = LocalTime.of(hour, minutes);
                timeFormat = DateTimeFormatter.ofPattern("h:mm a");
            }
        }
        return localTime.format(timeFormat.withLocale(I18nMessages.getCurrentLocale()));
    }

    public static String getDayOfWeekName(int dayOfWeek) {
        return DayOfWeek.of(dayOfWeek).getDisplayName(TextStyle.FULL, I18nMessages.getCurrentLocale());
    }

    /**
     * @param minutesExpression
     * @return
     * @since https://github.com/RedHogs/cron-parser/issues/2
     */
    public static String formatMinutes(String minutesExpression) {
        if (Strings.CS.contains(minutesExpression, ",")) {
            StringBuilder formattedExpression = new StringBuilder();
            for (String minute : StringUtils.split(minutesExpression, ',')) {
                formattedExpression.append(StringUtils.leftPad(minute, 2, '0'));
                formattedExpression.append(",");
            }
            return formattedExpression.toString();
        }
        return StringUtils.leftPad(minutesExpression, 2, '0');
    }

    private DateAndTimeUtils() {
    }
}
