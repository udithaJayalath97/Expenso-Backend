package com.example.expenso.repository;


import com.example.expenso.model.ExpenseUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseUserRepository extends JpaRepository<ExpenseUser, Long> {
    List<ExpenseUser> findByExpense_ExpenseId(Long expenseId);
}
