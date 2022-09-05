package com.catiger.logregservice.controller;

import com.catiger.logregservice.dao.Car;
import com.catiger.logregservice.repo.CarRepo;
import com.catiger.logregservice.res.Response;
import com.catiger.logregservice.res.ResponseBrief;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@RestController
@RequestMapping("/car")
public class CarController {

    @Autowired
    CarRepo carRepo;

    // Register a car and save it to database.
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Car car) {
        if(carRepo.findById(car.getLicense()).isPresent())
            return ResponseEntity.ok(new ResponseBrief(400,"注册失败，此车辆已注册"));
        carRepo.save(car);
        return ResponseEntity.ok(new ResponseBrief(200, "注册成功"));
    }

    // Get the cars' licenses that belong to a account.
    @PostMapping("/account2license")
    public ResponseEntity<?> getCarLicenseByAccount(@RequestParam("account") String s) {
        return ResponseEntity.ok(new ResponseBrief(200, "成功", carRepo.findLicenseByAccount(s)));
    }

}