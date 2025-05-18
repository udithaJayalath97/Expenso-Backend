package com.example.expenso.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "budget_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_user_id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "budget_id")
    @ToString.Exclude
    private Budget budget;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;
}
