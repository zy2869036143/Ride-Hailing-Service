package com.catiger.logregservice.repo;


import com.catiger.logregservice.dao.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CarRepo  extends JpaRepository<Car, String> {

    @Query("SELECT license FROM Car WHERE account=?1")
    List<String> findLicenseByAccount(String s);
}
