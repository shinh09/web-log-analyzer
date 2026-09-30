package dev.hyune.logstatistics.analyzer;

import dev.hyune.logstatistics.domain.LogRecord;
import dev.hyune.logstatistics.dto.ApiServiceCount;
import dev.hyune.logstatistics.dto.LogStatisticsResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class LogAnalyzer {

    public LogStatisticsResult analyze(List<LogRecord> records) {
        String mostFrequentApiKey = findMostFrequentApiKey(records);
        List<ApiServiceCount> topApiServices = findTopApiServices(records, 3);
        Map<String, Double> browserUsageRatios = calculateBrowserUsageRatios(records);

        return new LogStatisticsResult(mostFrequentApiKey, topApiServices, browserUsageRatios);
    }

    /** 최다 호출 API Key (모든 상태코드 기준) */
    private String findMostFrequentApiKey(List<LogRecord> records) {
        Map<String, Long> apiKeyCount = records.stream()
                .map(LogRecord::extractApiKey)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        apiKey -> apiKey,
                        Collectors.counting()
                ));

        return apiKeyCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /** 상위 N개 API Service ID (모든 상태코드 기준) */
    private List<ApiServiceCount> findTopApiServices(List<LogRecord> records, int topN) {
        Map<String, Long> serviceCount = records.stream()
                .map(LogRecord::extractApiServiceId)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        serviceId -> serviceId,
                        Collectors.counting()
                ));

        return serviceCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .map(entry -> new ApiServiceCount(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /** 브라우저별 사용 비율 (상태코드 200만, 백분율) */
    private Map<String, Double> calculateBrowserUsageRatios(List<LogRecord> records) {
        List<LogRecord> successRecords = records.stream()
                .filter(LogRecord::isSuccess)
                .toList();

        if (successRecords.isEmpty()) {
            return Collections.emptyMap();
        }

        long totalCount = successRecords.size();

        Map<String, Long> browserCount = successRecords.stream()
                .collect(Collectors.groupingBy(
                        LogRecord::browser,
                        Collectors.counting()
                ));

        return browserCount.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (entry.getValue() * 100.0) / totalCount
                ));
    }
}
