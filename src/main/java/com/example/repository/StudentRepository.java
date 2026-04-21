package com.example.repository;

import com.example.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Integer> {
    Student findByEmail(String email);

    // Using EntityGraph for optimized, paginated N+1 resolution
    @EntityGraph(attributePaths = {"department"})
    @Query("SELECT s FROM Student s")
    Page<Student> findAllWithDepartment(Pageable pageable);

    @Query("SELECT s FROM Student s JOIN FETCH s.department WHERE s.email = ?1")
    Student findByEmailWithDepartment(String email);

    @Query("SELECT s FROM Student s JOIN FETCH s.department WHERE s.regNumber = ?1")
    Student findByRegNumberWithDepartment(int regNumber);
}