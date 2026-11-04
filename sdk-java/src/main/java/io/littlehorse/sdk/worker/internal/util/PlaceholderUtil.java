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
            final String placeholderToken = matcher.group(1);
            final String placeholderKey;
            final String defaultValue;

            final int separatorIndex = placeholderToken.indexOf(':');
            if (separatorIndex >= 0) {
                placeholderKey = placeholderToken.substring(0, separatorIndex);
                defaultValue = placeholderToken.substring(separatorIndex + 1);
            } else {
                placeholderKey = placeholderToken;
                defaultValue = null;
            }

            final String replacement = values.containsKey(placeholderKey) ? values.get(placeholderKey) : defaultValue;

            if (replacement == null) {
                throw new IllegalArgumentException("No value has been provided for the placeholder with key: "
                        + placeholderKey + ". No default value supplied for the placeholder either.");
            }

            matcher.appendReplacement(resultingText, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(resultingText);
        return resultingText.toString();
    }
}
