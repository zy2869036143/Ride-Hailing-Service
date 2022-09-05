package com.catiger.logregservice.controller;

import com.catiger.logregservice.dao.Pos;
import com.catiger.logregservice.repo.PosRepo;
import com.catiger.logregservice.res.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/pos")
public class PosController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    PosRepo posRepo;
    @PostMapping("/save")
    public ResponseEntity<?> savePos(@RequestBody @Validated Pos pos) {
        pos.setTime(LocalDateTime.now());
        posRepo.save(pos);
        return ResponseEntity.ok(new Response(200, "已保存"));
    }
}
