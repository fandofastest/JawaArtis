package com.satux.duax.tigax.zlyric;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.satux.duax.tigax.PlayActivity;
import com.satux.duax.tigax.R;

import java.util.List;

public class AdapterLyrics extends RecyclerView.Adapter<AdapterLyrics.ViewHolder> {

    private PlayActivity activity;
    private List<ModelLyrics> result;
    private int mSelectedPosition;
    public AdapterLyrics(PlayActivity activity, List<ModelLyrics> result) {
        this.activity = activity;
        this.result = result;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lrc_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setTag(result.get(position));
        ModelLyrics pu = result.get(position);
        holder.time.setText(pu.getTime());
        if (activity.getDurationTimer().equals(pu.getTime())) {
            mSelectedPosition = position;
            holder.text.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fade_in));
        }

        if (mSelectedPosition == position) {
            holder.text.setTextColor(activity.getColor(R.color.yellow));
            activity.setPoint(position);
        } else {
            holder.text.setTextColor(activity.getColor(R.color.white));
        }
        holder.text.setText(pu.getText());
    }

    @Override
    public int getItemCount() {
        return result.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        TextView time;

        public ViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text1);
            time = itemView.findViewById(R.id.text2);
        }
    }
}
