package com.example.fitnessgachaapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TrackingRecordAdapter extends RecyclerView.Adapter<TrackingRecordAdapter.ViewHolder> {

    private List<TrackingRecord> mTrackingRecords;
    private LayoutInflater mInflater;

    // data is passed into the constructor
    TrackingRecordAdapter(Context context, List<TrackingRecord> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mTrackingRecords = data;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.tracking_record_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextViews in each row
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TrackingRecord trackingRecord = mTrackingRecords.get(position);
        holder.tvDate.setText(trackingRecord.getDate());
        holder.tvDistance.setText(String.format(Locale.getDefault(), "%.2f m", trackingRecord.getDistance()));
        holder.tvCalories.setText(String.format(Locale.getDefault(), "%.2f kcal", trackingRecord.getCalories()));
        holder.tvDuration.setText(String.format(Locale.getDefault(), "%d min", trackingRecord.getDuration()));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mTrackingRecords.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDistance, tvCalories, tvDuration;

        ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.Date);
            tvDistance = itemView.findViewById(R.id.Distance);
            tvCalories = itemView.findViewById(R.id.Calories);
            tvDuration = itemView.findViewById(R.id.Duration);
        }
    }
}
