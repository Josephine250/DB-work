package com.example.service;

import com.example.model.Course;
import com.example.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    public List<Course> getAllCourses() {
        return courseRepository.findAll(Sort.by("courseName"));
    }

    public Page<Course> getCoursesPaged(int page, int size) {
        return courseRepository.findAll(PageRequest.of(page, size, Sort.by("courseName")));
    }

    public long countCourses() {
        return courseRepository.count();
    }

    @Transactional
    public void saveCourse(Course course) {
        courseRepository.save(course);
    }
}
