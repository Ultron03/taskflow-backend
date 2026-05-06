package com.taskflow.parser;

public record ParsedTaskDTO(
    String title,
    String difficulty,
    String category,
    String externalUrl,
    Integer xpValue,
    String parseWarning
) {}
