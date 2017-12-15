package com.example.maciejwikira.prgnv2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
    Boolean showReceipts;

    ViewPagerImageAdapter(Context context, ArrayList<String> paths, Boolean showReceipts){
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.paths = paths;
        this.showReceipts = showReceipts;
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

        try{
            final String path = paths.get(position).toString();
            Glide.with(mContext).load(path).into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, DisplayPhotoActivity.class);
                    Bundle extras = new Bundle();
                    extras.putString("BitmapPath", path);
                    extras.putBoolean(MainViewActivity.CARDS_OR_RECEIPTS, showReceipts);
                    intent.putExtras(extras);
                    mContext.startActivity(intent);
                }
            });
        }catch (Exception e){
        }
        container.addView(viewItem);

        return viewItem;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((LinearLayout) object);
    }
}
