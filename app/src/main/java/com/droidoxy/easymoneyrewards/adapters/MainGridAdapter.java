package com.droidoxy.easymoneyrewards.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.droidoxy.easymoneyrewards.R;
import com.droidoxy.easymoneyrewards.activities.MainActivity;
import com.droidoxy.easymoneyrewards.model.OfferWalls;

import java.util.ArrayList;

public class MainGridAdapter  extends ArrayAdapter<OfferWalls> {

    private ArrayList<OfferWalls> dataSet;
    Context mContext;
    private int lastPosition = -1;

    public MainGridAdapter(ArrayList<OfferWalls> data, Context context) {
        super(context, R.layout.item_main_grid, data);
        this.dataSet = data;
        this.mContext=context;
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return dataSet.size();
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final OfferWalls offerWalls = getItem(position);

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.item_main_grid, null);
        }

//        Glide.with(mContext).load(offerWalls.getImage())
//                .apply(new RequestOptions().override(100,100))
//                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
//                .apply(RequestOptions.skipMemoryCacheOf(true))
//                .into((ImageView)v.findViewById(R.id.image));
        Glide.with(mContext).load(offerWalls.getImage()).into(((ImageView) convertView.findViewById(R.id.image)));
        ((TextView) convertView.findViewById(R.id.title)).setText(offerWalls.getTitle());
        ((TextView) convertView.findViewById(R.id.sub_title)).setText(offerWalls.getSubtitle());
        convertView.findViewById(R.id.single_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) mContext).openOfferWall(offerWalls.getTitle(), offerWalls.getSubtitle(), offerWalls.getType());
            }
        });
        // Return the completed view to render on screen
        return convertView;
    }
}
