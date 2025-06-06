package com.example.expenso.controller;
import com.example.expenso.dto.*;
import com.example.expenso.service.Service;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api")
public class Controller {

    private final Service service;

    // Constructor injection
    @Autowired
    public Controller(Service service) {
        this.service = service;
    }

    @PostMapping("/users/signup")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDTO) {
        String response = service.registerUser(userDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("users")
    public List<AssignedUsersBudgetDTO> getAllUsers() {
        return service.getAllUsers();
    }

    @GetMapping("/{userId}/budgets")
    public ResponseEntity<UserResponseDTO> getUserData(@PathVariable Long userId) {
        try {
            UserResponseDTO userResponseDTO = service.getUserDataByUserId(userId);
            return ResponseEntity.ok(userResponseDTO);
        } catch (RuntimeException e) {
            // If user not found or other exception, respond with 404 or appropriate error
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("users/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDTO request) {
        Object response = service.loginUser(request);

        if (response instanceof String) {
            return ResponseEntity.badRequest().body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }



    @PostMapping("/budget/create")
    public ResponseEntity<?> createBudget(@RequestBody CreateBudgetRequestDTO request) {
        try {
            service.createBudget(request);
            return ResponseEntity.ok("Budget created successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }



    @PostMapping("/expense/create")
    public ResponseEntity<String> createExpense(@RequestBody CreateExpenseRequestDTO request) {
        try {
            service.createExpense(request);
            return ResponseEntity.ok("Expense and users' split amounts created successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("delete/budget/{id}")
    public ResponseEntity<String> deleteBudget(@PathVariable Long id) {
        try {
            service.deleteBudget(id); // Call service to delete the budget
            return ResponseEntity.ok("Budget and associated records deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Budget not found");
        }
    }
}
