package com.catiger.taxi.view;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.catiger.taxi.R;

public class FukuanDialog extends Dialog {
    private Activity context;
    private VerificationCodeInput vfCode;
    private TextView priceView;
    private double price;
    private VerificationCodeInput.Listener listener;
    public FukuanDialog(@NonNull Activity context, double price) {
        super(context);
        this.context = context;
        this.price = price;
    }
    public void setListener(VerificationCodeInput.Listener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.fukuan_view);
        vfCode = findViewById(R.id.verificationCodeInput);
        priceView = findViewById(R.id.price);
        priceView.setText(price+"元");
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
}
