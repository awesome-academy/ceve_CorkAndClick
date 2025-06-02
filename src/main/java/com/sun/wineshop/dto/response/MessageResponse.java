package com.sun.wineshop.dto.response;

import java.time.LocalDateTime;

public record MessageResponse(
        String senderRole,
        String content,
        LocalDateTime timestamp
) {
}
