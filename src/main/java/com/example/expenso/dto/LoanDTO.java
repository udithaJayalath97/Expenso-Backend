package com.example.expenso.dto;

import lombok.Data;

@Data
public class LoanDTO {
    private String description;
    private Double amount;
    private Long userId;
}
