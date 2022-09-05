package com.catiger.taxi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.idisfkj.mypicker.MyPicker;

import java.util.ArrayList;
import java.util.Calendar;

public class YuYueActivity extends AppCompatActivity {
    private static final int searchActivityCode = 10;
    private TextView chosenTime;
    class Order {
        double slat, slon;
        String splace, city;
    }
    Order order = new Order();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yu_yue);
        getSupportActionBar().hide();
        Intent intent = getIntent();
        order.slat = intent.getDoubleExtra("slat",0);
        order.slon = intent.getDoubleExtra("slon",0);
        order.splace = intent.getStringExtra("splace");
        order.city = intent.getStringExtra("city");
        TextView up = findViewById(R.id.shangche);
        up.setText(order.splace);
        ImageView back = findViewById(R.id.back);
        chosenTime = findViewById(R.id.time_c);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        View toSearchLink = findViewById(R.id.search_link);
        toSearchLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSearchActivity();
            }
        });
        View timerLink = findViewById(R.id.time_yuyue);
        timerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPopUpWindow();
            }
        });
    }


    private void startSearchActivity() {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("city",order.city);
        intent.putExtra("time",chosenTime.getText().toString());
        startActivityForResult(intent, searchActivityCode);
    }
    private ArrayList<String>[] getLocalTime() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        ArrayList<String> lists[] = new ArrayList[3];
        ArrayList<String> days = new ArrayList();
        ArrayList<String> hours = new ArrayList();
        ArrayList<String> minutes = new ArrayList();
        for(int i=0; i<3; ++i) {
            days.add(month+"月"+(day+i)+"日");
        }
        for(int i=0;i<24; ++i){
            hours.add(i+"点");
        }
        for (int i=0; i<=50; i+=10) {
            minutes.add(i+"分");
        }
        lists[0] = days;
        lists[1] = hours;
        lists[2] = minutes;
        return lists;
    }
    private int cyear, cmonth, cday, chour, cminute;
    private void initPopUpWindow() {
        MyPicker tp = new MyPicker(this);
        ArrayList<String>[] times = getLocalTime();
        tp.setData(times[0], 1);
        tp.setData(times[1], 2);
        tp.setData(times[2], 3 );
        tp.setPickerTitle("选择时间");
        tp.setPrepare();
        tp.setSelectedFinishListener(new MyPicker.SelectedFinishListener() {
            @Override
            public void onFinish() {
                String leftText = String.valueOf(tp.getText(1));
                String middleText = String.valueOf(tp.getText(2));
                String rightText = String.valueOf(tp.getText(3));
                String text = new String(leftText + middleText + rightText);
                Calendar calendar = Calendar.getInstance();
                cyear = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH)+1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                cmonth = Integer.parseInt(leftText.split("月")[0]);
                cday = Integer.parseInt(leftText.split("月")[1].split("日")[0]);
                chour = Integer.parseInt(middleText.split("点")[0]);
                cminute = Integer.parseInt(rightText.split("分")[0]);
                if(cday==day && chour<=hour && cminute < minute+20 ) {
                    Message msg = new Message();
                    msg.what = 2;
                    uiHandler.sendMessage(msg);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("text", text);
                    Message msg = new Message();
                    msg.what = 1;
                    msg.setData(bundle);
                    uiHandler.sendMessage(msg);
                }
                tp.dismiss();
            }
        });
        tp.showAtLocation(this.findViewById(R.id.search_link), Gravity.CENTER, 0, 0);
    }

    private UiHandler uiHandler = new UiHandler();

    class UiHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    chosenTime.setText(msg.getData().getString("text"));
                    break;
                case 2:
                    toast("选择的时间需至少大于当前时间20分钟");
            }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case searchActivityCode:
                // 当右滑返回上一个活动时，此Activity返回的Intent data为null.
                if (data!=null) {
                    String poiID = data.getStringExtra("PoiID");
                    Intent intent = new Intent(this, CallActivity.class);
                    int[] array = new int[5];
                    array[0] = cyear;
                    array[1] = cmonth;
                    array[2] = cday;
                    array[3] = chour;
                    array[4] = cminute;
                    intent.putExtra("time", array);
                    intent.putExtra("poiID", poiID);
                    intent.putExtra("slat", order.slat);
                    intent.putExtra("slon", order.slon);
                    intent.putExtra("splace", order.splace);
                    startActivity(intent);
                }
                break;
            case 100:
                break;
        }
    }
}