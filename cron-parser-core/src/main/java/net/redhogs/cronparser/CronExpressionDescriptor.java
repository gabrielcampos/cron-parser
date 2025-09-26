package net.redhogs.cronparser;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.redhogs.cronparser.builder.DayOfMonthDescriptionBuilder;
import net.redhogs.cronparser.builder.DayOfWeekDescriptionBuilder;
import net.redhogs.cronparser.builder.HoursDescriptionBuilder;
import net.redhogs.cronparser.builder.MinutesDescriptionBuilder;
import net.redhogs.cronparser.builder.MonthDescriptionBuilder;
import net.redhogs.cronparser.builder.SecondsDescriptionBuilder;
import net.redhogs.cronparser.builder.YearDescriptionBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author grhodes
 * @since 10 Dec 2012 11:36:38
 */
public class CronExpressionDescriptor {

    private static final Logger LOG = LoggerFactory.getLogger(CronExpressionDescriptor.class);
    private static final char[] specialCharacters = new char[]{'/', '-', ',', '*'};

    public static String getDescription(String expression) throws ParseException {
        return getDescription(DescriptionTypeEnum.FULL, expression, new Options(), I18nMessages.DEFAULT_LOCALE);
    }

    public static String getDescription(String expression, Options options) throws ParseException {
        return getDescription(DescriptionTypeEnum.FULL, expression, options, I18nMessages.DEFAULT_LOCALE);
    }

    public static String getDescription(String expression, Locale locale) throws ParseException {
        return getDescription(DescriptionTypeEnum.FULL, expression, new Options(), locale);
    }

    public static String getDescription(String expression, Options options, Locale locale) throws ParseException {
        return getDescription(DescriptionTypeEnum.FULL, expression, options, locale);
    }

    public static String getDescription(DescriptionTypeEnum type, String expression) throws ParseException {
        return getDescription(type, expression, new Options(), I18nMessages.DEFAULT_LOCALE);
    }

    public static String getDescription(DescriptionTypeEnum type, String expression, Locale locale) throws ParseException {
        return getDescription(type, expression, new Options(), locale);
    }

    public static String getDescription(DescriptionTypeEnum type, String expression, Options options) throws ParseException {
        return getDescription(type, expression, options, I18nMessages.DEFAULT_LOCALE);
    }

    public static String getDescription(DescriptionTypeEnum type, String expression, Options options, Locale locale) throws ParseException {
        I18nMessages.setCurrentLocale(locale);
        String[] expressionParts;
        String description = "";
        try {
            expressionParts = ExpressionParser.parse(expression, options);
            description = switch (type) {
                case FULL ->
                    getFullDescription(expressionParts, options);
                case TIMEOFDAY ->
                    getTimeOfDayDescription(expressionParts, options);
                case HOURS ->
                    getHoursDescription(expressionParts, options);
                case MINUTES ->
                    getMinutesDescription(expressionParts, options);
                case SECONDS ->
                    getSecondsDescription(expressionParts, options);
                case DAYOFMONTH ->
                    getDayOfMonthDescription(expressionParts, options);
                case MONTH ->
                    getMonthDescription(expressionParts, options);
                case DAYOFWEEK ->
                    getDayOfWeekDescription(expressionParts, options);
                case YEAR ->
                    getYearDescription(expressionParts, options);
                default ->
                    getSecondsDescription(expressionParts, options);
            };
        } catch (ParseException e) {
            if (!options.isThrowExceptionOnParseError()) {
                description = e.getMessage();
                LOG.debug("Exception parsing expression.", e);
            } else {
                LOG.error("Exception parsing expression.", e);
                throw e;
            }
        }
        return description;
    }

    /**
     * @param expressionParts
     * @return
     */
    private static String getYearDescription(String[] expressionParts, Options options) {
        return new YearDescriptionBuilder(options).getSegmentDescription(expressionParts[6], ", " + I18nMessages.get("every_year"));
    }

    /**
     * @param expressionParts
     * @return
     */
    private static String getDayOfWeekDescription(String[] expressionParts, Options options) {
        return new DayOfWeekDescriptionBuilder(options).getSegmentDescription(expressionParts[5], ", " + I18nMessages.get("every_day"));
    }

    /**
     * @param expressionParts
     * @return
     */
    private static String getMonthDescription(String[] expressionParts, Options options) {
        return new MonthDescriptionBuilder(options).getSegmentDescription(expressionParts[4], "");
    }

    /**
     * @param expressionParts
     * @return
     */
    private static String getDayOfMonthDescription(String[] expressionParts, Options options) {
        String description;
        String exp = expressionParts[3].replace("?", "*");
        if ("L".equals(exp)) {
            description = ", " + I18nMessages.get("on_the_last_day_of_the_month");
        } else if ("WL".equals(exp) || "LW".equals(exp)) {
            description = ", " + I18nMessages.get("on_the_last_weekday_of_the_month");
        } else {
            Pattern pattern = Pattern.compile("(\\dW)|(W\\d)");
            Matcher matcher = pattern.matcher(exp);
            if (matcher.matches()) {
                int dayNumber = Integer.parseInt(matcher.group().replace("W", ""));
                String dayString = dayNumber == 1 ? I18nMessages.get("first_weekday") : MessageFormat.format(I18nMessages.get("weekday_nearest_day"), dayNumber);
                description = MessageFormat.format(", " + I18nMessages.get("on_the_of_the_month"), dayString);
            } else {
                description = new DayOfMonthDescriptionBuilder(options).getSegmentDescription(exp, ", " + I18nMessages.get("every_day"));
            }
        }
        return description;
    }

