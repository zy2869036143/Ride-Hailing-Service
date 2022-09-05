package com.catiger.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.catiger.driver.data.LoginRepository;
import com.catiger.driver.data.model.LoggedInUser;
import com.catiger.driver.history.HistoryAdapter;
import com.catiger.driver.history.HistoryItem;
import com.catiger.driver.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HistoryActivity extends AppCompatActivity implements Callback {
    private static String historyURL = "http://192.168.42.61:8081/order/dorder";
    private static String appointmentOrderURL = "http://192.168.42.61:8081/order/dapporder";

    private HistoryAdapter historyAdapter;
    List<HistoryItem> historyItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        historyAdapter = initHistoryView();
        this.getSupportActionBar().hide();
        historyItems = new ArrayList<>();
        getAppointmentOrder();
        getHistory();
    }
    private HistoryAdapter initHistoryView() {
        RecyclerView recyclerView = findViewById(R.id.history);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        List<HistoryItem> mCurrentHistoryList = new ArrayList<>();
        HistoryAdapter historyAdapter = new HistoryAdapter(mCurrentHistoryList, this);
        recyclerView.setAdapter(historyAdapter);
        return historyAdapter;
    }

    public LoggedInUser getLoggedUser() {
        LoginRepository loginRepository = LoginRepository.getInstance(null);
        LoggedInUser user = loginRepository.getUser();
        return user;
    }

    private void getHistory() {
        HttpUtil httpUtil = HttpUtil.getHttpUtil();
        httpUtil.postAsy1(historyURL, "account", getLoggedUser().getDisplayName(),this);
    }

    private void getAppointmentOrder() {
        HttpUtil httpUtil = HttpUtil.getHttpUtil();
        httpUtil.postAsy1(appointmentOrderURL, "account", getLoggedUser().getDisplayName(),this);
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {

    }


    private class Update extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            historyAdapter.updateData(historyItems);
        }
    };
    private Update up = new Update();

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        String result = response.body().string();
        try {
            JSONObject jsonObject = new JSONObject(result);
            String msgr = jsonObject.getString("msg");
            boolean finished = true;
            if(msgr.equals("0001")) {
                finished = false;
            }
            String content = jsonObject.getString("content");
            content = content.replace("\\", "");
            JSONArray jsonArray = new JSONArray(content);
            for (int i=0; i< jsonArray.length(); ++i) {
                JSONObject objJson = jsonArray.getJSONObject(i);
                JSONObject fullTimeJson = objJson.getJSONObject("time");
                JSONObject dateJson = fullTimeJson.getJSONObject("date");
                JSONObject timeJson = fullTimeJson.getJSONObject("time");
                HistoryItem item = new HistoryItem(objJson.getInt("id"));
                item.setFinished(finished);
                item.setState(finished?"已完成":"未完成");
                item.setBegin(objJson.getString("splace"));
                item.setEnd(objJson.getString("eplace"));
                int year = dateJson.getInt("year");
                int month = dateJson.getInt("month");
                int day = dateJson.getInt("day");
                int hour = timeJson.getInt("hour");
                int minute = timeJson.getInt("minute");
                item.setTime(year+"年"+ month + "月" + day +"日" + " " + hour+":"+minute);
                historyItems.add(item);
            }
            Message msg = new Message();
            msg.what = 1;
            up.sendMessage(msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}