package com.catiger.taxi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.icu.util.BuddhistCalendar;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.catiger.taxi.utils.HttpUtil;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import tyrantgit.explosionfield.ExplosionField;

public class HistoryDetailActivity extends AppCompatActivity {

    private static final String TAG = "HistoryDetalActivity";
    private static final String traceURl = "http://192.168.42.61:8081/order/trace";
    private static final String detailURl = "http://192.168.42.61:8081/order/briefHis";
    private static final String rateURl = "http://192.168.42.61:8081/order/setRate";

    MapView mapView;
    AMap aMap;
    private static final int MAX_SAMPLE_POINT = 16;
    private RouteOverLay routeOverLay;
    private AMapNavi aMapNavi;
    private long oid;
    LinearLayout lin1, lin2, lin3;
    ImageView image1, image2, image3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);
        getSupportActionBar().hide();
        mapView = findViewById(R.id.map_in_history);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        aMap.moveCamera(CameraUpdateFactory.zoomTo (16));
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        lin1 = findViewById(R.id.lin1);
        lin2 = findViewById(R.id.lin2);
        lin3 = findViewById(R.id.lin3);
        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        View back = findViewById(R.id.back_history);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        oid = getIntent().getLongExtra("oid",0);
        try {
            this.routeOverLay = initRouteOverLay(aMap, this);
            this.aMapNavi = initAMapNavi(this, new MAMapNaviListener());
        } catch (AMapException e) {
            e.printStackTrace();
        }
        getTrace(oid);
        getOrderDetail(oid);
    }

    class UiHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            switch (msg.what) {
                case 1:
                    TextView textView = findViewById(R.id.license_history);
                    textView.setText(bundle.getString("first")+"师傅 " + bundle.getString("license"));
                    int rate = bundle.getInt("rate");
                    if(rate > 0) {
                        TextView manyi = findViewById(R.id.manyima);
                        manyi.setText("感谢您的评价");
                        disapear(rate);
                    }else {
                        lin1.setOnClickListener(new ClickToSend(oid, 1));
                        lin2.setOnClickListener(new ClickToSend(oid, 2));
                        lin3.setOnClickListener(new ClickToSend(oid, 3));
                    }
                    break;
                case 2:
                    TextView manyi = findViewById(R.id.manyima);
                    manyi.setText("感谢您的评价");
                    disapear(bundle.getInt("rate"));
                    break;
            }
        }
    }
    public void disapear(int rate) {
        ColorStateList colorStateList= ContextCompat.getColorStateList(getApplicationContext(), R.color.blue);
        ExplosionField explosionField = new ExplosionField(this);
        if(rate==1) {
            explosionField.explode(lin2);
            explosionField.explode(lin3);
            lin2.setVisibility(View.GONE);
            lin3.setVisibility(View.GONE);
            image1.setImageTintList(colorStateList);
        }else if(rate==2) {
            explosionField.explode(lin1);
            explosionField.explode(lin3);
            lin1.setVisibility(View.GONE);
            lin3.setVisibility(View.GONE);
            image2.setImageTintList(colorStateList);
        }else if(rate==3) {
            explosionField.explode(lin2);
            explosionField.explode(lin1);
            lin1.setVisibility(View.GONE);
            lin2.setVisibility(View.GONE);
            image3.setImageTintList(colorStateList);
        }

    }
    class ClickToSend implements View.OnClickListener {
        long oid;
        int rate;
        public ClickToSend(long oid, int rate) {
            this.oid = oid;
            this.rate = rate;
        }
        @Override
        public void onClick(View view) {
            HttpUtil httpUtil = HttpUtil.getHttpUtil();
            httpUtil.postAyn2(rateURl, "oid", this.oid+"", "rate", this.rate+"", new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        jsonObject.getInt("code");
                        if(jsonObject.getInt("code")==200) {
                            Message msg = new Message();
                            msg.what = 2;
                            Bundle bundle = new Bundle();
                            bundle.putInt("rate", rate);
                            msg.setData(bundle);
                            uiHandler.sendMessage(msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private UiHandler uiHandler = new UiHandler();

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
                    Bundle bundle = new Bundle();
                    bundle.putString("first", orderJson.getString("first"));
                    bundle.putString("license", orderJson.getString("license"));
                    bundle.putInt("rate", orderJson.getInt("rate")) ;
                    Message msg = new Message();
                    msg.what = 1;
                    msg.setData(bundle);
                    uiHandler.sendMessage(msg);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void getOrderDetail(long oid) {
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