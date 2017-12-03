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

/*
    Klasa przyporządkowuje dane karty lojalnościowej do elementów interfejsu.
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

    // Metoda przypisuje dane z kursora do elementów interfejsu.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

        ImageView imageView = (ImageView) view.findViewById(R.id.photoView);
        TextView nameView = (TextView) view.findViewById(R.id.nameTextView);
        TextView categoryView = (TextView) view.findViewById(R.id.categoryTextView);
        TextView dateView = (TextView) view.findViewById(R.id.dateView);
        TextView valueView = (TextView) view.findViewById(R.id.valueTextView);
        valueView.setVisibility(View.INVISIBLE);
        imageView.setImageBitmap(loadScaledBitmap(cursor.getString(cursor.getColumnIndex(CardContract.Card.IMAGE_PATH))));
        nameView.setText(cursor.getString(cursor.getColumnIndex(CardContract.Card.NAME)));
        categoryView.setText(cursor.getString(cursor.getColumnIndex(CardContract.Card.CATEGORY)));
        dateView.setText(cursor.getString(cursor.getColumnIndex(CardContract.Card.EXPIRATION_DATE)));

    }

    // Metoda ładuje bitmapę o zmniejszonej rozdzielczości.
    public Bitmap loadScaledBitmap(String path){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateSampleSize(options, 200, 150);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    // Metoda oblicza próbkowanie używane do załadowania wersji bitmapy o
    // porządanych rozmiarach.
    public int calculateSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth){

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Obliczanie optymalnej wartości próbkowania tak, aby uzyskać
            // rozmiar obrazu najbardziej zbliżony do porządanego.
            while((halfHeight / inSampleSize) >= reqHeight &&
                    (halfWidth / inSampleSize) >= reqWidth){
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
