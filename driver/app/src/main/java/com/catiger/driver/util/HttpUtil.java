package com.catiger.driver.util;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {
    private static String ip = "http://192.168.42.61:8083/driverPos";
    private static volatile  HttpUtil httpUtil;
    private HttpUtil(){}
    public static HttpUtil getHttpUtil() {
        if (httpUtil==null)
            httpUtil = new HttpUtil();
        return httpUtil;
    }
    public String getSyn(String url) {
        String result = null;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            result = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void getAsy(String url, Callback responseCallback) {
        String result = null;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(responseCallback);
    }

    public String postSyn(String url, String json) {
        String result = null;
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                result = response.body().string();
            } else {
                throw new IOException("Unexpected code " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void postAsy1(String url, String key, String value,  Callback responseCallback) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add(key, value)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try {
            client.newCall(request).enqueue(responseCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String postSyn2(String url, String key1, String value1,String key2, String value2) {
        String result = null;
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add(key1, value1)
                .add(key2, value2)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                result = response.body().string();
            } else {
                throw new IOException("Unexpected code " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String postAyn2(String url, String key1, String value1, String key2, String value2, Callback responseCallback) {
        String result = null;
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add(key1, value1)
                .add(key2, value2)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try {
            client.newCall(request).enqueue(responseCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void postAsy(String url, String json, Callback responseCallback) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try {
            client.newCall(request).enqueue(responseCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
