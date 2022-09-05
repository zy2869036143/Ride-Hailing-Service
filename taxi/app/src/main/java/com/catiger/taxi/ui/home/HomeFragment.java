package com.catiger.taxi.ui.home;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.security.identity.PersonalizationData;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.fastjson.JSONArray;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.RotateAnimation;
import com.amap.api.maps.model.animation.ScaleAnimation;
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
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.catiger.taxi.CallActivity;
import com.catiger.taxi.R;
import com.catiger.taxi.RigisterDriverActivity;
import com.catiger.taxi.SearchActivity;
import com.catiger.taxi.YuYueActivity;
import com.catiger.taxi.data.Driver;
import com.catiger.taxi.data.LoginDataSource;
import com.catiger.taxi.data.LoginRepository;
import com.catiger.taxi.data.model.LoggedInUser;
import com.catiger.taxi.databinding.BeginEndPosBinding;
import com.catiger.taxi.databinding.FragmentHomeBinding;
import com.amap.api.navi.view.RouteOverLay;
import com.catiger.taxi.databinding.SearchItemBinding;
import com.catiger.taxi.ui.login.LoginActivity;
import com.catiger.taxi.utils.HttpUtil;
import com.catiger.taxi.utils.WebsocketUtil;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;


public class HomeFragment extends Fragment implements AMapNaviListener, AMapLocationListener, WeatherSearch.OnWeatherSearchListener,
         GeocodeSearch.OnGeocodeSearchListener {
    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    final int searchActivityCode = 10;
    final int loginActivityCode = 11;
    private MapView mapView;
    private AMap aMap;
    private PoiSearch poiSearch;
    private boolean moved = false;
    // 起点，终点
    private TextView mCurrentLocationTextView, searchText;
    // 所在地，天气
    private TextView cityTextView, weatherTextView;
    private RouteSearch routeSearch;
    public  AMapLocationClient mLocationClient = null;
    private NaviLatLng startPoint, endPoint;
    private ArrayList<NaviLatLng> mStartPoints = new ArrayList<NaviLatLng>();
    private ArrayList<NaviLatLng> mEndPoints = new ArrayList<NaviLatLng>();
    private RouteOverLay mRouteOverLay;
    private AMapNavi aMapNavi;
    private Marker mapCenterPoint, driverPoint;
    GeocodeSearch geocoderSearch;
    private String tempAccount = "test";
    private SmoothMoveMarker[] driverMarkers = new SmoothMoveMarker[20];
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        View actionMore = binding.actionMore;
        actionMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), RigisterDriverActivity.class);
                startActivity(intent);
            }
        });
        cityTextView = binding.city;
        weatherTextView = binding.weather;
        View headView =  binding.homeHead;
        mapView = binding.map;
        mapView.onCreate(savedInstanceState);

        initAMap();
        // 申请定位全兴并获取当前位置信息
        getMyCurrentLocation();
        // 点击搜索框，跳转到搜索activity
       initSearchClick();
        try {
            initPoiSearch();
            initGeo();
        } catch (AMapException e) {
            e.printStackTrace();
        }
        // 设置化我的定位小蓝点
        positionPoint(aMap);
        // 初始化路径搜索功能
        mRouteOverLay = new RouteOverLay(aMap, null, this.getContext());
        try {
            aMapNavi = AMapNavi.getInstance(this.getContext());
            aMapNavi.addAMapNaviListener(this);
            aMapNavi.setUseInnerVoice(true);
            aMapNavi.setEmulatorNaviSpeed(150);
            mRouteOverLay.destroy();
        } catch (com.amap.api.maps.AMapException e) {
            e.printStackTrace();
        }
        try {
            enableWebSocket();
        }catch (Exception e){
            e.printStackTrace();
        }
        showProgressDialog("请稍等","正在加载");
        return root;
    }
    boolean f = false;
    boolean activate = true;
    LatLng centerLatLon;
    public void initAMap() {
        aMap = mapView.getMap();
        aMap.moveCamera(CameraUpdateFactory.zoomTo (16));
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (activate) {
                    mCurrentLocationTextView.setText("正在获取上车地点...");
                    LatLng latLng = cameraPosition.target;
                    // 不能使用aMap.clear();因为会造成之前绘制的定位小蓝点消失，且移动地图时会造成中心定位点闪烁。
                    // 绘制新的
                    centerLatLon = latLng;
                    if (f) {
                        mapCenterPoint.setPosition(latLng);
                        sendCenterRequest(latLng.latitude, latLng.longitude);
                    } else {
                        // 初始化Marker设置，且只初始化一次
                        MarkerOptions markerOption = new MarkerOptions();
                        markerOption.position(latLng);
                        markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(), R.drawable.center_pos_icon_s)));
                        mapCenterPoint = aMap.addMarker(markerOption);
