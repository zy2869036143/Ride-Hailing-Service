package com.catiger.rtpconsumer.inter;

import com.google.gson.Gson;

public class JsonUtil {
    public static String obj2Json(Object object){
        Gson gson = new Gson();
        String userJson = gson.toJson(object);
        return userJson;
    }
}
