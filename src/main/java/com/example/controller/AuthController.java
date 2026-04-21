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
        return "login";
    }

    @GetMapping({"/", "/home"})
    public String home(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated() ||
            authentication.getPrincipal().equals("anonymousUser")) {
            model.addAttribute("isLoggedIn", false);
        } else {
            // Use the name already stored in the Authentication principal — no DB query needed
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("username", authentication.getName());
            model.addAttribute("isFirstTime", false);
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
        user.setFirstLogin(true);
        userRepository.save(user);
        return "redirect:/login?registered";
    }

    @GetMapping("/dashboard")
    public String showDashboard(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")) {
            String email = authentication.getName();
            // Only fetch from DB when we need the firstLogin flag
            User user = userRepository.findByEmail(email);
            if (user != null) {
                model.addAttribute("username", user.getUsername());
                model.addAttribute("email", user.getEmail());
                if (user.isFirstLogin()) {
                    model.addAttribute("welcomeMessage", "Welcome to the College Registration System!");
                }
            } else {
                model.addAttribute("username", email);
            }
        } else {
            model.addAttribute("username", "Guest");
        }
        return "dashboard";
    }
}
