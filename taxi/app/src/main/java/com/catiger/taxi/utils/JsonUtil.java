package com.catiger.taxi.utils;

import com.google.gson.Gson;

public class JsonUtil {
    public static String obj2Json(Object obj) {
        Gson gson = new Gson();
        String userJson = gson.toJson(obj);
        return userJson;
    }
}
