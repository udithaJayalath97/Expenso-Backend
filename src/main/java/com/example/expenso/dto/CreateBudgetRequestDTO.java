package com.example.expenso.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateBudgetRequestDTO {
    public String name;
    public Long userId;
    private List<Long> userIds;
}
