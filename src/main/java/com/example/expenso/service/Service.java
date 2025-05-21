package com.example.expenso.service;

import com.example.expenso.dto.*;
import com.example.expenso.model.*;
import com.example.expenso.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@org.springframework.stereotype.Service

public class Service {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseUserRepository expenseUserRepository;

    @Autowired
    private BudgetUserRepository budgetUserRepository;

    public String registerUser(UserDTO userDTO) {
        Optional<User> existingUser = userRepository.findByMobileNumber(userDTO.getMobileNumber());
        if (existingUser.isPresent()) {
            return "Mobile number already registered";
        }

        // Hash the password before saving
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(userDTO.getPassword());

        User newUser = new User();
        newUser.setUsername(userDTO.getUsername());
        newUser.setMobileNumber(userDTO.getMobileNumber());
        newUser.setPassword(hashedPassword);  // Save the hashed password

        userRepository.save(newUser);
        return "User registered successfully";
    }

    public Object loginUser(LoginDTO request) {
        Optional<User> userOpt = userRepository.findByMobileNumber(request.getMobileNumber());

        if (userOpt.isEmpty()) {
            return "Mobile number not registered";
        }

        User user = userOpt.get();


        // Use BCrypt to check if the password matches
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            return "Incorrect password";
        }

        return getUserDataByUserId(user.getId());
    }

    public UserResponseDTO getUserDataByUserId(Long userId) {
        // 1. Get the User entity
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AssignedUsersBudgetDTO budgetCreatedBy = new AssignedUsersBudgetDTO(user.getId(),user.getUsername(),user.getMobileNumber());

        // 2. Get all BudgetUser entries where this user is assigned
        List<BudgetUser> userBudgetUsers = budgetUserRepository.findByUserId(userId);

        List<BudgetWithUsersAndExpensesDTO> budgetDTOs = new ArrayList<>();

        for (BudgetUser budgetUser : userBudgetUsers) {
            Budget budget = budgetUser.getBudget();

            // 3a. Get all users assigned to this budget
            List<BudgetUser> assignedBudgetUsers = budgetUserRepository.findByBudgetId(budget.getId());
            List<AssignedUsersBudgetDTO> assignedUsersBudgetDTOs = assignedBudgetUsers.stream()
                    .map(bu -> {
                        User assignedUser = bu.getUser();
                        return new AssignedUsersBudgetDTO(
                                assignedUser.getId(),
                                assignedUser.getUsername(),
                                assignedUser.getMobileNumber()
                        );
                    })
                    .collect(Collectors.toList());

            // 3b. Get all expenses for this budget
            List<Expense> expenses = expenseRepository.findByBudgetId(budget.getId());

            List<ExpenseWithUsersDTO> expenseDTOs = new ArrayList<>();

            for (Expense expense : expenses) {
                // 3c. Get all assigned users for this expense
                User expenseCreatedUser = expense.getCreatedBy();
                AssignedUsersBudgetDTO expenseCreatedBy = new AssignedUsersBudgetDTO(
                        expenseCreatedUser.getId(),
                        expenseCreatedUser.getUsername(),
                        expenseCreatedUser.getMobileNumber()
                );
                List<ExpenseUser> assignedExpenseUsers = expenseUserRepository.findByExpense_ExpenseId(expense.getExpenseId());
                List<AssignedUsersExpenseDTO> assignedUsersExpenseDTOs = assignedExpenseUsers.stream()
                        .map(eu -> {
                            User assignedUser = eu.getUser();
                            return new AssignedUsersExpenseDTO(
                                    assignedUser.getId(),
                                    assignedUser.getUsername(),
                                    assignedUser.getMobileNumber(),
                                    eu.getSplitAmount()
                            );
                        })
                        .collect(Collectors.toList());

                expenseDTOs.add(new ExpenseWithUsersDTO(
                        expense.getExpenseId(),
                        expense.getDescription(),
                        expense.getAmount(),
                        expenseCreatedBy,
                        assignedUsersExpenseDTOs
                ));
            }

            // Add this budget and its nested data to the list
            budgetDTOs.add(new BudgetWithUsersAndExpensesDTO(
                    budget.getId(),
                    budget.getName(),
                    budget.getTotalAmount(),
                    budgetCreatedBy,
                    assignedUsersBudgetDTOs,
                    expenseDTOs
            ));
        }

        // Build and return the user response DTO with all nested data
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getMobileNumber(),
                budgetDTOs
        );
    }



    public void createBudget(CreateBudgetRequestDTO request) {
        if (request.getName() == null || request.getName().trim().isEmpty() || request.getUserId() == null) {
            throw new IllegalArgumentException("Name and user ID are required");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Budget budget = new Budget(request.getName(), user);
        budgetRepository.save(budget);

        BudgetUser creatorBudgetUser = new BudgetUser();
        creatorBudgetUser.setBudget(budget);
        creatorBudgetUser.setUser(user);

        budgetUserRepository.save(creatorBudgetUser);

        if (request.getUserIds() != null) {
            for (Long userId : request.getUserIds()) {
                User users = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

                BudgetUser budgetUser = new BudgetUser();
                budgetUser.setBudget(budget);
                budgetUser.setUser(users);
                budgetUserRepository.save(budgetUser);
            }
        }

    }

    public void createExpense(CreateExpenseRequestDTO request) {
        Expense expense = new Expense();
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());

        User createdBy = userRepository.findById(request.getCreatedBy())
                .orElseThrow(() -> new RuntimeException("User not found"));
        expense.setCreatedBy(createdBy);

        Budget budget = budgetRepository.findById(request.getBudgetId())
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        expense.setBudget(budget);

        expenseRepository.save(expense);

        Double currentTotal = budget.getTotalAmount();
        if (currentTotal == null) {
            currentTotal = 0.0;
        }
        budget.setTotalAmount(currentTotal + request.getAmount());

        // Save updated budget
        budgetRepository.save(budget);

        Double splitAmount = request.getAmount() / request.getUserIds().size();

        for (Long userId : request.getUserIds()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ExpenseUser expenseUser = new ExpenseUser();
            expenseUser.setExpense(expense);
            expenseUser.setUser(user);
            expenseUser.setSplitAmount(splitAmount);

            expenseUserRepository.save(expenseUser);
        }
    }
}
