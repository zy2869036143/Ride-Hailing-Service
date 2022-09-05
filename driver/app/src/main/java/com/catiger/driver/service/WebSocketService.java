package com.catiger.driver.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.UserHandle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.amap.api.maps.model.LatLng;
import com.catiger.driver.data.LoginRepository;
import com.catiger.driver.data.model.LoggedInUser;
import com.catiger.driver.util.WebsocketUtil;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        LoginRepository repo = LoginRepository.getInstance(null);
        LoggedInUser user = repo.getUser();
        WebsocketUtil websocketUtil = new WebsocketUtil(user.getDisplayName(),"冀A·123AABC", 37.77486330279372, 114.52840787939559);
        websocketUtil.setWebSocketListener(new MWebSocketListener());
    }

    private class MWebSocketListener extends WebSocketListener {
        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
            super.onMessage(webSocket, text);
            try {
                JSONObject jsonObject = new JSONObject(text);
                int code = jsonObject.getInt("code");
                switch (code) {
                    case 1:
                        // 实时位置通知
                        double lat = jsonObject.getDouble("lat");
                        double lon = jsonObject.getDouble("lon");
                        break;
                    case 3:
                        // 订单派送给司机的接受
                        JSONObject orderJson = new JSONObject(jsonObject.getString("order"));
                        double startLat = orderJson.getDouble("myLat");
                        double startLon = orderJson.getDouble("myLon");
                        double endLat = orderJson.getDouble("toLat");
                        double endLon = orderJson.getDouble("endLon");
                        // 再开启接单Activity界面
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}