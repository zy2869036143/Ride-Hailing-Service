package com.catiger.driver;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
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
import com.amap.api.services.core.LatLonPoint;
import com.catiger.driver.data.LoginDataSource;
import com.catiger.driver.data.LoginRepository;
import com.catiger.driver.data.model.LoggedInUser;
import com.catiger.driver.gaode.SlideUnlockView;
import com.catiger.driver.util.HttpUtil;
import com.catiger.driver.util.PhoneDialog;
import com.catiger.driver.util.SlideLockView;
import com.catiger.driver.util.VerificationCodeInput;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class OrderActivity extends AppCompatActivity implements AMapLocationListener {
    private static String TAG = "OrderActivity";
    private static int disappearAnimationDuration = 1;
    private AppCompatTextView counterView;
    private CounterHandle counterHandle;
    private MapView mapView;
    private AMap aMap;
    private AMapLocationClient mLocationClient;
    private RouteOverLay routeOverLay;
    private AMapNavi aMapNavi;
    private boolean accept = false;
    private boolean result = false;
    private View pickEndZone;
    private class Order {
        public long id;
        public boolean app;
        public String account;
        public double slat, slon;
        public double elat, elon;
    }
    private Order order;
    Button finishButton;
    private static String startServe = "http://192.168.42.61:8083/server";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        getSupportActionBar().hide();
        Button refuseButton = findViewById(R.id.refuse_btn);
        finishButton = findViewById(R.id.finish);
        refuseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRefuseDialog();
            }
        });
        finishButton.setVisibility(View.INVISIBLE);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toast("已完成订单");
                HttpUtil httpUtil = HttpUtil.getHttpUtil();
                httpUtil.postAsy1("http://192.168.42.61:8083/finish", "account", getLoggedInAccount(), new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e(TAG,"订单结束请求失败");
                        e.printStackTrace();
                    }
                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            if (jsonObject.getInt("code")==200) {
                                Log.d(TAG,"订单结束");
                                double getPrice = jsonObject.getDouble("content");
                                Message msg = new Message();
                                Bundle bundle = new Bundle();
                                bundle.putDouble("price", getPrice);
                                msg.what = 88;
                                msg.setData(bundle);
                                animationHandler.sendMessage(msg);
//                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        pickEndZone = findViewById(R.id.pick_end_zone);
        Button acceptButton = findViewById(R.id.accept_btn);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accept = true;
                acceptOrder();
                View time = findViewById(R.id.time_zone);
                AnimationSet animationSetOfDisappear = new AnimationSet(true); //true表示共用同一个插值器
                final AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
                TranslateAnimation translateAnimation = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF,1.0f,
                        Animation.RELATIVE_TO_SELF,0.0f,
                        Animation.RELATIVE_TO_SELF,0.0f);
                animationSetOfDisappear.addAnimation(alphaAnimation);
                animationSetOfDisappear.addAnimation(translateAnimation);
                animationSetOfDisappear.setDuration(1000*disappearAnimationDuration);
                time.startAnimation(animationSetOfDisappear);
                time.setVisibility(View.INVISIBLE);
                Thread thread = new Thread(() -> {
                    try {
                        Thread.sleep(1000*disappearAnimationDuration);
                        Message dispearMsg = new Message();
                        dispearMsg.what = 1;
                        animationHandler.sendMessage(dispearMsg);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
                toast("您已接受订单，请前往上车点");
            }
        });
        counterView = findViewById(R.id.counter);
        counterHandle = new CounterHandle();
        mapView = findViewById(R.id.map_in_order);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        aMap.moveCamera(CameraUpdateFactory.zoomTo (16));
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        positionPoint(aMap);
        getMyCurrentLocation();
        Intent intent = getIntent();
        double slat = intent.getDoubleExtra("slat", 0.0);
        double slon = intent.getDoubleExtra("slon", 0.0);
        double elat = intent.getDoubleExtra("elat", 0.0);
        double elon = intent.getDoubleExtra("elon", 0.0);
        long oid = intent.getLongExtra("oid", 0);
        String splace = intent.getStringExtra("splace");
        String eplace = intent.getStringExtra("eplace");
        String account = intent.getStringExtra("account");
        String apptime = intent.getStringExtra("apptime");
        EditText tv = findViewById(R.id.my_position); tv.setText(splace);
        TextView ev = findViewById(R.id.eplace_view); ev.setText(eplace);
        order = new Order();
        order.id = oid;
        order.account = account;
        order.slat = slat;
        order.slon = slon;
        order.elat = elat;
        order.elon = elon;
        TextView orderType = findViewById(R.id.orderType);
        pickEndZone.setVisibility(View.INVISIBLE);
        if(apptime==null) {
            order.app = false;
            timer();
            orderType.setText("实时单");
            Button button = findViewById(R.id.order_start);
            button.setVisibility(View.GONE);
        }
        else {
            order.app = true;
            orderType.setText("预约单");
            View view = findViewById(R.id.time_zone);
            view.setVisibility(View.GONE);
            Button button = findViewById(R.id.order_start);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    class StartServeCallBack implements Callback {

                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {

                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().string());
                                if(jsonObject.getInt("code")==200) {
                                    Log.d(TAG,"开始服务啦");
                                    Message msg = new Message();
                                    msg.what=4;
                                    animationHandler.sendMessage(msg);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    LoginRepository loginRepository = LoginRepository.getInstance(new LoginDataSource());
                    LoggedInUser user = loginRepository.getUser();
                    user.getActivity().websocketLink();
                    HttpUtil httpUtil = HttpUtil.getHttpUtil();
                    httpUtil.postAyn2(startServe, "oid", order.id+"", "account", user.getDisplayName(), new StartServeCallBack());
                }
            });
        }
        drawStartMarker(new LatLng(slat, slon));
        drawEndMarker(new LatLng(elat, elon));
        try {
            routeOverLay = this.initRouteOverLay(aMap, getApplicationContext());
            aMapNavi = this.initAMapNavi(getApplicationContext(), new MAMapNaviListener() );
        } catch (AMapException e) {
            e.printStackTrace();
        }
        slider();
    }

    private void inputPhone(Activity activity) {
        PhoneHandler ph1 = new PhoneHandler(activity);
        PhoneHandler2 ph2 = new PhoneHandler2(activity, ph1);
        ph1.setPh2(ph2);
        Message msg = new Message();
        msg.what = 1;
        ph1.sendMessage(msg);
    }

    @Override
    public void onBackPressed() {
        if(!order.app)
            toast("不可返回");
        else{
            super.onBackPressed();
        }
    }

    // Responsible for get phone input and check whether or not it's correct.
    // If not, this class will send a message to the following class PhoneHandler2, which will restart a input dialog.
    private class PhoneHandler extends Handler {
        Activity activity;
        PhoneHandler2 ph2;
        public PhoneHandler(Activity activity) {
            this.activity = activity;
        }
        public void setPh2(PhoneHandler2 ph2) {
            this.ph2 = ph2;
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            String accb4 = order.account.substring(order.account.length()-4);
            switch (msg.what) {
                case 1:
                    PhoneDialog pg = new PhoneDialog(activity);
                    PhoneDialog finalPg = pg;
                    pg.setListener(new VerificationCodeInput.Listener() {
                        @Override
                        public void onComplete(String content) {
                            if(!content.equals(accb4)) {
                                toast("输入错误");
                                Message msg = new Message();
                                msg.what = 2;
                                ph2.sendMessage(msg);
                            }else {
                                calculateDrivingRoute(aMapNavi, new LatLonPoint(order.slat, order.slon), new LatLonPoint(order.elat, order.elon));
                                toast("已接到乘客，请前往目的地");
                                View pickStart = findViewById(R.id.pick_start_zone);
                                AnimationSet animationSet = new AnimationSet(true); //true表示共用同一个插值器
                                final AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
                                alphaAnimation.setDuration(1000*disappearAnimationDuration);
                                TranslateAnimation translateAnimation = new TranslateAnimation(
                                        Animation.RELATIVE_TO_SELF, 0.0f,
                                        Animation.RELATIVE_TO_SELF,1.0f,
                                        Animation.RELATIVE_TO_SELF,0.0f,
                                        Animation.RELATIVE_TO_SELF,0.0f);
                                translateAnimation.setDuration(1000*disappearAnimationDuration);
                                animationSet.addAnimation(alphaAnimation);
                                animationSet.addAnimation(translateAnimation);
                                animationSet.setDuration(1000*disappearAnimationDuration);
                                pickStart.startAnimation(animationSet);
                                pickStart.setVisibility(View.INVISIBLE);
                                Thread thread = new Thread(() -> {
                                    try {
                                        Thread.sleep(1000*disappearAnimationDuration);
                                        Message dispearMsg = new Message();
                                        dispearMsg.what = 2;
                                        animationHandler.sendMessage(dispearMsg);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                });
                                thread.start();
                                toast("您已接受订单，请前往上车点");
                                HttpUtil httpUtil = HttpUtil.getHttpUtil();
                                httpUtil.postAsy1("http://192.168.42.61:8083/arrivePick", "account", getLoggedInAccount(), new okhttp3.Callback() {
                                    @Override
                                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                        Log.e(TAG,"司机到达上车点请求失败");
                                    }

                                    @Override
                                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                        Log.d(TAG,"司机到达上车点请求成功");
                                    }
                                });
                            }
                            finalPg.dismiss();
                        }
                    });
                    pg.show();
            }
        }
    }

    // If the phone number is not correct, this class will send a message to the previous class——PhoneHandler.
    private class PhoneHandler2 extends  Handler{
        Activity activity;
        PhoneHandler ph;
        public PhoneHandler2(Activity activity, PhoneHandler ph) {
            this.activity = activity;
            this.ph = ph;
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 2:
                    Message msgn = new Message();
                    msgn.what = 1;
                    ph.sendMessage(msgn);
                    break;

            }
        }
    }

    private class CounterHandle extends Handler{
        @SuppressLint("ResourceAsColor")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            counterView.setText(msg.what + "s");

            if(msg.what <= 10) {
                counterView.setTextColor(R.color.red);
                // 设置透明度渐变动画
                final AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
                //设置动画持续时间
                alphaAnimation.setDuration(1000);
                counterView.startAnimation(alphaAnimation);
                // 设置缩放渐变动画
                final ScaleAnimation scaleAnimation =new ScaleAnimation(0.5f, 2f, 0.5f,2f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(1000);
                counterView.startAnimation(scaleAnimation);
            }

            if(msg.what == 0 && !accept) {
                refuseOrder();
                Intent result = new Intent();
                result.putExtra("result", -1);
                setResult(2, result);
                finish();
            }
        }
    }

    private class AnimationHandler extends  Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    View time = findViewById(R.id.time_zone);
                    time.setVisibility(View.GONE);
                    AnimationSet animationSetOfAppear = new AnimationSet(true); //true表示共用同一个插值器
                    final AlphaAnimation appearAlphaAnimation = new AlphaAnimation(0, 1);
                    TranslateAnimation appearTranslateAnimation = new TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, 0.0f,
                            Animation.RELATIVE_TO_SELF,0.0f,
                            Animation.RELATIVE_TO_SELF,1.0f,
                            Animation.RELATIVE_TO_SELF,0.0f);
                    animationSetOfAppear.addAnimation(appearTranslateAnimation);
                    animationSetOfAppear.addAnimation(appearAlphaAnimation);
                    animationSetOfAppear.setDuration(1000*disappearAnimationDuration);
                    pickEndZone.startAnimation(animationSetOfAppear);
                    Thread thread = new Thread(() -> {
                        try {
                            Thread.sleep(1000*disappearAnimationDuration);
                            Message newmsg = new Message();
                            newmsg.what = 3;
                            animationHandler.sendMessage(newmsg);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    thread.start();
                    break;
                case 2:
                    View pickStart = findViewById(R.id.pick_start_zone);
                    finishButton.setVisibility(View.VISIBLE);
                    pickStart.setVisibility(View.GONE);
                    break;
                case 3:
                    View pickEnd = findViewById(R.id.pick_end_zone);
                    pickEnd.setVisibility(View.VISIBLE);
                    break;
                case 4:
                    toast("开始服务");
                    Button button = findViewById(R.id.order_start);
                    button.setVisibility(View.GONE);
                    pickEndZone.setVisibility(View.VISIBLE);
                    break;
                case 88:
                    double price = msg.getData().getDouble("price");
                    showBenefition(price);
                    break;
            }
        }
    }

    private void showBenefition(double price) {
        new AlertDialog.Builder(this)
                .setTitle("已完成")
                .setMessage("预估收益"+price+"元")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .show();
    }


    private AnimationHandler animationHandler = new AnimationHandler();

    private Vibrator vibrator;

    private SlideUnlockView slideUnlockView;

    public void slider() {
        Activity activity = this;
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        slideUnlockView = (SlideUnlockView) findViewById(R.id.slideUnlockView);
        slideUnlockView.setOnUnLockListener(new SlideUnlockView.OnUnLockListener() {
            @Override
            public void setUnLocked(boolean unLock) {
                // 如果是true，证明解锁
                if (unLock) {
                    // 启动震动器 100ms
                    inputPhone(activity);
                    vibrator.vibrate(100);
                    // 当解锁的时候，执行逻辑操作，在这里仅仅是将图片进行展示
                    // 重置一下滑动解锁的控件
                    slideUnlockView.reset();
                    // 让滑动解锁控件消失
                    slideUnlockView.setVisibility(View.GONE);
                }
            }
        });
    }

    public void timer() {
        Thread thread = new Thread(()-> {
            int count = 30;
            while (count >= 0) {
                Message ms = new Message();
                ms.what = count;
                counterHandle.sendMessage(ms);
                count--;
                if (accept || result)
                    break;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void positionPoint(AMap aMap) {
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.interval(2000);
        myLocationStyle.showMyLocation(true);
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMyLocationEnabled(true);
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                double lat = aMapLocation.getLatitude();
                double lon = aMapLocation.getLongitude();
                AMapOptions aOptions = new AMapOptions();
                aOptions.camera(new CameraPosition(new LatLng(lat, lon), 10f, 0, 0));
                aMapLocation.getCity();
                if (order!=null)
                    calculateDrivingRoute(aMapNavi, new LatLonPoint(lat, lon), new LatLonPoint(order.slat, order.slon));
                else Log.e(TAG,"空order对象");
                aMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lon)));
                positionPoint(aMap);
            } else {
                Log.e(TAG,"location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }

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
                                AMapLocationClient.updatePrivacyShow(getApplicationContext(), true, true);
                                AMapLocationClient.updatePrivacyAgree(getApplicationContext(), true);
                                try {
                                    mLocationClient = new AMapLocationClient(getApplicationContext());
                                    mLocationClient.setLocationListener(this);
                                    AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
                                    mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                                    mLocationOption.setOnceLocation(true);
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

    private void timeZoneVisibility() {
        LinearLayoutCompat timeZone = findViewById(R.id.time_zone);
        timeZone.setVisibility(View.GONE);
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

    private void toast(String msg) {
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

    private void showRefuseDialog() {
        Activity activity = this;
        Dialog dialog = new AlertDialog.Builder(this)
                .setTitle("您确定放弃订单吗")
                .setMessage("放弃后无法再次获得")
                .setPositiveButton("放弃", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        refuseOrder();
                        result = true;
                        activity.finish();
                    }
                })
                .setNegativeButton("再想想", null)
                .show();
    }

    private String getLoggedInAccount() {
        LoginRepository loginRepository = LoginRepository.getInstance(new LoginDataSource());
        LoggedInUser user = loginRepository.getUser();
        return user.getDisplayName();
    }

    private void refuseOrder() {
        Log.d(TAG,"发送拒绝订单请求");
        HttpUtil httpUtil = HttpUtil.getHttpUtil();
        httpUtil.postAsy1("http://192.168.42.61:8083/refuse", "account", getLoggedInAccount(), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG,"取消订单失败");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d(TAG,"取消订单成功");
            }
        });
    }

    private void acceptOrder() {
        HttpUtil httpUtil = HttpUtil.getHttpUtil();
        httpUtil.postAsy1("http://192.168.42.61:8083/accept", "account", getLoggedInAccount(), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG,"接受订单失败");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d(TAG,"已接受订单");
            }
        });
    }

    // < Route Search Settings Begin>
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
        ArrayList<NaviLatLng> mStartPoints = new ArrayList<NaviLatLng>();
        ArrayList<NaviLatLng> mEndPoints = new ArrayList<NaviLatLng>();
        mStartPoints.add(new NaviLatLng(start.getLatitude(), start.getLongitude()));
        mEndPoints.add(new NaviLatLng(end.getLatitude(), end.getLongitude()));
        boolean isSuccess = aMapNavi.calculateDriveRoute(mStartPoints, mEndPoints, null , PathPlanningStrategy.DRIVING_DEFAULT);
        if (!isSuccess)
            Log.e(TAG,"Calculate driving route failed.");
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
            int lengthm = naviPath.getAllLength();
            int times = naviPath.getAllTime();
            TextView distance = findViewById(R.id.distanceView);
            TextView time = findViewById(R.id.timeView);
            DecimalFormat format = new DecimalFormat("#.00");
            distance.setText( (lengthm<1000?"0":"") +format.format(lengthm*1.0/1000) + "公里");
            time.setText(format.format(times*1.0/60)+"分钟");
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
}