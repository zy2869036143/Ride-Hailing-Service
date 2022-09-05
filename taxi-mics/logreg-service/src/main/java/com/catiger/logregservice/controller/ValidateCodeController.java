package com.catiger.logregservice.controller;

import com.catiger.logregservice.res.ValidateCode;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class ValidateCodeController {
    Map<String, String> account2Code = new ConcurrentHashMap<>();
    
    // 生成验证码图片
    @RequestMapping("/getCaptchaImage")
    @ResponseBody
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        
        try {
            
            response.setContentType("image/png");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Expire", "0");
            response.setHeader("Pragma", "no-cache");
            
            ValidateCode validateCode = new ValidateCode();
            // 直接返回图片
            validateCode.getRandomCodeImage(request, response);
            
        } catch (Exception e) {
            System.out.println(e);
        }
        
    }

    // 生成验证码,返回的是 base64
    @RequestMapping("/getCaptchaBase64")
    @ResponseBody
    public Object getCaptchaBase64(HttpServletRequest request, HttpServletResponse response) {

        Map result = new HashMap();
        try {
            response.setContentType("image/png");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Expire", "0");
            response.setHeader("Pragma", "no-cache");
            ValidateCode validateCode = new ValidateCode();
            // 返回base64
            String[] strs = validateCode.getRandomCodeBase64(request, response);
            result.put("url", "data:image/png;base64," + strs[1]);
            result.put("message", "created successfully");
            result.put("key", strs[0]);
            System.out.println("test=" + result.get("url"));
        } catch (Exception e) {
            System.out.println(e);
        }
        return result;
    }

    @PostMapping("/validate")
    public boolean validate(@RequestParam("an") String an, HttpServletRequest request) {
        return false;
    }


}

