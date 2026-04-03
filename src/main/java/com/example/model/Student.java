package com.example.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @Column(name = "regnumber")
    private Integer regNumber; // Primary Key for student management

    @Column(nullable = false, unique = true)
    private String email; // Link to User email

    private String firstName;
    private String lastName;
    private String department;
}