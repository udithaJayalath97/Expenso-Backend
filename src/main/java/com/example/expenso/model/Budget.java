package com.example.expenso.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "budget")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private Long id;

    @Column(name = "budget_name")
    private String name;

    @Column(name = "total_amount")
    private Double totalAmount;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


    public Budget(String name, User user) {
        this.name = name;
        this.user = user;
    }
}
