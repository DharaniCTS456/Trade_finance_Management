package com.project.tfms.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.tfms.model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer>{
	Optional<Customer> findByEmailAndPassword(String email,String password);

	Optional<Customer> findByEmail(String email);
}
