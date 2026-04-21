package com.example.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "departments", indexes = {
    @Index(name = "idx_dept_name", columnList = "name")
})
@Data
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;
}