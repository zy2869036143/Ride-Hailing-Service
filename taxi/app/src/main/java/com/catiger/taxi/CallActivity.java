package com.catiger.taxi;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.utils.SpatialRelationUtil;
import com.amap.api.maps.utils.overlay.SmoothMoveMarker;
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
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DistanceResult;
import com.amap.api.services.route.DistanceSearch;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearchV2;
import com.amap.api.services.route.WalkRouteResult;
import com.catiger.taxi.data.Driver;
import com.catiger.taxi.data.LoginRepository;
import com.catiger.taxi.data.model.LoggedInUser;
import com.catiger.taxi.tool.Order;
import com.catiger.taxi.ui.RippleView;
import com.catiger.taxi.utils.HttpUtil;
import com.catiger.taxi.utils.JsonUtil;
import com.catiger.taxi.utils.WebsocketUtil;
import com.catiger.taxi.view.FukuanDialog;
import com.catiger.taxi.view.VerificationCodeInput;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.NotificationCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.catiger.taxi.databinding.ActivityCallBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class CallActivity extends AppCompatActivity implements PoiSearch.OnPoiSearchListener, DistanceSearch.OnDistanceSearchListener, RouteSearch.OnRouteSearchListener {
    private static String TAG = "CallActivity";
    private static String sendOrderURL = "http://192.168.42.61:8083/assign";
    private static String appOrderURL = "http://192.168.42.61:8083/task";
    private static String cancelOrderURL = "http://192.168.42.61:8083/cancel";
    private AppBarConfiguration appBarConfiguration;
    private ActivityCallBinding binding;
    private RouteSearch routeSearch;
    private MapView mapView;
    private AMap aMap;
    private PoiSearch poiSearch;
    private LatLonPoint startLatLonPoint, endLatLonPoint;
    private RouteOverLay routeOverLay;
    private AMapNavi aMapNavi;
    private SmoothMoveMarker driverMarker;
    private FloatingActionButton call;
    private DistanceSearch distanceSearch;
    private Driver driver;
    private boolean picking = false;
    private LatLng preLatLon, nextLatLon;
    private Order currentOrder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //< Default Area Begin >
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ImageView backImage = binding.registerBackButton;
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        call = binding.callTaxiButton;
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!activated)
                    if(currentOrder.getApptime()==null)
                        sendOrder(sendOrderURL);
                    else
                        sendOrder(appOrderURL);
                else
                    cancelOrder(cancelOrderURL);
            }
        });
        Button phoneButton = binding.phoneBtn;
        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPhone();
            }
        });
        Button orderCancelButton = binding.canBtn;
        orderCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelOrder();
            }
        });
        //< Default Area End >
        Intent intent = getIntent();
        double slat = intent.getDoubleExtra("slat",0.0);
        double slon = intent.getDoubleExtra("slon", 0.0);
        double averMin = intent.getDoubleExtra("aver", 9999);
        double averDis = intent.getDoubleExtra("dis", 9999);
        int[] yuyue  = intent.getIntArrayExtra("time");
        String splace = intent.getStringExtra("splace");
        String endPoiID = intent.getStringExtra("poiID");
        String apptime = intent.getStringExtra("apptime");
        if(yuyue!=null) {
            TextView titleView = findViewById(R.id.reg_title);
            titleView.setText("申请预约单");
        }
        TextView averTime = findViewById(R.id.estimateTime);
        averTime.setText(averMin+"分钟");
        LoggedInUser user = this.getLoggedUser();
        currentOrder = new Order(user.getDisplayName());
        currentOrder.setSlon(slon);  currentOrder.setSlat(slat);  currentOrder.setSplace(splace);
        if(yuyue!=null) {
            String str = yuyue[0]+"-";
            if (yuyue[1]<10)
                str += 0 + "" + yuyue[1];
            else
                str += yuyue[1];
            str += "-";
            if (yuyue[2]<10)
                str += 0 + "" + yuyue[2];
            else
                str += yuyue[2];
            str += " ";
            if (yuyue[3]<10)
                str += 0 + "" + yuyue[3];
            else
                str += yuyue[3];
            str += ":";
            if (yuyue[4]<10)
                str += 0 + "" + yuyue[4];
            else
                str += yuyue[4];
            str+=":00";
            currentOrder.setApptime(str);
        }
        startLatLonPoint = new LatLonPoint(slat, slon);
        mapView = findViewById(R.id.map_call);
        mapView.onCreate(savedInstanceState);
        aMap = initAMap(mapView);
        this.enablePositionPoint(aMap);
