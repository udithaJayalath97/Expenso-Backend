package com.example.expenso.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class NotificationDTO {
    private Long id;
    private Long userId;
    private String message;
    private boolean readStatus;
    private LocalDateTime createdAt;
}
