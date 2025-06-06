package com.example.expenso.repository;


import com.example.expenso.model.BudgetUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetUserRepository extends JpaRepository<BudgetUser, Long> {
    List<BudgetUser> findByUserId(Long userId);
    List<BudgetUser> findByBudgetId(Long budgetId);

}
