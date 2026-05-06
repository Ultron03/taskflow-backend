package com.taskflow.parser;

import com.taskflow.list.TaskListService;
import com.taskflow.task.Difficulty;
import com.taskflow.task.TaskService;
import com.taskflow.task.dto.TaskRequest;
import com.taskflow.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileParserService {

    private final ExcelParser excelParser;
    private final CsvParser csvParser;
    private final PdfParser pdfParser;
    private final TxtParser txtParser;
    private final TaskListService taskListService;
    private final TaskService taskService;

    public Map<String, Object> parseFile(Long listId, User user, MultipartFile file) throws Exception {
        taskListService.findOwned(listId, user);

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        List<ParsedTaskDTO> tasks;

        if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            tasks = excelParser.parse(file.getInputStream());
        } else if (filename.endsWith(".csv")) {
            tasks = csvParser.parse(file.getInputStream());
        } else if (filename.endsWith(".pdf")) {
            tasks = pdfParser.parse(file.getInputStream());
        } else if (filename.endsWith(".txt")) {
            tasks = txtParser.parse(file.getInputStream());
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + filename);
        }

        List<String> warnings = tasks.stream()
            .filter(t -> t.parseWarning() != null)
            .map(ParsedTaskDTO::parseWarning)
            .toList();

        log.info("Parsed {} tasks from file {} for list {}", tasks.size(), filename, listId);
        return Map.of("tasks", tasks, "parseWarnings", warnings);
    }

    @Transactional
    public Map<String, Integer> confirmImport(Long listId, User user, List<ParsedTaskDTO> tasks) {
        int created = taskService.bulkCreate(listId, user, tasks.stream().map(dto ->
            new TaskRequest(
                dto.title(),
                null,
                null,
                dto.difficulty() != null ? parseDifficulty(dto.difficulty()) : null,
                dto.category(),
                null,
                dto.externalUrl(),
                dto.xpValue(),
                null,
                null
            )
        ).toList());
        return Map.of("created", created);
    }

    private Difficulty parseDifficulty(String s) {
        try {
            return Difficulty.valueOf(s.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
