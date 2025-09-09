package net.redhogs.cronparser.builder;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import net.redhogs.cronparser.I18nMessages;
import net.redhogs.cronparser.Options;

/**
 * @author grhodes
 * @since 10 Dec 2012 14:23:50
 */
public class MonthDescriptionBuilder extends AbstractDescriptionBuilder {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM");

    private final Options options;

    public MonthDescriptionBuilder(Options options) {
        this.options = options;
    }

    @Override
    protected String getSingleItemDescription(String expression) {
        return LocalDateTime.now().withDayOfMonth(1).withMonth(Integer.parseInt(expression)).format(MONTH_FORMATTER.withLocale(I18nMessages.getCurrentLocale()));
    }

    @Override
    protected String getIntervalDescriptionFormat(String expression) {
        return MessageFormat.format(", " + I18nMessages.get("every_x") + getSpace(options) + plural(expression, I18nMessages.get("month"), I18nMessages.get("months")), expression);
    }

    @Override
    protected String getBetweenDescriptionFormat(String expression, boolean omitSeparator) {
        String format = I18nMessages.get("between_description_format");
        return omitSeparator ? format : ", " + format;
    }

    @Override
    protected String getDescriptionFormat(String expression) {
        return ", " + I18nMessages.get("only_in_month");
    }

    @Override
    protected Boolean needSpaceBetweenWords() {
        return options.isNeedSpaceBetweenWords();
    }
}
