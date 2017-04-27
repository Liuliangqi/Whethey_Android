package liuliangqi.whethey.weather;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by liuliangqi on 2017/4/25.
 */

public class Day implements Parcelable{
    private long mTime;
    private String mSummary;
    private double mTemperatureMax;
    private String mIcon;
    private String mTimezone;


    public int getIconId(){
        return Forecast.getIconId(mIcon);
    }

    public String getDayOfTheWeek(){
        SimpleDateFormat formater = new SimpleDateFormat("EEEE");
        formater.setTimeZone(TimeZone.getTimeZone(mTimezone));

        Date dateTime = new Date(mTime * 1000);

        return formater.format(dateTime);
    }

    public Day(){}
    
    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public int getTemperatureMax() {
        return (int) Math.round(mTemperatureMax);
    }

    public void setTemperatureMax(double temperatureMax) {
        mTemperatureMax = temperatureMax;
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    public String getTimezone() {
        return mTimezone;
    }

    public void setTimezone(String timezone) {
        mTimezone = timezone;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(mTime);
        parcel.writeString(mSummary);
        parcel.writeDouble(mTemperatureMax);
        parcel.writeString(mIcon);
        parcel.writeString(mTimezone);
    }

    private Day(Parcel in){
        mTime = in.readLong();
        mSummary = in.readString();
        mTemperatureMax = in.readDouble();
        mIcon = in.readString();
        mTimezone = in.readString();
    }


    public static final Parcelable.Creator<Day> CREATOR = new Creator<Day>() {
        @Override
        public Day createFromParcel(Parcel parcel) {
            return new Day(parcel);
        }

        @Override
        public Day[] newArray(int i) {
            return new Day[i];
        }
    };
}
