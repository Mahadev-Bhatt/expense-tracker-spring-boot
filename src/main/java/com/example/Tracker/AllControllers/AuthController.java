package com.example.Tracker.AllControllers;

import com.example.Tracker.Models.Role;
import com.example.Tracker.Models.User;
import com.example.Tracker.Repositories.RoleRepository;
import com.example.Tracker.Repositories.UserRepository;
import org.springframework.dao.DataIntegrityViolationException; // For catching database unique constraint violations
import org.springframework.security.authentication.AuthenticationManager; // For programmatic login
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService; // For loading UserDetails after registration
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService; // Injected for loading user details to set authentication context
    private final AuthenticationManager authenticationManager; // Injected for programmatic login

    // Constructor Injection: All dependencies are automatically provided by Spring
    public AuthController(UserRepository userRepository, RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder, UserDetailsService userDetailsService,
                          AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
    }

    // Handles requests to display the registration form
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User()); // Add an empty User object to bind form data
        return "register"; // Renders src/main/resources/templates/register.html
    }

    // Handles requests to display the home/landing page
    @GetMapping("/")
    public String showHomePage() {
        return "index"; // Renders src/main/resources/templates/index.html
    }

    // Handles the submission of the registration form
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        // 1. Password Confirmation Validation
        if (!user.getPassword().equals(user.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match!");
            model.addAttribute("user", user); // Retain form data
            return "register"; // Stay on registration page with error
        }

        // 2. Hash the password before saving
        String rawPassword = user.getPassword(); // Store the raw password for auto-login
        user.setPassword(passwordEncoder.encode(rawPassword)); // Hash and set the password

        // 3. Assign default role (e.g., ROLE_USER)
        // Ensure this role 'ROLE_USER' exists in your database or is created.
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    // Create ROLE_USER if it doesn't exist (for initial setup convenience)
                    Role newRole = new Role();
                    newRole.setName("ROLE_USER");
                    return roleRepository.save(newRole);
                });
        user.addRole(userRole); // Add the role to the user

        // 4. Attempt to save the user and handle potential data integrity violations
        try {
            userRepository.save(user); // Save the new user to the database

            // 5. Automatically log in the user after successful registration
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    user.getEmail(), // Use email as the principal for authentication
                    rawPassword // Use the raw password for AuthenticationManager
            );
            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 6. Redirect to the dashboard after successful registration and auto-login
            return "redirect:/expenses/view"; // Redirect to your dashboard URL

        } catch (DataIntegrityViolationException e) {
            // This exception is caught when a unique constraint (like duplicate email) is violated
            System.err.println("Registration failed due to data integrity violation: " + e.getMessage());
            String errorMessage = "An account with this email already exists."; // Default message

            // You can add more sophisticated parsing of 'e.getMessage()' here
            // to distinguish between unique constraint violations on different columns (e.g., email vs. username)
            // if you have multiple unique constraints. For email as primary, this is usually sufficient.

            model.addAttribute("error", errorMessage);
            model.addAttribute("user", user); // Retain form data for user to correct
            return "register"; // Re-render registration page with the error
        } catch (Exception e) {
            // Catch any other unexpected exceptions during registration or auto-login process
            System.err.println("An unexpected error occurred during registration: " + e.getMessage());
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            model.addAttribute("user", user); // Retain form data
            return "register"; // Re-render registration page with the error
        }
    }

    // Handles requests to display the custom login page
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // Renders src/main/resources/templates/login.html
    }
}