package com.example.controller;

import com.example.model.Student;
import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;

@Controller
public class RegistrationController {

    @Autowired private StudentService studentService;
    @Autowired private UserRepository userRepo;

    @InitBinder("student")
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("department", "password");
    }

    @GetMapping("/registration-form")
    public String showRegistrationForm(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("departments", studentService.getAllDepartments());
        return "registration_form";
    }

    @PostMapping("/complete-registration")
    @Transactional
    public String completeRegistration(@ModelAttribute("student") Student student,
                                       @RequestParam(value = "departmentId", required = false) Long departmentId,
                                       BindingResult bindingResult,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        String email = principal.getName();

        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("error", "Invalid form data. Please check your inputs.");
                return "redirect:/registration-form";
            }
            if (student.getRegNumber() == null) {
                redirectAttributes.addFlashAttribute("error", "Registration number is required.");
                return "redirect:/registration-form";
            }

            // Resolve department by ID
            if (departmentId != null) {
                student.setDepartment(studentService.getDepartmentById(departmentId));
            }

            Student studentWithReg = studentService.getStudentByRegNumber(student.getRegNumber());
            if (studentWithReg != null && !studentWithReg.getEmail().equalsIgnoreCase(email)) {
                redirectAttributes.addFlashAttribute("error",
                    "Registration number " + student.getRegNumber() + " is already taken by another account.");
                return "redirect:/registration-form";
            }

            Student existingStudent = studentService.getStudentByEmail(email);

            if (existingStudent != null) {
                if (!existingStudent.getRegNumber().equals(student.getRegNumber())) {
                    studentService.deleteStudent(existingStudent.getRegNumber());
                    Student newRecord = new Student();
                    newRecord.setRegNumber(student.getRegNumber());
                    newRecord.setEmail(email);
                    newRecord.setFirstName(student.getFirstName());
                    newRecord.setLastName(student.getLastName());
                    newRecord.setDepartment(student.getDepartment());
                    studentService.saveStudent(newRecord);
                } else {
                    existingStudent.setFirstName(student.getFirstName());
                    existingStudent.setLastName(student.getLastName());
                    existingStudent.setDepartment(student.getDepartment());
                    studentService.saveStudent(existingStudent);
                }
            } else {
                student.setEmail(email);
                studentService.saveStudent(student);
            }

            User user = userRepo.findByEmail(email);
            if (user != null && user.isFirstLogin()) {
                user.setFirstLogin(false);
                userRepo.save(user);
            }

            redirectAttributes.addFlashAttribute("success", "Profile established! Welcome to the portal.");
            return "redirect:/students";

        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error",
                "A database conflict occurred. Please check your details (possibly duplicate ID).");
            return "redirect:/registration-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                "System error: " + e.getClass().getSimpleName());
            System.err.println("Registration error: " + e.getMessage());
            return "redirect:/registration-form";
        }
    }
}
