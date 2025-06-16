package com.example.Tracker.Repositories; // Adjust package name

import com.example.Tracker.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Import Optional

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // You can keep findByUsername if you intend to use 'username' as a display name
    // and might need to query by it for other purposes, even if it's not unique for login.
    Optional<User> findByUsername(String username);

    // This is the correct way to declare findByEmail.
    // It should return Optional<User> as it finds a User entity by email.
    Optional<User> findByEmail(String email); // <-- CORRECTED LINE
}