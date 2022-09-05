package com.catiger.inforconsumer.controller;

import com.catiger.inforconsumer.entity.User;
import com.catiger.inforconsumer.inter.LogregClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logreg")
public class CUserController {
    @Autowired
    private LogregClient logregClient;

    @GetMapping("/test")
    public String test() {
        return logregClient.test();
    }

    @PostMapping("/postTest")
    public String postTest(@RequestBody User user) {
        return logregClient.postTest(user);
    }

}
