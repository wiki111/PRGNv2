package com.example.maciejwikira.prgnv2;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by Maciej on 2017-12-14.
 */

public class ViewPagerImageAdapter extends PagerAdapter {

    private ArrayList<String> paths;
    private Context mContext;
    LayoutInflater mLayoutInflater;

    ViewPagerImageAdapter(Context context, ArrayList<String> paths){
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.paths = paths;
    }

    @Override
    public int getCount() {
        return paths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        View viewItem = mLayoutInflater.inflate(R.layout.pager_item, container, false);

        ImageView imageView = (ImageView) viewItem.findViewById(R.id.receiptPhotoView);

        String path;

        try{
            path = paths.get(position).toString();
            Glide.with(mContext).load(path).into(imageView);
        }catch (Exception e){
        }

        container.addView(viewItem);

        return viewItem;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((LinearLayout) object);
    }

    public void setResources(ArrayList<String> paths){
        this.paths = paths;
    }
}
