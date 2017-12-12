package com.example.maciejwikira.prgnv2;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * Created by Maciej on 2017-11-03.
 */

/*
    Klasa przyporządkowuje dane karty lojalnościowej do elementów interfejsu.
 */
public class CardListAdapter extends SimpleCursorAdapter {

    private LayoutInflater inflater;
    private int layout;
    private Context mContext;
    private Cursor mCursor;

    private static class ViewHolder {
        ImageView imageView;
        TextView nameView;
        TextView categoryView;
        TextView dateView;
        TextView valueView;
    }

    public CardListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.inflater=LayoutInflater.from(context);
        this.layout = layout;
        mContext = context;
        mCursor = c;
    }

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(layout, null);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){
        ViewHolder viewHolder;

        final View result;

        if(view == null){
            viewHolder = new ViewHolder();
            inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.list_item, parent, false);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.photoView);
            viewHolder.nameView = (TextView) view.findViewById(R.id.nameTextView);
            viewHolder.categoryView = (TextView) view.findViewById(R.id.categoryTextView);
            viewHolder.dateView = (TextView) view.findViewById(R.id.dateView);
            viewHolder.valueView = (TextView) view.findViewById(R.id.valueTextView);

            result = view;

            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
            result = view;
        }

        viewHolder.valueView.setVisibility(View.GONE);
        Glide.with(mContext).load(mCursor.getString(mCursor.getColumnIndex(ReceiptContract.Receipt.IMAGE_PATH))).into(viewHolder.imageView);
        viewHolder.nameView.setText("Nazwa : " + mCursor.getString(mCursor.getColumnIndex(CardContract.Card.NAME)));
        viewHolder.categoryView.setText("Kategoria : " + mCursor.getString(mCursor.getColumnIndex(CardContract.Card.CATEGORY)));
        viewHolder.dateView.setText("Data wygaśnięcia : " + mCursor.getString(mCursor.getColumnIndex(CardContract.Card.EXPIRATION_DATE)));

        return view;
    }
}
