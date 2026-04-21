package com.example.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "courses", indexes = {
    @Index(name = "idx_course_code", columnList = "course_code"),
    @Index(name = "idx_course_name", columnList = "course_name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "c_ID") // Matches your 'c_ID' column
    private int cId;

    @Column(name = "c_name", length = 20, nullable = false)
    private String cName;

    @Column(name = "credits")
    private Integer credits;

    @Column(name = "course_code", unique = true)
    private String courseCode;

    @Column(name = "course_name", nullable = false)
    private String courseName;
}