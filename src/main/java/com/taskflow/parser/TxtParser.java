package com.taskflow.parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
@Slf4j
public class TxtParser {

    private static final Pattern BULLET_PREFIX = Pattern.compile("^[-•☐□*]\\s+|^\\d+\\.\\s+");
    private static final Pattern SECTION_HEADER = Pattern.compile("^[A-Z][A-Z\\s]+:?$|^.+:$");
    private static final Pattern DIFFICULTY_KEYWORD = Pattern.compile("(?i)\\b(easy|medium|hard)\\b");

    public List<ParsedTaskDTO> parse(InputStream inputStream) throws Exception {
        List<ParsedTaskDTO> results = new ArrayList<>();
        String currentCategory = null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isBlank()) continue;

                if (SECTION_HEADER.matcher(line).matches()) {
                    currentCategory = line.replaceAll(":$", "").trim();
                    continue;
                }

                String title = BULLET_PREFIX.matcher(line).replaceFirst("").trim();
                if (title.isBlank()) continue;

                String difficulty = null;
                var matcher = DIFFICULTY_KEYWORD.matcher(line);
                if (matcher.find()) {
                    difficulty = normalizeDifficulty(matcher.group(1));
                    title = title.replaceAll("(?i)\\b(easy|medium|hard)\\b", "").trim();
                }

                Integer xp = computeXp(difficulty);
                results.add(new ParsedTaskDTO(title, difficulty, currentCategory, null, xp, null));
            }
        }

        log.info("TXT parsed {} tasks", results.size());
        return results;
    }

    private String normalizeDifficulty(String raw) {
        if (raw == null) return null;
        return switch (raw.toLowerCase()) {
            case "easy" -> "EASY";
            case "medium" -> "MEDIUM";
            case "hard" -> "HARD";
            default -> null;
        };
    }

    private Integer computeXp(String difficulty) {
        if (difficulty == null) return 10;
        return switch (difficulty) {
            case "MEDIUM" -> 25;
            case "HARD" -> 50;
            default -> 10;
        };
    }
}
