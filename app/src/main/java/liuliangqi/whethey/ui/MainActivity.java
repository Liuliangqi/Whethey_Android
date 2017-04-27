package liuliangqi.whethey.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import liuliangqi.whethey.R;
import liuliangqi.whethey.weather.CurrentWhether;
import liuliangqi.whethey.weather.Day;
import liuliangqi.whethey.weather.Forecast;
import liuliangqi.whethey.weather.Hour;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements LocationListener{
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String DAILY_FORECAST = "DAILY_FORECAST";
    private Forecast mForecast;

    @BindView(R.id.timeLabel)
    TextView mTimeLabel;
    @BindView(R.id.temperatureLabel)
    TextView mTemperatureLabel;

    @BindView(R.id.humidityValue)
    TextView mHumidityValue;

    @BindView(R.id.precipValue)
    TextView mPrecipValue;

    @BindView(R.id.summaryLabel)
    TextView mSummaryLabel;

    @BindView(R.id.iconLabel)
    ImageView mIconImageView;

    @BindView(R.id.refreshImageView)
    ImageView mRefreshImageView;

    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    @BindView(R.id.locationLabel) TextView mLocationLabel;

    @BindView(R.id.relativeLayout)
    RelativeLayout mRelativeLayout;

    private Location mLocation;
    private double mLatitude;
    private double mLongitude;
    private LocationManager mLocationManager;
    private Criteria mCriteria;
    private String mBestProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        getLocation();

        //每次点击刷新都重新加载界面
        mProgressBar.setVisibility(View.INVISIBLE);
        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getForecast(mLatitude, mLongitude);
            }
        });

        getForecast(mLatitude, mLongitude);


    }

    //获取当前位置
    private void getLocation() {
        if (isLocationEnabled(MainActivity.this)) {
            if (isLocationEnabled(MainActivity.this)) {
                mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                mCriteria = new Criteria();
                //设置标准
                mCriteria = new Criteria();
                mCriteria.setAccuracy(Criteria.ACCURACY_FINE);//精度要求
                mCriteria.setAltitudeRequired(false);//不需要海拔要求
                mCriteria.setBearingRequired(false);//不要求provider提供方位信息
                mCriteria.setCostAllowed(true);//允许付费
                mCriteria.setPowerRequirement(Criteria.POWER_LOW);//对电量的要求
                mBestProvider = mLocationManager.getBestProvider(mCriteria, true);

                //You can still do this if you like, you might get lucky:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                mLocation = mLocationManager.getLastKnownLocation(mBestProvider);
                if (mLocation != null) {
                    mLatitude = mLocation.getLatitude();
                    mLongitude = mLocation.getLongitude();
                }
                else{
                    //This is what you need:
                    mLocationManager.requestLocationUpdates(mBestProvider, 1000, 0, this);
                }
            }
            else
            {
                //prompt user to enable location....
                //.................
                Toast.makeText(this, "please open location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //判断位置是否开启
    private boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            try
            {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        }
        else
        {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    //根据经纬度获取请求链接
    private String getUrl(double latitude, double longitude){
        String apiKey = "f2aaf05b44b8fafe19fb8dc04552c4ba";
        String forecastUrl = "https://api.darksky.net/forecast/"
                + apiKey +"/" + latitude+ "," + longitude;
        return forecastUrl;
    }
    //获取当前预报
    private void getForecast(double latitude, double longitude) {
        if(isNetWorkAvailable()){
            toggleRefresh();
            OkHttpClient client = new OkHttpClient();
            //request
            Request request = new Request.Builder()
                    .url(getUrl(latitude,longitude))
                    .build();

            //call
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    //response
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        //注释部分是Synchronized
                        //Response response = call.execute();
                        if(response.isSuccessful()){
                            mForecast = getForecastDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        }else{
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }catch (JSONException e){
                        Log.e(TAG, "Exception caught: ", e);
                    }

                }
            });
        }else {
            Toast.makeText(this, "Network is unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    //点击刷新
    private void toggleRefresh() {
        if(mProgressBar.getVisibility() == View.INVISIBLE){
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        }else{
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
        if(mLocation == null){
            Toast.makeText(this, "Network doesn't work", Toast.LENGTH_SHORT).show();
            mLatitude = 0.0;
            mLongitude = 0.0;
            return;
        }
        mLatitude = mLocation.getLatitude();
        mLongitude = mLocation.getLongitude();
    }

    private void updateDisplay() {
        CurrentWhether currentWhether = mForecast.getCurrentWhether();
        mTemperatureLabel.setText(currentWhether.getTemperature() + "");
        mTimeLabel.setText("At " + currentWhether.getFormattedTime() + " it will be");
        mHumidityValue.setText(currentWhether.getHumidity() + "");
        mPrecipValue.setText(currentWhether.getPrecipChance() + "%");
        mSummaryLabel.setText(currentWhether.getSummary());
        mLocationLabel.setText(updateWithNewLocation(mLatitude, mLongitude));
        Drawable drawable = getResources().getDrawable(currentWhether.getIconId());
        mIconImageView.setImageDrawable(drawable);
        setBackground();
    }

    private void setBackground(){
        CurrentWhether current = mForecast.getCurrentWhether();
        if(current.getTemperature() > 20 && current.getTemperature() < 40){
            mRelativeLayout.setBackgroundColor(Color.parseColor("#FFC0CB"));
        }else if(current.getTemperature() >= 40 && current.getTemperature() < 70){
            mRelativeLayout.setBackgroundColor(Color.parseColor("#40E0D0"));
        }else if(current.getTemperature() >= 70){
            mRelativeLayout.setBackgroundColor(Color.parseColor("#FC970B"));
        }else{
            mRelativeLayout.setBackgroundColor(Color.parseColor("#3F4651"));
        }
    }

    private Forecast getForecastDetails(String jsonData) throws JSONException{
        Forecast forecast = new Forecast();

        forecast.setCurrentWhether(getCurrentDetails(jsonData));
        forecast.setHourlyForecast(getHourlyForecast(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));

        return forecast;
    }

    private Day[] getDailyForecast(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");

        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");
        Day[] days = new Day[data.length()];
        for(int i = 0; i < data.length(); i++){
            JSONObject jsonDay = data.getJSONObject(i);
            Day day = new Day();

            day.setSummary(jsonDay.getString("summary"));
            day.setIcon(jsonDay.getString("icon"));
            day.setTime(jsonDay.getLong("time"));
            day.setTemperatureMax(jsonDay.getDouble("temperatureMax"));
            day.setTimezone(timezone);

            days[i] = day;
        }

        return days;
    }

    private Hour[] getHourlyForecast(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timeZone = forecast.getString("timezone");

        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");
        Hour[] hours = new Hour[data.length()];
        for(int i = 0; i < data.length(); i++){
            JSONObject jsonHour = data.getJSONObject(i);
            Hour hour = new Hour();

            hour.setSummary(jsonHour.getString("summary"));
            hour.setTemperature(jsonHour.getDouble("temperature"));
            hour.setIcon(jsonHour.getString("icon"));
            hour.setTime(jsonHour.getLong("time"));
            hour.setTimezone(timeZone);

            hours[i] = hour;
        }
        return hours;
    }

    private CurrentWhether getCurrentDetails(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timeZone = forecast.getString("timezone");
        Log.v(TAG, timeZone);

        JSONObject currently = forecast.getJSONObject("currently");

        CurrentWhether currentWeather = new CurrentWhether();
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setTimezone(timeZone);


        Log.v(TAG, currentWeather.getFormattedTime());
        return currentWeather;
    }

    private boolean isNetWorkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "ERROR");
    }

    public String updateWithNewLocation(double latitude, double longitude){
        String cityName = "";
        Geocoder geocoder = new Geocoder(this);
        List<Address> addList = null;
        try {
            addList = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(addList != null && addList.size() > 0){
            for(int i = 0; i < addList.size(); i++){
                Address add = addList.get(i);
                cityName += add.getLocality();
            }
        }
            return cityName;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocationManager.removeUpdates(this);
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(this);
    }


    @OnClick (R.id.daily)
    public void startDailyActivity(View view){
        Intent intent = new Intent(this, DailyForecastActivity.class);
        intent.putExtra(DAILY_FORECAST, mForecast.getDailyForecast());
        startActivity(intent);
    }
}
