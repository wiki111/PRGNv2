package com.example.maciejwikira.prgnv2;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Maciej Wikira on 21.04.2017.
 */

public class PrgnDBAdapter extends SimpleCursorAdapter {

    private Context mContext;
    private Context appContext;
    private int layout;
    private Cursor cr;
    private final LayoutInflater inflater;

    public PrgnDBAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);

        this.layout=layout;
        this.mContext = context;
        this.inflater=LayoutInflater.from(context);
        this.cr=c;
    }

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.list_item_layout, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);


        TextView name = (TextView)view.findViewById(R.id.prgnNameView);
        TextView category = (TextView)view.findViewById(R.id.prgnCategoryView);
        TextView value = (TextView)view.findViewById(R.id.prgnValueView);
        TextView date = (TextView)view.findViewById(R.id.prgnDateView);
        ImageView img = (ImageView)view.findViewById(R.id.prgnPhotoView);
        TextView text = (TextView)view.findViewById(R.id.prgnTextView);


        int name_index = cursor.getColumnIndex("name");
        int category_index = cursor.getColumnIndex("category");
        int value_index = cursor.getColumnIndex("value");
        int date_index = cursor.getColumnIndex("date");
        int img_index = cursor.getColumnIndex("img");
        int text_index = cursor.getColumnIndex("text");

        Bitmap imgr = BitmapFactory.decodeFile(cursor.getString(img_index));

        name.setText(cursor.getString(name_index));
        category.setText(cursor.getString(category_index));
        value.setText(cursor.getString(value_index));
        date.setText(cursor.getString(date_index));
        img.setImageBitmap(imgr);
        text.setText(cursor.getString(text_index));




    }
}