//                        jumpPoint(mapCenterPoint);
                        f = true;
                    }
                }
            }
            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                LatLng latLng = mapCenterPoint.getPosition();
                String string =  mapCenterPoint.getTitle();
                startPoint = new NaviLatLng(latLng.latitude, latLng.longitude);
                try {
                    geoCode(new LatLonPoint(latLng.latitude, latLng.longitude));
                } catch (AMapException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "坐标：" + latLng.latitude + "," + latLng.longitude+ " 标题" + string);
            }
        });
    }

    private void markerAnimation(Marker marker) {
//        Animation animation = new RotateAnimation(marker.getRotateAngle(),marker.getRotateAngle()+180,0,0,0);
        Animation animation = new ScaleAnimation(1,0.4f,1,0.4f);
        long duration = 500L;
        animation.setDuration(duration);
        animation.setInterpolator(new LinearInterpolator());
        marker.setAnimation(animation);
        marker.startAnimation();
    }

    public void jumpPoint(final Marker marker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = aMap.getProjection();
        final LatLng markerLatlng = marker.getPosition();
        Point markerPoint = proj.toScreenLocation(markerLatlng);
        markerPoint.offset(0, -100);
        final LatLng startLatLng = proj.fromScreenLocation(markerPoint);
        final long duration = 1500;
        final Interpolator interpolator = new BounceInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * markerLatlng.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * markerLatlng.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private void initGeo() throws AMapException {
        geocoderSearch = new GeocodeSearch(this.getContext());
        geocoderSearch.setOnGeocodeSearchListener(this);
    }

    private void geoCode(LatLonPoint latLonPoint) throws AMapException {
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        List<PoiItem> pois = regeocodeResult.getRegeocodeAddress().getPois();
        mCurrentLocationTextView.setText(regeocodeResult.getRegeocodeAddress().getFormatAddress());
        mapCenterPoint.setTitle(regeocodeResult.getRegeocodeAddress().getFormatAddress());
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        Log.e(TAG,"AS");
    }

    class MarkerHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int index = msg.what;
            double lat = ((LatLng)msg.obj).latitude;
            double lon = ((LatLng)msg.obj).longitude;
            if(driverMarkers[index]!=null) {
                driverMarkers[index].destroy();
                Log.d(TAG, "Destroy Marker With index " +  index);
            }
            driverMarkers[index] = new SmoothMoveMarker(aMap);
            driverMarkers[index].setDescriptor(BitmapDescriptorFactory.fromResource(R.drawable.customcar_mid));
            driverMarkers[index].setPosition(new LatLng(lat, lon));
        }
    }

    private MarkerHandler markerHandler = new MarkerHandler();

    double averMinutes = 9999;

    double averDistance = 9999;

    private void sendCenterRequest(double lat, double lon) {
        HttpUtil httpUtil = HttpUtil.getHttpUtil();
        String url = "http://192.168.42.61:8083/center?account="+tempAccount+"&lat="+lat+"&lon=" + lon;
        Thread thread = new Thread(()->{
            try {
                averMinutes = 9999;
                averDistance = 9999;
                String result =  httpUtil.getSyn(url);
                JSONObject resultJson = new JSONObject(result);
                if (resultJson.getInt("code")==200) {
                    String content =  resultJson.getString("content").replace("\\","");
                    content = content.substring(1, content.length()-1);
                    JSONArray jsonArr = JSONArray.parseArray(content);
                    Log.d(TAG,"车辆数量:" + jsonArr.size());
                    if(jsonArr.size() > 0) {
                        averMinutes = 0;
                        averDistance = 0;
                    }
                    int i = 0;
                    for (;i < jsonArr.size(); ++i) {
                        com.alibaba.fastjson.JSONObject obj = jsonArr.getJSONObject(i);
                        int index = obj.getInteger("index");
                        Log.e(TAG, "index : " + index);
                        double latnew = obj.getDouble("lat");
                        double lonnew = obj.getDouble("lon");
                        averMinutes += obj.getDouble("min");
                        averDistance += obj.getDouble("km");
                        Message msg = new Message();
                        msg.obj = new LatLng(latnew, lonnew);
                        msg.what = index;
                        markerHandler.sendMessage(msg);
                    }
                    if(i>0) {
                        averMinutes /= i;
                        averDistance /= i;
                    }
                } else {
                    toast("请求失败");
                }
            } catch (JSONException e) {
            }catch (Exception e) {
            }
        });
        thread.start();
    }

    private void toast(String msg) {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.toast_custom, getView().findViewById(R.id.toast_cus));
            TextView msgView = view.findViewById(R.id.toastView);
            msgView.setText(msg);
            Toast toast = new Toast(getContext());
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(view);
            toast.show();
    }

    // < Logged to Search else to Login Settings Begin >
    private boolean hasLogged(Context context) {
        LoginRepository loginRepository = LoginRepository.getInstance(new LoginDataSource());
        LoggedInUser user = loginRepository.getUser();
        return user != null;
    }

    private void startLoginActivity(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        startActivityForResult(intent, loginActivityCode);
    }

    private void loggedToSearch(Context context) {
        if(hasLogged(context))
            startSearchActivity(context);
        else
            startLoginActivity(context);
    }

    private void smoothMove(LatLng latlon, int index) {
        // 获取轨迹坐标点
        Log.e(TAG,"Smooth Move With Index:" + index);
        SmoothMoveMarker marker = driverMarkers[index];
        List<LatLng> points = new ArrayList<>();
        points.add(marker.getPosition());
        points.add(latlon);
        LatLng drivePoint = points.get(0);
        Pair<Integer, LatLng> pair = SpatialRelationUtil.calShortestDistancePoint(points, drivePoint);
        points.set(pair.first, drivePoint);
        List<LatLng> subList = points.subList(pair.first, points.size());
        marker.setPoints(subList);
        marker.setTotalDuration(10);
        marker.startSmoothMove();
    }
    // < Watching cars moving around settings begin >

    public void enableWebSocket() {
        WebsocketUtil websocketUtil = new WebsocketUtil(tempAccount,12,12);
        websocketUtil.setWebSocketListener(new MWebSocketListener());
    }

    public class MWebSocketListener extends WebSocketListener {
        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
            super.onMessage(webSocket, text);
            try {
                JSONObject jsonObject = new JSONObject(text);
                Log.d(TAG, "RC:" + text);
                int code = jsonObject.getInt("code");
                switch (code) {
                    case 8:
                        int index = jsonObject.getInt("index");
                        double lat = jsonObject.getDouble("lat");
                        double lon = jsonObject.getDouble("lon");
                        smoothMove(new LatLng(lat, lon), index);
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
        public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
            super.onFailure(webSocket, t, response);
            Log.e(TAG,"Web Socket Link Failed.");
        }
    }
    // < Watching cars moving around settings end >

    private void startSearchActivity(Context context) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra("city",cityTextView.getText().toString());
        startActivityForResult(intent, searchActivityCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case searchActivityCode:
                // 当右滑返回上一个活动时，此Activity返回的Intent data为null.
                if (data!=null) {
                    String poiID = data.getStringExtra("PoiID");
                    Intent intent = new Intent(getContext(), CallActivity.class);
                    intent.putExtra("aver", averMinutes);
                    intent.putExtra("dis", averDistance);
                    intent.putExtra("slat", mapCenterPoint.getPosition().latitude);
                    intent.putExtra("slon", mapCenterPoint.getPosition().longitude);
                    intent.putExtra("splace", mCurrentLocationTextView.getText().toString());
                    intent.putExtra("poiID", poiID);
                    startActivity(intent);
                    // poiSearch.searchPOIIdAsyn(poiID);
                    activate = false;
                }
                break;
            case loginActivityCode:
                break;
        }
    }
    // < Logged to Search else to Login Settings End >
    // 当前位置改变后的回调函数，在其中与司机通过WebSocket胡同位置信息
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                /*
                 * 获取定位成功
                 * 可在其中解析aMapLocation获取相应内容。
                 */
                double lat = aMapLocation.getLatitude();
                double lon = aMapLocation.getLongitude();
                AMapOptions aOptions = new AMapOptions();
                aOptions.camera(new CameraPosition(new LatLng(lat, lon), 10f, 0, 0));
                Log.e(TAG,"--------------------------------------------------");
                aMapLocation.getCity();
                if(!moved) {
                    aMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lon)));
                    moved = true;
                }
                cityTextView.setText(aMapLocation.getCity());
                if(cityTextView.getText().toString()!="" && moved) {
                    hideProgressDialog();
                }
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("code", 1);
                data.put("lat", lat);
                data.put("lon", lon);
                JSONObject obj = new JSONObject(data);
                try {
                    weatherSearch(aMapLocation.getCity());
                } catch (AMapException e) {
                    e.printStackTrace();
                }
                mCurrentLocationTextView.setText(aMapLocation.getAoiName());
            } else {
                /*
                 * 定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                 */
                Log.e(TAG,"location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }

    ProgressDialog progressDialog ;

    public void showProgressDialog(String title, String message) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this.getContext(), title,
                    message, true, false);
        } else if (progressDialog.isShowing()) {
            progressDialog.setTitle(title);
            progressDialog.setMessage(message);
        }
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    // 获取我的当前位置的函数，提示用户并获取定位权限后，在其中启动我的定位功能
    private void getMyCurrentLocation() {
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                                AMapLocationClient.updatePrivacyShow(this.getContext(), true, true);
                                AMapLocationClient.updatePrivacyAgree(this.getContext(), true);
                                try {
                                    mLocationClient = new AMapLocationClient(this.getContext());
                                    mLocationClient.setLocationListener(this);
                                    AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
                                    mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                                    mLocationOption.setLastLocationLifeCycle(1000);
                                    mLocationOption.setOnceLocation(false);
                                    mLocationClient.setLocationOption(mLocationOption);
                                    mLocationClient.startLocation();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                            } else {
                                // No location access granted.
                            }
                        }
                );
        // Before you perform the actual permission request, check whether your app
        // already has the permissions, and whether your app needs to show a permission
        // rationale dialog. For more details, see Request permissions.
        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void positionPoint(AMap aMap) {
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.interval(1000);
        myLocationStyle.showMyLocation(true);
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true
    }

    private void initPoiSearch() throws AMapException {
        poiSearch = new PoiSearch(this.getContext(), null);
        poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int i) {
            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int i) {
                LatLonPoint latLngPoint = poiItem.getLatLonPoint();
                endPoint = new NaviLatLng(latLngPoint.getLatitude(), latLngPoint.getLongitude());
                LatLng latLng = new LatLng(latLngPoint.getLatitude(),latLngPoint.getLongitude());
//                aMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(latLngPoint.getLatitude(), latLngPoint.getLongitude())));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.visible(true);
                aMap.addMarker(markerOptions);
                searchText.setText(poiItem.getTitle());
//                calculateDriveRoute(startPoint, endPoint);
            }
        });
    }

    private void weatherSearch(String city) throws AMapException {
        AMapLocationClient.updatePrivacyShow(this.getContext(), true, true);
        AMapLocationClient.updatePrivacyAgree(this.getContext(), true);
        WeatherSearchQuery mquery = new WeatherSearchQuery(city, WeatherSearchQuery.WEATHER_TYPE_LIVE);
        WeatherSearch mweathersearch = new WeatherSearch(this.getContext());
        mweathersearch.setOnWeatherSearchListener(this);
        mweathersearch.setQuery(mquery);
        mweathersearch.searchWeatherAsyn(); //异步搜索
    }

    @Override
    public void onWeatherLiveSearched(LocalWeatherLiveResult weatherLiveResult , int i) {
        if (i == 1000) {
            if (weatherLiveResult != null&&weatherLiveResult.getLiveResult() != null) {
                LocalWeatherLive weatherlive = weatherLiveResult.getLiveResult();
                weatherTextView.setText(weatherlive.getTemperature()+"°");
            }
        }
    }

    @Override
    public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {

    }

    private void initSearchClick() {
        BeginEndPosBinding beginEndLines = binding.beginEndLines;
        SearchItemBinding endLine = beginEndLines.endLine;
        searchText = endLine.dialogSearch;
        searchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus)
                    loggedToSearch(getContext());
            }
        });
        searchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loggedToSearch(getContext());
            }
        });
        mCurrentLocationTextView = beginEndLines.myPosition;

        View view = beginEndLines.yuyueView;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startYuYueActivity();
            }
        });
    }
    private void startYuYueActivity() {
        if(!hasLogged(this.getContext())) {
            startLoginActivity(this.getContext());
            return;
        }
        Intent intent = new Intent(this.getContext(), YuYueActivity.class);
        intent.putExtra("slat", mapCenterPoint.getPosition().latitude);
        intent.putExtra("slon", mapCenterPoint.getPosition().longitude);
        intent.putExtra("city", cityTextView.getText().toString());
        intent.putExtra("splace", mCurrentLocationTextView.getText().toString());
        startActivity(intent);
    }

    private void notification(String title, String msg) {
        Intent intent = new Intent(this.getContext(), YuvImage.class);
        PendingIntent pi = PendingIntent.getActivity(this.getContext(), 0, intent, 0);
        NotificationManager manager = (NotificationManager) this.getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this.getContext())
                .setContentTitle(title).setWhen(System.currentTimeMillis()).setContentIntent(pi).build();
        manager.notify(1, notification);
    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {
        AMapNaviPath naviPath = aMapNavi.getNaviPath();
        if (naviPath == null) {
            Log.e(TAG,"Navi Path is a null object!");
            return;
        }
        int lengthm = naviPath.getAllLength();
        int times = naviPath.getAllTime();
        // 获取路径规划线路，显示到地图上
        mRouteOverLay.setAMapNaviPath(naviPath);
        mRouteOverLay.addToMap();
    }

    @Override
    public void onCalculateRouteFailure(int i) {
        Log.e(TAG,"路径规划出错 : " + i);
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
        moved = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        moved = false;
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mapView!=null)
            mapView.onSaveInstanceState(outState);
    }

    //----empty method dividing line------

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