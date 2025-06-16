package com.example.Tracker.Models; // Adjust package name as per your project structure

import jakarta.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Display Name (No longer unique, not used for login) ---
    // This 'username' field will now serve as a display name.
    // It is NOT unique, allowing multiple users to have the same display name.
    private String username;

    // --- Email (Unique Login Identifier) ---
    // This 'email' field will be the unique identifier for logging in.
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // --- For Registration Form Only (Not persisted to DB) ---
    @Transient // This field is used for form validation but is not stored in the database
    private String confirmPassword;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // --- Constructors ---
    public User() {
        // Default constructor for JPA
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // --- UserDetails Interface Implementations ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Returns the authorities (roles) granted to the user.
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        // Returns the stored (hashed) password
        return password;
    }

    @Override
    public String getUsername() {
        // IMPORTANT: For email-based login, this method *must* return the email.
        // Spring Security will use this value as the principal's "username" for authentication.
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        // Default to true. Implement actual expiration logic if needed (e.g., based on a date field).
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Default to true. Implement actual locking logic if needed (e.g., after too many failed login attempts).
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Default to true. Implement password expiration logic if needed (e.g., enforce password changes).
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Default to true. Implement account activation/deactivation logic if needed.
        return true;
    }

    // --- End UserDetails Interface Implementations ---


    // --- Getters and Setters (Updated for new fields and consistency) ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Getter for display name
    public String getDisplayName() {
        return username;
    }

    // Setter for display name
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Setter for the password (used during registration to set the hashed password)
    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        this.roles.add(role);
        // It's good practice to manage both sides of the relationship if @ManyToMany is bidirectional
        if (role.getUsers() == null) {
            role.setUsers(new HashSet<>());
        }
        role.getUsers().add(this);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
        // And remove from the other side of the relationship
        if (role.getUsers() != null) {
            role.getUsers().remove(this);
        }
    }
}