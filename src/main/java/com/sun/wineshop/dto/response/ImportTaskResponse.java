package com.sun.wineshop.dto.response;

import java.time.LocalDateTime;

public record ImportTaskResponse(
        Long id,
        String fileName,
        String status,
        String errorMessage,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {}
