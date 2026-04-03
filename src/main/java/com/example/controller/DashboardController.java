package com.example.controller;

import com.example.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class DashboardController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private com.example.service.CourseService courseService;

    @GetMapping("/students")
    public String listStudents(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        model.addAttribute("totalStudents", studentService.countStudents());
        return "student_list";
    }

    @GetMapping("/students/add")
    public String showAddForm(Model model) {
        model.addAttribute("student", new com.example.model.Student());
        model.addAttribute("isEdit", false);
        return "student_form";
    }

    @GetMapping("/students/edit/{regNumber}")
    public String showEditForm(@PathVariable int regNumber, Model model) {
        com.example.model.Student student = studentService.getStudentByRegNumber(regNumber);
        if (student != null) {
            model.addAttribute("student", student);
            model.addAttribute("isEdit", true);
            return "student_form";
        }
        return "redirect:/students";
    }

    @PostMapping("/students/save")
    public String saveStudent(@ModelAttribute("student") com.example.model.Student student,
                              RedirectAttributes redirectAttributes) {
        try {
            boolean isNew = (student.getRegNumber() == null);

            // Guard: reg number is required for a new student
            if (isNew) {
                redirectAttributes.addFlashAttribute("error", "Registration number is required.");
                return "redirect:/students/add";
            }

            // Check for duplicate reg number on new records
            if (isNew && studentService.getStudentByRegNumber(student.getRegNumber()) != null) {
                redirectAttributes.addFlashAttribute("error",
                    "A student with registration number " + student.getRegNumber() + " already exists.");
                return "redirect:/students/add";
            }

            studentService.saveStudent(student);
            redirectAttributes.addFlashAttribute("success",
                isNew ? "Registration successful! " + student.getFirstName() + " has been added."
                      : "Profile updated successfully!");

        } catch (DataIntegrityViolationException e) {
            // Catches duplicate reg number or duplicate email from DB constraint
            String msg = e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage();
            if (msg != null && msg.toLowerCase().contains("email")) {
                redirectAttributes.addFlashAttribute("error",
                    "That email address is already linked to another student record.");
            } else {
                redirectAttributes.addFlashAttribute("error",
                    "A student with that registration number or email already exists.");
            }
            boolean isEdit = student.getRegNumber() != null
                && studentService.getStudentByRegNumber(student.getRegNumber()) != null;
            return isEdit
                ? "redirect:/students/edit/" + student.getRegNumber()
                : "redirect:/students/add";
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
    public String showDepartments() {
        return "department";
    }

    @GetMapping("/courses")
    public String showCourses(Model model) {
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("totalCourses", courseService.countCourses());
        return "courses";
    }

    @GetMapping("/guardians")
    public String showGuardians() {
        return "gaurdian"; // Note the filename spelling in your repository
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