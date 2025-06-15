package com.project.tfms;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.project.tfms.model.Admin;
import com.project.tfms.service.AdminService;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		System.out.println("Spring Boot Project ");
	}

	@Bean
    public CommandLineRunner createDefaultAdmin(AdminService adminService) {
        return args -> {
            // Check if an admin with a specific email already exists
            // Consider using a more robust check if the email exists, e.g., adminService.findByEmail()
            if (adminService.getDetails("admin@gmail.com").isEmpty()) {
                Admin defaultAdmin = new Admin();
                defaultAdmin.setEmail("admin@gmail.com");
                defaultAdmin.setPassword("adminpass");
                defaultAdmin.setName("admin");
                // *** FIX: Set the role here ***
                defaultAdmin.setRole("ROLE_ADMIN"); // Assign a default role for the admin

                adminService.saveAdmin(defaultAdmin);
                System.out.println("Default admin user created: admin@example.com");
            } else {
                System.out.println("Admin user already exists. Skipping default creation.");
            }
        };
    }
}