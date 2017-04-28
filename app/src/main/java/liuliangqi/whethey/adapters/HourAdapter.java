package liuliangqi.whethey.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import liuliangqi.whethey.R;
import liuliangqi.whethey.weather.Hour;

/**
 * Created by liuliangqi on 2017/4/27.
 */

public class HourAdapter extends RecyclerView.Adapter<HourAdapter.HourViewHolder> {


    private Hour[] mHours;


    public HourAdapter(Hour[] hours){
        mHours = hours;
    }
    @Override
    public HourViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hourly_list_item, parent, false);
        HourViewHolder viewHolder = new HourViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(HourViewHolder holder, int position) {
        holder.bindHour(mHours[position]);

    }

    @Override
    public int getItemCount() {
        return mHours.length;
    }

    public class HourViewHolder extends RecyclerView.ViewHolder{
        public TextView mTimeLabel;
        public TextView mSummaryLabel;
        public TextView mTemperatureLabel;
        public ImageView mIconImageView;


        public HourViewHolder(View itemView) {
            super(itemView);

            mTimeLabel = (TextView) itemView.findViewById(R.id.hourly_timeLabel);
            mSummaryLabel = (TextView) itemView.findViewById(R.id.hourly_summaryLabel);
            mTemperatureLabel = (TextView) itemView.findViewById(R.id.hourly_temperatureLabel);
            mIconImageView = (ImageView) itemView.findViewById(R.id.hourly_icon_imageView);
        }

        public void bindHour(Hour hour){
            mTimeLabel.setText(hour.getHour());
            mSummaryLabel.setText(hour.getSummary());
            mTemperatureLabel.setText(hour.getTemperature() + "");
            mIconImageView.setImageResource(hour.getIconId());
        }
    }
}
