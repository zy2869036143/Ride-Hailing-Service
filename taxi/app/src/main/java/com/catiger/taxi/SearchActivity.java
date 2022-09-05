package com.catiger.taxi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements Inputtips.InputtipsListener, TextWatcher{
    private static final String TAG = "SearchActivity";
    private RecyclerView searchResult;
    private TextView textView;
    private PlaceAdapter placeAdapter;
    private ProgressBar loadingBar;
    private TextView tvMsg;
    private AutoCompleteTextView cityTextView;
    private List<Place> mCurrentPlaceList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();
        setContentView(R.layout.activity_search);
        cityTextView = findViewById(R.id.city_in_search);
        String city = getIntent().getStringExtra("city");
        cityTextView.setText(city);
        initTextView();
        initRecycleView();
        loadingBar = (ProgressBar) findViewById(R.id.search_loading);
        tvMsg = (TextView) findViewById(R.id.tv_msg);
        initButtons();
    }

    private void queryPlaces(String keyword) throws AMapException {
        // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        PoiSearch.Query query = new PoiSearch.Query(keyword, "", "");
        // 设置每页最多返回多少条poiItem
        query.setPageSize(20);
        // 设置查第一页
        query.setPageNum(0);
        PoiSearch poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int errcode) {
                if (errcode == 1000) {
                    List<Place> placeList = new ArrayList<>();
                    ArrayList<PoiItem> pois = poiResult.getPois();
                    for (int i = 0; i < pois.size(); i++) {
                        String title = pois.get(i).getTitle();
                        String snippet = pois.get(i).getSnippet();
                        placeList.add(new Place(title, snippet));
                        Log.d(TAG, "Title:" + title + " Snippet:" + snippet);
                    }
                    placeAdapter.updateData(placeList);
                }else {
                    Log.e(TAG, poiResult.getPois().toString() + "" + errcode);
                }
            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int i) {
            }
        });
        poiSearch.searchPOIAsyn();

    }

    private void initTextView() {
        textView = findViewById(R.id.search_input);
        textView.addTextChangedListener(this);
        // 主动获取焦点
        textView.setFocusable(true);
        textView.setFocusableInTouchMode(true);
        textView.requestFocus();
    }
    @Override
    public void onGetInputtips(List<Tip> tipList, int rCode) {
        setLoadingVisible(false);
        try {
            if (rCode == 1000) {
                mCurrentPlaceList = new ArrayList<>();
                for (Tip tip : tipList) {
                    if (null == tip.getPoint()) {
                        continue;
                    }
                    Place place = new Place(tip.getName(), tip.getDistrict() + tip.getAddress(), tip.getPoint().getLatitude(), tip.getPoint().getLongitude());
                    place.setPoiID(tip.getPoiID());
                    mCurrentPlaceList.add(place);
                }

                if (null == mCurrentPlaceList || mCurrentPlaceList.isEmpty()) {
                    tvMsg.setText("抱歉，没有搜索到结果，请换个关键词试试");
                    tvMsg.setVisibility(View.VISIBLE);
                    searchResult.setVisibility(View.GONE);
                } else {
                    searchResult.setVisibility(View.VISIBLE);
                    placeAdapter.updateData(mCurrentPlaceList);
                }
            } else {
                tvMsg.setText("出错了，请稍后重试");
                tvMsg.setVisibility(View.VISIBLE);
            }
        } catch (Throwable e) {
            tvMsg.setText("出错了，请稍后重试");
            tvMsg.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        try {
            {
                if (tvMsg.getVisibility() == View.VISIBLE) {
                    tvMsg.setVisibility(View.GONE);
                }
                String newText = charSequence.toString().trim();
                if (!TextUtils.isEmpty(newText)) {
                    setLoadingVisible(true);
                    InputtipsQuery inputquery = new InputtipsQuery(newText, cityTextView.getText().toString());
                    Inputtips inputTips = new Inputtips(getApplicationContext(), inputquery);
                    inputTips.setInputtipsListener(this);
                    inputTips.requestInputtipsAsyn();
                } else {
                    searchResult.setVisibility(View.GONE);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void initButtons() {
        ImageView button = findViewById(R.id.dialog_search_back);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        });
    }

    private void initRecycleView() {
        searchResult = findViewById(R.id.recycle);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        searchResult.setLayoutManager(layoutManager);
        mCurrentPlaceList = new ArrayList<>();
        placeAdapter = new PlaceAdapter(mCurrentPlaceList, this);
        searchResult.setAdapter(placeAdapter);
    }

    private void setLoadingVisible(boolean isVisible) {
        if (isVisible) {
            loadingBar.setVisibility(View.VISIBLE);
        } else {
            loadingBar.setVisibility(View.GONE);
        }
    }

    private void back() {
        this.finish();
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }
}