//        this.drawStartMarker(new LatLng(slat, slon));
        try {
            distanceSearch = initDistanceSearch();
            routeSearch = initRouteSearch(getApplicationContext());
            poiSearch = this.initPoiSearch(getApplicationContext());
            poiSearch.searchPOIIdAsyn(endPoiID);
            routeOverLay = this.initRouteOverLay(aMap, getApplicationContext());
            aMapNavi = this.initAMapNavi(getApplicationContext(), new MAMapNaviListener() );
            if(apptime!=null) {
                enableWebSocket();
                // 是预约单界面
                TextView titleView = findViewById(R.id.reg_title);
                titleView.setText("预约单");
                View waitView = findViewById(R.id.waiting);
                waitView.setVisibility(View.GONE);
                TextView timeView = binding.timeC;
                timeView.setText(apptime);
                TextView destView = binding.dest;
                destView.setText(intent.getStringExtra("eplace"));
                TextView startView = binding.shangche;
                startView.setText(intent.getStringExtra("splace"));
                double elat = intent.getDoubleExtra("elat",0.0);
                double elon = intent.getDoubleExtra("elon",0.0);
                calculateDrivingRoute(aMapNavi, new LatLonPoint(slat, slon), new LatLonPoint(elat, elon));
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(new LatLng(slat, slon));
                builder.include(new LatLng(elat, elon));
                LatLngBounds bounds = builder.build();
                String dacc = intent.getStringExtra("driver");
                driver = new Driver(dacc);
                if(!driver.equals("null")) {
                    // 预约单已被司机接受
                    LinearLayoutCompat picking = findViewById(R.id.picking);
                    picking.setVisibility(View.VISIBLE);
                    View v1 = binding.nod;
                    v1.setVisibility(View.GONE);
                    View v2 = binding.afnode;
                    v2.setVisibility(View.GONE);
                    orderCancelButton.setVisibility(View.GONE);
                }else {
                    // 预约单尚未接单
                    LinearLayoutCompat picking = findViewById(R.id.picking);
                    picking.setVisibility(View.INVISIBLE);
                }
                aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
            } else {
                // 实时单页面
                View view = binding.yuyue;
                view.setVisibility(View.GONE);
                LinearLayoutCompat picking = findViewById(R.id.picking);
                picking.setVisibility(View.INVISIBLE);
            }
        } catch (AMapException | com.amap.api.maps.AMapException e) {
            e.printStackTrace();
        }
    }

    private AMap initAMap(MapView mapView) {
        AMap aMap = mapView.getMap();
        aMap.moveCamera(CameraUpdateFactory.zoomTo(16));
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        return aMap;
    }

    private void toast(String message) {
        Snackbar.make(mapView, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public LoggedInUser getLoggedUser() {
        LoginRepository loginRepository = LoginRepository.getInstance(null);
        LoggedInUser user = loginRepository.getUser();
        return user;
    }
    // < Dialog settings begin>
    private void showPicking(String license){
        new AlertDialog.Builder(this)
                .setTitle("已接单")
                .setMessage("车牌号:"+ license + "的司机已接单")
                .setPositiveButton("确定", null)
                .show();
    }

    private void showPickArea(String license) {
        new AlertDialog.Builder(this)
                .setTitle("司机到达上车点")
                .setMessage("车牌号:"+ license + "的司机已到达上车点\n")
                .setPositiveButton("确定", null)
                .show();
    }

    private void showPhone() {
        new AlertDialog.Builder(this)
                .setTitle("呼叫司机")
                .setMessage("确定给司机拨打电话吗")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        Uri data = Uri.parse("tel:" + driver.getAccount() );
                        intent.setData(data);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("否", null)
                .show();
    }

    private void cancelOrder() {
        new AlertDialog.Builder(this)
                .setTitle("取消订单")
                .setMessage("确定取消此次行程吗")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cancelOrder(cancelOrderURL);
                    }
                })
                .setNegativeButton("否", null)
                .show();
    }
    //< Ripple Animation Settings Begin >
    private boolean activated = false;

    private RippleHandler rippleHandler = new RippleHandler(this);

    private class RippleHandler extends Handler {
        private Activity activity;
        public RippleHandler(Activity activity){
            this.activity = activity;
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    enableWebSocket();
                    startSearchWave();
                    activated = true;
                    call.setImageResource(R.drawable.cancle);
                    break;
                case 2:
                    stopSearchWave();
                    webSocket.close(1000,"123");
                    activated = false;
                    routeOverLay.removeFromMap();
                    finish();
                    call.setImageResource(R.drawable.call_taxi);
                    break;
                case 3:
                    LinearLayoutCompat waiting = findViewById(R.id.waiting);
                    waiting.setVisibility(View.GONE);
                    LinearLayoutCompat picking = findViewById(R.id.picking);
                    picking.setVisibility(View.VISIBLE);
                    showPicking(driver.getLicense());
                    break;
                case 4:
                    try {
                        Button btn = binding.canBtn;
                        btn.setEnabled(false);
                        calculateDrivingRoute(aMapNavi, startLatLonPoint, endLatLonPoint);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 10:
                    Button fukuanBtn = findViewById(R.id.fukuan);
                    fukuanBtn.setVisibility(View.VISIBLE);
                    double price = msg.getData().getDouble("price");
                    Log.d(TAG,"PRICE:" + price );
                    FukuanDialog fk = new FukuanDialog(activity, price);
                    fk.setListener(new VerificationCodeInput.Listener() {
                        @Override
                        public void onComplete(String content) {
                            finish();
                        }
                    });
                    fk.show();
                    break;
                case 11:
                    TextView textView = findViewById(R.id.license);
                    textView.setText(msg.getData().getString("license"));
                    break;
            }
        }
    }

    private void startSearchWave() {
        RippleView mRippleView = findViewById(R.id.RippleView);
        mRippleView.startRippleAnimation();
    }

    private void stopSearchWave() {
        RippleView mRippleView = findViewById(R.id.RippleView);
        mRippleView.stopRippleAnimation();
    }
    //< Ripple Animation Settings End >

    // < Web Socket Settings Begin>
    public void enableWebSocket() {
        LoggedInUser user = getLoggedUser();
        WebsocketUtil websocketUtil = new WebsocketUtil(user.getDisplayName(),12,12);
        webSocket = websocketUtil.setWebSocketListener(new MWebSocketListener());
    }

    private WebSocket webSocket;

    public class MWebSocketListener extends WebSocketListener {
        private boolean aBoolean = false;
        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
            super.onMessage(webSocket, text);
            try {
                JSONObject jsonObject = new JSONObject(text);
                Log.d(TAG, "RC:" + text);
                int code = jsonObject.getInt("code");
                switch (code) {
                    case 1:
                        // 订单被接受通知
                        try {
                            double lat = jsonObject.getDouble("lat");
                            double lon = jsonObject.getDouble("lon");
                            String license = jsonObject.getString("license");
                            String account = jsonObject.getString("account");
                            driver = new Driver(account, license);
                            picking = true;
                            LatLng latLng = new LatLng(lat, lon);
                            // driverMarker.setPosition(new LatLng(lat, lon));
                            calculateDrivingRoute(aMapNavi, new LatLonPoint(lat ,lon), startLatLonPoint);
                            Message msg = new Message();
                            msg.what = 3;
                            rippleHandler.sendMessage(msg);
                            driverMarker = drawDriverMarker(latLng);
                            preLatLon = latLng;
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 2:
                        // 位置移动的通知
                        double clat = jsonObject.getDouble("lat");
                        double clon = jsonObject.getDouble("lon");
                        if(aBoolean) {
                            nextLatLon = new LatLng(clat, clon);
                            smoothMove(preLatLon, nextLatLon);
                            preLatLon = nextLatLon;
                        }
                        else {
                            preLatLon = new LatLng(clat, clon);
                            driverMarker = drawDriverMarker(preLatLon);
                            aBoolean = true;
                            picking=true;
                            Message msg = new Message();
                            String lic = jsonObject.getString("license");
                            Bundle bundle = new Bundle();
                            bundle.putString("license", lic);
                            msg.setData(bundle);
                            msg.what = 11;
                            rippleHandler.sendMessage(msg);
                            calculateDrivingRoute(aMapNavi, new LatLonPoint(clat, clon), startLatLonPoint);
                        }
                        break;
                    case 3:
                        // 司机到达上车点的通知
                        Message msg = new Message();
                        msg.what = 4;
                        rippleHandler.sendMessage(msg);
                        break;
                    case 88:
                        // 到达下车点的通知 + 收费通知
                        double price = jsonObject.getDouble("price");
                        currentOrder.setPrice(price);
                        Message priceMsg = new Message();
                        priceMsg.what = 10;
                        Bundle bundle = new Bundle();
                        bundle.putDouble("price", price);
                        Log.d(TAG,"PRICE12:" + price );
                        priceMsg.setData(bundle);
                        rippleHandler.sendMessage(priceMsg);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
            super.onOpen(webSocket, response);
        }

        @Override
        public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            super.onClosed(webSocket, code, reason);
            toast("与服务器断开连接");
            Message stopRippleMsg = new Message();
            stopRippleMsg.what = 2;
            rippleHandler.sendMessage(stopRippleMsg);
        }

        @Override
        public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
            super.onFailure(webSocket, t, response);
            Message stopRippleMsg = new Message();
            stopRippleMsg.what = 2;
            rippleHandler.sendMessage(stopRippleMsg);
            toast("与服务器连接失败");
            Log.e(TAG,"Web Socket Link Failed.");
        }
    }
    // < Web Socket Settings End>

    //< Order Send Settings Begin>
    public void sendOrder(String url) {
        Activity activity = this;
        class SendCallBack implements Callback {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String str =  response.body().string();
                    Log.d(TAG,"Order Send Received: " + str);
                    JSONObject message = new JSONObject(str);
                    int code = message.getInt("code");
                    if (code == 200 ) {
                        if(message.getString("msg").equals("ok")) {
                            toast("预约单发送成功");
                            Thread.sleep(1000*3);
                            activity.finish();
                        }

                        long oid = message.getLong("content");
                        currentOrder.setOid(oid);
                        Message startRippleMsg = new Message();
                        startRippleMsg.what = 1;
                        rippleHandler.sendMessage(startRippleMsg);
                    }else {
                        toast("订单发送后收到服务器内部错误");
                        Log.e(TAG, "/assign error code:" + code + " "+ message.getString("msg"));
                    }
                } catch (JSONException | InterruptedException e) {
                    Log.e(TAG, "Order Send Received Error Feed Back");
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG,"Order Send Error:");
                toast("订单发送失败");
                e.printStackTrace();
            }
        }
        HttpUtil httpUtil = HttpUtil.getHttpUtil();
        currentOrder.setElat(endLatLonPoint.getLatitude());
        currentOrder.setElon(endLatLonPoint.getLongitude());
        httpUtil.postAsy(url, JsonUtil.obj2Json(currentOrder), new SendCallBack());
    }

    public void cancelOrder(String url) {
        class CancelCallBack implements Callback {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                toast("取消订单失败");
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String str =  response.body().string();
                    Log.d(TAG,"Order Cancel Received: " + str);
                    JSONObject message = new JSONObject(str);
                    int code = message.getInt("code");
                    if (code == 200 ) {
                        toast("订单已取消");
                        Message stopRippleMsg = new Message();
                        stopRippleMsg.what = 2;
                        rippleHandler.sendMessage(stopRippleMsg);
                    }else {
                        toast("订单取消后收到服务器返回错误");
                        Log.e(TAG, "/assign error code:" + code + " "+ message.getString("msg"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Order Send Received Error Feed Back");
                    e.printStackTrace();
                }
            }
        }
        HttpUtil httpUtil = HttpUtil.getHttpUtil();
        httpUtil.postAyn2(url, "oid", currentOrder.getOid()+"", "driver", driver==null?"null":driver.getAccount(),new CancelCallBack());
    }
    //< Order Send Settings End>

    //< PoiSearch Settings Begin >
    private PoiSearch initPoiSearch(Context context) throws AMapException {
        PoiSearch poiSearch = new PoiSearch(context, null);
        poiSearch.setOnPoiSearchListener(this);
        return poiSearch;
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {

    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {
         endLatLonPoint = poiItem.getLatLonPoint();
         LatLng endLatLon = new LatLng(endLatLonPoint.getLatitude(), endLatLonPoint.getLongitude());
         aMap.animateCamera(CameraUpdateFactory.newLatLng(endLatLon));
         drawEndMarker(endLatLon);
         LatLngBounds.Builder builder = new LatLngBounds.Builder();
         builder.include(new LatLng(startLatLonPoint.getLatitude(), startLatLonPoint.getLongitude()));
         builder.include(endLatLon);
         currentOrder.setEplace(poiItem.getSnippet() + poiItem.getTitle());
         LatLngBounds bounds = builder.build();
         aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
         calculateDrivingRoute(aMapNavi, startLatLonPoint, endLatLonPoint);
    }
    //< PoiSearch Settings End >

    // < Map Marker Settings Begin >
    private Marker drawStartMarker(LatLng latLng) {
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(latLng);
        markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                .decodeResource(getResources(), R.drawable.blue_pos)));
        return aMap.addMarker(markerOption);
    }

    private Marker drawEndMarker(LatLng latLng) {
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(latLng);
        markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                .decodeResource(getResources(), R.drawable.red_pos)));
        return aMap.addMarker(markerOption);
    }

    private SmoothMoveMarker drawDriverMarker(LatLng latLng) {
        SmoothMoveMarker smoothMarker = new SmoothMoveMarker(aMap);
        // 设置滑动的图标
        smoothMarker.setDescriptor(BitmapDescriptorFactory.fromResource(R.drawable.customcar_mid));
        return smoothMarker;
    }

    private void smoothMove(LatLng latLng1, LatLng latLng2) {
        // 获取轨迹坐标点
        List<LatLng> points = new ArrayList<>();
        points.add(latLng1);
        points.add(latLng2);
        // 设置滑动的图标
        LatLng drivePoint = points.get(0);
        Pair<Integer, LatLng> pair = SpatialRelationUtil.calShortestDistancePoint(points, drivePoint);
        points.set(pair.first, drivePoint);
        List<LatLng> subList = points.subList(pair.first, points.size());
        // 设置滑动的轨迹左边点
        driverMarker.setPoints(subList);
        // 设置滑动的总时间
        driverMarker.setTotalDuration(10);
        // 开始滑动
        driverMarker.startSmoothMove();
    }

    private void enablePositionPoint(AMap aMap) {
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.interval(2000);
        myLocationStyle.showMyLocation(true);
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMyLocationEnabled(true);
    }
    // < Map Marker Settings End >
    // < Distance Search Settings Begin >tt
    private DistanceSearch initDistanceSearch () throws AMapException {
        DistanceSearch distanceSearch = new DistanceSearch(getApplicationContext());
        distanceSearch.setDistanceSearchListener(this);
        return distanceSearch;
    }
    @Override
    public void onDistanceSearched(DistanceResult distanceResult, int i) {
        toast("预估价格:" + distanceResult.getDistanceResults().get(0).getDistance());
    }

    // < Distance Search Settings End>
    // < Route Search Settings Begin>
    private RouteSearch initRouteSearch(Context context) throws AMapException {
        RouteSearch routeSearch = new RouteSearch(context);
        routeSearch.setRouteSearchListener(this);
        return routeSearch;
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
        if(i==1000) {
            Log.d(TAG, "价格" + driveRouteResult.getTaxiCost());
            toast("价格"  + driveRouteResult.getTaxiCost());
        } else {
            Log.e(TAG, "Calculate Route failed with error code " + i);
        }
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

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

    private void calculateDrivingRoute(AMapNavi aMapNavi, LatLonPoint start, LatLonPoint end) {
        Log.d(TAG,"ASDSADDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
        ArrayList<NaviLatLng> mStartPoints = new ArrayList<NaviLatLng>();
        ArrayList<NaviLatLng> mEndPoints = new ArrayList<NaviLatLng>();
        mStartPoints.add(new NaviLatLng(start.getLatitude(), start.getLongitude()));
        mEndPoints.add(new NaviLatLng(end.getLatitude(), end.getLongitude()));
        boolean isSuccess = aMapNavi.calculateDriveRoute(mStartPoints, mEndPoints, null , PathPlanningStrategy.DRIVING_DEFAULT);
        if (!isSuccess)
            Log.e(TAG,"Calculate driving route failed.---------------------------");
    }

    public class MAMapNaviListener implements AMapNaviListener  {
        @Override
        public void onCalculateRouteFailure(int i) {

        }

        @Override
        public void onCalculateRouteSuccess(int[] ints) {
            AMapNaviPath naviPath = aMapNavi.getNaviPath();
            if (naviPath == null) {
                Log.e(TAG,"Navi Path is a null object!");
                return;
            }
            DecimalFormat df = new DecimalFormat("#.00");
            int lengthm = naviPath.getAllLength();
            int times = naviPath.getAllTime();
            if (!picking) {
                TextView distanceView = binding.distanceView;
                distanceView.setText(df.format(lengthm*1.0/1000) + "公里");
                currentOrder.setKm(lengthm*1.0/1000);
                TextView timeView = binding.timeView;
                timeView.setText(df.format(times*1.0/60) + "分钟");
                currentOrder.setMinutes(times*1.0/60);
            } else {
                TextView licenseView = binding.license;
                licenseView.setText(driver.getLicense());
                TextView distanceView = binding.pickedDistance;
                TextView timeView = binding.pickedTime;
                distanceView.setText(df.format(lengthm*1.0/1000) + "公里");
                timeView.setText(df.format(times*1.0/60) + "分钟");
            }
            // 获取路径规划线路，显示到地图上
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
    // < Route Search Settings End >

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_call);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        binding = null;
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
        NotificationChannel chan = new NotificationChannel(
                "MyChannelId",
                "My Foreground Service",
                NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, chan.getId())
                .setContentTitle("后台定位")
                .setContentText("!23")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Notification notification = builder.setOngoing(true)
                .setContentTitle("嘟嘟司机正在后台定位")
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setChannelId(chan.getId())
                .build();
//        this.startForeground(1, notification);
//        mLocationClient.enableBackgroundLocation(2000, notification);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

}