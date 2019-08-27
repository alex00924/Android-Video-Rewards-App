package com.droidoxy.easymoneyrewards.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.droidoxy.easymoneyrewards.R;
import com.droidoxy.easymoneyrewards.activities.MainActivity;
import com.droidoxy.easymoneyrewards.model.OfferWalls;

import java.util.ArrayList;

public class GridAdapter extends BaseAdapter {

    private ArrayList<OfferWalls> data;
    private Context mContext;
    public GridAdapter(Context con, ArrayList<OfferWalls> data) {
        this.data = data;
        mContext = con;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final OfferWalls offerWalls = data.get(position);

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.item_main_grid, null);
        }

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