    /**
     * @param expressionParts
     * @return
     */
    private static String getSecondsDescription(String[] expressionParts, Options opts) {
        return new SecondsDescriptionBuilder(opts).getSegmentDescription(expressionParts[0], I18nMessages.get("every_second"));
    }

    /**
     * @param expressionParts
     * @return
     */
    private static String getMinutesDescription(String[] expressionParts, Options opts) {
        return new MinutesDescriptionBuilder(opts).getSegmentDescription(expressionParts[1], I18nMessages.get("every_minute"));
    }

    /**
     * @param expressionParts
     * @return
     */
    private static String getHoursDescription(String[] expressionParts, Options opts) {
        return new HoursDescriptionBuilder(opts).getSegmentDescription(expressionParts[2], I18nMessages.get("every_hour"));
    }

    /**
     * @param expressionParts
     * @return
     */
    private static String getTimeOfDayDescription(String[] expressionParts, Options opts) {
        String secondsExpression = expressionParts[0];
        String minutesExpression = expressionParts[1];
        String hoursExpression = expressionParts[2];
        StringBuilder description = new StringBuilder();
        // Handle special cases first
        if (!StringUtils.containsAny(minutesExpression, specialCharacters) && !StringUtils.containsAny(hoursExpression, specialCharacters) && !StringUtils.containsAny(secondsExpression, specialCharacters)) {
            description.append(I18nMessages.get("at"));
            if (opts.isNeedSpaceBetweenWords()) {
                description.append(" ");
            }
            description.append(DateAndTimeUtils.formatTime(hoursExpression, minutesExpression, secondsExpression, opts)); // Specific time of day (e.g. 10 14)
        } else if (minutesExpression.contains("-") && !minutesExpression.contains("/") && !StringUtils.containsAny(hoursExpression, specialCharacters)) {
            // Minute range in single hour (e.g. 0-10 11)
            String[] minuteParts = minutesExpression.split("-");
            description.append(MessageFormat.format(I18nMessages.get("every_minute_between"), DateAndTimeUtils.formatTime(hoursExpression, minuteParts[0], opts),
                    DateAndTimeUtils.formatTime(hoursExpression, minuteParts[1], opts)));
        } else if (hoursExpression.contains(",") && !StringUtils.containsAny(minutesExpression, specialCharacters)) {
            // Hours list with single minute (e.g. 30 6,14,16)
            String[] hourParts = hoursExpression.split(",");
            description.append(I18nMessages.get("at"));
            for (int i = 0; i < hourParts.length; i++) {
                if (opts.isNeedSpaceBetweenWords()) {
                    description.append(" ");
                }
                description.append(DateAndTimeUtils.formatTime(hourParts[i], minutesExpression, opts));
                if (i < hourParts.length - 2) {
                    description.append(",");
                }
                if (i == hourParts.length - 2) {
                    if (opts.isNeedSpaceBetweenWords()) {
                        description.append(" ");
                    }
                    description.append(I18nMessages.get("and"));
                }
            }
        } else {
            String secondsDescription = getSecondsDescription(expressionParts, opts);
            String minutesDescription = getMinutesDescription(expressionParts, opts);
            String hoursDescription = getHoursDescription(expressionParts, opts);
            description.append(secondsDescription);
            if (description.length() > 0 && StringUtils.isNotEmpty(minutesDescription)) {
                description.append(", ");
            }
            description.append(minutesDescription);
            if (description.length() > 0 && StringUtils.isNotEmpty(hoursDescription)) {
                description.append(", ");
            }
            description.append(hoursDescription);
        }
        return description.toString();
    }

    /**
     * @param options
     * @param expressionParts
     * @return
     */
    private static String getFullDescription(String[] expressionParts, Options options) {
        String timeSegment = getTimeOfDayDescription(expressionParts, options);
        String dayOfMonthDesc = getDayOfMonthDescription(expressionParts, options);
        String monthDesc = getMonthDescription(expressionParts, options);
        String dayOfWeekDesc = getDayOfWeekDescription(expressionParts, options);
        String yearDesc = getYearDescription(expressionParts, options);
        String description = MessageFormat.format("{0}{1}{2}{3}", timeSegment, ("*".equals(expressionParts[3]) ? dayOfWeekDesc : dayOfMonthDesc), monthDesc, yearDesc);
        description = transformVerbosity(description, options);
        description = transformCase(description, options);
        return description;
    }

    /**
     * @param description
     * @return
     */
    private static String transformCase(String description, Options options) {
        return switch (options.getCasingType()) {
            case Sentence ->
                StringUtils.upperCase("" + description.charAt(0)) + description.substring(1);
            case Title ->
                StringUtils.capitalize(description);
            default ->
                description.toLowerCase();
        };
    }

    /**
     * @param description
     * @param options
     * @return
     */
    private static String transformVerbosity(String description, Options options) {
        String descTemp = description;
        if (!options.isVerbose()) {
            descTemp = descTemp.replace(I18nMessages.get("every_1_minute"), I18nMessages.get("every_minute"));
            descTemp = descTemp.replace(I18nMessages.get("every_1_hour"), I18nMessages.get("every_hour"));
            descTemp = descTemp.replace(I18nMessages.get("every_1_day"), I18nMessages.get("every_day"));
            descTemp = descTemp.replace(", " + I18nMessages.get("every_minute"), "");
            descTemp = descTemp.replace(", " + I18nMessages.get("every_hour"), "");
            descTemp = descTemp.replace(", " + I18nMessages.get("every_day"), "");
            descTemp = descTemp.replace(", " + I18nMessages.get("every_year"), "");
        }
        return descTemp;
    }

    private CronExpressionDescriptor() {
    }
}
