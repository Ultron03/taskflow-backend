package com.taskflow.task;

import com.taskflow.task.dto.TaskRequest;
import com.taskflow.task.dto.TaskResponse;
import com.taskflow.user.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/api/v1/lists/{listId}/tasks")
    public ResponseEntity<List<TaskResponse>> getTasks(
        @PathVariable Long listId,
        @AuthenticationPrincipal User user,
        @RequestParam(required = false) Boolean completed,
        @RequestParam(required = false) Priority priority
    ) {
        return ResponseEntity.ok(taskService.getTasksForList(listId, user, completed, priority));
    }

    @PostMapping("/api/v1/lists/{listId}/tasks")
    public ResponseEntity<TaskResponse> createTask(
        @PathVariable Long listId,
        @AuthenticationPrincipal User user,
        @RequestBody TaskRequest request
    ) {
        return ResponseEntity.ok(taskService.createTask(listId, user, request));
    }

    @PatchMapping("/api/v1/tasks/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
        @PathVariable Long taskId,
        @AuthenticationPrincipal User user,
        @RequestBody TaskRequest request
    ) {
        return ResponseEntity.ok(taskService.updateTask(taskId, user, request));
    }

    @DeleteMapping("/api/v1/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(
        @PathVariable Long taskId,
        @AuthenticationPrincipal User user
    ) {
        taskService.deleteTask(taskId, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/lists/{listId}/tasks/bulk")
    public ResponseEntity<Map<String, Integer>> bulkCreate(
        @PathVariable Long listId,
        @AuthenticationPrincipal User user,
        @RequestBody List<TaskRequest> requests
    ) {
        int created = taskService.bulkCreate(listId, user, requests);
        return ResponseEntity.ok(Map.of("created", created));
    }

    @PatchMapping("/api/v1/tasks/{taskId}/complete")
    public ResponseEntity<TaskResponse> toggleComplete(
        @PathVariable Long taskId,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(taskService.toggleComplete(taskId, user));
    }
}
