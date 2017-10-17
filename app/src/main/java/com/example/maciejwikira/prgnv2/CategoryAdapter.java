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
 * Created by Maciej on 2017-10-17.
 */
public class CategoryAdapter extends SimpleCursorAdapter {

    private Context mContext;
    private Context appContext;
    private int layout;
    private Cursor cr;
    private final LayoutInflater inflater;

    public CategoryAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);

        this.layout=layout;
        this.mContext = context;
        this.inflater=LayoutInflater.from(context);
        this.cr=c;
    }

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.category_list_item, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);


        TextView categoryName = (TextView)view.findViewById(R.id.categoryName);

        int category_index = cr.getColumnIndex("category");

        categoryName.setText(cr.getString(category_index));

    }
}
