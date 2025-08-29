package com.example.expenso.service;

import com.example.expenso.dto.*;
import com.example.expenso.model.*;
import com.example.expenso.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' HH:mm");
    String formattedDateTime = now.format(formatter);

    public List<AssignedUsersBudgetDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new AssignedUsersBudgetDTO(user.getId(), user.getUsername(), user.getMobileNumber()))
                .collect(Collectors.toList());
    }
    @Transactional
    public void deleteBudget(Long id) {
        if (!budgetRepository.existsById(id)) {
            throw new RuntimeException("Budget not found with ID: " + id);
        }
        budgetRepository.deleteById(id);
    }


    @Transactional
    public void deleteExpenseAndUsers(Long expenseId) {
        // Find the Expense first to retrieve the related Budget
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        // Subtract the amount of the expense from the associated Budget's total amount
        Budget budget = expense.getBudget();  // Get the related Budget
        if (budget != null) {
            // Update the totalAmount of the budget
            budget.setTotalAmount(budget.getTotalAmount() - expense.getAmount());
            budgetRepository.save(budget); // Save the updated budget
        }



        // Delete the ExpenseUser records first
        expenseUserRepository.deleteByExpenseExpenseId(expenseId);

        // Then, delete the Expense record
        expenseRepository.deleteById(expenseId);
    }

    public void createNotification(Long userId, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setReadStatus(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Activity> activities = activityRepository.findByUserId(userId);
        List<ActivityDTO> activityDTOs = new ArrayList<>();

        for (Activity activity : activities) {
            // Map Activity entity to ActivityDTO
            ActivityDTO activityDTO = new ActivityDTO(activity.getDescription());
            activityDTOs.add(activityDTO);
        }

        List<Notification> notifications = notificationRepository.findByUserId(userId);
        List<NotificationDTO> notificationDTOs = new ArrayList<>();

        for (Notification notification : notifications) {
            NotificationDTO notificationDTO = new NotificationDTO(notification.getId(),notification.getUserId(),notification.getMessage(),notification.isReadStatus(),notification.getCreatedAt());
            notificationDTOs.add(notificationDTO);
        }

        // Budgets where user is assigned via BudgetUser
        List<BudgetUser> userBudgetUsers = budgetUserRepository.findByUserId(userId);
        List<Budget> assignedBudgets = userBudgetUsers.stream()
                .map(BudgetUser::getBudget)
                .toList();

        // Budgets where user is creator (owner)
        List<Budget> createdBudgets = budgetRepository.findByUserId(userId);

        // Combine budgets and remove duplicates (by budget id)
        Map<Long, Budget> combinedBudgetsMap = new HashMap<>();

        for (Budget b : assignedBudgets) combinedBudgetsMap.put(b.getId(), b);
        for (Budget b : createdBudgets) combinedBudgetsMap.put(b.getId(), b);

        List<Budget> combinedBudgets = new ArrayList<>(combinedBudgetsMap.values());

        List<BudgetWithUsersAndExpensesDTO> budgetDTOs = new ArrayList<>();

        for (Budget budget : combinedBudgets) {
            User budgetCreatedUser = budget.getUser();
            AssignedUsersBudgetDTO budgetCreatedBy = new AssignedUsersBudgetDTO(
                    budgetCreatedUser.getId(),
                    budgetCreatedUser.getUsername(),
                    budgetCreatedUser.getMobileNumber()
            );

            // Assigned users for budget
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

            // Expenses for budget
            List<Expense> expenses = expenseRepository.findByBudgetId(budget.getId());
            List<ExpenseWithUsersDTO> expenseDTOs = new ArrayList<>();

            for (Expense expense : expenses) {
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

            budgetDTOs.add(new BudgetWithUsersAndExpensesDTO(
                    budget.getId(),
                    budget.getName(),
                    budget.getTotalAmount(),
                    budgetCreatedBy,
                    assignedUsersBudgetDTOs,
                    expenseDTOs
            ));
        }

        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getMobileNumber(),
                budgetDTOs,
                activityDTOs,
                notificationDTOs
        );
    }


    public Notification markAsRead(Long notificationId) throws Exception {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);

        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setReadStatus(true);  // Set the notification as read
            return notificationRepository.save(notification);  // Save the updated notification back to the database
        } else {
            throw new Exception("Notification not found");
        }
    }

    public void createBudget(CreateBudgetRequestDTO request) {
        if (request.getName() == null || request.getName().trim().isEmpty() || request.getUserId() == null) {
            throw new IllegalArgumentException("Name and user ID are required");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Budget budget = new Budget(request.getName(), user);
        budgetRepository.save(budget);
        Activity activity = new Activity();
        activity.setUserId(user.getId());
        activity.setDescription("You created budget : "+budget.getName()+" on "+formattedDateTime);

        activityRepository.save(activity);


        if (request.getUserIds() != null) {
            for (Long userId : request.getUserIds()) {
                User users = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

                BudgetUser budgetUser = new BudgetUser();
                budgetUser.setBudget(budget);
                budgetUser.setUser(users);
                budgetUserRepository.save(budgetUser);
                createNotification(userId,"You were added to the budget: "+budget.getName()+" by "+user.getUsername()+" on "+formattedDateTime);
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

        Activity activity = new Activity();
        activity.setUserId(createdBy.getId());
        activity.setDescription("You created expense: "+request.getDescription()+" on "+formattedDateTime);
        activityRepository.save(activity);

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
            createNotification(userId,"You were added to the expense in budget "+budget.getName()+" : "+request.getDescription()+" by "+createdBy.getUsername()+" on "+formattedDateTime+". \nTotal amount of Expense: "+request.getAmount()+".\nYour contribution: "+splitAmount+".");
        }
    }


}
