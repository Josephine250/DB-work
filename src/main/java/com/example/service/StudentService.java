package com.example.service;

import com.example.model.Student;
import com.example.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private com.example.repository.DepartmentRepository departmentRepository;

    public long countStudents() {
        return studentRepository.count();
    }

    public Page<Student> getAllStudents(int page, int size) {
        return studentRepository.findAllWithDepartment(PageRequest.of(page, size));
    }

    public Student getStudentByRegNumber(int regNumber) {
        return studentRepository.findById(regNumber).orElse(null);
    }

    public Student getStudentByEmail(String email) {
        return studentRepository.findByEmail(email);
    }

    @Transactional
    public void saveStudent(Student student) {
        studentRepository.save(student);
    }

    @Transactional
    public void deleteStudent(int regNumber) {
        studentRepository.deleteById(regNumber);
    }

    public com.example.model.Department findOrCreateDepartment(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        com.example.model.Department dept = departmentRepository.findByName(name);
        if (dept == null) {
            dept = new com.example.model.Department();
            dept.setName(name);
            dept = departmentRepository.save(dept);
        }
        return dept;
    }

    public java.util.List<com.example.model.Department> getAllDepartments() {
        return departmentRepository.findAll(org.springframework.data.domain.Sort.by("name"));
    }

    public com.example.model.Department getDepartmentById(Long id) {
        return departmentRepository.findById(id).orElse(null);
    }
}
