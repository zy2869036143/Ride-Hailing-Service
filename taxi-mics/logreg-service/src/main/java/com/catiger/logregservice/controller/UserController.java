package com.catiger.logregservice.controller;

import com.catiger.logregservice.dao.Driver;
import com.catiger.logregservice.dao.Passenger;
import com.catiger.logregservice.dao.UserDao;
import com.catiger.logregservice.repo.DriverRepo;
import com.catiger.logregservice.repo.PassengerRepo;
import com.catiger.logregservice.repo.UserRepository;
import com.catiger.logregservice.res.Response;
import com.catiger.logregservice.res.ResponseBrief;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    DriverRepo driverRepo;
    @Autowired
    PassengerRepo passengerRepo;

    @PostMapping("/register")
    public ResponseEntity<?>  register(@RequestBody UserDao user) {
        userRepository.save(user);
        return ResponseEntity.ok(new String("/user/register"));
    }

    @PostMapping("/regdriver")
    public ResponseEntity<?> regDriver(@RequestBody Driver driver) {
        if (hasDriver(driver.getAccount()))
            return ResponseEntity.ok(new Response(400,"该手机号已注册。"));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String password = passwordEncoder.encode(driver.getPassword());
        driver.setPassword(password);
        driverRepo.save(driver);
        return ResponseEntity.ok(new Response(200, "注册成功"));
    }

    @PostMapping("/regpass")
    public ResponseEntity<?> regDriver(@RequestBody Passenger passenger) {
        if (hasPassenger(passenger.getAccount()))
            return ResponseEntity.ok(new Response(400,"该手机号已注册。"));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String password = passwordEncoder.encode(passenger.getPassword());
        passenger.setPassword(password);
        passengerRepo.save(passenger);
        return ResponseEntity.ok(new Response(200, "注册成功"));
    }

    @PostMapping("/logon")
    public ResponseEntity<?> logonCheck(@RequestParam("account") String account, @RequestParam("password") String password) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodePass = passengerRepo.findPasswordByAccount(account);
        if (passwordEncoder.matches(password, encodePass))
            return ResponseEntity.ok(new Response(200,"登录成功"));
        return ResponseEntity.ok(new Response(400,"密码错误"));
    }

    @PostMapping("/driverLogon")
    public ResponseEntity<?> driverLogonCheck(@RequestParam("account") String account, @RequestParam("password") String password) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodePass = driverRepo.findPasswordByAccount(account);
        if (passwordEncoder.matches(password, encodePass))
            return ResponseEntity.ok(new Response(200,"登录成功"));
        return ResponseEntity.ok(new Response(400,"密码错误"));
    }

    @PostMapping("/driverRealName")
    public ResponseEntity<?> getDriverRealNameByAccount(@RequestParam("account") String account) {
        return ResponseEntity.ok(new ResponseBrief(200, "成功", driverRepo.findRealNameByAccount(account)));
    }

    @PostMapping("/rate")
    public ResponseEntity<?> getDriverRateByAccount(@RequestParam("account") String account) {
        return ResponseEntity.ok(new ResponseBrief(200, "成功", driverRepo.getRateByAccount(account)));
    }

    @GetMapping("/get")
    public String getTest() {
        return "success";
    }

    private boolean hasDriver(String account) {
        return driverRepo.findById(account).isPresent();
    }
    private boolean hasPassenger(String account) {
        return passengerRepo.findById(account).isPresent();
    }
}
