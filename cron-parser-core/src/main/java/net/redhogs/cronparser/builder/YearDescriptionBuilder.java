package net.redhogs.cronparser.builder;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import net.redhogs.cronparser.I18nMessages;
import net.redhogs.cronparser.Options;

/**
 * @author grhodes
 * @since 15 Sep 2014
 */
public class YearDescriptionBuilder extends AbstractDescriptionBuilder {

    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");

    private final Options options;

    public YearDescriptionBuilder(Options options) {
        this.options = options;
    }

    @Override
    protected String getSingleItemDescription(String expression) {
        return LocalDateTime.now().withYear(Integer.parseInt(expression)).format(YEAR_FORMATTER.withLocale(I18nMessages.getCurrentLocale()));
    }

    @Override
    protected String getIntervalDescriptionFormat(String expression) {
        return MessageFormat.format(", " + I18nMessages.get("every_x") + getSpace(options) + plural(expression, I18nMessages.get("year"), I18nMessages.get("years")), expression);
    }

    @Override
    protected String getBetweenDescriptionFormat(String expression, boolean omitSeparator) {
        String format = I18nMessages.get("between_description_format");
        return omitSeparator ? format : ", " + format;
    }

    @Override
    protected String getDescriptionFormat(String expression) {
        return ", " + I18nMessages.get("only_in_year");
    }

    @Override
    protected Boolean needSpaceBetweenWords() {
        return options.isNeedSpaceBetweenWords();
    }
}
