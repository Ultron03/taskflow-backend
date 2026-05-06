package com.taskflow.task.dto;

import com.taskflow.task.Difficulty;
import com.taskflow.task.Priority;
import com.taskflow.task.Task;

import java.time.LocalDateTime;

public record TaskResponse(
    Long id,
    Long listId,
    String title,
    String notes,
    Boolean completed,
    LocalDateTime completedAt,
    Priority priority,
    Difficulty difficulty,
    String category,
    String tags,
    String externalUrl,
    Integer xpValue,
    Integer sortOrder,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getTaskList().getId(),
            task.getTitle(),
            task.getNotes(),
            task.getCompleted(),
            task.getCompletedAt(),
            task.getPriority(),
            task.getDifficulty(),
            task.getCategory(),
            task.getTags(),
            task.getExternalUrl(),
            task.getXpValue(),
            task.getSortOrder(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }
}
