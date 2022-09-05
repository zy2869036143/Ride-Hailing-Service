package com.catiger.driver;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.enums.PathPlanningStrategy;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviRouteNotifyData;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.catiger.driver.data.LoginDataSource;
import com.catiger.driver.data.LoginRepository;
import com.catiger.driver.data.model.LoggedInUser;
import com.catiger.driver.util.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class YuYueActivity extends AppCompatActivity {
    class Order {
        long oid;
        double[] pos;
        String[] place;
        String time;
        String account;
    }
    private static String acceptURl = "http://192.168.42.61:8081/order/appapp";
    private Order order;
    private MapView mapView;
    private AMap aMap;
    RouteOverLay routeOverLay;
    AMapNavi aMapNavi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yu_yue);
        getSupportActionBar().hide();
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        Button accept = findViewById(R.id.accept);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOrder();
            }
        });
        Intent intent = getIntent();
        order = new Order();
        order.account = intent.getStringExtra("account");
        order.pos = intent.getDoubleArrayExtra("pos");
        order.oid = intent.getLongExtra("oid",0);
        order.place = intent.getStringArrayExtra("place");
        order.time = intent.getStringExtra("otime");
        System.out.println("你是傻逼吧"+order.time);
        TextView startView = findViewById(R.id.shangche);
        TextView endView = findViewById(R.id.search_link);
        TextView timeView = findViewById(R.id.time_c);
        startView.setText(order.place[0]);
        endView.setText(order.place[1]);
        timeView.setText(order.time);
        mapView = findViewById(R.id.map);
        aMap = mapView.getMap();
        aMap.moveCamera(CameraUpdateFactory.zoomTo (16));
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        positionPoint(aMap);
        mapView.onCreate(savedInstanceState);
        routeOverLay = initRouteOverLay(aMap, this);
        try {
            aMapNavi = initAMapNavi(this, new MAMapNaviListener());
            calculateDrivingRoute(aMapNavi, new NaviLatLng(order.pos[0], order.pos[1]), new NaviLatLng(order.pos[2], order.pos[3]));
        } catch (AMapException e) {
            e.printStackTrace();
        }
    }

    private RouteOverLay initRouteOverLay(AMap aMap, Context context) {
        RouteOverLay mRouteOverLay = new RouteOverLay(aMap, null, context);
        return mRouteOverLay;
    }

    private AMapNavi initAMapNavi(Context context, AMapNaviListener aMapNaviListener) throws com.amap.api.maps.AMapException {
        AMapNavi aMapNavi = AMapNavi.getInstance(context);
        aMapNavi.addAMapNaviListener(aMapNaviListener);
        aMapNavi.setUseInnerVoice(true);
        aMapNavi.setEmulatorNaviSpeed(150);
        return aMapNavi;
    }

    private void calculateDrivingRoute(AMapNavi aMapNavi, NaviLatLng start, NaviLatLng end) {
        ArrayList<NaviLatLng> mStartPoints = new ArrayList<NaviLatLng>();
        ArrayList<NaviLatLng> mEndPoints = new ArrayList<NaviLatLng>();
        mStartPoints.add(start);
        mEndPoints.add(end);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(start.getLatitude(), start.getLongitude()));
        builder.include(new LatLng(end.getLatitude(), end.getLongitude()));
        LatLngBounds bounds = builder.build();
        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
        boolean isSuccess = aMapNavi.calculateDriveRoute(mStartPoints, mEndPoints, null , PathPlanningStrategy.DRIVING_DEFAULT);
    }
    private void positionPoint(AMap aMap) {
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.interval(1000);
        myLocationStyle.showMyLocation(true);
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setMyLocationEnabled(true);// 设置为true
    }

    private void getOrder() {
        HttpUtil httpUtil = HttpUtil.getHttpUtil();
        httpUtil.postAyn2(acceptURl, "oid", order.oid+"", "account", getLoggedInAccount(), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if(jsonObject.getInt("code")==200){
                        System.out.println("已接受预约单"+ order.oid);
                        Message msg = new Message();
                        msg.what = 1;
                        toastHandler.sendMessage(msg);
                        Thread.sleep(1000*3);
                        Message msg2 = new Message();
                        msg2.what = 2;
                        toastHandler.sendMessage(msg2);
                    }
                } catch (JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    ToastHandler toastHandler = new ToastHandler(this);
    class ToastHandler extends Handler {
        Activity activity;
        public ToastHandler(Activity activity) {
            this.activity = activity;
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1)
                toast("已接受订单");
            else if(msg.what ==2 )
                activity.finish();

        }
    }


    public void toast(String msg) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.toast_custom, findViewById(R.id.toast_cus));
        TextView msgView = view.findViewById(R.id.toastView);
        msgView.setText(msg);
        Toast toast = new Toast(this);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        toast.show();
    }

    private String getLoggedInAccount() {
        LoginRepository loginRepository = LoginRepository.getInstance(new LoginDataSource());
        LoggedInUser user = loginRepository.getUser();
        return user.getDisplayName();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    public class MAMapNaviListener implements AMapNaviListener {
        @Override
        public void onCalculateRouteFailure(int i) {

        }

        @Override
        public void onCalculateRouteSuccess(int[] ints) {
            AMapNaviPath naviPath = aMapNavi.getNaviPath();
            if (naviPath == null) {
                return;
            }
            routeOverLay.setAMapNaviPath(naviPath);
            routeOverLay.addToMap();
        }
        @Override
        public void onInitNaviFailure() {

        }

        @Override
        public void onInitNaviSuccess() {

        }

        @Override
        public void onStartNavi(int i) {

        }

        @Override
        public void onTrafficStatusUpdate() {

        }

        @Override
        public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

        }

        @Override
        public void onGetNavigationText(int i, String s) {

        }

        @Override
        public void onGetNavigationText(String s) {

        }

        @Override
        public void onEndEmulatorNavi() {

        }

        @Override
        public void onArriveDestination() {

        }

        @Override
        public void onReCalculateRouteForYaw() {

        }

        @Override
        public void onReCalculateRouteForTrafficJam() {

        }

        @Override
        public void onArrivedWayPoint(int i) {

        }

        @Override
        public void onGpsOpenStatus(boolean b) {

        }

        @Override
        public void onNaviInfoUpdate(NaviInfo naviInfo) {

        }

        @Override
        public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

        }

        @Override
        public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo aMapNaviCameraInfo1, int i) {

        }

        @Override
        public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

        }

        @Override
        public void showCross(AMapNaviCross aMapNaviCross) {

        }

        @Override
        public void hideCross() {

        }

        @Override
        public void showModeCross(AMapModelCross aMapModelCross) {

        }

        @Override
        public void hideModeCross() {

        }

        @Override
        public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

        }

        @Override
        public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {

        }

        @Override
        public void hideLaneInfo() {

        }

        @Override
        public void notifyParallelRoad(int i) {

        }

        @Override
        public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

        }

        @Override
        public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

        }

        @Override
        public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

        }

        @Override
        public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

        }

        @Override
        public void onPlayRing(int i) {

        }

        @Override
        public void onCalculateRouteSuccess(AMapCalcRouteResult aMapCalcRouteResult) {

        }

        @Override
        public void onCalculateRouteFailure(AMapCalcRouteResult aMapCalcRouteResult) {

        }

        @Override
        public void onNaviRouteNotify(AMapNaviRouteNotifyData aMapNaviRouteNotifyData) {

        }

        @Override
        public void onGpsSignalWeak(boolean b) {

        }
    }

}