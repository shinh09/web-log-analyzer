package dev.hyune.logstatistics.parser;

import dev.hyune.logstatistics.domain.LogRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParser {
    private static final Pattern LOG_PATTERN = Pattern.compile("\\[(\\d+)]\\[(.*?)]\\[(.*?)]\\[(.*?)]");

    public List<LogRecord> parseStream(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return parseLines(reader);
        }
    }

    private List<LogRecord> parseLines(BufferedReader reader) throws IOException {
        List<LogRecord> records = new ArrayList<>();
        String line;
        int lineNumber = 0;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            try {
                LogRecord record = parseLine(line);
                records.add(record);
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: Failed to parse line " + lineNumber + ": " + line);
                System.err.println("Reason: " + e.getMessage());
            }
        }

        return records;
    }

    private LogRecord parseLine(String line) {
        Matcher matcher = LOG_PATTERN.matcher(line);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid log format: " + line);
        }

        try {
            int statusCode = Integer.parseInt(matcher.group(1));
            String url = matcher.group(2);
            String browser = matcher.group(3);
            String timestampStr = matcher.group(4);
            LocalDateTime timestamp = LogRecord.parseTimestamp(timestampStr);

            return new LogRecord(statusCode, url, browser, timestamp);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse log line: " + line, e);
        }
    }
}
