package com.taskflow.task.dto;

import com.taskflow.task.Difficulty;
import com.taskflow.task.Priority;

public record TaskRequest(
    String title,
    String notes,
    Priority priority,
    Difficulty difficulty,
    String category,
    String tags,
    String externalUrl,
    Integer xpValue,
    Integer sortOrder,
    Boolean completed
) {}
