package com.catiger.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.NavigateArrow;
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
import com.amap.api.services.core.LatLonPoint;
import com.catiger.driver.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HistoryDetalActivity extends AppCompatActivity {
    private static final String TAG = "HistoryDetalActivity";
    private static final String traceURl = "http://192.168.42.61:8081/order/trace";
    private static final String detailURl = "http://192.168.42.61:8081/order/detailOrder";
    MapView mapView;
    AMap aMap;
    private static final int MAX_SAMPLE_POINT = 16;
    private RouteOverLay routeOverLay;
    private AMapNavi aMapNavi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detal);
        getSupportActionBar().hide();
        mapView = findViewById(R.id.map_in_history);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        aMap.moveCamera(CameraUpdateFactory.zoomTo (16));
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        long oid = getIntent().getLongExtra("oid",0);
        try {
            this.routeOverLay = initRouteOverLay(aMap, this);
            this.aMapNavi = initAMapNavi(this, new MAMapNaviListener());
        } catch (AMapException e) {
            e.printStackTrace();
        }
        getTrace(oid);
        getOrderDetail(oid);
    }

    public void getTrace(long oid) {
        class TraceCallBack implements Callback {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG,"请求订单轨迹失败");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if(jsonObject.getInt("code")==200) {
                        JSONArray jsonArray = jsonObject.getJSONArray("content");
                        int size = jsonArray.length();
                        JSONObject startJson = jsonArray.getJSONObject(0);
                        JSONObject endJson = jsonArray.getJSONObject(size-1);
                        NaviLatLng startNaviLatLon = new NaviLatLng(startJson.getDouble("lat"), startJson.getDouble("lon"));
                        NaviLatLng endNaviLatLon = new NaviLatLng(endJson.getDouble("lat"), endJson.getDouble("lon"));
                        drawStartMarker(new LatLng(startNaviLatLon.getLatitude(), startNaviLatLon.getLongitude()));
                        drawEndMarker(new LatLng(endNaviLatLon.getLatitude(), endNaviLatLon.getLongitude()));
                        ArrayList<NaviLatLng> wayPoints = new ArrayList<>();
                        int step = (size-2)/MAX_SAMPLE_POINT;
                        for(int i = 1; i <= size-2; ++i) {
                            JSONObject naviPoint = jsonArray.getJSONObject(step>0?i*step:i);
                            NaviLatLng naviLatLng = new NaviLatLng(naviPoint.getDouble("lat"), naviPoint.getDouble("lon"));
                            wayPoints.add(naviLatLng);
                        }
                        calculateDrivingRoute(aMapNavi, startNaviLatLon, endNaviLatLon, wayPoints);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        HttpUtil httpUtil = HttpUtil.getHttpUtil();
        httpUtil.postAsy1(traceURl, "oid", oid+"", new TraceCallBack() );
    }

    public void getOrderDetail(long oid) {
        class OrderDetailCallBack implements Callback {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response.body().string());
                    if(jsonObject.getInt("code")==200){
                        JSONObject orderJson = jsonObject.getJSONObject("content");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        HttpUtil httpUtil = HttpUtil.getHttpUtil();
        httpUtil.postAsy1(detailURl, "oid", oid+"", new OrderDetailCallBack());
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

    private void calculateDrivingRoute(AMapNavi aMapNavi, NaviLatLng start, NaviLatLng end, ArrayList<NaviLatLng> wayPoints) {
        ArrayList<NaviLatLng> mStartPoints = new ArrayList<NaviLatLng>();
        ArrayList<NaviLatLng> mEndPoints = new ArrayList<NaviLatLng>();
        mStartPoints.add(start);
        mEndPoints.add(end);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(start.getLatitude(), start.getLongitude()));
        builder.include(new LatLng(end.getLatitude(), end.getLongitude()));
        LatLngBounds bounds = builder.build();
        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
        boolean isSuccess = aMapNavi.calculateDriveRoute(mStartPoints, mEndPoints, wayPoints , PathPlanningStrategy.DRIVING_DEFAULT);
        if (!isSuccess)
            Log.e(TAG,"Calculate driving route failed.");
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    public class MAMapNaviListener implements AMapNaviListener {
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