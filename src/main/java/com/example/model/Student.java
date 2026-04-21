package com.example.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "students", indexes = {
    @Index(name = "idx_regnumber", columnList = "regnumber"),
    @Index(name = "idx_student_email", columnList = "email"),
    @Index(name = "idx_student_dept", columnList = "department_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @Column(name = "regnumber")
    private Integer regNumber;

    @Column(nullable = false, unique = true)
    private String email;

    private String firstName;
    private String lastName;

    // Legacy column — kept nullable so existing rows without a password are unaffected.
    // Students authenticate via the 'users' table; this field is not used for login.
    @Column(nullable = true)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
}