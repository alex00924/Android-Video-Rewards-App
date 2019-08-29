package com.droidoxy.easymoneyrewards.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.droidoxy.easymoneyrewards.R;
import com.droidoxy.easymoneyrewards.activities.FragmentsActivity;
import com.droidoxy.easymoneyrewards.app.App;
import com.droidoxy.easymoneyrewards.model.Videos;

import java.util.List;

/**
 * Created by DroidOXY
 */

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.ViewHolder>{

    private Context context;
    private List<Videos> listItem;

    public VideosAdapter(Context context, List<Videos> listItem) {
        this.context = context;
        this.listItem = listItem;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_list,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        String strPremium = App.getInstance().get("user_premium", "0");
        boolean bPremium = strPremium.equals("1") ? true : false;

        String PrevItem;
        final Videos video = listItem.get(position);

        final String videoId = video.getVideoId();
        final String title = video.getTitle();
        final String subtitle = video.getSubtitle();
        final String videoURL = video.getVideoURL();
        final String videoPoints = bPremium ? video.getAmountPremium() : video.getAmount();
        final String timeDuration = video.getDuration();
        final String image = video.getImage();
        final String status = video.getStatus();

        holder.title.setText(title);
        if(App.getInstance().get("APPVIDEO_"+videoId,false)){

            holder.SingleItem.setVisibility(View.GONE);

        }else{

            holder.subtitle.setText(subtitle);
        }
        holder.duration.setText("Duration : " + timeDuration);
        holder.amount.setText("+ " + video.getAmount());
        holder.amountPremium.setText(" " + video.getAmountPremium());
        Glide.with(context).load(image)
                .apply(new RequestOptions().override(120,120))
                .apply(RequestOptions.placeholderOf(R.drawable.ic_place_holder))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .into(holder.image);


        holder.SingleItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((FragmentsActivity)context).playVideo(videoId, videoPoints, videoURL, title, image);
                holder.SingleItem.setVisibility(View.GONE);

            }
        });

    }

    @Override
    public int getItemCount() {
        return listItem.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView date,title,subtitle,amount,duration, amountPremium;
        ImageView image;
        LinearLayout SingleItem;
        ViewHolder(View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.date);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            duration = itemView.findViewById(R.id.duration);
            amount = itemView.findViewById(R.id.amount);
            amountPremium = itemView.findViewById(R.id.amount_premium);
            image = itemView.findViewById(R.id.image);
            SingleItem = itemView.findViewById(R.id.SingleItem);
        }
    }
}
