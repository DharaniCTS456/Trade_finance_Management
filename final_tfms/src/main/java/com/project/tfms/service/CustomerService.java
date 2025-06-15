package com.project.tfms.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.tfms.model.Customer; // Corrected package for Customer model
import com.project.tfms.repository.CustomerRepository;

import at.favre.lib.crypto.bcrypt.BCrypt; // Import BCrypt library

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public Customer saveCustomer(Customer customer) {
        // Assign a default role if it's a new customer and no role is explicitly set
        // This ensures that all customers have a role, which is crucial for security.
        if (customer.getCustomerId() == null && customer.getRole() == null) {
            customer.setRole("ROLE_CUSTOMER"); // Set the default role here
        }

        // Hash the password using BCrypt before saving
        if (customer.getPassword() != null && !customer.getPassword().isEmpty()) {
            // Generate a BCrypt hash with 12 rounds (cost factor)
            String bcryptHashString = BCrypt.withDefaults().hashToString(12, customer.getPassword().toCharArray());
            customer.setPassword(bcryptHashString);
        }
        return customerRepository.save(customer);
    }

    public Optional<Customer> authenticate(String email, String password) {
        Optional<Customer> customerOptional = customerRepository.findByEmail(email);

        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();
            // Verify the raw input password against the stored BCrypt hashed password
            // BCrypt.verifyer().verify returns a Result object; .verified checks if it's true
            if (BCrypt.verifyer().verify(password.toCharArray(), customer.getPassword().toCharArray()).verified) {
                return Optional.of(customer); // Authentication successful
            }
        }
        return Optional.empty(); // Authentication failed
    }
}
