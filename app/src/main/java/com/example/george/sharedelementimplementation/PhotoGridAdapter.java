package com.example.george.sharedelementimplementation;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SectionIndexer;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

public class PhotoGridAdapter extends BaseAdapter implements SectionIndexer {

    private final List<PictureData> mRes;
    private Context mContext;
    private String[] mDates;

    public PhotoGridAdapter(Context context) {
        this.mContext = context;
        this.mRes = new ArrayList<PictureData>();
    }

    public void updateData(List<PictureData> urls) {
        mRes.clear();
        mRes.addAll(urls);
        notifyDataSetChanged();
    }

    public void clearData() {
        mRes.clear();
        mDates = null;
    }

    @Override
    public int getCount() {
        return mRes.size();
    }

    @Override
    public Object getItem(int position) {
        if (position >= 0 && position < mRes.size()) {
            return mRes.get(position);
        } else {
            return "";
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new ImageView(mContext);
            ((ImageView) convertView).setScaleType(CENTER_CROP);
            convertView.setLayoutParams(new GridView.LayoutParams(
                (int) mContext.getResources().getDimension(R.dimen.photo_grid_size),
                (int) mContext.getResources().getDimension(R.dimen.photo_grid_size)));
        }
        Picasso.with(mContext)
            .load(mRes.get(position).resourceId)
            .placeholder(android.R.color.darker_gray)
            .error(android.R.color.holo_red_light)
            .into((ImageView) convertView);

        return convertView;
    }

    @Override public Object[] getSections() {
        return mDates;
    }

    @Override public int getPositionForSection(int sectionIndex) {
        return sectionIndex;
    }

    @Override public int getSectionForPosition(int position) {
        return position;
    }

}
