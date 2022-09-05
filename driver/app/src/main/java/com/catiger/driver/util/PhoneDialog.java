package com.catiger.driver.util;

import  android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.catiger.driver.R;

public class PhoneDialog extends Dialog {

    private Activity context;
    private VerificationCodeInput vfCode;
    private VerificationCodeInput.Listener listener;
    public PhoneDialog(@NonNull Activity  context) {
        super(context);
        this.context = context;
    }
    public void setListener(VerificationCodeInput.Listener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.phone_dialog);
        vfCode = findViewById(R.id.verificationCodeInput);
        Window dialogWindow = this.getWindow();
        WindowManager m = context.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        // p.height = (int) (d.getHeight() * 0.6); // 高度设置为屏幕的0.6
        p.width = (int) (d.getWidth() * 0.8); // 宽度设置为屏幕的0.8
        dialogWindow.setAttributes(p);
        vfCode.setOnCompleteListener(this.listener);
        setCancelable(false);
    }

    private void toast(String msg) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.toast_custom, findViewById(R.id.toast_cus));
        TextView msgView = view.findViewById(R.id.toastView);
        msgView.setText(msg);
        this.dismiss();
        Toast toast = new Toast(context.getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        toast.show();
    }
}
