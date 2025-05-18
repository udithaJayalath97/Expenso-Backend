package com.example.expenso.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@AllArgsConstructor
@Data
public class BudgetWithUsersAndExpensesDTO {
    private Long id;
    private String name;
    private Double totalAmount;
    private AssignedUsersBudgetDTO createdBy;
    private List<AssignedUsersBudgetDTO> assignedUsers;
    private List<ExpenseWithUsersDTO> expenses;
}
