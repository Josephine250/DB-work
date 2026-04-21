package com.example.controller;

import com.example.service.StudentService;
import com.example.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

@Controller
public class DashboardController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseService courseService;

    // Prevent Spring from trying to auto-bind the 'department' field on Student.
    // We resolve it manually from departmentId to avoid ConversionFailedException.
    @InitBinder("student")
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("department", "password");
    }

    @GetMapping("/students")
    public String listStudents(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<com.example.model.Student> studentPage = studentService.getAllStudents(page, 6);
        model.addAttribute("students", studentPage.getContent());
        model.addAttribute("totalStudents", studentPage.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", studentPage.getTotalPages());
        return "student_list";
    }

    @GetMapping("/students/add")
    public String showAddForm(Model model) {
        model.addAttribute("student", new com.example.model.Student());
        model.addAttribute("isEdit", false);
        model.addAttribute("departments", studentService.getAllDepartments());
        return "student_form";
    }

    @GetMapping("/students/edit/{regNumber}")
    public String showEditForm(@PathVariable int regNumber, Model model) {
        com.example.model.Student student = studentService.getStudentByRegNumber(regNumber);
        if (student != null) {
            model.addAttribute("student", student);
            model.addAttribute("isEdit", true);
            model.addAttribute("departments", studentService.getAllDepartments());
            return "student_form";
        }
        return "redirect:/students";
    }

    @PostMapping("/students/save")
    public String saveStudent(@ModelAttribute("student") com.example.model.Student student,
                              @RequestParam(value = "departmentId", required = false) Long departmentId,
                              @RequestParam(value = "departmentName", required = false) String departmentName,
                              RedirectAttributes redirectAttributes) {
        try {
            if (student.getRegNumber() == null) {
                redirectAttributes.addFlashAttribute("error", "Registration number is required.");
                return "redirect:/students";
            }

            // Resolve department — by ID (from student_form) or by name (from modal)
            if (departmentId != null) {
                student.setDepartment(studentService.getDepartmentById(departmentId));
            } else if (departmentName != null && !departmentName.trim().isEmpty()) {
                student.setDepartment(studentService.findOrCreateDepartment(departmentName.trim()));
            }

            boolean existsAlready = studentService.getStudentByRegNumber(student.getRegNumber()) != null;
            studentService.saveStudent(student);
            redirectAttributes.addFlashAttribute("success",
                existsAlready ? "Profile updated successfully!"
                              : "Registration successful! " + student.getFirstName() + " has been added.");

        } catch (DataIntegrityViolationException e) {
            String msg = e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage();
            if (msg != null && msg.toLowerCase().contains("email")) {
                redirectAttributes.addFlashAttribute("error",
                    "That email address is already linked to another student record.");
            } else {
                redirectAttributes.addFlashAttribute("error",
                    "A student with that registration number or email already exists.");
            }
        }
        return "redirect:/students";
    }

    @GetMapping("/students/delete/{regNumber}")
    public String deleteStudent(@PathVariable int regNumber, RedirectAttributes redirectAttributes) {
        studentService.deleteStudent(regNumber);
        redirectAttributes.addFlashAttribute("success", "Student record successfully erased.");
        return "redirect:/students";
    }

    @GetMapping("/departments")
    public String showDepartments(Model model) {
        model.addAttribute("departments", studentService.getAllDepartments());
        return "department";
    }

    @GetMapping("/courses")
    public String showCourses(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<com.example.model.Course> coursePage = courseService.getCoursesPaged(page, 12);
        model.addAttribute("courses", coursePage.getContent());
        model.addAttribute("totalCourses", coursePage.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", coursePage.getTotalPages());
        return "courses";
    }

    @GetMapping("/guardians")
    public String showGuardians() {
        return "gaurdian";
    }

    @GetMapping("/academic-years")
    public String showAcademicYears() {
        return "academic";
    }

    @GetMapping("/about")
    public String showAbout() {
        return "about";
    }
}
