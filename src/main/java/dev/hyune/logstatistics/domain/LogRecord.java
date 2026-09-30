package dev.hyune.logstatistics.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record LogRecord(
        int statusCode,
        String url,
        String browser,
        LocalDateTime timestamp
) {
    private static final Pattern API_SERVICE_PATTERN = Pattern.compile("http://apis\\.kokoa\\.com/search/([^?]+)");
    private static final Pattern API_KEY_PATTERN = Pattern.compile("apikey=([a-zA-Z0-9]{4})");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String extractApiServiceId() {
        Matcher matcher = API_SERVICE_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public String extractApiKey() {
        Matcher matcher = API_KEY_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public boolean isSuccess() {
        return statusCode == 200;
    }

    public static LocalDateTime parseTimestamp(String timestampStr) {
        return LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER);
    }
}
