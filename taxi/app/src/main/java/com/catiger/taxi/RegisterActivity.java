package com.catiger.taxi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.catiger.taxi.utils.HttpUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http1.HeadersReader;

public class RegisterActivity extends AppCompatActivity {
    private static String TAG = "RegisterActivity";
    private static String base64URL = "http://192.168.42.61:8081/getCaptchaBase64";
    private static String checkURL= "http://192.168.42.61:8081/validate";
    private static String register= "http://192.168.42.61:8081/user/regpass";
    ImageView veriCode;
    String encode;
    String key;
    TextView phoneView, passwordView, confirmView, nameView;
    RadioButton mBtn, fBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        this.getSupportActionBar().hide();
        ImageView back = findViewById(R.id.register_back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        veriCode = findViewById(R.id.vericode);
        phoneView = findViewById(R.id.phoneView);
        passwordView = findViewById(R.id.passwordView);
        confirmView = findViewById(R.id.confrimView);
        nameView = findViewById(R.id.nameView);
        mBtn = findViewById(R.id.mBTN);
        fBtn = findViewById(R.id.fBTN);
        getBase64();
        veriCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBase64();
            }
        });
        Button button = findViewById(R.id.reigister_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkFull();
            }
        });
    }
    class UiHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    veriCode.setImageBitmap(stringToBitmap(encode));
            }
        }
    }
    UiHandler uiHandler = new UiHandler();

    public void getBase64() {
        HttpUtil httpUtil = HttpUtil.getHttpUtil();
        httpUtil.getAsy(base64URL, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "获取验证码失败");
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    encode = jsonObject.getString("url");
                    key = jsonObject.getString("key");
                    Log.d(TAG, "Current Key:" + key);
                    Message msg = new Message();
                    msg.what = 1;
                    uiHandler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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

    public void checkFull() {
        String s = phoneView.getText().toString();
        if(phoneView.getText().toString().length()!=11) {
            toast("请正确填写手机号码");
            return;
        }
        if(passwordView.getText().length()<6 || !passwordView.getText().toString().equals(confirmView.getText().toString())) {
            toast("请检查密码,长度需大于5");
            return;
        }
        if(nameView.getText().toString().length()==0) {
            toast("请正确填写您的真实姓名");
            return;
        }
        if(fBtn.isChecked()==mBtn.isChecked()) {
            toast("请选择性别");
            return;
        }
        if(!check()) return;
        try {
            class RegisterCallBack implements Callback{
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG,"注册失败");
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        if(jsonObject.getInt("code")==200) {
                            Log.d(TAG,"注册成功");
                            finish();
                        }else if(jsonObject.getInt("code")==400) {
                            toast("此手机号码已注册");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("account", phoneView.getText().toString());
            jsonObject.put("password", passwordView.getText().toString());
            jsonObject.put("gender",mBtn.isChecked()?"M":"F");
            jsonObject.put("realname",nameView.getText().toString());
            HttpUtil httpUtil = HttpUtil.getHttpUtil();
            httpUtil.postAsy(register, jsonObject.toString(), new RegisterCallBack());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean check() {
        TextView code = findViewById(R.id.et_registeractivity_phoneCodes);
        String an = code.getText().toString();
        if(key.equals(an)){
            Log.d(TAG,"验证码输入正确");
            return true;
        }else {
            Log.e(TAG,"验证码输入错误");
            toast("验证码输入错误");
            return false;
        }
    }

    public static Bitmap stringToBitmap(String string) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray = Base64.decode(string.split(",")[1], Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }


}