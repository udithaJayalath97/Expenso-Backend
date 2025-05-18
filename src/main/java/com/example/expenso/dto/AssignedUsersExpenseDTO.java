package com.example.expenso.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class AssignedUsersExpenseDTO {
    private Long id;
    private String username;
    private String mobileNumber;
    private Double splitAmount;
}
