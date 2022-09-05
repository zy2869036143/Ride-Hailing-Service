package com.catiger.inforconsumer.inter;

import com.catiger.inforconsumer.config.FeignConfig;
import com.catiger.inforconsumer.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "logreg-service",
        configuration = FeignConfig.class,
        fallback = LogregClientFallback.class)
public interface LogregClient {

    @GetMapping("/user/get")
    String test();

    @PostMapping("/user/register")
    String postTest(User user);

}