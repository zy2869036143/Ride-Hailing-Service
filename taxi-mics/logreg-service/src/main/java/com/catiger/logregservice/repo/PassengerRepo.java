package com.catiger.logregservice.repo;

import com.catiger.logregservice.dao.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PassengerRepo extends JpaRepository<Passenger, String> {
    @Override
    Optional<Passenger> findById(String s);
    @Query("SELECT password FROM Passenger WHERE account=?1")
    String findPasswordByAccount(String s);
}
