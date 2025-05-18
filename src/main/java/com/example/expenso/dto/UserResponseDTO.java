package com.example.expenso.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@AllArgsConstructor
@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private String mobileNumber;
    private List<BudgetWithUsersAndExpensesDTO> budgets;
}
