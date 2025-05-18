package com.example.expenso.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@AllArgsConstructor
@Data
public class ExpenseWithUsersDTO {
    private Long expenseId;
    private String description;
    private Double amount;
    private List<AssignedUsersExpenseDTO> assignedUsers;
}
