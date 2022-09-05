package com.catiger.logregservice.res;

import com.google.gson.Gson;

public class JsonUtil {
    public static String obj2Json(Object object){
        Gson gson = new Gson();
        String userJson = gson.toJson(object);
        return userJson;
    }
}
