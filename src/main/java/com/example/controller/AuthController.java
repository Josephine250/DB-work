package com.example.controller;

import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String login() {
        return "login"; // Points to login.html
    }

    @GetMapping({"/", "/home"})
    public String home(Authentication authentication, Model model) {
        // If not logged in, we still show the home page but with limited options
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            model.addAttribute("isLoggedIn", false);
        } else {
            // User is logged in
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("username", user != null ? user.getUsername() : "Guest");
            model.addAttribute("isFirstTime", user != null && user.isFirstLogin());
        }
        return "home";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User()); 
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user) {
       if (userRepository.findByEmail(user.getEmail()) != null) {
           return "redirect:/register?error=exists";
       }
       if (userRepository.findByUsername(user.getUsername()) != null) {
           return "redirect:/register?error=username";
       }
    
       // Setting default values for your database columns
       user.setFirstLogin(true); 
       userRepository.save(user);
    
       // Redirect to login page so the user can authenticate properly
       return "redirect:/login?registered"; 
    }
    
    @GetMapping("/dashboard")
    public String showDashboard(Authentication authentication, Model model) {
        // Since Spring Security is disabled, authentication might be null. We need to handle this.
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
        
            if (user != null) {
                model.addAttribute("username", user.getUsername());
                model.addAttribute("email", user.getEmail());
            
                if (user.isFirstLogin()) {
                    model.addAttribute("welcomeMessage", "Welcome to the College Registration System!");
                }
            }
        } else {
            // Provide a default username so the Thymeleaf template doesn't fail
            model.addAttribute("username", "Guest");
        }
    
        return "dashboard"; // Points to src/main/resources/templates/dashboard.html
    }
     // 1. This SHOWS the form when the user is redirected
 

}