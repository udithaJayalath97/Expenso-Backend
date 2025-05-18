package com.example.expenso.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateExpenseRequestDTO {
    private String description;
    private Double amount;
    private Long budgetId;
    private Long createdBy;
    private List<Long> userIds;
}
