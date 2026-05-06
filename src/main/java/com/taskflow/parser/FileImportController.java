package com.taskflow.parser;

import com.taskflow.user.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/lists/{listId}/import")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FileImportController {

    private final FileParserService fileParserService;

    @PostMapping("/parse")
    public ResponseEntity<Map<String, Object>> parseFile(
        @PathVariable Long listId,
        @AuthenticationPrincipal User user,
        @RequestParam("file") MultipartFile file
    ) throws Exception {
        return ResponseEntity.ok(fileParserService.parseFile(listId, user, file));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Integer>> confirmImport(
        @PathVariable Long listId,
        @AuthenticationPrincipal User user,
        @RequestBody List<ParsedTaskDTO> tasks
    ) {
        return ResponseEntity.ok(fileParserService.confirmImport(listId, user, tasks));
    }
}
