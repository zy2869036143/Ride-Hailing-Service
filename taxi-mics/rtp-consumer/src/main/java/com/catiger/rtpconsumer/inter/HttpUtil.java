package com.catiger.rtpconsumer.inter;

import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.DecimalFormat;
@Component
public class HttpUtil {

    private static String url = "https://restapi.amap.com/v3/direction/driving?origin=116.45925,39.910031&destination=116.587922,40.081577&output=json&key=your_own_gaode_apikey";
    private static String pre = "https://restapi.amap.com/v3/direction/driving?origin=";
    private static String mid = "&destination=";
    private static String end = "&output=json&key=your_own_gaode_apikey";
    public double[] getDistanceAnd(double olon, double olat, double dlon, double dlat) {
        String result = null;
        String url = pre + olon + "," + olat + mid + dlon + "," + dlat + end;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            result = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return analyseJson(result);
    }
    /*
    * return [cost, km, minutes]
    */
    public double[] analyseJson(String json) {
        JSONObject jsonObject = new JSONObject(json);
        JSONObject route = jsonObject.getJSONObject("route");
        int taxiCost = route.getInt("taxi_cost");
        JSONArray pathArray = route.getJSONArray("paths");
        JSONObject path1 = pathArray.getJSONObject(0);
        int m = path1.getInt("distance");
        int s = path1.getInt("duration");
        return new double[]{taxiCost, format(m*1.0/1000), format(s*1.0/60)};
    }
    private static double format(double num) {
        DecimalFormat df = new DecimalFormat("#.00");
        return Double.parseDouble(df.format(num));
    }

}
