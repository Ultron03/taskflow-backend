package com.taskflow.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
@Slf4j
public class PdfParser {

    private static final Pattern BULLET_PREFIX = Pattern.compile("^[-•☐□]\\s+|^\\d+\\.\\s+");
    private static final Pattern DIFFICULTY_KEYWORD = Pattern.compile("(?i)\\b(easy|medium|hard)\\b");

    public List<ParsedTaskDTO> parse(InputStream inputStream) throws Exception {
        List<ParsedTaskDTO> results = new ArrayList<>();

        try (PDDocument doc = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);

            for (String line : text.split("\\r?\\n")) {
                line = line.trim();
                if (line.isBlank()) continue;

                String title = BULLET_PREFIX.matcher(line).replaceFirst("").trim();
                if (title.isBlank()) continue;

                String difficulty = null;
                var matcher = DIFFICULTY_KEYWORD.matcher(line);
                if (matcher.find()) {
                    difficulty = normalizeDifficulty(matcher.group(1));
                    title = title.replaceAll("(?i)\\b(easy|medium|hard)\\b", "").trim();
                }

                Integer xp = computeXp(difficulty);
                results.add(new ParsedTaskDTO(title, difficulty, null, null, xp, null));
            }
        }

        log.info("PDF parsed {} tasks", results.size());
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
