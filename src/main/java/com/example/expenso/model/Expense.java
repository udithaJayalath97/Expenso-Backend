package com.example.expenso.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "expense")
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Long expenseId;

    @Column(name = "description")
    private String description;

    @Column(name = "amount")
    private Double amount;


    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;


    @ManyToOne
    @JoinColumn(name = "budget_id")
    @ToString.Exclude
    private Budget budget;

}
