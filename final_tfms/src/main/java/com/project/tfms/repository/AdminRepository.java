package com.project.tfms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.tfms.model.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long>{ // Changed Integer to Long
    Optional<Admin> findByEmailAndPassword(String email, String password);

Optional<Admin> findByEmail(String adminUserName);
}