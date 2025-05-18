package com.example.expenso.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "expense_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_user_id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "expense_id")
    @ToString.Exclude
    private Expense expense;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @Column(name = "split_amount")
    private Double splitAmount;
}

