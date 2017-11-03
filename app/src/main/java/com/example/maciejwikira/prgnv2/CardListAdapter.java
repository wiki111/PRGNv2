package com.example.maciejwikira.prgnv2;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Created by Maciej on 2017-11-03.
 */

public class CardListAdapter extends SimpleCursorAdapter {

    private final LayoutInflater inflater;
    private int layout;


    public CardListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.inflater=LayoutInflater.from(context);
        this.layout = layout;
    }

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(layout, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

        ImageView imageView = (ImageView) view.findViewById(R.id.photoView);
        TextView nameView = (TextView) view.findViewById(R.id.nameTextView);
        TextView categoryView = (TextView) view.findViewById(R.id.categoryTextView);
        TextView dateView = (TextView) view.findViewById(R.id.dateView);
        TextView valueView = (TextView) view.findViewById(R.id.valueTextView);

        valueView.setVisibility(View.INVISIBLE);

        Bitmap image = BitmapFactory.decodeFile(cursor.getString(cursor.getColumnIndex(CardContract.Card.IMAGE_PATH)));
        imageView.setImageBitmap(image);
        nameView.setText(cursor.getString(cursor.getColumnIndex(CardContract.Card.NAME)));
        categoryView.setText(cursor.getString(cursor.getColumnIndex(CardContract.Card.CATEGORY)));
        dateView.setText(cursor.getString(cursor.getColumnIndex(CardContract.Card.EXPIRATION_DATE)));

    }
}
