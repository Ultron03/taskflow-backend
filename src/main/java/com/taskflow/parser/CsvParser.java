package com.taskflow.parser;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.CSVReaderHeaderAwareBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class CsvParser {

    public List<ParsedTaskDTO> parse(InputStream inputStream) throws Exception {
        byte[] bytes = inputStream.readAllBytes();

        List<ParsedTaskDTO> results = tryParse(bytes, ',');
        if (results.isEmpty()) {
            results = tryParse(bytes, ';');
        }

        log.info("CSV parsed {} tasks", results.size());
        return results;
    }

    private List<ParsedTaskDTO> tryParse(byte[] bytes, char separator) throws Exception {
        List<ParsedTaskDTO> results = new ArrayList<>();

        try (CSVReaderHeaderAware reader = new CSVReaderHeaderAwareBuilder(
            new InputStreamReader(new java.io.ByteArrayInputStream(bytes), StandardCharsets.UTF_8))
            .withCSVParser(new com.opencsv.CSVParserBuilder().withSeparator(separator).build())
            .build()) {

            Map<String, String> row;
            while ((row = reader.readMap()) != null) {
                String title = findValue(row, "title", "task", "name");
                if (title == null || title.isBlank()) continue;

                String difficulty = findValue(row, "difficulty", "level");
                String category = findValue(row, "category", "type");
                String url = findValue(row, "url", "link", "externalurl");
                Integer xp = computeXp(difficulty);

                results.add(new ParsedTaskDTO(
                    title.trim(),
                    normalizeDifficulty(difficulty),
                    category,
                    url,
                    xp,
                    null
                ));
            }
        }
        return results;
    }

    private String findValue(Map<String, String> row, String... keys) {
        for (String key : keys) {
            for (Map.Entry<String, String> entry : row.entrySet()) {
                if (entry.getKey().toLowerCase().trim().contains(key.toLowerCase())) {
                    String val = entry.getValue();
                    return (val != null && !val.isBlank()) ? val : null;
                }
            }
        }
        return null;
    }

    private String normalizeDifficulty(String raw) {
        if (raw == null) return null;
        return switch (raw.trim().toLowerCase()) {
            case "easy", "e" -> "EASY";
            case "medium", "med", "m" -> "MEDIUM";
            case "hard", "h" -> "HARD";
            case "project" -> "PROJECT";
            case "concept" -> "CONCEPT";
            default -> null;
        };
    }

    private Integer computeXp(String difficulty) {
        if (difficulty == null) return 10;
        return switch (difficulty.trim().toLowerCase()) {
            case "medium", "med", "m" -> 25;
            case "hard", "h" -> 50;
            case "project" -> 100;
            case "concept" -> 15;
            default -> 10;
        };
    }
}
