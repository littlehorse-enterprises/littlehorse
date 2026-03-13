package io.littlehorse.sdk.worker.internal.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlaceholderUtil {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{(.*?)\\}", Pattern.DOTALL);

    private PlaceholderUtil() {}

    public static String replacePlaceholders(String template, Map<String, String> values) {
        final StringBuilder resultingText = new StringBuilder();
        final Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);

        while (matcher.find()) {
            final String placeholderKey = matcher.group(1);
            final String replacement = values.get(placeholderKey);

            if (replacement == null) {
                throw new IllegalArgumentException(
                        "No value has been provided for the placeholder with key: " + placeholderKey);
            }

            matcher.appendReplacement(resultingText, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(resultingText);
        return resultingText.toString();
    }
}
