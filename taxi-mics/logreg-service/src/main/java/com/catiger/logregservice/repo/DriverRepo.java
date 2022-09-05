package com.catiger.logregservice.repo;

import com.catiger.logregservice.dao.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DriverRepo extends JpaRepository<Driver, String> {

    @Override
    Optional<Driver> findById(String s);
    @Query("SELECT rate FROM Driver WHERE account=?1")
    Optional<Driver> getRateByAccount(String s);

    @Query("SELECT password FROM Driver WHERE account=?1")
    String findPasswordByAccount(String s);

    @Query("SELECT realname FROM Driver WHERE account=?1")
    String findRealNameByAccount(String s);
}
