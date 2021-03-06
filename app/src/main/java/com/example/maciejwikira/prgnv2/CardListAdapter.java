package com.example.maciejwikira.prgnv2;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    public CardListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.inflater = LayoutInflater.from(context);
        this.layout = layout;
    }

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
        View inflated = inflater.inflate(layout, null);
        return inflated;
    }

    // Metoda przypisuje dane z kursora do elementów interfejsu.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ImageView imageView = (ImageView) view.findViewById(R.id.photoView);
        TextView nameView = (TextView) view.findViewById(R.id.nameTextView);
        TextView categoryView = (TextView) view.findViewById(R.id.categoryTextView);
        TextView dateView = (TextView) view.findViewById(R.id.dateView);
        TextView valueView = (TextView) view.findViewById(R.id.valueTextView);

        valueView.setVisibility(View.GONE);

        String path = cursor.getString(cursor.getColumnIndex(CardContract.Card.IMAGE_PATH));
        if(path != null){
            Glide.with(context)
                    .load(path)
                    .into(imageView);
        }else{
            imageView.setImageResource(R.drawable.ic_card);
        }



        nameView.setText(cursor.getString(cursor.getColumnIndex(CardContract.Card.NAME)));
        categoryView.setText(cursor.getString(cursor.getColumnIndex(CardContract.Card.CATEGORY)));
        dateView.setText(cursor.getString(cursor.getColumnIndex(CardContract.Card.EXPIRATION_DATE)));

    }

}
