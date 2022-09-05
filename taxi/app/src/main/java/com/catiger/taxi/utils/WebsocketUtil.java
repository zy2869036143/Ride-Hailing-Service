package com.catiger.taxi.utils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebsocketUtil {
    private static String TAG = "WebsocketUtil";
    private OkHttpClient okHttpClient;
    // example ws://localhost:8083/ws/websocket?account=188&lat=13.3&lon=13.3
    private String address = "ws://192.168.42.61:8083/ws/websocket?";

    public WebsocketUtil(String account, double lat, double lon) {
        okHttpClient = new OkHttpClient.Builder()
                .pingInterval(40, TimeUnit.SECONDS) // 设置 PING 帧发送间隔---包活
                .build();
        address = address + "account=" + account + "&lat=" + lat + "&lon=" + lon;
    }

    public WebsocketUtil(String account, String licence, double lat, double lon) {
        okHttpClient = new OkHttpClient.Builder()
                .pingInterval(40, TimeUnit.SECONDS) // 设置 PING 帧发送间隔---包活
                .build();
        address = address + "account=" + account + "&license="+ licence + "&lat=" + lat + "&lon=" + lon;
    }

    public WebSocket setWebSocketListener(WebSocketListener webSocketListener){
        Request request = new Request.Builder()
                .url(address)
                .build();
        WebSocket socket = okHttpClient.newWebSocket(request, webSocketListener);
        return socket;
    }
    public void closeWebSocketLink() {
        okHttpClient.dispatcher().executorService().shutdown();
    }
}
