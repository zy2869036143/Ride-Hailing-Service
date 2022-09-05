package com.catiger.taxi;

import android.app.Activity;
import android.content.Intent;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PlaceAdapter extends  RecyclerView.Adapter<PlaceAdapter.ViewHolder>{
    private static final String TAG = "PlaceAdapter";
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView placeName, placePosition;
        String poiID;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            placeName = itemView.findViewById(R.id.place_name);
            placePosition = itemView.findViewById(R.id.place_position);
        }
    }

    private List<Place> mPlaceList;
    private Activity fatherActivity;
    public PlaceAdapter(List<Place> placeList, Activity activity) {
        mPlaceList = placeList;
        fatherActivity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Click on" + holder.placeName.getText() + " POI id:" + holder.poiID);
                Intent intent = new Intent();
                intent.putExtra("PoiID", holder.poiID);
                fatherActivity.setResult(Activity.RESULT_OK, intent);
//                fatherActivity.startActivity(intent);
                fatherActivity.finish();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Place place = mPlaceList.get(position);
        holder.placeName.setText(place.getPlaceName());
        holder.placePosition.setText(place.getPlacePosition());
        holder.poiID = place.getPoiID();
    }

    @Override
    public int getItemCount() {
        return mPlaceList.size();
    }

    public void updateData(List<Place> data){
        this.mPlaceList = data;
        notifyDataSetChanged();
    }

//    private void onItemClicked(){
//        Intent intent = new Intent();
//        intent.putExtra("PoiID", holder.poiID);
//        fatherActivity.setResult(Activity.RESULT_OK, intent);
//        fatherActivity.finish();
//        Log.d(TAG, "Click on" + holder.placeName.getText() + " POI id:" + holder.poiID);
//    }


}
