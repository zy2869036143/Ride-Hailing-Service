package com.catiger.taxi.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.catiger.taxi.HistoryAdapter;
import com.catiger.taxi.HistoryDetailActivity;
import com.catiger.taxi.HistoryItem;
import com.catiger.taxi.PlaceAdapter;
import com.catiger.taxi.R;
import com.catiger.taxi.data.LoginDataSource;
import com.catiger.taxi.data.LoginRepository;
import com.catiger.taxi.data.model.LoggedInUser;
import com.catiger.taxi.databinding.FragmentDashboardBinding;
import com.catiger.taxi.ui.login.LoginActivity;
import com.catiger.taxi.utils.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DashboardFragment extends Fragment implements Callback {
    private static String TAG = "DashboardFragment History View";
    private FragmentDashboardBinding binding;
    private RecyclerView recyclerView;
    int loginActivityCode = 10;
    private static String historyURL = "http://192.168.42.61:8081/order/check";
    private static final String appOrderURl = "http://192.168.42.61:8081/order/checkapp";
    private List<HistoryItem> mCurrentHistoryList;
    private HistoryAdapter historyAdapter;
    List<HistoryItem> historyItems;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        historyAdapter = initHistoryView();
        historyItems = new ArrayList<>();
        loggedToSearch(getContext());
        return root;
    }
    private void getHistory(String account) {
        HttpUtil httpUtil = HttpUtil.getHttpUtil();
        httpUtil.postAsy1(historyURL, "account", account,this);
    }
    public void getOrderAppDetail(String account) {
        HttpUtil httpUtil = HttpUtil.getHttpUtil();
        httpUtil.postAsy1(appOrderURl, "account", account, this);
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        Log.e(TAG, "请求订单历史失败");

    }
    private class Update extends Handler{
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
            String content = jsonObject.getString("content");
            String msgr = jsonObject.getString("msg");
            boolean finished = true;
            if(msgr.equals("0001")) {
                finished = false;
            }
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

    private boolean hasLogged(Context context) {
        LoginRepository loginRepository = LoginRepository.getInstance(new LoginDataSource());
        LoggedInUser user = loginRepository.getUser();
        return user != null;
    }

    private void startLoginActivity(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        startActivityForResult(intent, loginActivityCode);
    }

    public LoggedInUser getLoggedUser() {
        LoginRepository loginRepository = LoginRepository.getInstance(null);
        LoggedInUser user = loginRepository.getUser();
        return user;
    }

    private void loggedToSearch(Context context) {
        if(!hasLogged(context))
            startLoginActivity(context);
        else {
            LoggedInUser user = getLoggedUser();
            getHistory(user.getDisplayName());
            getOrderAppDetail(user.getDisplayName());
        }
    }

    private HistoryAdapter initHistoryView() {
        recyclerView = binding.history;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(layoutManager);
        mCurrentHistoryList = new ArrayList<>();
        HistoryAdapter historyAdapter = new HistoryAdapter(mCurrentHistoryList, this.getActivity());
        recyclerView.setAdapter(historyAdapter);
        return historyAdapter;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}