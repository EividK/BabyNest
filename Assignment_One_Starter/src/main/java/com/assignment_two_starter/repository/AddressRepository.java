package com.assignment_two_starter.repository;

import com.assignment_two_starter.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Integer> {
}

