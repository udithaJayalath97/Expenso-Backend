package com.example.expenso.controller;

import com.example.expenso.dto.*;
import com.example.expenso.model.Budget;
import com.example.expenso.service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            Budget savedBudget = service.createBudget(request);
            return ResponseEntity.ok(savedBudget);
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

}
