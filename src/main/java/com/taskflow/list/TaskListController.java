package com.taskflow.list;

import com.taskflow.user.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/lists")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TaskListController {

    private final TaskListService taskListService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getLists(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(taskListService.getAllLists(user));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createList(
        @AuthenticationPrincipal User user,
        @RequestBody Map<String, Object> body
    ) {
        Map<String, Object> list = taskListService.createList(
            user,
            (String) body.get("name"),
            (String) body.get("description"),
            (String) body.get("color"),
            (String) body.get("icon"),
            body.get("category") != null ? ListCategory.valueOf((String) body.get("category")) : ListCategory.CUSTOM
        );
        return ResponseEntity.ok(Map.of("list", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getList(
        @PathVariable Long id,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(taskListService.getList(id, user));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateList(
        @PathVariable Long id,
        @AuthenticationPrincipal User user,
        @RequestBody Map<String, Object> updates
    ) {
        Map<String, Object> list = taskListService.updateList(id, user, updates);
        return ResponseEntity.ok(Map.of("list", list));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteList(
        @PathVariable Long id,
        @AuthenticationPrincipal User user
    ) {
        taskListService.deleteList(id, user);
        return ResponseEntity.noContent().build();
    }
}
