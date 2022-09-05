package com.catiger.inforconsumer.inter;

import com.catiger.inforconsumer.entity.User;

public class LogregClientFallback implements LogregClient {
    @Override
    public String test() {
        return "fail";
    }

    @Override
    public String postTest(User user) {
        return "fail";
    }
}
