package com.project.tfms.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.tfms.model.Admin;
import com.project.tfms.repository.AdminRepository;

import at.favre.lib.crypto.bcrypt.BCrypt; // Import BCrypt library

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    public Admin saveAdmin(Admin admin) {
        // Hash the password using BCrypt before saving
        if (admin.getPassword() != null && !admin.getPassword().isEmpty()) {
            // Generate a BCrypt hash with 12 rounds (cost factor)
            String bcryptHashString = BCrypt.withDefaults().hashToString(12, admin.getPassword().toCharArray());
            admin.setPassword(bcryptHashString);
        }
        return adminRepository.save(admin);
    }

    public boolean authenticate(String email, String password) {
        Optional<Admin> adminOptional = adminRepository.findByEmail(email);

        if (adminOptional.isPresent()) {
            Admin admin = adminOptional.get();
            // Verify the raw input password against the stored BCrypt hashed password
            return BCrypt.verifyer().verify(password.toCharArray(), admin.getPassword().toCharArray()).verified;
        }
        return false; // Authentication failed if admin not found or password doesn't match
    }

    public Optional<Admin> getDetails(String email) {
        return adminRepository.findByEmail(email);
    }
}