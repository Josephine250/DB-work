package com.example.controller;

import com.example.model.Student;
import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;

@Controller
public class RegistrationController {

    @Autowired private StudentService studentService;
    @Autowired private UserRepository userRepo;

    // Shows the profile completion form for first-time login users
    @GetMapping("/registration-form")
    public String showRegistrationForm(Model model) {
        model.addAttribute("student", new Student());
        return "registration_form";
    }

    @PostMapping("/complete-registration")
    public String completeRegistration(@ModelAttribute("student") Student student, BindingResult bindingResult, Principal principal, RedirectAttributes redirectAttributes) {
        // 1. Get logged-in email from the Security session
        if (principal == null) {
            return "redirect:/login";
        }
        String email = principal.getName();

        try {
            // Check for form binding errors (e.g., malformed ID)
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("error", "Invalid form data. Please check your inputs.");
                return "redirect:/registration-form";
            }

            // Guard: reg number required
            if (student.getRegNumber() == null) {
                redirectAttributes.addFlashAttribute("error", "Registration number is required.");
                return "redirect:/registration-form";
            }

            // Check if THIS specific regNumber is already taken BY SOMEONE ELSE
            Student studentWithReg = studentService.getStudentByRegNumber(student.getRegNumber());
            if (studentWithReg != null && !studentWithReg.getEmail().equalsIgnoreCase(email)) {
                redirectAttributes.addFlashAttribute("error", "Registration number " + student.getRegNumber() + " is already taken by another account.");
                return "redirect:/registration-form";
            }

            // Check if student record exists for THIS email
            Student existingStudent = studentService.getStudentByEmail(email);
            
            if (existingStudent != null) {
                // JPA restriction: you cannot change the @Id of a managed entity.
                // If the user wants a DIFFERENT regNumber, we must delete the old record and create a new one.
                if (!existingStudent.getRegNumber().equals(student.getRegNumber())) {
                    studentService.deleteStudent(existingStudent.getRegNumber());
                    
                    // Create fresh record with new ID
                    Student newRecord = new Student();
                    newRecord.setRegNumber(student.getRegNumber());
                    newRecord.setEmail(email);
                    newRecord.setFirstName(student.getFirstName());
                    newRecord.setLastName(student.getLastName());
                    newRecord.setDepartment(student.getDepartment());
                    studentService.saveStudent(newRecord);
                } else {
                    // ID is the same, just update the non-key fields
                    existingStudent.setFirstName(student.getFirstName());
                    existingStudent.setLastName(student.getLastName());
                    existingStudent.setDepartment(student.getDepartment());
                    studentService.saveStudent(existingStudent);
                }
            } else {
                // Purely NEW student record
                student.setEmail(email);
                studentService.saveStudent(student);
            }

            // 3. Update 'user' table flag
            User user = userRepo.findByEmail(email);
            if (user != null) {
                user.setFirstLogin(false);
                userRepo.save(user);
            }

            redirectAttributes.addFlashAttribute("success", "Profile established! Welcome to the portal.");
            return "redirect:/students";

        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "A database conflict occurred. Please check your details (possibly duplicate ID).");
            return "redirect:/registration-form";
        } catch (Exception e) {
            // Catch-all for unexpected errors
            String errorMsg = e.getClass().getSimpleName() + ": " + (e.getMessage() != null ? e.getMessage() : "Unknown error");
            redirectAttributes.addFlashAttribute("error", "System error: " + errorMsg);
            // Also log to console if available
            System.err.println("Registration completion error: " + errorMsg);
            e.printStackTrace();
            return "redirect:/registration-form";
        }
    }
}