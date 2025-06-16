package com.example.Tracker.Service;


import com.example.Tracker.Models.User; // Import your User entity model
import com.example.Tracker.Repositories.UserRepository; // Import your UserRepository interface
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service; // This annotation makes it a Spring-managed bean

/**
 * Custom implementation of Spring Security's UserDetailsService interface.
 * This class is responsible for loading user-specific data during the authentication process.
 * It fetches user details from the database using UserRepository.
 *
 * Since the 'User' model now implements UserDetails, this service can directly return the User entity.
 */
@Service // Tells Spring to manage this class as a service bean
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Constructor Injection: Spring will automatically inject an instance of UserRepository
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Locates the user based on the username. In our setup, the 'username' parameter
     * here will actually be the user's email address, as configured in our User model's
     * getUsername() method and in our login form processing.
     *
     * @param email The email address of the user attempting to authenticate.
     * @return A UserDetails object (which is our User entity, as it implements UserDetails).
     * @throws UsernameNotFoundException if the user with the given email is not found.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Attempt to find the user by their email address in the database.
        // If not found, throw UsernameNotFoundException as required by the interface.
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}