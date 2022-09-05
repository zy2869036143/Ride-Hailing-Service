package com.catiger.driver.util;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebsocketUtil {
    private static String TAG = "WebsocketUtil";
    private OkHttpClient okHttpClient;
    // example ws://localhost:8083/ws/websocket?account=13722819304&lat=37.77486330279372&lon=114.52840787939559&license=冀A·123AABC
    private String address = "ws://192.168.42.61:8083/ws/websocket?";
    private WebSocket webSocket;
    public WebsocketUtil(String account, double lat, double lon) {
        okHttpClient = new OkHttpClient.Builder()
                .pingInterval(40, TimeUnit.SECONDS) // 设置 PING 帧发送间隔---包活
                .build();
        address = address + "account=" + account + "&lat=" + lat + "&lon=" + lon;
    }
    public WebsocketUtil(String account, String licence, double lat, double lon) {
        okHttpClient = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MICROSECONDS)
                .pingInterval(40, TimeUnit.SECONDS) // 设置 PING 帧发送间隔---包活
                .build();
        address = address + "account=" + account + "&license="+ licence + "&lat=" + lat + "&lon=" + lon;
    }

    public WebSocket setWebSocketListener(WebSocketListener webSocketListener){
        Request request = new Request.Builder()
                .url(address)
                .build();
        webSocket = okHttpClient.newWebSocket(request, webSocketListener);
        return webSocket;
    }

    public void closeWebSocketLink() {
        webSocket.close(1,"关闭");
    }


}
