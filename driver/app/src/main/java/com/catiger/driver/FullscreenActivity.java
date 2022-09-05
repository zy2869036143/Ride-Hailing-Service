package com.catiger.driver;

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.YuvImage;
import android.icu.util.BuddhistCalendar;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.ServiceSettings;
import com.catiger.driver.data.LoginDataSource;
import com.catiger.driver.data.LoginRepository;
import com.catiger.driver.data.model.LoggedInUser;
import com.catiger.driver.databinding.ActivityFullscreenBinding;
import com.catiger.driver.ui.login.LoginActivity;
import com.catiger.driver.util.HttpUtil;
import com.catiger.driver.util.WebsocketUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.security.PublicKey;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class FullscreenActivity extends AppCompatActivity implements AMapLocationListener {
    private static final String TAG = "FullscreenActivity";
    private static final boolean AUTO_HIDE = true;
    private static final int OrderActivityCode = 2;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler(Looper.myLooper());
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
            if (Build.VERSION.SDK_INT >= 30) {
                mContentView.getWindowInsetsController().hide(
                        WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            } else {
                // Note that some of these constants are new as of API 16 (Jelly Bean)
                // and API 19 (KitKat). It is safe to use them, as they are inlined
                // at compile-time and do nothing on earlier devices.
                mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (AUTO_HIDE) {
                        delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private ActivityFullscreenBinding binding;
    private MapView mapView;
    private AMap aMap;
    private AMapLocationClient mLocationClient = null;
    private RippleView mRippleView;
    private FloatingActionButton floatingButton;
    private boolean linking = false;
    private boolean chosen = false;
    private TextView stateView, licenseView;
    private String remind, license;
    private WebSocket webSocket;
    private LatLng latestLatLon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ServiceSettings.updatePrivacyShow(getApplicationContext(), true, true);
        ServiceSettings.updatePrivacyAgree(getApplicationContext(), true);
        binding = ActivityFullscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        remind = "请先登录";
        stateView = findViewById(R.id.stateView);
        licenseView = findViewById(R.id.licenseView);
        mapView = binding.map;
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        aMap.moveCamera(CameraUpdateFactory.zoomTo (16));
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        mVisible = true;
        mContentView = binding.map;
        mRippleView= (RippleView) findViewById(R.id.RippleView);
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
        getMyCurrentLocation();
        setFloatingButton();
        View toLogon = findViewById(R.id.login_view);
        toLogon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!linking) {
                    // User hasn't logged on or hasn't chose the car.
                    if(hasLogged(getApplicationContext())) {
                        // Chose car
                        showCarList(view);
                    }
                    else
                        // Logon
                        loggedToSearch(getApplicationContext());
                } else {
                    toast("请先取消听单");
                }
            }
        });
        initListListener();
    }

    public void initListListener() {
        View view = findViewById(R.id.jiedanjilu);
        Activity activity = this;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasLogged(activity)) {
                    Intent intent = new Intent(activity, HistoryActivity.class);
                    startActivity(intent);
                }else
                    toast("请先登录");
            }
        });
    }

    public void getCountPrice()  {
        HttpUtil httpUtil = HttpUtil.getHttpUtil();
        httpUtil.postAsy1("http://192.168.42.61:8081/order/dprice", "account", getLoggedInAccount(), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "请求今日订单数和收益失败");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if(jsonObject.getInt("code")==200) {
                        JSONObject content = jsonObject.getJSONObject("content");
                        Bundle bundle = new Bundle();
                        bundle.putInt("count", content.getInt("count"));
                        bundle.putDouble("price", content.getDouble("price"));
                        Message msg = new Message();
                        msg.what = 13;
                        msg.setData(bundle);
                        uiHandle.sendMessage(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void showCarList(View view) {
        HttpUtil httpUtil = HttpUtil.getHttpUtil();
        httpUtil.postAsy1("http://192.168.42.61:8081/car/account2license", "account", getLoggedInAccount(), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                toast("获取我的车辆失败");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if(jsonObject.getInt("code")==200) {
                        JSONArray jsonArray = jsonObject.getJSONArray("content");
                        ArrayList<String> licenseList = new ArrayList<>();
                        for (int i=0; i< jsonArray.length(); ++i) {
                            licenseList.add(jsonArray.getString(i));
                        }
                        Message msg = new Message();
                        msg.what = 5;
                        Bundle bundle = new Bundle();
                        bundle.putStringArrayList("license", licenseList);
                        msg.setData(bundle);
                        uiHandle.sendMessage(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setFloatingButton() {
        floatingButton = findViewById(R.id.link_server);
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!linking) {
                    if(chosen)
                        websocketLink();
                    else toast(remind);
                } else showConfirm();
            }
        });
    }

    private void showConfirm() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("确认取消停单?");
        builder.setMessage("取消听单可能导致您错失订单");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                Message msg = new Message();
                msg.what = 2;
                uiHandle.sendMessage(msg);
                webSocket.close(1000,"123");
            }
        });
        builder.create().show();
    }

    private void loggedToSearch(Context context) {
        if(!hasLogged(context))
            startLoginActivity(context);
    }

    private boolean hasLogged(Context context) {
        LoginRepository loginRepository = LoginRepository.getInstance(new LoginDataSource());
        LoggedInUser user = loginRepository.getUser();
        return user != null;
    }

    private String getLoggedInAccount() {
        LoginRepository loginRepository = LoginRepository.getInstance(new LoginDataSource());
        LoggedInUser user = loginRepository.getUser();
        return user.getDisplayName();
    }

    private void startLoginActivity(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        startActivityForResult(intent, 1);
    }

    private void toast(String msg) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.toast_custom, findViewById(R.id.toast_cus));
        TextView msgView = view.findViewById(R.id.toastView);
        msgView.setText(msg);
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        toast.show();
    }

    private class UiHandle extends Handler {
        FullscreenActivity activity;
        public UiHandle(FullscreenActivity activity) {
            this.activity = activity;
        }
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case  1:
                    mRippleView.startRippleAnimation();
                    stateView.setText("听单中");
                    stateView.setTextColor(getApplicationContext().getResources().getColor(R.color.orange));
                    TextView info1 = findViewById(R.id.info1);
                    info1.setTextColor(getApplicationContext().getResources().getColor(R.color.orange));
                    TextView info2 = findViewById(R.id.info2);
                    info2.setTextColor(getApplicationContext().getResources().getColor(R.color.orange));
                    ColorStateList colorStateList = ContextCompat.getColorStateList(getApplicationContext(), R.color.orange);
                    floatingButton.setSupportImageTintList(colorStateList);
                    break;
                case 2:
                    stateView.setText("空闲");
                    stateView.setTextColor(getApplicationContext().getResources().getColor(R.color.colorAccent));
                    ColorStateList colorStateList2= ContextCompat.getColorStateList(getApplicationContext(), R.color.blue);
                    floatingButton.setSupportImageTintList(colorStateList2);
                    TextView info11 = findViewById(R.id.info1);
                    info11.setTextColor(getApplicationContext().getResources().getColor(R.color.black));
                    TextView info22 = findViewById(R.id.info2);
                    info22.setTextColor(getApplicationContext().getResources().getColor(R.color.black));
                    linking = false;
                    mRippleView.stopRippleAnimation();
                    break;
                case 3:
                    String rn = msg.getData().getString("realname");
                    TextView nameView = findViewById(R.id.nameView);
                    nameView.setText(rn.charAt(0)+"师傅");
                    LoginRepository loginRepository = LoginRepository.getInstance(new LoginDataSource());
                    LoggedInUser user = loginRepository.getUser();
                    user.setActivity(this.activity);
                    remind = "请先选择车辆";
                    break;
                case 4:
                    double rate = msg.getData().getDouble("rate");
                    TextView rateView = findViewById(R.id.rateView);
                    rateView.setText(rate+"");
                    break;
                case 5:
                    ArrayList<String> licenses = msg.getData().getStringArrayList("license");
                    String[] items = new String[licenses.size()];
                    for (int i=0; i< licenses.size(); ++i) {
                        items[i] = licenses.get(i);
                    }
                    set(items);
                    break;
                case 6:
                    String time = msg.getData().getString("time");
                    String[] place = msg.getData().getStringArray("place");
                    double[] pos = msg.getData().getDoubleArray("pos");
                    long oid = msg.getData().getLong("oid");
                    notification("收到预约订单", "点击查看", pos, "ASdasdas", place, time, oid);
                    break;
                case 10:
                    showCancel(account.substring(account.length()-4));
                    break;
                case 13:
                    TextView countView = findViewById(R.id.countView);
                    TextView priceView = findViewById(R.id.priceView);
                    countView.setText(msg.getData().getInt("count")+"单");
                    double price = msg.getData().getDouble("price");
                    DecimalFormat df = new DecimalFormat("#.00");
                    df.format(price);
                    priceView.setText(df.format(price)+"￥");
                    break;
                default:
                    break;
            }
        }
    }

    private void showCancel(String license){
        new AlertDialog.Builder(this)
                .setTitle("已取消")
                .setMessage("尾号:"+ license + "的顾客已取消订单")
                .setPositiveButton("确定", null)
                .show();
    }

    private void set(final String[] items) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("驾驶车辆");
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                licenseView.setText(items[i]);
                license = items[i];
                chosen = true;
            }
        });
        Dialog alertDialog1 = alertBuilder.create();
        alertDialog1.show();
    }

    UiHandle uiHandle = new UiHandle(this);
    int code = 20;
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void notification(String title, String msg, double[] pos , String account, String[] place, String time, long oid) {
        NotificationChannel chan = new NotificationChannel(
                "appointment",
                "AppointmentService",
                NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        Intent intent = new Intent(this, YuYueActivity.class);
        intent.putExtra("pos", pos);
        intent.putExtra("account", account);
        intent.putExtra("splace", place[0]);
        intent.putExtra("place", place);
        intent.putExtra("otime", time);
        intent.putExtra("oid", oid);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, code, intent, 0);
        code++;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, chan.getId())
                .setContentTitle(title)
                .setContentText(msg)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        manager.notify(0, builder.build());
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if(data != null) {
                    int success = data.getIntExtra("result", 0);
                    if(success > 0) {
                        stateView.setText("空闲");
                        stateView.setTextColor(getApplicationContext().getResources().getColor(R.color.colorAccent));
                        licenseView.setText("点击以选择车辆");
                        HttpUtil httpUtil = HttpUtil.getHttpUtil();
                        httpUtil.postAsy1("http://192.168.42.61:8081/user/driverRealName", "account", getLoggedInAccount(), new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                toast("请求真实姓名失败");
                            }
                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                try {
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    if(jsonObject.getInt("code")==200) {
                                        // Display real name
                                        String rn = jsonObject.getString("content");
                                        Message msg = new Message();
                                        msg.what = 3;
                                        Bundle bundle = new Bundle();
                                        bundle.putString("realname", rn);
                                        msg.setData(bundle);
                                        uiHandle.sendMessage(msg);
                                        // Ger driver's rate
                                        HttpUtil rate = HttpUtil.getHttpUtil();
                                        rate.postAsy1("http://192.168.42.61:8081/user/rate", "account", getLoggedInAccount(), new Callback() {
                                            @Override
                                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                toast("请求司机评分失败");
                                            }
                                            @Override
                                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                try {
                                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                                    if(jsonObject.getInt("code")==200) {
                                                        double r = jsonObject.getDouble("content");
                                                        Message msg = new Message();
                                                        msg.what = 4;
                                                        Bundle bundle = new Bundle();
                                                        bundle.putDouble("rate", r);
                                                        msg.setData(bundle);
                                                        uiHandle.sendMessage(msg);
                                                    }
                                                }catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                        getCountPrice();
                    }
                }
                break;
            case OrderActivityCode:
                if(data != null) {
                    int success = data.getIntExtra("result", 0);
                    if(success < 0) {
                        toast("已拒绝订单");
                    }
                }
        }
    }

    String account;

    public void websocketLink() {

        if (latestLatLon==null) {
            toast("尚未获得定位信息");
            return;
        }
        WebsocketUtil websocketUtil = new WebsocketUtil(getLoggedInAccount(), license, latestLatLon.latitude, latestLatLon.longitude);
        webSocket = websocketUtil.setWebSocketListener(new WebSocketListener() {
            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosed(webSocket, code, reason);
                linking = false;
                Message ms = new Message();
                ms.what = 2;
                uiHandle.sendMessage(ms);
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosing(webSocket, code, reason);
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);
                linking = false;
                Message ms = new Message();
                ms.what = 2;
                uiHandle.sendMessage(ms);
                Log.e(TAG, "Web socket link failed.\n" + t.getMessage());
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                super.onMessage(webSocket, text);
                try {
                    JSONObject jsonObject = new JSONObject(text);
                    int code = jsonObject.getInt("code");
                    Log.d(TAG, "Socket Message: " + text);
                    switch (code) {
                        case 1:
                            // 实时位置通知，司机端并不需要貌似
                            Log.d(TAG, "RC:" + text);
                            double lat = jsonObject.getDouble("lat");
                            double lon = jsonObject.getDouble("lon");
                            break;
                        case 3:
                            // 订单派送给司机的接受
                            double startLat = jsonObject.getDouble("slat");
                            double startLon = jsonObject.getDouble("slon");
                            double endLat = jsonObject.getDouble("elat");
                            double endLon = jsonObject.getDouble("elon");
                            long oid = jsonObject.getLong("oid");
                            String splace = jsonObject.getString("splace");
                            String eplace = jsonObject.getString("eplace");
                            account = jsonObject.getString("account");
                            startOrderActivity(oid, account, startLat, startLon, endLat, endLon, splace, eplace);
                            break;
                        case 4:
                            double[] pos = new double[4];
                            pos[0] = jsonObject.getDouble("slat");
                            pos[1] = jsonObject.getDouble("slon");
                            pos[2] = jsonObject.getDouble("elat");
                            pos[3] = jsonObject.getDouble("elon");
                            long ooid = jsonObject.getLong("oid");
                            String time = jsonObject.getString("time");
                            String[] place = new String[2];
                            place[0] = jsonObject.getString("splace");
                            place[1] = jsonObject.getString("eplace");
                            account = jsonObject.getString("account");
                            Message masdas = new Message();
                            masdas.what = 6;
                            Bundle bundle = new Bundle();
                            bundle.putDoubleArray("pos", pos);
                            bundle.putStringArray("place", place);
                            bundle.putString("time", time);
                            bundle.putLong("oid", ooid);
                            masdas.setData(bundle);
                            uiHandle.sendMessage(masdas);
//                            notification("收到预约订单","点击查看", pos, account, place, time);
                            break;
                        case 8:
                            finishActivity(OrderActivityCode);
                            Message msg = new Message();
                            msg.what = 10;
                            uiHandle.sendMessage(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
                super.onMessage(webSocket, bytes);
            }

            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                super.onOpen(webSocket, response);
                linking = true;
                Message ms = new Message();
                ms.what = 1;
                uiHandle.sendMessage(ms);
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void startOrderActivity(long oid, String account, double slat, double slon, double elat, double elon, String splace, String eplace) {
        Intent intent = new Intent(this.getApplicationContext(), OrderActivity.class);
        intent.putExtra("oid", oid);
        intent.putExtra("slat", slat);
        intent.putExtra("slon", slon);
        intent.putExtra("elat", elat);
        intent.putExtra("elon", elon);
        intent.putExtra("splace", splace);
        intent.putExtra("eplace", eplace);
        intent.putExtra("account", account);
        startActivityForResult(intent, OrderActivityCode);
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;
        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {
        if (Build.VERSION.SDK_INT >= 30) {
            mContentView.getWindowInsetsController().show(
                    WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        } else {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
        mVisible = true;
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
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
                                    mLocationOption.setOnceLocation(false);
                                    mLocationClient.setLocationOption(mLocationOption);
                                    mLocationClient.startLocation();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                            } else {
                            }
                        }
                );
        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void sendLocationInfor(double lat, double lon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code",2);
        jsonObject.put("lat", lat);
        jsonObject.put("lon",lon);
        jsonObject.put("account",  getLoggedInAccount());
        jsonObject.put("license", license);
        webSocket.send(jsonObject.toString());
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                double lat = aMapLocation.getLatitude();
                double lon = aMapLocation.getLongitude();
                AMapOptions aOptions = new AMapOptions();
                latestLatLon = new LatLng(lat, lon);
                aOptions.camera(new CameraPosition(latestLatLon, 10f, 0, 0));
                aMapLocation.getCity();
                aMap.animateCamera(CameraUpdateFactory.newLatLng(latestLatLon));
                positionPoint(aMap);
                try {
                    sendLocationInfor(lat, lon);
                } catch (JSONException e) {
                    Log.e(TAG, "向服务器WebSocket发送位置信息失败");
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG,"location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }

    private void positionPoint(AMap aMap) {
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.interval(2000);
        myLocationStyle.showMyLocation(true);
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.setMyLocationEnabled(true);// 设置为true
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
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("嘟嘟司机正在后台定位")
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setChannelId(chan.getId())
                .build();
//        manager.notify(1, builder.build());
//        this.startForeground(1, notification);
        mLocationClient.enableBackgroundLocation(2000, notification);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}