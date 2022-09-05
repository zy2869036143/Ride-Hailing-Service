package com.catiger.taxi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.autonavi.amap.mapcore.interfaces.IMapConfig;
import com.catiger.taxi.utils.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RigisterDriverActivity extends AppCompatActivity {
    private static int[] code = {1, 2, 3, 4};
    private static String base64URL = "http://192.168.42.61:8081/getCaptchaBase64";
    String encode, key;
    ImageView shenfenzheng0, shenfenzheng1, jiashizheng, xingshizheng, veriCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rigister_driver);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.back_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        shenfenzheng0 = findViewById(R.id.shenfen0);
        shenfenzheng0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chosePic(code[0]);
            }
        });
        shenfenzheng1 = findViewById(R.id.shenfen1);
        shenfenzheng1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chosePic(code[1]);
            }
        });
        jiashizheng = findViewById(R.id.jiashizheng);
        jiashizheng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chosePic(code[2]);
            }
        });
        xingshizheng = findViewById(R.id.xingshizheng);
        xingshizheng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chosePic(code[3]);
            }
        });
        veriCode = findViewById(R.id.vericode);
        getBase64();
        veriCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBase64();
            }
        });

    }

    private void chosePic(int code) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == code[0]) {
            try {
                Uri uri = data.getData();
                Bitmap bitmapStream = null;
                // 使用流获取图片
                bitmapStream = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                shenfenzheng0.setImageBitmap(bitmapStream);
                View view = findViewById(R.id.text000);
                view.setVisibility(View.GONE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }catch (Exception e){}
        }else if(requestCode == code[1]) {
            try {
                Uri uri = data.getData();
                Bitmap bitmapStream = null;
                // 使用流获取图片
                bitmapStream = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                shenfenzheng1.setImageBitmap(bitmapStream);
                View view = findViewById(R.id.text111);
                view.setVisibility(View.GONE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }catch (Exception e){}
        }else if(requestCode == code[2]) {
            try {
                Uri uri = data.getData();
                Bitmap bitmapStream = null;
                // 使用流获取图片
                bitmapStream = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                jiashizheng.setImageBitmap(bitmapStream);
                View view = findViewById(R.id.text222);
                view.setVisibility(View.GONE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }catch (Exception e){}
        }else if(requestCode == code[3]) {
            try {
                Uri uri = data.getData();
                Bitmap bitmapStream = null;
                // 使用流获取图片
                bitmapStream = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                xingshizheng.setImageBitmap(bitmapStream);
                View view = findViewById(R.id.text333);
                view.setVisibility(View.GONE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }catch (Exception e){}
        }
    }

    public void getBase64() {
        HttpUtil httpUtil = HttpUtil.getHttpUtil();
        httpUtil.getAsy(base64URL, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    encode = jsonObject.getString("url");
                    key = jsonObject.getString("key");
                    Message msg = new Message();
                    msg.what = 1;
                    uiHandler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

    UiHandler uiHandler = new UiHandler();

    public boolean check() {
        TextView code = findViewById(R.id.et_registeractivity_phoneCodes);
        String an = code.getText().toString();
        if(key.equals(an)){
            return true;
        }else {
            toast("验证码输入错误");
            return false;
        }
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
}