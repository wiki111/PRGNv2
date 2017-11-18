package com.example.maciejwikira.prgnv2;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maciej on 2017-10-21.
 */

public class ParagonListAdapter extends SimpleCursorAdapter{

    private LayoutInflater inflater;
    private int layout;

    public ParagonListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.inflater = LayoutInflater.from(context);
        this.layout = layout;
    }

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent){
        return inflater.inflate(layout, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor){
        super.bindView(view, context, cursor);

        ImageView imageView = (ImageView) view.findViewById(R.id.photoView);
        TextView nameView = (TextView) view.findViewById(R.id.nameTextView);
        TextView categoryView = (TextView) view.findViewById(R.id.categoryTextView);
        TextView dateView = (TextView) view.findViewById(R.id.dateView);
        TextView valueView = (TextView) view.findViewById(R.id.valueTextView);

        Bitmap image = BitmapFactory.decodeFile(cursor.getString(cursor.getColumnIndex(ParagonContract.Paragon.IMAGE_PATH)));
        imageView.setImageBitmap(image);

        nameView.setText("Nazwa: " + cursor.getString(cursor.getColumnIndex(ParagonContract.Paragon.NAME)));
        categoryView.setText("Kategoria: " + cursor.getString(cursor.getColumnIndex(ParagonContract.Paragon.CATEGORY)));
        dateView.setText("Data: " + cursor.getString(cursor.getColumnIndex(ParagonContract.Paragon.DATE)));
        valueView.setText("Wartość: " + cursor.getString(cursor.getColumnIndex(ParagonContract.Paragon.VALUE)));

    }
}
