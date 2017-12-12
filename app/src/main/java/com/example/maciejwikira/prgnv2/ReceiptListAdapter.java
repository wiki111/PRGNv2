package com.example.maciejwikira.prgnv2;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * Created by Maciej on 2017-10-21.
 */

// Klasa mapuje dane o paragonach pobrane z kolumn tabeli w bazie danych na odpowiednie elementy
// interfejsu użytkownika.
public class ReceiptListAdapter extends SimpleCursorAdapter{

    // Obiekt tworzący widok z podanego zasobu
    private LayoutInflater inflater;
    // id zasobu zawierającego definicję widoku
    private int layout;

    public ReceiptListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.inflater = LayoutInflater.from(context);
        this.layout = layout;
    }

    private static class ViewHolder {
        ImageView imageView;
        TextView nameView;
        TextView categoryView;
        TextView dateView;
        TextView valueView;
    }

    // Każdy nowy element listy jest tworzony przez obiekt LayoutInflater z zasobu o podanym ID
    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent){
        return inflater.inflate(layout, null);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){
        ReceiptListAdapter.ViewHolder viewHolder;

        if(view == null){
            viewHolder = new ReceiptListAdapter.ViewHolder();
            inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.list_item, parent, false);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.photoView);
            viewHolder.nameView = (TextView) view.findViewById(R.id.nameTextView);
            viewHolder.categoryView = (TextView) view.findViewById(R.id.categoryTextView);
            viewHolder.dateView = (TextView) view.findViewById(R.id.dateView);
            viewHolder.valueView = (TextView) view.findViewById(R.id.valueTextView);
            view.setTag(viewHolder);
        }else{
            viewHolder = (ReceiptListAdapter.ViewHolder) view.getTag();
        }

        viewHolder.valueView.setVisibility(View.GONE);
        Glide.with(mContext).load(mCursor.getString(mCursor.getColumnIndex(ReceiptContract.Receipt.IMAGE_PATH))).into(viewHolder.imageView);
        viewHolder.nameView.setText("Nazwa: " + mCursor.getString(mCursor.getColumnIndex(ReceiptContract.Receipt.NAME)));
        viewHolder.categoryView.setText("Kategoria: " + mCursor.getString(mCursor.getColumnIndex(ReceiptContract.Receipt.CATEGORY)));
        viewHolder.dateView.setText("Data: " + mCursor.getString(mCursor.getColumnIndex(ReceiptContract.Receipt.DATE)));
        viewHolder.valueView.setText("Wartość: " + mCursor.getString(mCursor.getColumnIndex(ReceiptContract.Receipt.VALUE)));

        return view;
    }

}
