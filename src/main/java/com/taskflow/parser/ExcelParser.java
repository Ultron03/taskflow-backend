package com.taskflow.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ExcelParser {

    private final DataFormatter formatter = new DataFormatter();

    public List<ParsedTaskDTO> parse(InputStream inputStream) throws Exception {
        List<ParsedTaskDTO> results = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return results;

            int titleCol = -1, difficultyCol = -1, categoryCol = -1, urlCol = -1;

            for (Cell cell : headerRow) {
                String header = formatter.formatCellValue(cell).toLowerCase().trim();
                int idx = cell.getColumnIndex();
                if (header.contains("task") || header.contains("title")) titleCol = idx;
                else if (header.contains("difficulty") || header.contains("level")) difficultyCol = idx;
                else if (header.contains("category") || header.contains("type")) categoryCol = idx;
                else if (header.contains("url") || header.contains("link")) urlCol = idx;
            }

            if (titleCol == -1) {
                titleCol = 0;
                difficultyCol = 1;
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String title = getCellValue(row, titleCol);
                if (title == null || title.isBlank()) continue;

                String difficulty = difficultyCol >= 0 ? getCellValue(row, difficultyCol) : null;
                String category = categoryCol >= 0 ? getCellValue(row, categoryCol) : null;
                String url = urlCol >= 0 ? getCellValue(row, urlCol) : null;
                Integer xp = computeXp(difficulty);

                results.add(new ParsedTaskDTO(title.trim(), normalizeDifficulty(difficulty), category, url, xp, null));
            }
        }

        log.info("Excel parsed {} tasks", results.size());
        return results;
    }

    private String getCellValue(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        String value = formatter.formatCellValue(cell);
        return value.isBlank() ? null : value;
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
