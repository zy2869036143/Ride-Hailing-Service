package com.catiger.taxi;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.catiger.taxi.utils.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private static final String TAG = "HistoryAdapter";
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView carType, orderState, time, begin, end;
        long orderID;
        boolean finished;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            carType = itemView.findViewById(R.id.car_type);
            orderState = itemView.findViewById(R.id.order_state);
            time = itemView.findViewById(R.id.time);
            begin = itemView.findViewById(R.id.begin);
            end = itemView.findViewById(R.id.end);
        }
    }
    private List<HistoryItem> mHistoryList;
    private Activity activity;
    public HistoryAdapter(List<HistoryItem> itemList, Activity activity) {
        mHistoryList = itemList;
        this.activity = activity;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        HistoryAdapter.ViewHolder holder = new HistoryAdapter.ViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.finished) {
                    // 完成单
                    Intent intent = new Intent(activity, HistoryDetailActivity.class);
                    intent.putExtra("oid", holder.orderID);
                    activity.startActivity(intent);
                }else {
                    HttpUtil httpUtil = HttpUtil.getHttpUtil();
                    httpUtil.postAsy1("http://192.168.42.61:8081/order/getapp", "id", holder.orderID + "", new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {

                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            try {
                                JSONObject content =new JSONObject(response.body().string());
                                double oid = content.getDouble("id");
                                double slat = content.getDouble("slat");
                                double slon = content.getDouble("slon");
                                double elat = content.getDouble("elat");
                                double elon = content.getDouble("elon");
                                String splace = content.getString("splace");
                                String apptime = content.getString("apptime");
                                String eplace = content.getString("eplace");
                                String account = content.getString("account");
                                String driver = content.getString("appdriver");
                                Intent intent = new Intent(activity.getApplicationContext(), CallActivity.class);
                                intent.putExtra("oid", oid);
                                intent.putExtra("apptime", apptime);
                                intent.putExtra("slat", slat);
                                intent.putExtra("slon", slon);
                                intent.putExtra("elat", elat);
                                intent.putExtra("elon", elon);
                                intent.putExtra("splace", splace);
                                intent.putExtra("driver", driver);
                                intent.putExtra("eplace", eplace);
                                intent.putExtra("account", account);
                                activity.startActivity(intent);
                            }catch (JSONException jsonException) {

                            }
                        }
                    });
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        HistoryItem item = mHistoryList.get(position);
        holder.time.setText(item.getTime());
        holder.orderState.setText(item.getState());
        holder.orderID = item.getOrderID();
        holder.finished = item.getFinished();
        holder.begin.setText(item.getBegin());
        holder.end.setText(item.getEnd());
    }

    public void updateData(List<HistoryItem> data){
        this.mHistoryList = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mHistoryList.size();
    }

}
